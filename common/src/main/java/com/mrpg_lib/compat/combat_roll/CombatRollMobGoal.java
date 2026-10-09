package com.mrpg_lib.compat.combat_roll;

import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import com.mrpg_lib.compat.player_animator.api.MobAnimationOptions;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import com.mrpg_lib.util.AllyHelper;
import net.combat_roll.CombatRollMod;
import net.combat_roll.api.CombatRoll;
import net.combat_roll.api.RollInvulnerable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CombatRollMobGoal extends Goal {
    private static final Identifier ROLL_ANIMATION = Identifier.of("combat_roll", "roll");
    private static final Identifier ROLL_SOUND = Identifier.of("combat_roll", "roll");
    private static final double ROLL_VELOCITY_FACTOR = 0.475;
    private static final float GROUND_SLIPPERINESS = Blocks.GRASS_BLOCK.getSlipperiness();
    private static final double PATH_STEP = 0.5;
    private static final int MAX_SAFE_DROP = 3;
    private static final double DEFENSIVE_TRIGGER_DISTANCE = 6.0;
    private static final double FLEEING_MIN_SPEED_SQUARED = 0.0025;
    private static final double FLEEING_ENGAGE_MIN_DISTANCE = 3.0;
    private static final double PROJECTILE_SCAN_RADIUS = 10.0;
    private static final double PROJECTILE_LOOKAHEAD_TICKS = 15.0;
    private static final double MIN_PROJECTILE_SPEED_SQUARED = 0.04;
    private static final Set<PathNodeType> DANGEROUS_NODES = EnumSet.of(
            PathNodeType.LAVA, PathNodeType.DAMAGE_FIRE, PathNodeType.DANGER_FIRE,
            PathNodeType.DAMAGE_OTHER, PathNodeType.DANGER_OTHER, PathNodeType.POWDER_SNOW,
            PathNodeType.DANGER_POWDER_SNOW, PathNodeType.DAMAGE_CAUTIOUS);

    private final PathAwareEntity mob;
    private final CombatRollCompat.RollSettings settings;

    private int charges = -1;
    private float rechargeProgress;
    private int lastUpdateAge;
    private int lastRollAge = Integer.MIN_VALUE / 2;
    private int rollTicksLeft;
    private float rollYaw;
    @Nullable private Vec3d pendingDirection;

    public CombatRollMobGoal(PathAwareEntity mob, CombatRollCompat.RollSettings settings) {
        this.mob = mob;
        this.settings = settings;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    static Goal create(PathAwareEntity mob, CombatRollCompat.RollSettings settings) {
        return new CombatRollMobGoal(mob, settings);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean canStart() {
        if (!(mob.getWorld() instanceof ServerWorld)) {
            return false;
        }
        updateCharges();
        if (charges <= 0 || mob.age - lastRollAge < settings.cooldownTicks()) {
            return false;
        }
        if (mob.hasVehicle() || mob.isUsingItem() || (!mob.isOnGround() && !CombatRollMod.config.allow_rolling_while_airborn)) {
            return false;
        }
        pendingDirection = chooseDirection();
        return pendingDirection != null;
    }

    @Override
    public boolean shouldContinue() {
        return rollTicksLeft > 0 && mob.isAlive();
    }

    @Override
    public void start() {
        Vec3d direction = pendingDirection;
        pendingDirection = null;
        if (direction == null) {
            return;
        }
        mob.getNavigation().stop();
        rollYaw = (float) (MathHelper.atan2(direction.z, direction.x) * MathHelper.DEGREES_PER_RADIAN) - 90.0F;
        faceRollDirection();

        Vec3d velocity = direction.multiply(ROLL_VELOCITY_FACTOR * rollDistance());
        if (mob.isTouchingWater()) {
            double fluidHeight = Math.min(mob.getFluidHeight(FluidTags.WATER), 1.0);
            velocity = velocity.multiply(Math.max(1.0 - fluidHeight * 3.0, 0.3));
        }
        if (mob.isInLava()) {
            velocity = velocity.multiply(0.3);
        }
        float slipperiness = mob.getWorld().getBlockState(mob.getBlockPos().down()).getBlock().getSlipperiness();
        if (slipperiness > GROUND_SLIPPERINESS) {
            float ratio = GROUND_SLIPPERINESS / slipperiness;
            velocity = velocity.multiply(ratio * ratio);
        }
        mob.addVelocity(velocity.x, 0.0, velocity.z);
        mob.velocityModified = true;

        int invulnerableTicks = settings.invulnerableTicks() >= 0 ? settings.invulnerableTicks() : CombatRollMod.config.invulnerable_ticks_upon_roll;
        if (invulnerableTicks > 0) {
            ((RollInvulnerable) mob).setRollInvulnerableTicks(invulnerableTicks);
        }

        int duration = Math.max(1, CombatRollMod.config.roll_duration);
        if (settings.animations()) {
            MobAnimations.play(mob, ROLL_ANIMATION, MobAnimationOptions.DEFAULT
                    .withLayer(MobAnimationLayer.DODGE)
                    .withDuration(duration)
                    .withMirror(MobAnimationOptions.Mirror.NEVER));
        }
        playEffects();

        charges--;
        lastRollAge = mob.age;
        rollTicksLeft = duration;
    }

    @Override
    public void tick() {
        rollTicksLeft--;
        mob.getNavigation().stop();
        faceRollDirection();
    }

    @Override
    public void stop() {
        rollTicksLeft = 0;
        pendingDirection = null;
    }

    private void faceRollDirection() {
        mob.setYaw(rollYaw);
        mob.setBodyYaw(rollYaw);
        mob.setHeadYaw(rollYaw);
    }

    private void playEffects() {
        if (!(mob.getWorld() instanceof ServerWorld world)) {
            return;
        }
        Registries.SOUND_EVENT.getEntry(ROLL_SOUND).ifPresent(sound ->
                world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), sound, mob.getSoundCategory(), 1.0F, 1.0F));
        world.spawnParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 0.1, mob.getZ(), 6,
                mob.getWidth() * 0.5, 0.05, mob.getWidth() * 0.5, 0.02);
    }

    @Nullable
    private Vec3d chooseDirection() {
        if (settings.dodgeProjectiles()) {
            Vec3d threat = incomingProjectileVelocity();
            if (threat != null) {
                Vec3d side = new Vec3d(-threat.z, 0.0, threat.x).normalize();
                if (mob.getRandom().nextBoolean()) {
                    side = side.negate();
                }
                Vec3d dodge = firstSafe(List.of(side, side.negate(), rotate(side, 35), rotate(side.negate(), -35),
                        rotate(side, -35), rotate(side.negate(), 35)));
                if (dodge != null) {
                    return dodge;
                }
            }
        }
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return null;
        }
        Vec3d toTarget = horizontal(target.getPos().subtract(mob.getPos()));
        if (toTarget == null) {
            return null;
        }
        double distance = mob.distanceTo(target);
        boolean fleeing = isMovingAway(target, toTarget);
        if (settings.engage() && fleeing && distance >= FLEEING_ENGAGE_MIN_DISTANCE && distance - rollDistance() >= 1.5
                && mob.getVisibilityCache().canSee(target)) {
            Vec3d chase = firstSafe(List.of(toTarget, rotate(toTarget, 20), rotate(toTarget, -20)));
            if (chase != null) {
                return chase;
            }
        }
        if (mob.getHealth() < mob.getMaxHealth() * settings.defensiveHealthThreshold() && distance <= DEFENSIVE_TRIGGER_DISTANCE
                && !fleeing) {
            Vec3d away = toTarget.negate();
            Vec3d retreat = firstSafe(List.of(away, rotate(away, 35), rotate(away, -35), rotate(away, 70), rotate(away, -70),
                    rotate(away, 100), rotate(away, -100)));
            if (retreat != null) {
                return retreat;
            }
        }
        if (settings.engage() && distance >= settings.engageMinDistance() && distance - rollDistance() >= 1.5
                && distance <= settings.engageMinDistance() + rollDistance() * 2.0
                && mob.getVisibilityCache().canSee(target)
                && mob.getRandom().nextFloat() < settings.engageChance()) {
            return firstSafe(List.of(toTarget, rotate(toTarget, 20), rotate(toTarget, -20)));
        }
        return null;
    }

    private static boolean isMovingAway(LivingEntity target, Vec3d toTarget) {
        Vec3d movement = new Vec3d(target.getX() - target.prevX, 0.0, target.getZ() - target.prevZ);
        return movement.lengthSquared() > FLEEING_MIN_SPEED_SQUARED && movement.dotProduct(toTarget) > 0.0;
    }

    @Nullable
    private Vec3d incomingProjectileVelocity() {
        Vec3d center = mob.getBoundingBox().getCenter();
        double hitRadius = mob.getWidth() * 0.5 + 0.6;
        List<ProjectileEntity> projectiles = mob.getWorld().getEntitiesByClass(ProjectileEntity.class,
                mob.getBoundingBox().expand(PROJECTILE_SCAN_RADIUS), projectile -> isHostileProjectile(projectile));
        for (ProjectileEntity projectile : projectiles) {
            Vec3d velocity = projectile.getVelocity();
            double speedSquared = velocity.lengthSquared();
            if (speedSquared < MIN_PROJECTILE_SPEED_SQUARED) {
                continue;
            }
            Vec3d relative = center.subtract(projectile.getPos());
            double ticksToClosest = relative.dotProduct(velocity) / speedSquared;
            if (ticksToClosest < 0 || ticksToClosest > PROJECTILE_LOOKAHEAD_TICKS) {
                continue;
            }
            double missDistance = relative.subtract(velocity.multiply(ticksToClosest)).length();
            if (missDistance <= hitRadius + projectile.getWidth() * 0.5) {
                Vec3d flat = horizontal(velocity);
                return flat != null ? flat : horizontal(relative);
            }
        }
        return null;
    }

    private boolean isHostileProjectile(ProjectileEntity projectile) {
        Entity owner = projectile.getOwner();
        return owner != mob && (owner == null || AllyHelper.canHurt(owner, mob));
    }

    @Nullable
    private Vec3d firstSafe(List<Vec3d> candidates) {
        double distance = rollDistance();
        for (Vec3d candidate : candidates) {
            if (candidate != null && isPathSafe(candidate, distance)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isPathSafe(Vec3d direction, double distance) {
        World world = mob.getWorld();
        Box box = mob.getBoundingBox();
        double stepHeight = mob.getStepHeight();
        for (double travelled = PATH_STEP; travelled <= distance + PATH_STEP + 0.01; travelled += PATH_STEP) {
            double dx = direction.x * travelled;
            double dz = direction.z * travelled;
            Box moved = box.offset(dx, 0.0, dz);
            if (!world.isSpaceEmpty(mob, moved)) {
                moved = box.offset(dx, stepHeight, dz);
                if (stepHeight <= 0 || !world.isSpaceEmpty(mob, moved)) {
                    return false;
                }
            }
            if (!isFootprintSafe(world, moved)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFootprintSafe(World world, Box box) {
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.floor(box.maxX);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.floor(box.maxZ);
        int feetY = MathHelper.floor(box.minY);
        int headY = MathHelper.floor(box.maxY);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = feetY - 1; y <= headY; y++) {
                    pos.set(x, y, z);
                    if (isDangerous(world, pos)) {
                        return false;
                    }
                }
                pos.set(x, feetY, z);
                if (world.getFluidState(pos).isIn(FluidTags.WATER) && world.getFluidState(pos.down()).isIn(FluidTags.WATER)) {
                    return false;
                }
                if (dropBelow(world, x, feetY, z) > MAX_SAFE_DROP) {
                    return false;
                }
            }
        }
        BlockPos center = BlockPos.ofFloored(box.getCenter().x, box.minY, box.getCenter().z);
        return !DANGEROUS_NODES.contains(LandPathNodeMaker.getLandNodeType(mob, center));
    }

    private static boolean isDangerous(World world, BlockPos pos) {
        if (world.getFluidState(pos).isIn(FluidTags.LAVA)) {
            return true;
        }
        BlockState state = world.getBlockState(pos);
        return state.isIn(BlockTags.FIRE)
                || CampfireBlock.isLitCampfire(state)
                || state.isOf(Blocks.CACTUS)
                || state.isOf(Blocks.MAGMA_BLOCK)
                || state.isOf(Blocks.SWEET_BERRY_BUSH)
                || state.isOf(Blocks.WITHER_ROSE)
                || state.isOf(Blocks.POWDER_SNOW)
                || state.isOf(Blocks.POINTED_DRIPSTONE);
    }

    private static int dropBelow(World world, int x, int feetY, int z) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int depth = 0; depth <= MAX_SAFE_DROP; depth++) {
            pos.set(x, feetY - 1 - depth, z);
            if (world.getFluidState(pos).isIn(FluidTags.LAVA)) {
                return MAX_SAFE_DROP + 1;
            }
            if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
                return world.getFluidState(pos.down()).isIn(FluidTags.WATER) ? MAX_SAFE_DROP + 1 : depth;
            }
            if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) {
                return depth;
            }
        }
        return MAX_SAFE_DROP + 1;
    }

    private void updateCharges() {
        int maxCharges = Math.max(0, (int) attribute(CombatRoll.Attributes.COUNT));
        if (charges < 0) {
            charges = maxCharges;
            lastUpdateAge = mob.age;
            return;
        }
        int elapsed = Math.max(0, mob.age - lastUpdateAge);
        lastUpdateAge = mob.age;
        if (charges >= maxCharges) {
            charges = maxCharges;
            rechargeProgress = 0;
            return;
        }
        int cooldownLength = Math.max(1, Math.round(CombatRollMod.config.roll_cooldown * 20.0F));
        rechargeProgress += elapsed * (float) (attribute(CombatRoll.Attributes.RECHARGE) / 20.0);
        while (rechargeProgress >= cooldownLength && charges < maxCharges) {
            rechargeProgress -= cooldownLength;
            charges++;
        }
    }

    private double rollDistance() {
        return attribute(CombatRoll.Attributes.DISTANCE) + CombatRollMod.config.additional_roll_distance;
    }

    private double attribute(CombatRoll.Attributes.Entry entry) {
        var registryEntry = entry.entry;
        if (registryEntry != null && mob.getAttributes().hasAttribute(registryEntry)) {
            return mob.getAttributeValue(registryEntry);
        }
        return entry.baseValue;
    }

    @Nullable
    private static Vec3d horizontal(Vec3d vector) {
        Vec3d flat = new Vec3d(vector.x, 0.0, vector.z);
        return flat.lengthSquared() < 1.0E-6 ? null : flat.normalize();
    }

    private static Vec3d rotate(Vec3d direction, float degrees) {
        return direction.rotateY(degrees * MathHelper.RADIANS_PER_DEGREE);
    }
}
