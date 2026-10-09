package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.spell_engine.SpellEngineMod;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.Fx;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.internals.SpellExecution;
import net.spell_engine.internals.SpellParameters;
import net.spell_engine.internals.delivery.melee.OrientedBoundingBox;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.AnimationHelper;
import net.spell_engine.utils.SoundHelper;
import net.spell_engine.utils.VectorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MobMeleeSpellAttacks {
    private static final double DEFAULT_ATTACK_SPEED = 1.6;
    private static final float MOMENTUM_REACH_FACTOR = 2F;
    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.of(MRPGCMod.MOD_ID, "melee_spell_attack");

    private final MobEntity mob;
    private final RegistryEntry<Spell> spellEntry;
    private final float range;
    private final boolean animations;
    private final Deque<Spell.Delivery.Melee.Attack> queue;
    @Nullable private Active current;

    private record Active(Spell.Delivery.Melee.Attack data, int startedAt, int duration, List<Integer> hitTicks,
                          float speed, Set<Integer> hitIds) {
        boolean finished(int age) {
            return age >= startedAt + duration && age >= hitTicks.get(hitTicks.size() - 1);
        }
    }

    private MobMeleeSpellAttacks(MobEntity mob, RegistryEntry<Spell> spellEntry, List<Spell.Delivery.Melee.Attack> attacks, boolean animations) {
        this.mob = mob;
        this.spellEntry = spellEntry;
        this.range = meleeRange(mob, spellEntry);
        this.animations = animations;
        this.queue = new ArrayDeque<>(attacks);
    }

    @Nullable
    public static MobMeleeSpellAttacks start(MobEntity mob, RegistryEntry<Spell> spellEntry, int channelIndex, boolean animations) {
        var melee = spellEntry.value().deliver.melee;
        if (melee == null || melee.attacks.isEmpty()) return null;
        if (!melee.allow_airborne && !mob.isOnGround()) return null;
        List<Spell.Delivery.Melee.Attack> attacks = channelIndex >= 0
                ? List.of(melee.attacks.get(channelIndex % melee.attacks.size()))
                : melee.attacks;
        var runner = new MobMeleeSpellAttacks(mob, spellEntry, attacks, animations);
        runner.tick(mob.getTarget());
        return runner;
    }

    public static boolean isMeleeDelivery(Spell spell) {
        return spell.deliver != null && spell.deliver.type == Spell.Delivery.Type.MELEE
                && spell.deliver.melee != null && !spell.deliver.melee.attacks.isEmpty();
    }

    public static float meleeRange(LivingEntity caster, RegistryEntry<Spell> spellEntry) {
        return SpellParameters.getRangeCurved(caster, spellEntry, 1F);
    }

    public static float momentumReach(Spell spell) {
        if (!isMeleeDelivery(spell)) return 0F;
        return spell.deliver.melee.attacks.get(0).forward_momentum * MOMENTUM_REACH_FACTOR;
    }

    public static boolean canStrikeFromHere(MobEntity mob, Spell spell) {
        return !isMeleeDelivery(spell) || spell.deliver.melee.allow_airborne || mob.isOnGround();
    }

    public static double distanceToBox(LivingEntity caster, Entity target) {
        return VectorHelper.distanceVector(tracingOrigin(caster), target.getBoundingBox()).length();
    }

    public boolean isDone() {
        return current == null && queue.isEmpty();
    }

    public void tick(@Nullable LivingEntity target) {
        if (!mob.isAlive()) {
            current = null;
            queue.clear();
            return;
        }
        int age = mob.age;
        if (current == null) activateNext(target, age);
        if (current == null) return;
        applySlipperiness(current.data.movement_slipperiness);
        if (current.hitTicks.contains(age)) {
            if (target != null && target.isAlive()) faceTowards(target);
            strike(current);
        }
        if (current.finished(age)) {
            current = null;
            activateNext(target, age);
        }
    }

    private void activateNext(@Nullable LivingEntity target, int age) {
        var data = queue.poll();
        if (data == null) return;
        float speed = data.attack_speed_multiplier;
        double cooldownTicks = 20.0 / attackSpeed();
        float rawDuration = data.duration > 0 ? data.duration : (float) Math.max(cooldownTicks / speed, 1.0);
        int duration = Math.round(rawDuration);
        int firstHit = age + Math.round(rawDuration * data.delay);
        int strikeGap = Math.max(Math.round(rawDuration * data.additional_strike_delay), 1);
        List<Integer> hitTicks = new ArrayList<>();
        for (int i = 0; i <= data.additional_strikes; i++) {
            hitTicks.add(firstHit + i * strikeGap);
        }
        current = new Active(data, age, duration, hitTicks, speed, new HashSet<>());

        if (target != null && target.isAlive()) faceTowards(target);
        mob.getNavigation().stop();
        if (data.forward_momentum > 0 && (data.allow_momentum_airborne || mob.isOnGround())) {
            Vec3d push = new Vec3d(0, 0, 1).rotateY((float) Math.toRadians(-mob.getYaw())).multiply(data.forward_momentum);
            mob.addVelocity(push.x, push.y, push.z);
            mob.velocityModified = true;
        }
        if (animations && data.animation != null) {
            MobAnimations.playSpellAnimation(mob, MobAnimationLayer.RELEASE,
                    AnimationHelper.getAnimationId(mob, data.animation), speed * data.animation.speed);
        }
        SoundHelper.playSound(mob.getWorld(), mob, data.swing_sound);
        if (data.visuals != null) {
            ParticleHelper.sendBatches(mob, data.visuals.resolved(Fx.Context.NONE).particles);
        }
    }

    private void applySlipperiness(float extra) {
        if (extra <= 0 || !mob.isOnGround()) return;
        float slip = mob.getWorld().getBlockState(mob.getVelocityAffectingPos()).getBlock().getSlipperiness();
        if (slip <= 0) return;
        float factor = Math.min(slip + extra, 1F) / slip;
        Vec3d v = mob.getVelocity();
        mob.setVelocity(v.x * factor, v.y, v.z * factor);
    }

    private void strike(Active active) {
        var data = active.data;
        List<Entity> found = findTargets(data.hitbox);
        if (!data.additional_hits_on_same_target) {
            found.removeIf(e -> active.hitIds.contains(e.getId()));
        }
        found.forEach(e -> active.hitIds.add(e.getId()));
        if (found.isEmpty()) return;

        var focusMode = SpellEngineMod.config != null && SpellEngineMod.config.melee_skills_area_focus_mode
                ? SpellTarget.FocusMode.AREA : SpellTarget.FocusMode.DIRECT;
        var damage = mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                ? mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) : null;
        EntityAttributeModifier bonus = null;
        if (damage != null && data.damage_bonus != 0) {
            bonus = new EntityAttributeModifier(DAMAGE_MODIFIER_ID, data.damage_bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            damage.addTemporaryModifier(bonus);
        }
        int soundsLeft = data.impact_sound_cap > 0 ? data.impact_sound_cap : Integer.MAX_VALUE;
        List<Entity> hit = new ArrayList<>();
        try {
            for (Entity target : found) {
                if (!target.isAttackable() || !EntityRelations.actionAllowed(focusMode, SpellTarget.Intent.HARMFUL, mob, target)) continue;
                if (damage != null) {
                    int regen = target.timeUntilRegen;
                    target.timeUntilRegen = 0;
                    mob.tryAttack(target);
                    target.timeUntilRegen = regen;
                }
                if (data.impact_sound != null && soundsLeft > 0) {
                    SoundHelper.playSound(target.getWorld(), target, data.impact_sound);
                    soundsLeft--;
                }
                hit.add(target);
            }
        } finally {
            if (bonus != null) damage.removeModifier(bonus);
        }
        if (!hit.isEmpty()) {
            SpellImpacts.meleeImpact(mob, hit, spellEntry, new SpellExecution.ImpactContext().position(mob.getPos()).charge(1F));
        }
    }

    private List<Entity> findTargets(Spell.Delivery.Melee.HitBox hitbox) {
        Vec3d origin = tracingOrigin(mob);
        Vec3d size = new Vec3d(hitbox.width * range, hitbox.height * range, hitbox.length * range);
        var obb = new OrientedBoundingBox(origin, size, mob.getPitch(), mob.getYaw(), hitbox.roll);
        if (hitbox.arc <= 180) {
            obb = obb.offsetAlongAxisZ(size.z / 2F);
        }
        obb.updateVertex();
        final var box = obb;
        double halfArc = MathHelper.clamp(hitbox.arc, 0, 360) / 2.0;
        List<Entity> result = new ArrayList<>();
        for (Entity entity : mob.getWorld().getOtherEntities(mob, mob.getBoundingBox().expand(range * 2F + 1.0),
                e -> !e.isSpectator() && e.canHit() && e.isAttackable() && e != mob.getVehicle())) {
            Vec3d center = entity.getPos().add(0, entity.getHeight() / 2F, 0);
            if (!box.intersects(entity.getBoundingBox().expand(entity.getTargetingMargin())) && !box.contains(center)) continue;
            Vec3d toBox = VectorHelper.distanceVector(origin, entity.getBoundingBox());
            Vec3d toCenter = center.subtract(origin);
            if (toBox.length() > range) continue;
            if (halfArc > 0 && VectorHelper.angleBetween(toCenter, box.axisZ) > halfArc
                    && VectorHelper.angleBetween(toBox, box.axisZ) > halfArc) continue;
            if (!clearPath(origin, origin.add(toBox)) && !clearPath(origin, origin.add(toCenter))) continue;
            result.add(entity);
        }
        return result;
    }

    private boolean clearPath(Vec3d from, Vec3d to) {
        var hit = mob.getWorld().raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mob));
        return hit.getType() != HitResult.Type.BLOCK;
    }

    private double attackSpeed() {
        if (mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_SPEED)) {
            double value = mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);
            if (value > 0) return value;
        }
        return DEFAULT_ATTACK_SPEED;
    }

    private void faceTowards(LivingEntity target) {
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();
        double dy = target.getEyeY() - mob.getEyeY();
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI)));
        mob.setYaw(yaw);
        mob.setPitch(pitch);
        mob.headYaw = yaw;
        mob.bodyYaw = yaw;
    }

    private static Vec3d tracingOrigin(LivingEntity entity) {
        return entity.getEyePos().subtract(0, entity.getHeight() * 0.15 * entity.getScaleFactor(), 0);
    }
}
