package net.more_rpg_classes.entity.goal;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.entity.ISpellCasterEntity;
import net.more_rpg_classes.network.MobBeamPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.utils.SoundHelper;

import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.TargetHelper;
import net.spell_power.api.SpellPower;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MobSpellCastGoal extends Goal {

    private enum DeliveryBehavior { PROJECTILE, METEOR, CLOUD, DIRECT, AREA, BEAM, SELF, TELEPORT }

    private final ISpellCasterEntity caster;
    private final String spellOrTag;
    @Nullable private final List<MobSpellCastGoal> otherGoals;

    private final Map<Identifier, Integer> cooldowns = new HashMap<>();

    private static final float MIN_CAST_DISTANCE = 3.0F;
    private static final float INTELLIGENT_SELF_HEAL_THRESHOLD = 0.5F;
    private static final float INTELLIGENT_KILL_THRESHOLD = 0.3F;
    private static final float INTELLIGENT_ALLY_HEAL_THRESHOLD = 0.6F;

    private boolean intelligentSpellcasting = false;

    @Nullable private Identifier activeSpellId;
    private int castingTime;
    private int totalCastTime;
    private int channelReleases;
    private int channelInterval;
    private int nextReleaseCountdown;
    private int releasesDone;
    private boolean isSelfCast;
    private boolean isBeamCast;
    @Nullable private LivingEntity healingTarget;
    private double healingSpellRange = 8.0;
    private float castMovementSpeed = 0.2F;
    private double castSpellRange = 16.0;

    public MobSpellCastGoal(ISpellCasterEntity caster, String spellOrTag) {
        this(caster, spellOrTag, null);
    }

    public MobSpellCastGoal(ISpellCasterEntity caster, String spellOrTag, @Nullable List<MobSpellCastGoal> otherGoals) {
        this.caster = caster;
        this.spellOrTag = spellOrTag;
        this.otherGoals = otherGoals;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public MobSpellCastGoal withIntelligentSpellcasting() {
        this.intelligentSpellcasting = true;
        return this;
    }

    private List<RegistryEntry<Spell>> resolveSpells(net.minecraft.world.World world) {
        if (spellOrTag.startsWith("#")) {
            return SpellRegistry.entries(world, spellOrTag.substring(1));
        }
        var entry = SpellRegistry.from(world).getEntry(Identifier.of(spellOrTag)).orElse(null);
        return entry != null ? List.of(entry) : List.of();
    }

    @Override
    public boolean canStart() {
        MobEntity entity = caster.asMobEntity();
        if (caster.isCastingSpell()) return false;
        if (otherGoals != null) {
            for (MobSpellCastGoal other : otherGoals) {
                if (other != this && other.isActive()) return false;
            }
        }

        healingTarget = null;
        LivingEntity target = entity.getTarget();
        List<Identifier> candidates = new ArrayList<>();

        var spells = resolveSpells(entity.getWorld());

        for (RegistryEntry<Spell> entry : spells) {
            Identifier id = entry.getKey().orElseThrow().getValue();
            int cd = cooldowns.getOrDefault(id, 0);
            if (cd > 0) continue;
            Spell spell = entry.value();

            Spell.Target.Type targetType = spell.target != null ? spell.target.type : Spell.Target.Type.CASTER;

            if (targetType == Spell.Target.Type.NONE || targetType == Spell.Target.Type.FROM_TRIGGER) continue;

            double range = spell.range > 0 ? spell.range : 16.0;

            if (targetType == Spell.Target.Type.CASTER) {
                if (hasHealingImpact(spell)) {
                    if (entity.getHealth() < entity.getMaxHealth() || findWoundedAlly(entity, 16.0) != null) {
                        candidates.add(id);
                    }
                } else {
                    candidates.add(id);
                }
            } else {
                boolean hasCombatTarget = target != null && target.isAlive()
                        && Math.sqrt(entity.squaredDistanceTo(target)) <= range;
                if (hasCombatTarget && !onlyHasHealingImpacts(spell)) {
                    candidates.add(id);
                } else if (hasHealingImpact(spell)) {
                    LivingEntity wounded = findWoundedAlly(entity, range);
                    if (wounded != null) {
                        healingTarget = wounded;
                        candidates.add(id);
                    }
                }
            }
        }

        if (candidates.isEmpty()) return false;

        if (intelligentSpellcasting) {
            candidates = applyIntelligentFilters(entity, target, candidates);
            if (candidates.isEmpty()) return false;
        }

        activeSpellId = candidates.get(entity.getRandom().nextInt(candidates.size()));
        return true;
    }

    private List<Identifier> applyIntelligentFilters(MobEntity entity, @Nullable LivingEntity target, List<Identifier> candidates) {
        var registry = SpellRegistry.from(entity.getWorld());

        candidates = new ArrayList<>(candidates);
        candidates.removeIf(id -> !SpellBehaviorRegistry.shouldCast(entity, healingTarget != null ? healingTarget : target, id));
        if (candidates.isEmpty()) return candidates;

        float selfHpFrac = entity.getHealth() / entity.getMaxHealth();
        if (selfHpFrac < INTELLIGENT_SELF_HEAL_THRESHOLD) {
            List<Identifier> selfHeal = candidates.stream().filter(id -> {
                var e = registry.getEntry(id).orElse(null);
                if (e == null) return false;
                Spell.Target.Type t = e.value().target != null ? e.value().target.type : Spell.Target.Type.CASTER;
                return t == Spell.Target.Type.CASTER && hasHealingImpact(e.value());
            }).collect(java.util.stream.Collectors.toList());
            if (!selfHeal.isEmpty()) {
                healingTarget = null;
                return selfHeal;
            }
        }

        if (target != null) {
            float targetHpFrac = target.getHealth() / target.getMaxHealth();
            if (targetHpFrac < INTELLIGENT_KILL_THRESHOLD) {
                List<Identifier> damaging = candidates.stream().filter(id -> {
                    var e = registry.getEntry(id).orElse(null);
                    if (e == null) return false;
                    return !onlyHasHealingImpacts(e.value());
                }).collect(java.util.stream.Collectors.toList());
                if (!damaging.isEmpty()) return damaging;
            }
        }

        return candidates;
    }

    @Override
    public boolean shouldContinue() {
        if (castingTime <= 0) return false;
        if (isSelfCast) return true;
        if (healingTarget != null) return healingTarget.isAlive();
        LivingEntity target = caster.asMobEntity().getTarget();
        if (target == null || !target.isAlive()) return false;
        return true;
    }

    @Override
    public void start() {
        if (activeSpellId == null) return;
        RegistryEntry<Spell> entry = SpellRegistry.from(caster.asMobEntity().getWorld()).getEntry(activeSpellId).orElse(null);
        if (entry == null) return;

        Spell spell = entry.value();
        DeliveryBehavior startBehavior = deriveDelivery(spell);
        isSelfCast = (startBehavior == DeliveryBehavior.SELF);
        isBeamCast = (startBehavior == DeliveryBehavior.BEAM);
        if (healingTarget != null) {
            healingSpellRange = spell.range > 0 ? spell.range : 16.0;
        }

        int castDurationTicks = (spell.active != null && spell.active.cast != null)
                ? Math.max(1, Math.round(spell.active.cast.duration * 20F)) : 40;
        castingTime = castDurationTicks;
        totalCastTime = castingTime;

        channelReleases = (spell.active != null && spell.active.cast != null) ? spell.active.cast.channel_ticks : 0;
        if (channelReleases > 0) {
            channelInterval = Math.max(1, castingTime / channelReleases);
            nextReleaseCountdown = channelInterval;
            releasesDone = 0;
        }

        castMovementSpeed = (spell.active != null && spell.active.cast != null) ? spell.active.cast.movement_speed : 0.2F;
        castSpellRange = spell.range > 0 ? spell.range : 16.0;

        caster.startSpellCast(castingTime);
        if (spell.active != null && spell.active.cast != null) {
            ParticleHelper.sendBatches(caster.asMobEntity(), spell.active.cast.particles);
            SoundHelper.playSound(caster.asMobEntity().getWorld(), caster.asMobEntity(), spell.active.cast.start_sound);
        }
    }

    @Override
    public void tick() {
        MobEntity entity = caster.asMobEntity();
        LivingEntity target = entity.getTarget();
        LivingEntity lookAt = target != null ? target : healingTarget;
        if (lookAt != null) entity.getLookControl().lookAt(lookAt, 30.0F, 30.0F);

        if (healingTarget != null && healingTarget.isAlive()) {
            double distSq = entity.squaredDistanceTo(healingTarget);
            double rangeSq = healingSpellRange * healingSpellRange;
            if (distSq > rangeSq) {
                entity.getNavigation().startMovingTo(healingTarget, 1.0);
            } else {
                entity.getNavigation().stop();
            }
        } else if (!isSelfCast && target != null) {
            handleCastMovement(target);
        }

        castingTime--;

        if (channelReleases > 0) {
            RegistryEntry<Spell> entry = SpellRegistry.from(entity.getWorld()).getEntry(activeSpellId).orElse(null);
            if (entry != null) {
                Spell spell = entry.value();
                if (spell.active != null && spell.active.cast != null) {
                    ParticleHelper.sendBatches(entity, spell.active.cast.particles);
                }
                if (isBeamCast && target != null) {
                    sendBeamPacket(entity, target, activeSpellId);
                }
            }
            nextReleaseCountdown--;
            if (nextReleaseCountdown <= 0 && releasesDone < channelReleases) {
                faceTarget(target != null ? target : healingTarget);
                castSpell(releasesDone);
                releasesDone++;
                nextReleaseCountdown = channelInterval;
            }
        } else {
            if (castingTime == totalCastTime / 2) {
                faceTarget(target != null ? target : healingTarget);
                castSpell(0);
            }
        }
    }

    @Override
    public void stop() {
        caster.stopSpellCast();
        sendBeamClearPacket(caster.asMobEntity());
        if (activeSpellId != null) {
            RegistryEntry<Spell> entry = SpellRegistry.from(caster.asMobEntity().getWorld()).getEntry(activeSpellId).orElse(null);
            if (entry != null) {
                float cd = entry.value().cost.cooldown.duration;
                if (cd > 0) cooldowns.put(activeSpellId, Math.round(cd * 20F));
            }
            activeSpellId = null;
        }
        castingTime = 0;
        totalCastTime = 0;
        channelReleases = 0;
        channelInterval = 0;
        nextReleaseCountdown = 0;
        releasesDone = 0;
        isSelfCast = false;
        isBeamCast = false;
        healingTarget = null;
        healingSpellRange = 8.0;
        castMovementSpeed = 0.2F;
        castSpellRange = 16.0;
    }

    private void castSpell(int channelIndex) {
        MobEntity entity = caster.asMobEntity();
        if (entity.getWorld().isClient() || activeSpellId == null) return;

        RegistryEntry<Spell> entry = SpellRegistry.from(entity.getWorld()).getEntry(activeSpellId).orElse(null);
        if (entry == null) {
            MRPGCMod.LOGGER.warn("Spell not found: {}", activeSpellId);
            return;
        }

        try {
            Spell spell = entry.value();
            DeliveryBehavior behavior = deriveDelivery(spell);
            SpellPower.Result power = SpellPower.getSpellPower(spell.school, entity);

            switch (behavior) {
                case PROJECTILE -> {
                    LivingEntity target = entity.getTarget();
                    if (target == null) return;
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(SpellHelper.focusMode(spell));
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.shootProjectile(entity.getWorld(), entity, target, entry, ctx, channelIndex);
                }
                case METEOR -> {
                    boolean selfTarget = spell.target == null || spell.target.type == Spell.Target.Type.CASTER;
                    LivingEntity impactTarget = selfTarget ? entity : entity.getTarget();
                    if (!selfTarget && impactTarget == null) return;
                    var pos = impactTarget.getPos();
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(pos).target(SpellTarget.FocusMode.AREA);
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    try {
                        SpellHelper.fallProjectile(entity.getWorld(), entity, impactTarget, pos, entry, ctx);
                    } catch (Exception e) {
                        SpellHelper.performImpacts(entity.getWorld(), entity, impactTarget, entity, entry, spell.impacts, ctx, false, null);
                    }
                }
                case CLOUD -> {
                    boolean selfTarget = spell.target == null || spell.target.type == Spell.Target.Type.CASTER;
                    LivingEntity impactTarget = selfTarget ? entity : entity.getTarget();
                    if (!selfTarget && impactTarget == null) return;
                    var pos = impactTarget.getPos();
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(SpellTarget.FocusMode.AREA);
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.placeCloud(entity.getWorld(), entity, impactTarget, pos, entry, ctx);
                }
                case AREA -> {
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext().power(power).position(entity.getPos()).target(SpellTarget.FocusMode.AREA);
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    if (spell.release.particles_scaled_with_ranged != null) {
                        ParticleBatch[] scaledParticles = new ParticleBatch[spell.release.particles_scaled_with_ranged.length];
                        for (int i = 0; i < spell.release.particles_scaled_with_ranged.length; i++) {
                            scaledParticles[i] = spell.release.particles_scaled_with_ranged[i].copy().scale(spell.range);
                        }
                        ParticleHelper.sendBatches(entity, scaledParticles);
                    }
                    if (hasSpawnImpact(spell)) {
                        LivingEntity spawnTarget = entity.getTarget();
                        if (spawnTarget != null) {
                            SpellHelper.ImpactContext selfCtx = new SpellHelper.ImpactContext()
                                    .power(power).position(spawnTarget.getPos()).target(SpellTarget.FocusMode.DIRECT);
                            SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, entry, spell.impacts, selfCtx, false, Spell.Impact.Action.Type.SPAWN);
                        }
                    }
                    if (spell.target.area != null && spell.target.area.include_caster) {
                        SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, entry, spell.impacts, ctx, false, null);
                    }
                    for (Entity t : TargetHelper.targetsFromArea(entity, spell.range, spell.target.area, e -> e != entity)) {
                        SpellHelper.performImpacts(entity.getWorld(), entity, t, entity, entry, spell.impacts, ctx, false, null);
                    }
                }
                case BEAM -> {
                    LivingEntity target = entity.getTarget();
                    if (target == null) return;
                    if (spell.active != null && spell.active.cast != null) {
                        ParticleHelper.sendBatches(entity, spell.active.cast.particles);
                    }
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(SpellTarget.FocusMode.AREA);
                    if (hasSpawnImpact(spell)) {
                        SpellHelper.ImpactContext selfCtx = new SpellHelper.ImpactContext()
                                .power(power).position(target.getPos()).target(SpellTarget.FocusMode.DIRECT);
                        SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, entry, spell.impacts, selfCtx, false, Spell.Impact.Action.Type.SPAWN);
                    }
                    SpellHelper.performImpacts(entity.getWorld(), entity, target, entity, entry, spell.impacts, ctx, false, null);
                    double beamRange = spell.range > 0 ? spell.range : 32.0;
                    Vec3d beamFrom = entity.getEyePos();
                    Vec3d beamDir = target.getEyePos().subtract(beamFrom).normalize();
                    ServerWorld beamWorld = (ServerWorld) entity.getWorld();
                    for (Entity candidate : beamWorld.getOtherEntities(entity,
                            entity.getBoundingBox().expand(beamRange),
                            e -> e instanceof LivingEntity && e.isAlive() && e != target)) {
                        Vec3d candidateCenter = candidate.getBoundingBox().getCenter();
                        Vec3d toCandidate = candidateCenter.subtract(beamFrom);
                        double projection = toCandidate.dotProduct(beamDir);
                        if (projection < 0 || projection > beamRange) continue;
                        double lateralDistSq = toCandidate.subtract(beamDir.multiply(projection)).lengthSquared();
                        if (lateralDistSq > 2.0 * 2.0) continue;
                        SpellHelper.performImpacts(entity.getWorld(), entity, candidate, entity, entry, spell.impacts, ctx, false, null);
                    }
                }
                case DIRECT -> {
                    LivingEntity target = entity.getTarget();
                    LivingEntity effectiveTarget = target != null ? target : healingTarget;
                    if (effectiveTarget == null) return;
                    if (spell.active != null && spell.active.cast != null) {
                        ParticleHelper.sendBatches(entity, spell.active.cast.particles);
                    }
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(SpellHelper.focusMode(spell));
                    if (hasSpawnImpact(spell)) {
                        SpellHelper.ImpactContext selfCtx = new SpellHelper.ImpactContext()
                                .power(power).position(effectiveTarget.getPos()).target(SpellTarget.FocusMode.DIRECT);
                        SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, entry, spell.impacts, selfCtx, false, Spell.Impact.Action.Type.SPAWN);
                    }
                    SpellHelper.performImpacts(entity.getWorld(), entity, effectiveTarget, entity, entry, spell.impacts, ctx, false, null);
                }
                case SELF -> {
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getPos()).target(SpellTarget.FocusMode.AREA);
                    if (spell.deliver != null && spell.deliver.type == Spell.Delivery.Type.STASH_EFFECT
                            && spell.deliver.stash_effect != null) {
                        var stash = spell.deliver.stash_effect;
                        net.minecraft.registry.Registries.STATUS_EFFECT
                                .getEntry(net.minecraft.util.Identifier.of(stash.id))
                                .ifPresent(effectEntry -> {
                                    int durationTicks = Math.max(1, (int) (stash.duration * 20));
                                    entity.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                                            effectEntry, durationTicks, stash.amplifier,
                                            false, stash.show_particles, true));
                                });
                    }
                    if (hasSpawnImpact(spell)) {
                        LivingEntity combatTarget = entity.getTarget();
                        Vec3d spawnPos = combatTarget != null ? combatTarget.getPos() : entity.getPos();
                        SpellHelper.ImpactContext spawnCtx = new SpellHelper.ImpactContext()
                                .power(power).position(spawnPos).target(SpellTarget.FocusMode.DIRECT);
                        SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, entry, spell.impacts, spawnCtx, false, Spell.Impact.Action.Type.SPAWN);
                    }
                    if (!onlyHasSpawnImpacts(spell)) {
                        SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, entry, spell.impacts, ctx, false, null);
                    }
                }
                case TELEPORT -> {
                    LivingEntity target = entity.getTarget();
                    if (spell.release != null) ParticleHelper.sendBatches(entity, spell.release.particles);
                    for (Spell.Impact impact : spell.impacts) {
                        if (impact.action == null || impact.action.type != Spell.Impact.Action.Type.TELEPORT) continue;
                        var data = impact.action.teleport;
                        if (data == null) continue;
                        Vec3d destination = null;
                        switch (data.mode) {
                            case FORWARD -> {
                                if (target == null) break;
                                Vec3d diff = new Vec3d(entity.getX() - target.getX(), 0, entity.getZ() - target.getZ());
                                Vec3d escapeDir = diff.length() > 0.001
                                        ? diff.normalize()
                                        : entity.getRotationVector();
                                float distance = data.forward != null ? data.forward.distance : 10F;
                                destination = TargetHelper.findTeleportDestination(entity, escapeDir, distance, data.required_clearance_block_y);
                            }
                            case BEHIND_TARGET -> {
                                if (target == null) break;
                                float distance = data.behind_target != null ? data.behind_target.distance : 1.5F;
                                Vec3d look = target.getRotationVector();
                                destination = target.getPos().add(look.multiply(-distance));
                            }
                        }
                        if (destination != null) {
                            Vec3d ground = TargetHelper.findSolidBlockBelow(entity, destination, entity.getWorld(), -1.5F);
                            if (ground != null) destination = ground;
                            if (data.depart_particles != null) ParticleHelper.sendBatches(entity, data.depart_particles);
                            entity.getWorld().emitGameEvent(GameEvent.TELEPORT, entity.getPos(), GameEvent.Emitter.of(entity));
                            entity.teleport(destination.x, destination.y, destination.z, false);
                            if (data.arrive_particles != null) ParticleHelper.sendBatches(entity, data.arrive_particles);
                        }
                    }
                }
            }
        } catch (Exception e) {
            MRPGCMod.LOGGER.error("Failed to cast spell {}", activeSpellId, e);
        }
    }

    private DeliveryBehavior deriveDelivery(Spell spell) {
        Spell.Target.Type targetType = spell.target != null ? spell.target.type : Spell.Target.Type.CASTER;
        switch (targetType) {
            case BEAM -> { return DeliveryBehavior.BEAM; }
            case AREA -> { return DeliveryBehavior.AREA; }
            default   -> { /* fall through */ }
        }
        if (hasTeleportImpact(spell)) return DeliveryBehavior.TELEPORT;
        if (spell.deliver != null) {
            return switch (spell.deliver.type) {
                case METEOR -> DeliveryBehavior.METEOR;
                case CLOUD  -> DeliveryBehavior.CLOUD;
                case PROJECTILE, SHOOT_ARROW -> targetType == Spell.Target.Type.CASTER
                        ? DeliveryBehavior.SELF : DeliveryBehavior.PROJECTILE;
                case MELEE -> targetType == Spell.Target.Type.CASTER
                        ? DeliveryBehavior.SELF : DeliveryBehavior.DIRECT;
                case STASH_EFFECT -> targetType == Spell.Target.Type.CASTER
                        ? DeliveryBehavior.SELF : DeliveryBehavior.DIRECT;
                default -> targetType == Spell.Target.Type.CASTER ? DeliveryBehavior.SELF : DeliveryBehavior.DIRECT;
            };
        }
        return targetType == Spell.Target.Type.CASTER ? DeliveryBehavior.SELF : DeliveryBehavior.DIRECT;
    }

    private static boolean hasSpawnImpact(Spell spell) {
        if (spell.impacts == null) return false;
        for (var impact : spell.impacts) {
            if (impact.action != null && impact.action.type == Spell.Impact.Action.Type.SPAWN) return true;
        }
        return false;
    }

    private static boolean onlyHasSpawnImpacts(Spell spell) {
        if (spell.impacts == null || spell.impacts.isEmpty()) return false;
        for (var impact : spell.impacts) {
            if (impact.action == null || impact.action.type != Spell.Impact.Action.Type.SPAWN) return false;
        }
        return true;
    }

    private static boolean hasHealingImpact(Spell spell) {
        if (spell.impacts == null) return false;
        for (var impact : spell.impacts) {
            if (impact.action != null && impact.action.type == Spell.Impact.Action.Type.HEAL) return true;
        }
        return false;
    }

    private static boolean onlyHasHealingImpacts(Spell spell) {
        if (spell.impacts == null || spell.impacts.isEmpty()) return false;
        for (var impact : spell.impacts) {
            if (impact.action == null) continue;
            if (impact.action.type != Spell.Impact.Action.Type.HEAL) return false;
        }
        return true;
    }

    @Nullable
    private static LivingEntity findWoundedAlly(MobEntity entity, double range) {
        if (entity.getHealth() < entity.getMaxHealth()) return entity;
        double rangeSq = range * range;
        LivingEntity best = null;
        float lowestFrac = 1.0f;
        for (Entity nearbyEntity : entity.getWorld().getOtherEntities(entity,
                entity.getBoundingBox().expand(range),
                e -> e instanceof LivingEntity && ((LivingEntity) e).isAlive())) {
            LivingEntity living = (LivingEntity) nearbyEntity;
            if (!isMobAlly(entity, living)) continue;
            float frac = living.getHealth() / living.getMaxHealth();
            if (frac >= 1.0f) continue;
            if (entity.squaredDistanceTo(living) <= rangeSq && frac < lowestFrac) {
                lowestFrac = frac;
                best = living;
            }
        }
        return best;
    }

    private static boolean isMobAlly(MobEntity caster, LivingEntity target) {
        if (caster == target) return true;
        if (caster.isTeammate(target)) return true;
        if (!(target instanceof MobEntity mobTarget)) return false;
        return caster.getTarget() != target && mobTarget.getTarget() != caster;
    }

    private static boolean hasTeleportImpact(Spell spell) {
        if (spell.impacts == null) return false;
        for (var impact : spell.impacts) {
            if (impact.action != null && impact.action.type == Spell.Impact.Action.Type.TELEPORT) return true;
        }
        return false;
    }

    private void handleCastMovement(LivingEntity target) {
        MobEntity entity = caster.asMobEntity();
        double dist = Math.sqrt(entity.squaredDistanceTo(target));

        if (channelReleases > 0) {
            if (dist > castSpellRange) {
                entity.getNavigation().startMovingTo(target, castMovementSpeed);
            } else if (dist < MIN_CAST_DISTANCE) {
                Vec3d awayDir = entity.getPos().subtract(target.getPos()).normalize();
                Vec3d dest = target.getPos().add(awayDir.multiply(MIN_CAST_DISTANCE + 1.5));
                entity.getNavigation().startMovingTo(dest.x, dest.y, dest.z, castMovementSpeed);
            } else {
                entity.getNavigation().stop();
            }
        } else {
            if (dist < MIN_CAST_DISTANCE) {
                Vec3d awayDir = entity.getPos().subtract(target.getPos()).normalize();
                Vec3d dest = target.getPos().add(awayDir.multiply(MIN_CAST_DISTANCE + 1.5));
                entity.getNavigation().startMovingTo(dest.x, dest.y, dest.z, castMovementSpeed);
            } else {
                entity.getNavigation().stop();
            }
        }
    }

    private void faceTarget(@Nullable LivingEntity target) {
        if (target == null) return;
        MobEntity entity = caster.asMobEntity();
        double dx = target.getX() - entity.getX();
        double dy = target.getEyeY() - entity.getEyeY();
        double dz = target.getZ() - entity.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, hDist) * (180.0 / Math.PI)));
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entity.headYaw = yaw;
        entity.bodyYaw = yaw;
    }

    private static void sendBeamPacket(MobEntity caster, LivingEntity target, Identifier spellId) {
        if (!(caster.getWorld() instanceof ServerWorld sw)) return;
        var packet = new MobBeamPacket(caster.getId(), target.getId(), spellId);
        sw.getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    private static void sendBeamClearPacket(MobEntity caster) {
        if (!(caster.getWorld() instanceof ServerWorld sw)) return;
        var packet = new MobBeamPacket(caster.getId(), -1, null);
        sw.getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    public boolean isActive() {
        return castingTime > 0;
    }

    public void updateCooldown() {
        cooldowns.replaceAll((id, ticks) -> Math.max(0, ticks - 1));
    }
}
