package com.mrpg_lib.compat.better_combat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import com.mrpg_lib.compat.player_animator.api.MobAnimationOptions;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import com.mrpg_lib.util.AllyHelper;
import net.bettercombat.BetterCombatMod;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.ComboState;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.fx.ParticlePlacement;
import net.bettercombat.api.fx.TrailAppearance;
import net.bettercombat.client.collision.CollisionHelper;
import net.bettercombat.client.collision.OrientedBoundingBox;
import net.bettercombat.client.collision.WeaponHitBoxes;
import net.bettercombat.client.particle.TrailParticles;
import net.bettercombat.config.ServerConfig;
import net.bettercombat.config.TrailConfig;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.logic.knockback.ConfigurableKnockback;
import net.bettercombat.particle.SlashParticleEffect;
import net.bettercombat.utils.SoundHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BCMobMeleeAttackGoal extends MeleeAttackGoal {
    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.of("mrpg_lib", "bc_mob_attack");
    private static final Identifier MOVEMENT_MODIFIER_ID = Identifier.of("mrpg_lib", "bc_mob_attack_movement");
    private static final Identifier DUAL_WIELD_SPEED_ID = Identifier.of("mrpg_lib", "bc_mob_dual_wield");
    private static final double DEFAULT_BASE_RANGE = 3.0;
    private static final double START_RANGE_FACTOR = 0.8;
    private static final int STOP_FADE_TICKS = 4;

    private double baseRange = DEFAULT_BASE_RANGE;
    private float intervalScale = 1.0F;
    private boolean animations = true;
    private boolean resetInvulnerability = true;
    private boolean weaponTrails = true;

    private int comboCount;
    private int readyAge;
    private int lastSwingAge = Integer.MIN_VALUE;
    private int comboResetTicks;
    private boolean slowed;
    @Nullable private Swing pending;
    private final Map<MobAnimationLayer, String> activePoses = new EnumMap<>(MobAnimationLayer.class);

    private record Swing(AttackHand hand, double range, LivingEntity target, int impactAge, float length,
                         ItemStack mainStack, ItemStack offStack, boolean dualWielding, boolean animated) {
    }

    public BCMobMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
    }

    public BCMobMeleeAttackGoal withBaseRange(double baseRange) {
        this.baseRange = Math.max(0.5, baseRange);
        return this;
    }

    public BCMobMeleeAttackGoal withIntervalScale(float intervalScale) {
        this.intervalScale = Math.max(0.1F, intervalScale);
        return this;
    }

    public BCMobMeleeAttackGoal withAnimations(boolean animations) {
        this.animations = animations;
        return this;
    }

    public BCMobMeleeAttackGoal withResetInvulnerability(boolean resetInvulnerability) {
        this.resetInvulnerability = resetInvulnerability;
        return this;
    }

    public BCMobMeleeAttackGoal withWeaponTrails(boolean weaponTrails) {
        this.weaponTrails = weaponTrails;
        return this;
    }

    @Nullable
    private WeaponAttributes weaponAttributes() {
        WeaponAttributes attributes = WeaponRegistry.getAttributes(mob.getMainHandStack());
        if (attributes == null || attributes.attacks() == null || attributes.attacks().length == 0) {
            return null;
        }
        return attributes;
    }

    private boolean isDualWielding() {
        return PlayerAttackHelper.isDualWielding(WeaponRegistry.getAttributes(mob.getMainHandStack()),
                WeaponRegistry.getAttributes(mob.getOffHandStack()));
    }

    @Override
    public boolean canStart() {
        updatePose();
        return mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null && super.canStart();
    }

    @Override
    public void start() {
        super.start();
        updatePose();
    }

    @Override
    public boolean shouldContinue() {
        return pending != null || super.shouldContinue();
    }

    @Override
    public void tick() {
        if (pending == null && lastSwingAge != Integer.MIN_VALUE && mob.age - lastSwingAge > comboResetTicks) {
            comboCount = 0;
        }
        super.tick();
        updatePose();
        if (slowed && mob.age >= readyAge) {
            removeSlowdown();
        }
        Swing swing = pending;
        if (swing == null) {
            return;
        }
        if (!ItemStack.areEqual(mob.getMainHandStack(), swing.mainStack())
                || (swing.dualWielding() && !ItemStack.areEqual(mob.getOffHandStack(), swing.offStack()))) {
            cancelSwing(swing);
            comboCount = 0;
            readyAge = mob.age;
            return;
        }
        if (mob.age >= swing.impactAge()) {
            pending = null;
            resolve(swing);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (pending != null) {
            cancelSwing(pending);
        }
        removeSlowdown();
        comboCount = 0;
        updatePose();
    }

    private void cancelSwing(Swing swing) {
        pending = null;
        removeSlowdown();
        if (swing.animated()) {
            int fade = (int) Math.round(swing.length() * (1 - 0.5 * BetterCombatMod.config.upswing_multiplier));
            MobAnimations.stop(mob, MobAnimationLayer.ATTACK, Math.max(1, fade));
        }
    }

    @Override
    protected boolean isCooledDown() {
        if (weaponAttributes() == null) {
            return super.isCooledDown();
        }
        return pending == null && mob.age >= readyAge;
    }

    @Override
    protected boolean canAttack(LivingEntity target) {
        WeaponAttributes attributes = weaponAttributes();
        if (attributes == null) {
            return pending == null && super.canAttack(target);
        }
        if (!isCooledDown() || !mob.getVisibilityCache().canSee(target)) {
            return false;
        }
        AttackHand next = nextAttack(attributes);
        if (next == null) {
            return false;
        }
        double range = attackRange(next.attributes(), next.attack()) * START_RANGE_FACTOR;
        return CollisionHelper.distance(tracingOrigin(), target.getBoundingBox()) <= range;
    }

    @Override
    protected void attack(LivingEntity target) {
        WeaponAttributes attributes = weaponAttributes();
        if (attributes == null) {
            if (pending == null) {
                super.attack(target);
            }
            return;
        }
        if (!canAttack(target)) {
            return;
        }
        AttackHand hand = nextAttack(attributes);
        if (hand == null) {
            return;
        }
        boolean dualWielding = isDualWielding();
        WeaponAttributes.Attack attack = hand.attack();
        float length = attackLength(false, dualWielding);
        float upswingRate = (float) hand.upswingRate();
        int upswingTicks = Math.max(1, Math.round(length * upswingRate));
        int intervalTicks = Math.max(upswingTicks + 1, Math.round(length));

        Identifier animation = null;
        if (animations && MobAnimations.isAvailable() && attack.animation() != null && !attack.animation().isEmpty()) {
            animation = Identifier.tryParse(attack.animation());
        }

        lastSwingAge = mob.age;
        comboResetTicks = Math.round(length * BetterCombatMod.config.combo_reset_rate);
        readyAge = mob.age + intervalTicks;
        pending = new Swing(hand, attackRange(hand.attributes(), attack), target, mob.age + upswingTicks, length,
                mob.getMainHandStack(), mob.getOffHandStack(), dualWielding, animation != null);
        applySlowdown(attack);

        if (animation != null) {
            MobAnimationOptions.Mirror mirror = hand.isOffHand()
                    ? (mob.getMainArm() == Arm.LEFT ? MobAnimationOptions.Mirror.NEVER : MobAnimationOptions.Mirror.ALWAYS)
                    : MobAnimationOptions.Mirror.AUTO;
            MobAnimations.play(mob, animation, MobAnimationOptions.DEFAULT
                    .withLayer(MobAnimationLayer.ATTACK)
                    .withMirror(mirror)
                    .withAttackTiming(length, upswingRate, BetterCombatMod.config.getUpswingMultiplier()));
        }
    }

    @Nullable
    private AttackHand nextAttack(WeaponAttributes mainAttributes) {
        int combo = Math.max(0, comboCount);
        boolean dualWielding = isDualWielding();
        boolean offHand = dualWielding && combo % 2 == 1;
        ItemStack stack = offHand ? mob.getOffHandStack() : mob.getMainHandStack();
        WeaponAttributes attributes = offHand ? WeaponRegistry.getAttributes(stack) : mainAttributes;
        if (attributes == null || attributes.attacks() == null) {
            return null;
        }
        int handCombo = dualWielding ? ((offHand && combo > 0) ? combo - 1 : combo) / 2 : combo;
        WeaponAttributes.Attack[] attacks = Arrays.stream(attributes.attacks())
                .filter(attack -> conditionsMet(attack, offHand))
                .toArray(WeaponAttributes.Attack[]::new);
        if (attacks.length == 0) {
            return null;
        }
        int index = handCombo % attacks.length;
        return new AttackHand(attacks[index], new ComboState(index + 1, attacks.length), offHand, attributes, stack);
    }

    private double attackRange(WeaponAttributes attributes, WeaponAttributes.Attack attack) {
        double rangeMultiplier = attack.rangeMultiplier() > 0 ? attack.rangeMultiplier() : 1.0;
        return PlayerAttackHelper.combineAttackRange(attributes, baseRange) * rangeMultiplier;
    }

    private void applySlowdown(WeaponAttributes.Attack attack) {
        removeSlowdown();
        ServerConfig config = BetterCombatMod.config;
        if (mob.hasVehicle() && !config.movement_speed_effected_while_mounting) {
            return;
        }
        double multiplier = MathHelper.clamp(config.movement_speed_while_attacking, 0.0, 1.0) * attack.movementSpeedMultiplier();
        double value = Math.max(-1.0, multiplier - 1.0);
        EntityAttributeInstance movement = mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (value == 0 || !Double.isFinite(value) || movement == null) {
            return;
        }
        movement.addTemporaryModifier(new EntityAttributeModifier(MOVEMENT_MODIFIER_ID, value, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        slowed = true;
    }

    private void removeSlowdown() {
        slowed = false;
        EntityAttributeInstance movement = mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (movement != null) {
            movement.removeModifier(MOVEMENT_MODIFIER_ID);
        }
    }

    private void resolve(Swing swing) {
        if (!mob.isAlive() || mob.isRemoved() || !(mob.getWorld() instanceof ServerWorld world)) {
            return;
        }
        comboCount++;
        AttackHand hand = swing.hand();
        if (!swing.animated()) {
            mob.swingHand(hand.isOffHand() ? Hand.OFF_HAND : Hand.MAIN_HAND);
        }
        Vec3d origin = tracingOrigin();
        float yaw;
        float pitch;
        LivingEntity target = swing.target();
        if (target != null && target.isAlive()) {
            Vec3d toTarget = target.getBoundingBox().getCenter().subtract(origin);
            yaw = (float) (MathHelper.atan2(toTarget.z, toTarget.x) * MathHelper.DEGREES_PER_RADIAN) - 90.0F;
            pitch = (float) -(MathHelper.atan2(toTarget.y, toTarget.horizontalLength()) * MathHelper.DEGREES_PER_RADIAN);
        } else {
            yaw = mob.getHeadYaw();
            pitch = mob.getPitch();
        }
        if (weaponTrails) {
            spawnTrails(world, hand, yaw, pitch);
        }
        withAttackingHand(hand.isOffHand(), () -> strike(world, swing, origin, yaw, pitch));
    }

    private void strike(ServerWorld world, Swing swing, Vec3d origin, float yaw, float pitch) {
        AttackHand hand = swing.hand();
        WeaponAttributes.Attack attack = hand.attack();
        if (attack.swingSound() != null) {
            SoundHelper.playSound(world, mob, attack.swingSound());
        }
        List<Entity> victims = findVictims(swing, origin, yaw, pitch);
        if (victims.isEmpty()) {
            return;
        }
        EntityAttributeInstance damage = mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double bonus = attack.damageMultiplier() - 1.0;
        if (swing.dualWielding()) {
            bonus += (hand.isOffHand()
                    ? BetterCombatMod.config.dual_wielding_off_hand_damage_multiplier
                    : BetterCombatMod.config.dual_wielding_main_hand_damage_multiplier) - 1.0;
        }
        boolean modified = damage != null && bonus != 0;
        if (modified) {
            damage.removeModifier(DAMAGE_MODIFIER_ID);
            damage.addTemporaryModifier(new EntityAttributeModifier(DAMAGE_MODIFIER_ID, bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        float knockback = knockbackMultiplier(hand.isOffHand() ? attackLength(true, swing.dualWielding()) : swing.length());
        try {
            for (Entity victim : victims) {
                ConfigurableKnockback knockbackTarget = null;
                if (victim instanceof LivingEntity living) {
                    if (resetInvulnerability && BetterCombatMod.config.allow_fast_attacks && living.getAttacker() == mob) {
                        living.timeUntilRegen = 0;
                    }
                    if (knockback != 1F && living instanceof ConfigurableKnockback configurable) {
                        knockbackTarget = configurable;
                        knockbackTarget.setKnockbackMultiplier_BetterCombat(knockback);
                    }
                }
                try {
                    mob.tryAttack(victim);
                } finally {
                    if (knockbackTarget != null) {
                        knockbackTarget.setKnockbackMultiplier_BetterCombat(1F);
                    }
                }
            }
        } finally {
            if (modified) {
                damage.removeModifier(DAMAGE_MODIFIER_ID);
            }
        }
        if (attack.impactSound() != null) {
            SoundHelper.playSound(world, mob, attack.impactSound());
        }
    }

    private void withAttackingHand(boolean offHand, Runnable action) {
        if (!offHand) {
            action.run();
            return;
        }
        ItemStack mainStack = mob.getMainHandStack();
        ItemStack offStack = mob.getOffHandStack();
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> mainModifiers = mainHandModifiers(mainStack);
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> offModifiers = mainHandModifiers(offStack);
        mob.getAttributes().removeModifiers(mainModifiers);
        mob.getAttributes().addTemporaryModifiers(offModifiers);
        mob.equipStack(EquipmentSlot.MAINHAND, offStack);
        try {
            action.run();
        } finally {
            mob.equipStack(EquipmentSlot.MAINHAND, mainStack);
            mob.getAttributes().removeModifiers(offModifiers);
            mob.getAttributes().addTemporaryModifiers(mainModifiers);
        }
    }

    private static Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> mainHandModifiers(ItemStack stack) {
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers = HashMultimap.create();
        stack.applyAttributeModifiers(EquipmentSlot.MAINHAND, modifiers::put);
        return modifiers;
    }

    private static float knockbackMultiplier(float length) {
        ServerConfig config = BetterCombatMod.config;
        if (!config.knockback_reduced_for_fast_attacks || !(config.knockback_reduction_threshold > 0)) {
            return 1F;
        }
        float multiplier = MathHelper.clamp(length / config.knockback_reduction_threshold, 0.1F, 1F);
        return switch (config.knockback_reduction_curve) {
            case SQUARE -> multiplier * multiplier;
            case HALF_SQUARE -> (multiplier * multiplier + multiplier) * 0.5F;
            default -> multiplier;
        };
    }

    private void spawnTrails(ServerWorld world, AttackHand hand, float yaw, float pitch) {
        List<ParticlePlacement> placements = trailPlacements(hand.attack());
        if (placements.isEmpty()) {
            return;
        }
        TrailAppearance appearance = trailAppearance(hand.attributes(), hand.itemStack());
        if (appearance == null) {
            return;
        }
        boolean mirror = (mob.getMainArm() == Arm.LEFT) != hand.isOffHand();
        float roll180 = mirror ? 180.0F : 0.0F;
        float flip = mirror ? -1.0F : 1.0F;
        float weaponRange = (float) PlayerAttackHelper.combineAttackRange(hand.attributes(), baseRange) + 0.25F;
        Vec3d right = Vec3d.fromPolar(0.0F, yaw + 90.0F).normalize();
        Vec3d forward = Vec3d.fromPolar(pitch, yaw).normalize();
        for (ParticlePlacement placement : placements) {
            List<TrailParticles.Entry> trails = TrailParticles.ENTRIES.get(placement.particle_type());
            if (trails == null) {
                continue;
            }
            Vec3d position = new Vec3d(mob.getX(), mob.getEyeY() - 0.25 + placement.y_addition(), mob.getZ())
                    .add(forward.multiply(placement.z_addition()))
                    .add(right.multiply(placement.x_addition() * flip));
            Vec3d stabPosition = position.add(forward.multiply(weaponRange - 1.5));
            for (TrailParticles.Entry trail : trails) {
                Vec3d at = trail.stabPosition() ? stabPosition : position;
                float roll = (placement.roll_set() + trail.rollOffset() + roll180) * flip;
                float particlePitch = pitch + placement.pitch_addition();
                float localYaw = placement.local_yaw() * flip;
                for (TrailParticles.LayeredParticle layered : trail.particles()) {
                    if (appearance.primary != null) {
                        world.spawnParticles(new SlashParticleEffect(layered.bottom(), weaponRange, particlePitch, yaw, localYaw, roll,
                                appearance.primary.glows(), appearance.primary.color_rgba()), at.x, at.y, at.z, 0, 0.0, 0.0, 0.0, 0.0);
                    }
                    if (appearance.secondary != null) {
                        world.spawnParticles(new SlashParticleEffect(layered.top(), weaponRange, particlePitch, yaw, localYaw, roll,
                                appearance.secondary.glows(), appearance.secondary.color_rgba()), at.x, at.y, at.z, 0, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }
        }
    }

    private static List<ParticlePlacement> trailPlacements(WeaponAttributes.Attack attack) {
        List<ParticlePlacement> own = attack.trailParticles();
        if (own != null && !own.isEmpty()) {
            return own;
        }
        TrailConfig config = BetterCombatMod.trailConfig.value;
        if (config != null && config.animation_based != null && attack.animation() != null) {
            List<ParticlePlacement> byAnimation = config.animation_based.get(attack.animation());
            if (byAnimation != null) {
                return byAnimation;
            }
        }
        return List.of();
    }

    @Nullable
    private static TrailAppearance trailAppearance(WeaponAttributes attributes, ItemStack stack) {
        TrailConfig config = BetterCombatMod.trailConfig.value;
        if (config == null || config.trail_appearance == null) {
            return null;
        }
        if (attributes != null && attributes.trailAppearance() != null) {
            return config.trail_appearance.merge(attributes.trailAppearance()).resolve(stack);
        }
        return config.trail_appearance.resolve(stack);
    }

    private List<Entity> findVictims(Swing swing, Vec3d origin, float yaw, float pitch) {
        WeaponAttributes.Attack attack = swing.hand().attack();
        double range = swing.range();
        boolean wide = attack.angle() > 180;
        Vec3d size = WeaponHitBoxes.createHitbox(attack.hitbox(), range, wide);
        OrientedBoundingBox obb = new OrientedBoundingBox(origin, size, pitch, yaw);
        if (!wide) {
            obb = obb.offsetAlongAxisZ(size.z / 2.0);
        }
        obb.updateVertex();

        double searchRange = range * BetterCombatMod.config.target_search_range_multiplier + 1.0;
        List<Entity> candidates = mob.getWorld().getOtherEntities(mob, mob.getBoundingBox().expand(searchRange),
                entity -> !entity.isSpectator() && entity.canHit() && entity.isAttackable());
        List<Entity> victims = new ArrayList<>();
        double halfAngle = MathHelper.clamp(attack.angle(), 0.0, 360.0) / 2.0;
        for (Entity entity : candidates) {
            if (entity == mob.getVehicle() || mob.hasPassenger(entity) || !AllyHelper.canHurt(mob, entity)) {
                continue;
            }
            Vec3d center = entity.getPos().add(0.0, entity.getHeight() / 2.0F, 0.0);
            if (!obb.intersects(entity.getBoundingBox().expand(entity.getTargetingMargin())) && !obb.contains(center)) {
                continue;
            }
            Vec3d nearest = CollisionHelper.distanceVector(origin, entity.getBoundingBox());
            Vec3d toCenter = center.subtract(origin);
            if (nearest.length() > range) {
                continue;
            }
            if (halfAngle > 0 && CollisionHelper.angleBetween(toCenter, obb.axisZ) > halfAngle
                    && CollisionHelper.angleBetween(nearest, obb.axisZ) > halfAngle) {
                continue;
            }
            if (!BetterCombatMod.config.allow_attacking_thru_walls
                    && !clearPath(origin, origin.add(nearest)) && !clearPath(origin, origin.add(toCenter))) {
                continue;
            }
            victims.add(entity);
        }
        return victims;
    }

    private boolean clearPath(Vec3d from, Vec3d to) {
        HitResult hit = mob.getWorld().raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mob));
        return hit.getType() != HitResult.Type.BLOCK;
    }

    private Vec3d tracingOrigin() {
        return mob.getEyePos().subtract(0.0, mob.getHeight() * 0.15 * mob.getScale(), 0.0);
    }

    private float attackLength(boolean offHandWeapon, boolean dualWielding) {
        return Math.max(attackIntervalTicks(offHandWeapon, dualWielding), BetterCombatMod.config.attack_interval_cap) * intervalScale;
    }

    private float attackIntervalTicks(boolean offHandWeapon, boolean dualWielding) {
        RegistryEntry<EntityAttribute> attribute = EntityAttributes.GENERIC_ATTACK_SPEED;
        Map<Identifier, EntityAttributeModifier> modifiers = new HashMap<>();
        EntityAttributeInstance instance = mob.getAttributeInstance(attribute);
        double base;
        if (instance != null) {
            base = instance.getBaseValue();
            instance.getModifiers().forEach(modifier -> modifiers.put(modifier.id(), modifier));
        } else {
            base = attribute.value().getDefaultValue();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                mob.getEquippedStack(slot).applyAttributeModifiers(slot, (entry, modifier) -> {
                    if (entry.equals(attribute)) {
                        modifiers.put(modifier.id(), modifier);
                    }
                });
            }
            for (StatusEffectInstance effect : mob.getStatusEffects()) {
                effect.getEffectType().value().forEachAttributeModifier(effect.getAmplifier(), (entry, modifier) -> {
                    if (entry.equals(attribute)) {
                        modifiers.put(modifier.id(), modifier);
                    }
                });
            }
        }
        if (offHandWeapon) {
            mainHandModifiers(mob.getMainHandStack()).get(attribute).forEach(modifier -> modifiers.remove(modifier.id()));
            mainHandModifiers(mob.getOffHandStack()).get(attribute).forEach(modifier -> modifiers.put(modifier.id(), modifier));
        }
        if (dualWielding) {
            modifiers.put(DUAL_WIELD_SPEED_ID, new EntityAttributeModifier(DUAL_WIELD_SPEED_ID,
                    BetterCombatMod.config.dual_wielding_attack_speed_multiplier - 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        double value = attribute.value().clamp(attributeValue(base, modifiers.values()));
        return (float) (20.0 / Math.max(0.1, value));
    }

    private static double attributeValue(double base, Collection<EntityAttributeModifier> modifiers) {
        double added = base;
        for (EntityAttributeModifier modifier : modifiers) {
            if (modifier.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                added += modifier.value();
            }
        }
        double value = added;
        for (EntityAttributeModifier modifier : modifiers) {
            if (modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                value += added * modifier.value();
            }
        }
        for (EntityAttributeModifier modifier : modifiers) {
            if (modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                value *= 1.0 + modifier.value();
            }
        }
        return value;
    }

    private boolean conditionsMet(WeaponAttributes.Attack attack, boolean offHandAttack) {
        if (attack.conditions() == null) {
            return true;
        }
        ItemStack mainHandStack = mob.getMainHandStack();
        ItemStack offHandStack = mob.getOffHandStack();
        WeaponAttributes mainHand = WeaponRegistry.getAttributes(mainHandStack);
        WeaponAttributes offHand = WeaponRegistry.getAttributes(offHandStack);
        boolean dualWielding = PlayerAttackHelper.isDualWielding(mainHand, offHand);
        for (WeaponAttributes.Condition condition : attack.conditions()) {
            if (condition == null) {
                continue;
            }
            boolean met = switch (condition) {
                case NOT_DUAL_WIELDING -> !dualWielding;
                case DUAL_WIELDING_ANY -> dualWielding;
                case DUAL_WIELDING_SAME -> dualWielding && mainHandStack.getItem() == offHandStack.getItem();
                case DUAL_WIELDING_SAME_CATEGORY -> dualWielding && mainHand.category() != null && !mainHand.category().isEmpty()
                        && mainHand.category().equals(offHand.category());
                case NO_OFFHAND_ITEM -> offHandStack.isEmpty();
                case OFF_HAND_SHIELD -> offHandStack.getItem() instanceof ShieldItem;
                case MAIN_HAND_ONLY -> !offHandAttack;
                case OFF_HAND_ONLY -> offHandAttack;
                case MOUNTED -> mob.hasVehicle();
                case NOT_MOUNTED -> !mob.hasVehicle();
            };
            if (!met) {
                return false;
            }
        }
        return true;
    }

    private void updatePose() {
        if (!animations) {
            return;
        }
        WeaponAttributes mainHand = WeaponRegistry.getAttributes(mob.getMainHandStack());
        WeaponAttributes offHand = WeaponRegistry.getAttributes(mob.getOffHandStack());
        boolean twoHanded = mainHand != null && mainHand.isTwoHanded();
        setPose(MobAnimationLayer.POSE, mainHand != null ? mainHand.pose() : null, twoHanded);
        setPose(MobAnimationLayer.OFF_HAND_POSE,
                offHand != null && PlayerAttackHelper.isDualWielding(mainHand, offHand) ? offHand.pose() : null, twoHanded);
    }

    public void clearPose() {
        setPose(MobAnimationLayer.POSE, null, false);
        setPose(MobAnimationLayer.OFF_HAND_POSE, null, false);
    }

    private void setPose(MobAnimationLayer layer, @Nullable String pose, boolean twoHanded) {
        Identifier id = pose == null || pose.isEmpty() ? null : Identifier.tryParse(pose);
        String key = id != null ? id + (twoHanded ? "|two_handed" : "") : null;
        if (Objects.equals(key, activePoses.get(layer))) {
            return;
        }
        if (key == null) {
            activePoses.remove(layer);
        } else {
            activePoses.put(layer, key);
        }
        MobAnimations.playWeaponPose(mob, layer, id, twoHanded);
    }

    static MeleeAttackGoal create(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, BetterCombatCompat.MeleeSettings settings) {
        return new BCMobMeleeAttackGoal(mob, speed, pauseWhenMobIdle)
                .withBaseRange(settings.baseRange())
                .withIntervalScale(settings.intervalScale())
                .withAnimations(settings.animations())
                .withResetInvulnerability(settings.resetInvulnerability())
                .withWeaponTrails(settings.weaponTrails());
    }
}
