package net.more_rpg_classes.entity.goal;

import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.entity.ISpellCasterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.SpellInfo;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.SpellRegistry;
import net.spell_engine.particle.ParticleHelper;
import net.spell_engine.utils.SoundHelper;
import net.spell_engine.utils.TargetHelper;
import net.spell_power.api.SpellPower;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MobSpellCastGoal extends Goal {

    private enum DeliveryBehavior { PROJECTILE, SHOOT_ARROW, METEOR, CLOUD, DIRECT, AREA, BEAM, SELF, TELEPORT }

    private final ISpellCasterEntity caster;
    private final String spellId;
    @Nullable private final List<MobSpellCastGoal> otherGoals;

    private final Map<Identifier, Integer> cooldowns = new HashMap<>();

    private static final float MIN_CAST_DISTANCE = 3.0F;
    private static final float INTELLIGENT_SELF_HEAL_THRESHOLD = 0.5F;
    private static final float INTELLIGENT_KILL_THRESHOLD = 0.3F;

    private boolean intelligentSpellcasting = false;

    @Nullable private Identifier activeSpellId;
    private int castingTime;
    private int totalCastTime;
    private int channelReleases;
    private int channelInterval;
    private int nextReleaseCountdown;
    private int releasesDone;
    private boolean isSelfCast;
    @Nullable private LivingEntity healingTarget;
    private double healingSpellRange = 8.0;
    private float castMovementSpeed = 0.2F;
    private double castSpellRange = 16.0;

    public MobSpellCastGoal(ISpellCasterEntity caster, String spellId) {
        this(caster, spellId, null);
    }

    public MobSpellCastGoal(ISpellCasterEntity caster, String spellId, @Nullable List<MobSpellCastGoal> otherGoals) {
        this.caster = caster;
        this.spellId = spellId;
        this.otherGoals = otherGoals;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public MobSpellCastGoal withIntelligentSpellcasting() {
        this.intelligentSpellcasting = true;
        return this;
    }

    private List<Identifier> resolveSpellIds() {
        List<Identifier> ids = new ArrayList<>();
        Identifier id = new Identifier(spellId);
        if (SpellRegistry.getSpell(id) != null) {
            ids.add(id);
        }
        return ids;
    }

    @Override
    public boolean canStart() {
        updateCooldown();
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

        for (Identifier id : resolveSpellIds()) {
            int cd = cooldowns.getOrDefault(id, 0);
            if (cd > 0) continue;
            Spell spell = SpellRegistry.getSpell(id);
            if (spell == null) continue;

            DeliveryBehavior behavior = deriveDelivery(spell);
            double range = spell.range > 0 ? spell.range : 16.0;

            if (behavior == DeliveryBehavior.SELF) {
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
        candidates = new ArrayList<>(candidates);
        candidates.removeIf(id -> !SpellBehaviorRegistry.shouldCast(entity, healingTarget != null ? healingTarget : target, id));
        if (candidates.isEmpty()) return candidates;

        float selfHpFrac = entity.getHealth() / entity.getMaxHealth();
        if (selfHpFrac < INTELLIGENT_SELF_HEAL_THRESHOLD) {
            List<Identifier> selfHeal = new ArrayList<>();
            for (Identifier id : candidates) {
                Spell spell = SpellRegistry.getSpell(id);
                if (spell == null) continue;
                if (deriveDelivery(spell) == DeliveryBehavior.SELF && hasHealingImpact(spell)) {
                    selfHeal.add(id);
                }
            }
            if (!selfHeal.isEmpty()) {
                healingTarget = null;
                return selfHeal;
            }
        }

        if (target != null) {
            float targetHpFrac = target.getHealth() / target.getMaxHealth();
            if (targetHpFrac < INTELLIGENT_KILL_THRESHOLD) {
                List<Identifier> damaging = new ArrayList<>();
                for (Identifier id : candidates) {
                    Spell spell = SpellRegistry.getSpell(id);
                    if (spell != null && !onlyHasHealingImpacts(spell)) damaging.add(id);
                }
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
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        if (activeSpellId == null) return;
        Spell spell = SpellRegistry.getSpell(activeSpellId);
        if (spell == null) return;

        DeliveryBehavior behavior = deriveDelivery(spell);
        isSelfCast = (behavior == DeliveryBehavior.SELF);
        if (healingTarget != null) {
            healingSpellRange = spell.range > 0 ? spell.range : 16.0;
        }

        int castDurationTicks = spell.cast != null
                ? Math.max(1, Math.round(spell.cast.duration * 20F)) : 40;
        castingTime = castDurationTicks;
        totalCastTime = castingTime;

        channelReleases = spell.cast != null ? spell.cast.channel_ticks : 0;
        if (channelReleases > 0) {
            channelInterval = Math.max(1, castingTime / channelReleases);
            nextReleaseCountdown = channelInterval;
            releasesDone = 0;
        }

        castMovementSpeed = spell.cast != null ? spell.cast.movement_speed : 0.2F;
        castSpellRange = spell.range > 0 ? spell.range : 16.0;

        caster.startSpellCast(castingTime);
        if (spell.cast != null) {
            ParticleHelper.sendBatches(caster.asMobEntity(), spell.cast.particles);
            SoundHelper.playSound(caster.asMobEntity().getWorld(), caster.asMobEntity(), spell.cast.start_sound);
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
            Spell spell = SpellRegistry.getSpell(activeSpellId);
            if (spell != null && spell.cast != null) {
                ParticleHelper.sendBatches(entity, spell.cast.particles);
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
        if (activeSpellId != null) {
            Spell spell = SpellRegistry.getSpell(activeSpellId);
            if (spell != null && spell.cost != null) {
                float cd = spell.cost.cooldown_duration;
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
        healingTarget = null;
        healingSpellRange = 8.0;
        castMovementSpeed = 0.2F;
        castSpellRange = 16.0;
    }

    private void castSpell(int channelIndex) {
        MobEntity entity = caster.asMobEntity();
        if (entity.getWorld().isClient() || activeSpellId == null) return;

        Spell spell = SpellRegistry.getSpell(activeSpellId);
        if (spell == null) {
            MRPGCMod.LOGGER.warn("Spell not found: {}", activeSpellId);
            return;
        }

        try {
            SpellInfo spellInfo = new SpellInfo(spell, activeSpellId);
            DeliveryBehavior behavior = deriveDelivery(spell);
            SpellPower.Result power = SpellPower.getSpellPower(spell.school, entity);

            switch (behavior) {
                case PROJECTILE, SHOOT_ARROW -> {
                    LivingEntity target = entity.getTarget();
                    if (target == null) return;
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(TargetHelper.TargetingMode.DIRECT);
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.shootProjectile(entity.getWorld(), entity, target, spellInfo, ctx);
                }
                case METEOR -> {
                    LivingEntity impactTarget = entity.getTarget();
                    if (impactTarget == null) impactTarget = entity;
                    var pos = impactTarget.getPos();
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(pos).target(TargetHelper.TargetingMode.AREA);
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.fallProjectile(entity.getWorld(), entity, impactTarget, spellInfo, ctx);
                }
                case CLOUD -> {
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(TargetHelper.TargetingMode.AREA);
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.placeCloud(entity.getWorld(), entity, spellInfo, ctx);
                }
                case AREA -> {
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getPos()).target(TargetHelper.TargetingMode.AREA);
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    if (spell.release.target.area != null && spell.release.target.area.include_caster) {
                        SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, spellInfo, ctx);
                    }
                    for (Entity t : TargetHelper.targetsFromArea(entity, spell.range, spell.release.target.area, e -> e != entity)) {
                        SpellHelper.performImpacts(entity.getWorld(), entity, t, entity, spellInfo, ctx);
                    }
                }
                case BEAM -> {
                    LivingEntity target = entity.getTarget();
                    if (target == null) return;
                    if (spell.cast != null) {
                        ParticleHelper.sendBatches(entity, spell.cast.particles);
                    }
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(TargetHelper.TargetingMode.AREA);
                    SpellHelper.performImpacts(entity.getWorld(), entity, target, entity, spellInfo, ctx);
                    double beamRange = spell.range > 0 ? spell.range : 32.0;
                    Vec3d beamFrom = entity.getEyePos();
                    Vec3d beamDir = target.getEyePos().subtract(beamFrom).normalize();
                    for (Entity candidate : entity.getWorld().getOtherEntities(entity,
                            entity.getBoundingBox().expand(beamRange),
                            e -> e instanceof LivingEntity && e.isAlive() && e != target)) {
                        Vec3d toCandidate = candidate.getBoundingBox().getCenter().subtract(beamFrom);
                        double projection = toCandidate.dotProduct(beamDir);
                        if (projection < 0 || projection > beamRange) continue;
                        double lateralDistSq = toCandidate.subtract(beamDir.multiply(projection)).lengthSquared();
                        if (lateralDistSq > 2.0 * 2.0) continue;
                        SpellHelper.performImpacts(entity.getWorld(), entity, candidate, entity, spellInfo, ctx);
                    }
                }
                case DIRECT -> {
                    LivingEntity target = entity.getTarget();
                    LivingEntity effectiveTarget = target != null ? target : healingTarget;
                    if (effectiveTarget == null) return;
                    if (spell.cast != null) {
                        ParticleHelper.sendBatches(entity, spell.cast.particles);
                    }
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getEyePos()).target(TargetHelper.TargetingMode.DIRECT);
                    SpellHelper.performImpacts(entity.getWorld(), entity, effectiveTarget, entity, spellInfo, ctx);
                }
                case SELF -> {
                    ParticleHelper.sendBatches(entity, spell.release.particles);
                    SoundHelper.playSound(entity.getWorld(), entity, spell.release.sound);
                    SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                            .power(power).position(entity.getPos()).target(TargetHelper.TargetingMode.AREA);
                    SpellHelper.performImpacts(entity.getWorld(), entity, entity, entity, spellInfo, ctx);
                }
                case TELEPORT -> {
                    if (spell.release != null) ParticleHelper.sendBatches(entity, spell.release.particles);
                    for (Spell.Impact impact : spell.impact) {
                        if (impact.action == null || impact.action.type != Spell.Impact.Action.Type.TELEPORT) continue;
                        var data = impact.action.teleport;
                        if (data == null) continue;
                        LivingEntity target = entity.getTarget();
                        Vec3d destination = null;
                        switch (data.mode) {
                            case FORWARD -> {
                                if (target == null) break;
                                Vec3d diff = new Vec3d(entity.getX() - target.getX(), 0, entity.getZ() - target.getZ());
                                Vec3d escapeDir = diff.length() > 0.001 ? diff.normalize() : entity.getRotationVector();
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
        if (spell.release == null || spell.release.target == null) return DeliveryBehavior.SELF;
        switch (spell.release.target.type) {
            case BEAM -> { return DeliveryBehavior.BEAM; }
            case AREA -> { return DeliveryBehavior.AREA; }
            case SELF -> { return DeliveryBehavior.SELF; }
            case PROJECTILE -> { return DeliveryBehavior.PROJECTILE; }
            case SHOOT_ARROW -> { return DeliveryBehavior.SHOOT_ARROW; }
            case METEOR -> { return DeliveryBehavior.METEOR; }
            case CLOUD -> { return DeliveryBehavior.CLOUD; }
            case CURSOR -> { return DeliveryBehavior.DIRECT; }
            default -> { return DeliveryBehavior.DIRECT; }
        }
    }

    private static boolean hasHealingImpact(Spell spell) {
        if (spell.impact == null || spell.impact.length == 0) return false;
        for (var impact : spell.impact) {
            if (impact.action != null && impact.action.type == Spell.Impact.Action.Type.HEAL) return true;
        }
        return false;
    }

    private static boolean onlyHasHealingImpacts(Spell spell) {
        if (spell.impact == null || spell.impact.length == 0) return false;
        for (var impact : spell.impact) {
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

    public boolean isActive() {
        return castingTime > 0;
    }

    public void updateCooldown() {
        cooldowns.replaceAll((id, ticks) -> Math.max(0, ticks - 1));
    }
}
