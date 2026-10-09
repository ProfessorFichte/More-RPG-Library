package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.entity.ISpellCasterEntity;
import com.mrpg_lib.network.MRPGCNetworking;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import com.mrpg_lib.compat.spell_engine.network.MobBeamPacket;
import com.mrpg_lib.compat.spell_engine.network.MobSpinPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.fx.ReleaseFx;
import net.spell_engine.internals.SpellExecution;
import net.spell_engine.entity.SummonedEntity;
import net.spell_engine.internals.delivery.CloudPlacer;
import net.spell_engine.internals.delivery.ProjectileLauncher;
import net.spell_engine.internals.delivery.SpellDelivery;
import net.spell_engine.internals.delivery.arrow.ArrowHelper;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellIntents;
import net.spell_engine.internals.SpellModifiers;
import net.spell_engine.internals.SpellParameters;
import net.spell_engine.utils.SoundHelper;

import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.AnimationHelper;
import net.spell_engine.utils.TargetHelper;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellPower;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class MobSpellCastGoal extends Goal {

    private enum DeliveryBehavior { PROJECTILE, ARROW, AFFECT_ARROW, STASH, METEOR, CLOUD, DIRECT, AREA, BEAM, SELF, TELEPORT, MELEE, CUSTOM }

    private final ISpellCasterEntity caster;
    private final String spellOrTag;
    @Nullable private final List<MobSpellCastGoal> otherGoals;

    private final Map<Identifier, Long> cooldownReadyAt = new HashMap<>();
    private final Map<Identifier, SpellTimings> spellTimings = new HashMap<>();
    private SpellTimings goalTimings = SpellTimings.NONE;

    private static final float MIN_CAST_DISTANCE = 3.0F;
    private static final float INTELLIGENT_SELF_HEAL_THRESHOLD = 0.5F;
    private static final float INTELLIGENT_KILL_THRESHOLD = 0.3F;
    private static final float INTELLIGENT_ALLY_HEAL_THRESHOLD = 0.6F;
    private static final int AFFECT_ARROW_WINDOW_TICKS = 100;
    private static final int SUMMON_ASSIST_TICKS = 40;
    private static final double SUMMON_ASSIST_RANGE = 24.0;
    private static final double MELEE_START_MARGIN = 3.0;
    private static final double MELEE_AIM_TOLERANCE = 0.5;
    private static final int MAX_MELEE_HOLD_TICKS = 40;
    private static final double AREA_APPROACH_FACTOR = 0.5;

    private long summonAssistUntil = 0L;

    private boolean intelligentSpellcasting = false;
    private boolean animations = false;
    private boolean equipmentSpells = false;
    @Nullable private String passiveSpellTag = null;

    @Nullable private Identifier activeSpellId;
    private int castingTime;
    private int totalCastTime;
    private int channelReleases;
    private float channelIntervalTicks;
    private float channelMultiplier = 1F;
    private int releasesDone;
    private boolean spinning;
    private boolean isSelfCast;
    private boolean isBeamCast;
    private boolean isChargeCast;
    private boolean chargeReleased;
    @Nullable private LivingEntity healingTarget;
    private double healingSpellRange = 8.0;
    private float castMovementSpeed = 0.2F;
    private double castSpellRange = 16.0;
    private int releaseTick;
    private boolean meleeRanged;
    private boolean meleeDelivery;
    private boolean meleeNeedsGround;
    private double meleeReach;
    private double meleeStrikeReach;
    private double meleeApproachSpeed = 1.0;
    private int meleeHoldTicks;
    private boolean castAborted;
    @Nullable private MobMeleeSpellAttacks meleeAttacks;

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

    public MobSpellCastGoal withAnimations(boolean animations) {
        this.animations = animations;
        return this;
    }

    public MobSpellCastGoal withEquipmentSpells(boolean equipmentSpells) {
        this.equipmentSpells = equipmentSpells;
        return this;
    }

    public MobSpellCastGoal withPassiveSpellTag(@Nullable String tag) {
        this.passiveSpellTag = tag;
        return this;
    }

    public MobSpellCastGoal withTimings(SpellTimings timings) {
        this.goalTimings = timings != null ? timings : SpellTimings.NONE;
        return this;
    }

    public MobSpellCastGoal withTimings(String spellId, SpellTimings timings) {
        Identifier id = Identifier.of(spellId);
        if (timings == null) {
            spellTimings.remove(id);
        } else {
            spellTimings.put(id, timings);
        }
        return this;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    private List<RegistryEntry<Spell>> resolveSpells(net.minecraft.world.World world) {
        if (spellOrTag.startsWith("#")) {
            return SpellRegistry.entries(world, spellOrTag.substring(1));
        }
        var entry = SpellRegistry.from(world).getEntry(Identifier.of(spellOrTag)).orElse(null);
        return entry != null ? List.of(entry) : List.of();
    }

    private List<RegistryEntry<Spell>> resolveEquipmentAndTagSpells(net.minecraft.world.World world) {
        if (!equipmentSpells && passiveSpellTag == null) return List.of();
        List<RegistryEntry<Spell>> result = new ArrayList<>();
        Set<Identifier> seen = new HashSet<>();

        if (equipmentSpells) {
            var mainhand = caster.asMobEntity().getMainHandStack();
            SpellContainer container = SpellContainerHelper.containerFromItemStack(mainhand);
            if (container != null) {
                for (String spellId : container.spell_ids()) {
                    var entry = SpellRegistry.from(world).getEntry(Identifier.of(spellId)).orElse(null);
                    if (entry != null && seen.add(entry.getKey().orElseThrow().getValue())) {
                        result.add(entry);
                    }
                }
            }
        }

        if (passiveSpellTag != null) {
            for (var entry : SpellRegistry.entries(world, passiveSpellTag)) {
                if (seen.add(entry.getKey().orElseThrow().getValue())) {
                    result.add(entry);
                }
            }
        }

        return result;
    }

    // Spell Engine only dispatches passive triggers for players.
    // So this just resolves the passives, applying their effect is up to the content mod.
    public List<RegistryEntry<Spell>> resolvedPassiveSpells(net.minecraft.world.World world) {
        List<RegistryEntry<Spell>> passives = new ArrayList<>();
        for (var entry : resolveEquipmentAndTagSpells(world)) {
            if (entry.value().type == Spell.Type.PASSIVE) {
                passives.add(entry);
            }
        }
        return passives;
    }

    private float matchingModifierRangeBonus(Identifier activeId, net.minecraft.world.World world) {
        float bonus = 0F;
        for (var entry : resolveEquipmentAndTagSpells(world)) {
            var modifiers = entry.value().modifiers;
            if (modifiers == null) continue;
            for (var modifier : modifiers) {
                if (modifier.spell_pattern != null && matchesPattern(modifier.spell_pattern, activeId.toString())) {
                    bonus += modifier.range_add;
                }
            }
        }
        return bonus;
    }

    private static boolean matchesPattern(String pattern, String spellId) {
        if (pattern.equals(spellId)) return true;
        if (pattern.endsWith("*")) {
            return spellId.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return false;
    }

    @Override
    public boolean canStart() {
        MobEntity entity = caster.asMobEntity();
        assistSummons(entity);
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
            if (entity.getWorld().getTime() < cooldownReadyAt.getOrDefault(id, 0L)) continue;
            Spell spell = entry.value();

            Spell.Target.Type targetType = spell.target != null ? spell.target.type : Spell.Target.Type.CASTER;

            if ((targetType == Spell.Target.Type.NONE && !MobMeleeSpellAttacks.isMeleeDelivery(spell) && !isCustomDelivery(spell))
                    || targetType == Spell.Target.Type.FROM_TRIGGER) continue;

            double range = startRange(entity, entry) + matchingModifierRangeBonus(id, entity.getWorld());

            if (needsCombatTarget(spell)) {
                boolean hasCombatTarget = target != null && target.isAlive()
                        && Math.sqrt(entity.squaredDistanceTo(target)) <= range;
                if (hasCombatTarget && (!isArrowDelivery(spell) || !entity.getMainHandStack().isEmpty())) {
                    candidates.add(id);
                }
            } else if (targetType == Spell.Target.Type.CASTER) {
                if (hasHealingImpact(spell)) {
                    if (entity.getHealth() < entity.getMaxHealth() || findWoundedAlly(entity, 16.0) != null) {
                        candidates.add(id);
                    }
                } else if (hasOffensiveImpact(spell)) {
                    boolean hasCombatTarget = target != null && target.isAlive()
                            && Math.sqrt(entity.squaredDistanceTo(target)) <= range;
                    if (hasCombatTarget) {
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

    private static boolean usesMeleeRange(Spell spell) {
        return spell.range_mechanic == Spell.RangeMechanic.MELEE || MobMeleeSpellAttacks.isMeleeDelivery(spell);
    }

    private static boolean hasCastDuration(Spell spell) {
        return spell.active != null && spell.active.cast != null && spell.active.cast.duration > 0;
    }

    private static double startRange(MobEntity entity, RegistryEntry<Spell> entry) {
        Spell spell = entry.value();
        if (!usesMeleeRange(spell)) return spellRange(entity, entry);
        double reach = MobMeleeSpellAttacks.meleeRange(entity, entry);
        return hasCastDuration(spell)
                ? reach + MELEE_AIM_TOLERANCE
                : reach + MobMeleeSpellAttacks.momentumReach(spell) + MELEE_START_MARGIN;
    }

    private static double spellRange(MobEntity entity, RegistryEntry<Spell> entry) {
        return entry.value().range > 0 ? SpellParameters.getRangeCurved(entity, entry, 1F) : 16.0;
    }

    private float chargeReleaseRatio(MobEntity entity, RegistryEntry<Spell> entry, @Nullable LivingEntity target) {
        var charge = entry.value().active.cast.charge;
        float min = MathHelper.clamp(charge.min_release_ratio, 0F, 1F);
        if (target == null || isSelfCast) return 1F;
        double dist = Math.sqrt(entity.squaredDistanceTo(target));
        float minRange = SpellParameters.getRangeCurved(entity, entry, 0F);
        float maxRange = SpellParameters.getRangeCurved(entity, entry, 1F);
        if (maxRange - minRange < 0.01F) {
            return dist <= MIN_CAST_DISTANCE ? min : 1F;
        }
        float curved = MathHelper.clamp((float) ((dist - minRange) / (maxRange - minRange)), 0F, 1F);
        float low = 0F;
        float high = 1F;
        for (int i = 0; i < 20; i++) {
            float mid = (low + high) / 2F;
            if (charge.curve.apply(mid) < curved) low = mid;
            else high = mid;
        }
        return MathHelper.clamp(high, min, 1F);
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
        if (meleeAttacks != null) return caster.asMobEntity().isAlive();
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
        isSelfCast = startBehavior == DeliveryBehavior.SELF || startBehavior == DeliveryBehavior.STASH
                || (startBehavior == DeliveryBehavior.CUSTOM && (spell.target == null || spell.target.type == Spell.Target.Type.CASTER));
        isBeamCast = (startBehavior == DeliveryBehavior.BEAM);
        float modifierRangeBonus = matchingModifierRangeBonus(activeSpellId, caster.asMobEntity().getWorld());
        if (healingTarget != null) {
            healingSpellRange = (spell.range > 0 ? spell.range : 16.0) + modifierRangeBonus;
        }

        SpellTimings perSpell = spellTimings.get(activeSpellId);
        boolean hasCast = spell.active != null && spell.active.cast != null;
        int spellChannelTicks = hasCast ? spell.active.cast.channelTicks() : 0;
        castingTime = SpellTimings.resolveCastTicks(perSpell, goalTimings, spellChannelTicks > 0,
                hasCast ? Float.valueOf(spell.active.cast.duration) : null);
        totalCastTime = castingTime;

        channelReleases = SpellTimings.resolveChannelReleases(perSpell, goalTimings, spellChannelTicks);
        if (channelReleases > 0) {
            channelIntervalTicks = castingTime / (float) channelReleases;
            channelMultiplier = SpellParameters.channelValueMultiplier(spell);
            if (channelIntervalTicks < 1F) channelMultiplier /= channelIntervalTicks;
            releasesDone = 0;
        }

        isChargeCast = channelReleases == 0 && spell.active != null && spell.active.cast != null
                && spell.active.cast.type == Spell.Active.Cast.Type.CHARGE
                && spell.active.cast.charge != null;
        chargeReleased = false;

        castMovementSpeed = (spell.active != null && spell.active.cast != null) ? spell.active.cast.movement_speed : 0.2F;
        castSpellRange = spellRange(caster.asMobEntity(), entry) + modifierRangeBonus;

        releaseTick = hasCastDuration(spell) ? 0 : totalCastTime / 2;
        meleeRanged = usesMeleeRange(spell);
        meleeDelivery = startBehavior == DeliveryBehavior.MELEE;
        meleeNeedsGround = meleeDelivery && !spell.deliver.melee.allow_airborne;
        meleeReach = MobMeleeSpellAttacks.meleeRange(caster.asMobEntity(), entry) + modifierRangeBonus;
        meleeStrikeReach = meleeReach + MobMeleeSpellAttacks.momentumReach(spell);
        if (startBehavior == DeliveryBehavior.AREA) meleeReach *= AREA_APPROACH_FACTOR;
        meleeApproachSpeed = hasCastDuration(spell) ? castMovementSpeed : 1.0;
        meleeHoldTicks = 0;
        castAborted = false;
        meleeAttacks = null;

        caster.startSpellCast(castingTime);
        if (animationsEnabled() && hasCast) {
            var cast = spell.active.cast;
            float naturalTicks = cast.duration * 20F;
            float timeScale = naturalTicks > 0 ? MathHelper.clamp(naturalTicks / castingTime, 0.25F, 4F) : 1F;
            var animation = cast.animation;
            MobAnimations.playSpellAnimation(caster.asMobEntity(), MobAnimationLayer.CASTING,
                    AnimationHelper.getAnimationId(caster.asMobEntity(), animation),
                    animation != null ? animation.speed * timeScale : 1F);
        }
        if (animations && hasCast && spell.active.cast.animation_spin != 0) {
            float spinInterval = channelReleases > 0 ? channelIntervalTicks : castingTime;
            sendSpin(caster.asMobEntity(), spell.active.cast.animation_spin * 20F / spinInterval, castingTime);
            spinning = true;
        }
        if (spell.active != null && spell.active.cast != null) {
            ParticleHelper.sendBatches(caster.asMobEntity(), spell.active.cast.particles);
            SoundHelper.playSound(caster.asMobEntity().getWorld(), caster.asMobEntity(), spell.active.cast.start_sound);
        }
    }

    @Override
    public void tick() {
        MobEntity entity = caster.asMobEntity();
        assistSummons(entity);
        LivingEntity target = entity.getTarget();
        LivingEntity lookAt = target != null ? target : healingTarget;
        if (lookAt != null) entity.getLookControl().lookAt(lookAt, 30.0F, 30.0F);

        if (meleeAttacks != null) {
            meleeAttacks.tick(target);
            if (meleeAttacks.isDone()) meleeAttacks = null;
            if (castingTime <= 0) return;
        }

        if (healingTarget != null && healingTarget.isAlive()) {
            double distSq = entity.squaredDistanceTo(healingTarget);
            double rangeSq = healingSpellRange * healingSpellRange;
            if (distSq > rangeSq) {
                entity.getNavigation().startMovingTo(healingTarget, 1.0);
            } else {
                entity.getNavigation().stop();
            }
        } else if (!isSelfCast && target != null) {
            if (meleeRanged) {
                handleMeleeApproach(target);
            } else {
                handleCastMovement(target);
            }
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
            int elapsed = totalCastTime - castingTime;
            if (releasesDone < channelReleases && elapsed >= channelIntervalTicks * (releasesDone + 0.5F)) {
                faceTarget(target != null ? target : healingTarget);
                castSpell(releasesDone);
                releasesDone++;
            }
            if (castingTime <= 0) finishChannel(entity);
        } else if (isChargeCast) {
            RegistryEntry<Spell> entry = SpellRegistry.from(entity.getWorld()).getEntry(activeSpellId).orElse(null);
            if (!chargeReleased && entry != null) {
                float charged = MathHelper.clamp(1F - (float) castingTime / (float) totalCastTime, 0F, 1F);
                float ratio = chargeReleaseRatio(entity, entry, lookAt);
                if (castingTime <= 0 || charged >= ratio) {
                    chargeReleased = true;
                    faceTarget(lookAt);
                    castSpell(0, ratio);
                    castingTime = 0;
                }
            } else if (entry == null) {
                castingTime = 0;
            }
        } else if (castingTime == releaseTick) {
            if (meleeDelivery && !meleeReady(entity, target)) {
                if (++meleeHoldTicks <= MAX_MELEE_HOLD_TICKS) {
                    castingTime = releaseTick + 1;
                } else {
                    castAborted = true;
                    castingTime = 0;
                }
            } else {
                faceTarget(target != null ? target : healingTarget);
                castSpell(0);
            }
        }
    }

    private boolean meleeReady(MobEntity entity, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (meleeNeedsGround && !entity.isOnGround()) return false;
        return MobMeleeSpellAttacks.distanceToBox(entity, target) <= meleeStrikeReach;
    }

    private void handleMeleeApproach(LivingEntity target) {
        MobEntity entity = caster.asMobEntity();
        if (MobMeleeSpellAttacks.distanceToBox(entity, target) > meleeReach) {
            entity.getNavigation().startMovingTo(target, meleeApproachSpeed);
        } else {
            entity.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        caster.stopSpellCast();
        sendBeamClearPacket(caster.asMobEntity());
        if (spinning) {
            sendSpin(caster.asMobEntity(), 0F, 0);
            spinning = false;
        }
        if (animationsEnabled()) {
            MobAnimations.stop(caster.asMobEntity(), MobAnimationLayer.CASTING, 5);
        }
        if (activeSpellId != null) {
            RegistryEntry<Spell> entry = castAborted ? null
                    : SpellRegistry.from(caster.asMobEntity().getWorld()).getEntry(activeSpellId).orElse(null);
            if (entry != null) {
                int cd = SpellTimings.resolveCooldownTicks(spellTimings.get(activeSpellId), goalTimings,
                        entry.value().cost.cooldown.duration);
                if (cd > 0) {
                    cooldownReadyAt.put(activeSpellId, caster.asMobEntity().getWorld().getTime() + cd);
                } else {
                    cooldownReadyAt.remove(activeSpellId);
                }
            }
            activeSpellId = null;
        }
        castingTime = 0;
        totalCastTime = 0;
        channelReleases = 0;
        channelIntervalTicks = 0F;
        channelMultiplier = 1F;
        releasesDone = 0;
        isSelfCast = false;
        isBeamCast = false;
        isChargeCast = false;
        chargeReleased = false;
        healingTarget = null;
        healingSpellRange = 8.0;
        castMovementSpeed = 0.2F;
        castSpellRange = 16.0;
        releaseTick = 0;
        meleeRanged = false;
        meleeDelivery = false;
        meleeNeedsGround = false;
        meleeHoldTicks = 0;
        castAborted = false;
        meleeAttacks = null;
    }

    private void castSpell(int channelIndex) {
        castSpell(channelIndex, 1F);
    }

    private void castSpell(int channelIndex, float chargeRatio) {
        MobEntity entity = caster.asMobEntity();
        if (entity.getWorld().isClient() || activeSpellId == null) return;

        RegistryEntry<Spell> entry = SpellRegistry.from(entity.getWorld()).getEntry(activeSpellId).orElse(null);
        if (entry == null) {
            MRPGCMod.LOGGER.warn("Spell not found: {}", activeSpellId);
            return;
        }

        try {
            Spell spell = entry.value();
            World world = entity.getWorld();
            boolean channeled = channelReleases > 0;
            boolean releaseFx = !channeled || (spell.active != null && spell.active.cast != null && spell.active.cast.channelReleaseFx());
            if (releaseFx) playReleaseAnimation(spell);
            DeliveryBehavior behavior = deriveDelivery(spell);
            SpellPower.Result power = SpellPower.getSpellPower(spell.school, entity);
            LivingEntity combatTarget = entity.getTarget();
            if (combatTarget != null) {
                entity.onAttacking(combatTarget);
            }

            Spell.Modifier chargeModifier = null;
            float curvedRatio = 1F;
            if (isChargeCast && spell.active != null && spell.active.cast != null && spell.active.cast.charge != null) {
                var charge = spell.active.cast.charge;
                curvedRatio = charge.curve.apply(chargeRatio);
                chargeModifier = SpellModifiers.scaledBy(charge.bonus, curvedRatio);
            }
            SpellExecution.ImpactContext base = new SpellExecution.ImpactContext(
                    channeled ? channelMultiplier : 1F, 1F, null, power, SpellIntents.focusMode(spell),
                    channeled ? channelIndex : 0)
                    .chargeModifier(chargeModifier).charge(curvedRatio);
            Runnable fx = () -> {
                if (releaseFx) ReleaseFx.send(world, entity, entry, chargeRatio);
            };
            Consumer<SpellExecution.DeliveryCompletion> completion = result -> {
                if (result.success()) fx.run();
            };

            switch (behavior) {
                case PROJECTILE -> {
                    LivingEntity target = entity.getTarget();
                    if (target == null) return;
                    SpellExecution.ImpactContext ctx = base.position(entity.getEyePos());
                    afterDeliveryDelay(entity, spell, () -> {
                        fx.run();
                        ProjectileLauncher.shootProjectile(world, entity, target.isRemoved() ? null : target, entry, ctx, channelIndex);
                    });
                }
                case ARROW -> {
                    LivingEntity target = entity.getTarget();
                    if (target == null || entity.getMainHandStack().isEmpty()) return;
                    SpellExecution.ImpactContext ctx = base.position(entity.getEyePos());
                    var launch = spell.deliver.shoot_arrow != null ? spell.deliver.shoot_arrow.launch_properties : null;
                    int window = launch != null ? launch.extra_launch_count * launch.extra_launch_delay + 2 : 2;
                    afterDeliveryDelay(entity, spell, () -> {
                        fx.run();
                        MobArrowContext.beginSpellShot(entity, entry, window);
                        ArrowHelper.shootArrow(world, entity, entry, ctx);
                    });
                }
                case AFFECT_ARROW -> afterDeliveryDelay(entity, spell, () -> {
                    fx.run();
                    MobArrowContext.affectNextArrow(entity, entry, AFFECT_ARROW_WINDOW_TICKS);
                });
                case STASH -> {
                    boolean selfTarget = spell.target == null || spell.target.type == Spell.Target.Type.CASTER;
                    LivingEntity stashTarget = !selfTarget && healingTarget != null ? healingTarget : entity;
                    SpellExecution.ImpactContext ctx = base.position(stashTarget.getPos());
                    SpellDelivery.deliver(world, entry, entity,
                            List.of(new SpellExecution.DeliveryTarget(stashTarget, ctx)), ctx, null, completion);
                }
                case METEOR -> {
                    boolean selfTarget = spell.target == null || spell.target.type == Spell.Target.Type.CASTER;
                    LivingEntity impactTarget = selfTarget ? entity : entity.getTarget();
                    if (!selfTarget && impactTarget == null) return;
                    var pos = impactTarget.getPos();
                    SpellExecution.ImpactContext ctx = base.position(pos).target(SpellTarget.FocusMode.AREA);
                    afterDeliveryDelay(entity, spell, () -> {
                        fx.run();
                        try {
                            ProjectileLauncher.fallProjectile(world, entity, impactTarget, pos, entry, ctx);
                        } catch (Exception e) {
                            SpellImpacts.performImpacts(world, entity, impactTarget, entity, entry, spell.impacts, ctx, false, null);
                        }
                    });
                }
                case CLOUD -> {
                    boolean selfTarget = spell.target == null || spell.target.type == Spell.Target.Type.CASTER;
                    LivingEntity impactTarget = selfTarget ? entity : entity.getTarget();
                    if (!selfTarget && impactTarget == null) return;
                    var pos = impactTarget.getPos();
                    SpellExecution.ImpactContext ctx = base.position(entity.getEyePos()).target(SpellTarget.FocusMode.AREA);
                    afterDeliveryDelay(entity, spell, () -> {
                        fx.run();
                        CloudPlacer.placeCloud(world, entity, impactTarget, pos, entry, ctx);
                    });
                }
                case AREA -> {
                    if (hasSpawnImpact(spell)) {
                        LivingEntity spawnTarget = entity.getTarget();
                        if (spawnTarget != null) {
                            SpellExecution.ImpactContext selfCtx = base.position(spawnTarget.getPos()).target(SpellTarget.FocusMode.DIRECT);
                            SpellImpacts.performImpacts(world, entity, entity, entity, entry, spell.impacts, selfCtx, false, Spell.Impact.Action.Type.SPAWN);
                        }
                    }
                    SpellDelivery.resolveAndDeliver(world, entity, entry, findSpellTargets(entity, entry, chargeRatio), base, completion);
                }
                case BEAM -> {
                    LivingEntity target = entity.getTarget();
                    if (target == null) return;
                    if (spell.active != null && spell.active.cast != null) {
                        ParticleHelper.sendBatches(entity, spell.active.cast.particles);
                    }
                    if (hasSpawnImpact(spell)) {
                        SpellExecution.ImpactContext selfCtx = base.position(target.getPos()).target(SpellTarget.FocusMode.DIRECT);
                        SpellImpacts.performImpacts(world, entity, entity, entity, entry, spell.impacts, selfCtx, false, Spell.Impact.Action.Type.SPAWN);
                    }
                    List<SpellExecution.DeliveryTarget> beamTargets = new ArrayList<>();
                    beamTargets.add(new SpellExecution.DeliveryTarget(target, base));
                    double beamRange = spell.range > 0 ? spell.range : 32.0;
                    Vec3d beamFrom = entity.getEyePos();
                    Vec3d beamDir = target.getEyePos().subtract(beamFrom).normalize();
                    for (Entity candidate : world.getOtherEntities(entity,
                            entity.getBoundingBox().expand(beamRange),
                            e -> e instanceof LivingEntity && e.isAlive() && e != target)) {
                        Vec3d toCandidate = candidate.getBoundingBox().getCenter().subtract(beamFrom);
                        double projection = toCandidate.dotProduct(beamDir);
                        if (projection < 0 || projection > beamRange) continue;
                        double lateralDistSq = toCandidate.subtract(beamDir.multiply(projection)).lengthSquared();
                        if (lateralDistSq > 2.0 * 2.0) continue;
                        beamTargets.add(new SpellExecution.DeliveryTarget(candidate, base));
                    }
                    SpellDelivery.deliver(world, entry, entity, beamTargets, base, null, completion);
                }
                case DIRECT -> {
                    LivingEntity target = entity.getTarget();
                    LivingEntity effectiveTarget = target != null ? target : healingTarget;
                    if (effectiveTarget == null) return;
                    if (spell.active != null && spell.active.cast != null) {
                        ParticleHelper.sendBatches(entity, spell.active.cast.particles);
                    }
                    SpellExecution.ImpactContext ctx = base.position(entity.getEyePos());
                    if (hasSpawnImpact(spell)) {
                        SpellExecution.ImpactContext selfCtx = base.position(effectiveTarget.getPos()).target(SpellTarget.FocusMode.DIRECT);
                        SpellImpacts.performImpacts(world, entity, entity, entity, entry, spell.impacts, selfCtx, false, Spell.Impact.Action.Type.SPAWN);
                    }
                    if (spell.deliver == null || spell.deliver.type == Spell.Delivery.Type.DIRECT) {
                        deliverDirect(entity, entry, effectiveTarget, ctx, completion);
                    } else {
                        fx.run();
                        SpellImpacts.performImpacts(world, entity, effectiveTarget, entity, entry, spell.impacts, ctx, false, null);
                    }
                }
                case MELEE -> {
                    fx.run();
                    meleeAttacks = MobMeleeSpellAttacks.start(entity, entry, channeled ? channelIndex : -1, animationsEnabled());
                    if (meleeAttacks != null && meleeAttacks.isDone()) meleeAttacks = null;
                    if (!channeled) castingTime = 0;
                }
                case SELF -> {
                    if (hasSpawnImpact(spell)) {
                        Vec3d spawnPos = combatTarget != null ? combatTarget.getPos() : entity.getPos();
                        SpellExecution.ImpactContext spawnCtx = base.position(spawnPos).target(SpellTarget.FocusMode.DIRECT);
                        SpellImpacts.performImpacts(world, entity, entity, entity, entry, spell.impacts, spawnCtx, false, Spell.Impact.Action.Type.SPAWN);
                    }
                    if (onlyHasSpawnImpacts(spell)) {
                        fx.run();
                    } else if (spell.deliver == null || spell.deliver.type == Spell.Delivery.Type.DIRECT) {
                        SpellDelivery.deliver(world, entry, entity,
                                List.of(new SpellExecution.DeliveryTarget(entity, base)), base, null, completion);
                    } else {
                        fx.run();
                        SpellExecution.ImpactContext ctx = base.position(entity.getPos()).target(SpellTarget.FocusMode.AREA);
                        SpellImpacts.performImpacts(world, entity, entity, entity, entry, spell.impacts, ctx, false, null);
                    }
                }
                case CUSTOM -> {
                    SpellTarget.SearchResult targets = findSpellTargets(entity, entry, chargeRatio);
                    LivingEntity aimTarget = combatTarget != null ? combatTarget : healingTarget;
                    Spell.Target.Type targetType = spell.target.type;
                    if (targets.entities().isEmpty() && aimTarget != null
                            && (targetType == Spell.Target.Type.AIM || targetType == Spell.Target.Type.BEAM)) {
                        targets = new SpellTarget.SearchResult(List.of(aimTarget), targets.location());
                    }
                    SpellDelivery.resolveAndDeliver(world, entity, entry, targets, base, completion);
                }
                case TELEPORT -> {
                    LivingEntity target = entity.getTarget();
                    if (spell.release != null) fx.run();
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
                            Vec3d ground = TargetHelper.findSolidBlockBelow(entity, destination, world, -1.5F);
                            if (ground != null) destination = ground;
                            if (data.depart != null) ParticleHelper.sendBatches(entity, data.depart.particles);
                            world.emitGameEvent(GameEvent.TELEPORT, entity.getPos(), GameEvent.Emitter.of(entity));
                            entity.teleport(destination.x, destination.y, destination.z, false);
                            if (data.arrive != null) ParticleHelper.sendBatches(entity, data.arrive.particles);
                        }
                    }
                }
            }
            if (hasSummonImpact(spell)) {
                summonAssistUntil = entity.getWorld().getTime() + SUMMON_ASSIST_TICKS;
            }
        } catch (Exception e) {
            MRPGCMod.LOGGER.error("Failed to cast spell {}", activeSpellId, e);
        }
    }

    private void finishChannel(MobEntity entity) {
        if (activeSpellId == null || entity.getWorld().isClient()) return;
        RegistryEntry<Spell> entry = SpellRegistry.from(entity.getWorld()).getEntry(activeSpellId).orElse(null);
        if (entry == null) return;
        Spell spell = entry.value();
        if (spell.active == null || spell.active.cast == null || spell.active.cast.channelReleaseFx()) return;
        playReleaseAnimation(spell);
        ReleaseFx.send(entity.getWorld(), entity, entry, 1F);
    }

    private static void afterDeliveryDelay(MobEntity entity, Spell spell, Runnable delivery) {
        int delay = spell.deliver != null ? spell.deliver.delay : 0;
        if (delay <= 0) {
            delivery.run();
            return;
        }
        ((WorldScheduler) entity.getWorld()).schedule(delay, () -> {
            if (!entity.isRemoved()) delivery.run();
        });
    }

    private SpellTarget.SearchResult findSpellTargets(MobEntity entity, RegistryEntry<Spell> entry, float chargeRatio) {
        Spell spell = entry.value();
        float range = SpellParameters.getRange(entity, entry, chargeRatio)
                + matchingModifierRangeBonus(activeSpellId, entity.getWorld());
        SpellTarget.SearchResult result = SpellTarget.findTargets(entity, entry, SpellTarget.SearchResult.empty(), true, range);
        if (spell.target.cap > 0 && result.entities().size() > spell.target.cap) {
            List<Entity> capped = result.entities().stream()
                    .sorted(Comparator.comparingDouble(t -> t.squaredDistanceTo(entity)))
                    .limit(spell.target.cap)
                    .toList();
            result = new SpellTarget.SearchResult(capped, result.location());
        }
        return result;
    }

    private DeliveryBehavior deriveDelivery(Spell spell) {
        Spell.Target.Type targetType = spell.target != null ? spell.target.type : Spell.Target.Type.CASTER;
        if (MobMeleeSpellAttacks.isMeleeDelivery(spell)) return DeliveryBehavior.MELEE;
        if (isCustomDelivery(spell)) return DeliveryBehavior.CUSTOM;
        switch (targetType) {
            case BEAM -> { return DeliveryBehavior.BEAM; }
            case AREA -> { return DeliveryBehavior.AREA; }
            default   -> { }
        }
        if (hasTeleportImpact(spell)) return DeliveryBehavior.TELEPORT;
        if (spell.deliver != null) {
            return switch (spell.deliver.type) {
                case METEOR -> DeliveryBehavior.METEOR;
                case CLOUD  -> DeliveryBehavior.CLOUD;
                case SHOOT_ARROW -> DeliveryBehavior.ARROW;
                case AFFECT_ARROW -> DeliveryBehavior.AFFECT_ARROW;
                case STASH_EFFECT -> DeliveryBehavior.STASH;
                case PROJECTILE -> targetType == Spell.Target.Type.CASTER
                        ? DeliveryBehavior.SELF : DeliveryBehavior.PROJECTILE;
                case MELEE -> targetType == Spell.Target.Type.CASTER
                        ? DeliveryBehavior.SELF : DeliveryBehavior.DIRECT;
                default -> targetType == Spell.Target.Type.CASTER ? DeliveryBehavior.SELF : DeliveryBehavior.DIRECT;
            };
        }
        return targetType == Spell.Target.Type.CASTER ? DeliveryBehavior.SELF : DeliveryBehavior.DIRECT;
    }

    private static boolean isCustomDelivery(Spell spell) {
        return spell.deliver != null && spell.deliver.type == Spell.Delivery.Type.CUSTOM;
    }

    private void deliverDirect(MobEntity entity, RegistryEntry<Spell> entry, LivingEntity target, SpellExecution.ImpactContext ctx,
                               Consumer<SpellExecution.DeliveryCompletion> completion) {
        Spell spell = entry.value();
        var world = entity.getWorld();
        if (meleeRanged && target != healingTarget && MobMeleeSpellAttacks.distanceToBox(entity, target) > meleeReach + MELEE_AIM_TOLERANCE) {
            if (spell.area_impact == null) return;
            Vec3d toTarget = target.getPos().subtract(entity.getPos());
            Vec3d location = entity.getPos().add(toTarget.normalize().multiply(meleeReach));
            var aim = spell.target != null ? spell.target.aim : null;
            if (aim != null && aim.reposition_vertically != 0) {
                Vec3d grounded = TargetHelper.findSolidBelow(entity, location, world, aim.reposition_vertically);
                if (grounded != null) location = grounded;
            }
            SpellDelivery.deliver(world, entry, entity, List.of(), ctx, location, completion);
            return;
        }
        SpellDelivery.deliver(world, entry, entity, List.of(new SpellExecution.DeliveryTarget(target, ctx)), ctx, null, completion);
    }

    private static void sendSpin(MobEntity mob, float degreesPerTick, int durationTicks) {
        if (!(mob.getWorld() instanceof ServerWorld)) return;
        MRPGCNetworking.sendToTracking(mob, new MobSpinPacket(mob.getId(), degreesPerTick, durationTicks));
    }

    private static boolean hasSpawnImpact(Spell spell) {
        if (spell.impacts == null) return false;
        for (var impact : spell.impacts) {
            if (impact.action != null && impact.action.type == Spell.Impact.Action.Type.SPAWN) return true;
        }
        return false;
    }

    private static boolean hasSummonImpact(Spell spell) {
        if (spell.impacts == null) return false;
        for (var impact : spell.impacts) {
            if (impact.action != null && impact.action.type == Spell.Impact.Action.Type.SUMMON) return true;
        }
        return false;
    }

    private static boolean isArrowDelivery(Spell spell) {
        return spell.deliver != null && spell.deliver.type == Spell.Delivery.Type.SHOOT_ARROW;
    }

    private static boolean needsCombatTarget(Spell spell) {
        if (spell.deliver == null) return false;
        return switch (spell.deliver.type) {
            case SHOOT_ARROW, AFFECT_ARROW, MELEE -> true;
            case STASH_EFFECT -> spell.target == null || spell.target.type == Spell.Target.Type.CASTER;
            default -> false;
        };
    }

    private void assistSummons(MobEntity entity) {
        if (summonAssistUntil == 0L || entity.getWorld().isClient()) return;
        if (entity.getWorld().getTime() > summonAssistUntil) {
            summonAssistUntil = 0L;
            return;
        }
        LivingEntity target = entity.getTarget();
        if (target == null || !target.isAlive()) return;
        UUID ownerId = entity.getUuid();
        for (SummonedEntity summon : entity.getWorld().getEntitiesByClass(SummonedEntity.class,
                entity.getBoundingBox().expand(SUMMON_ASSIST_RANGE),
                s -> s.isAlive() && s.getTarget() == null && ownerId.equals(s.getOwnerUuid()))) {
            summon.setTarget(target);
        }
    }

    private static boolean onlyHasSpawnImpacts(Spell spell) {
        if (spell.impacts == null || spell.impacts.isEmpty()) return false;
        for (var impact : spell.impacts) {
            if (impact.action == null || impact.action.type != Spell.Impact.Action.Type.SPAWN) return false;
        }
        return true;
    }

    private static boolean hasOffensiveImpact(Spell spell) {
        if (spell.impacts == null) return false;
        for (var impact : spell.impacts) {
            if (impact.action != null && impact.action.type == Spell.Impact.Action.Type.DAMAGE) return true;
        }
        return false;
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

    private boolean animationsEnabled() {
        return animations && MrpgCompat.PLAYER_ANIMATOR;
    }

    private void playReleaseAnimation(Spell spell) {
        if (!animationsEnabled() || spell.release == null || spell.release.animation == null) return;
        MobAnimations.playSpellAnimation(caster.asMobEntity(), MobAnimationLayer.RELEASE,
                AnimationHelper.getAnimationId(caster.asMobEntity(), spell.release.animation),
                spell.release.animation.speed);
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
        sw.getPlayers().forEach(player -> MRPGCNetworking.sendToPlayer(player, packet));
    }

    private static void sendBeamClearPacket(MobEntity caster) {
        if (!(caster.getWorld() instanceof ServerWorld sw)) return;
        var packet = new MobBeamPacket(caster.getId(), -1, null);
        sw.getPlayers().forEach(player -> MRPGCNetworking.sendToPlayer(player, packet));
    }

    public boolean isActive() {
        return castingTime > 0;
    }

    public void updateCooldown() {
    }
}
