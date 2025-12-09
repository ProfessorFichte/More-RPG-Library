package net.more_rpg_classes.entity;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.util.CustomMethods;
import net.spell_engine.internals.target.EntityRelation;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_power.api.SpellSchools;

import java.util.List;

public class FriendlyLightningEntity extends Entity {
    private int ambientTick;
    private int remainingActions = 2;
    private LivingEntity owner;
    public long seed;

    public FriendlyLightningEntity(EntityType<? extends FriendlyLightningEntity> entityType, World world) {
        super(entityType, world);
        this.owner = null;
        this.ignoreCameraFrustum = true;
        this.ambientTick = 2;
        this.seed = this.random.nextLong();
    }

    public FriendlyLightningEntity(EntityType<? extends FriendlyLightningEntity> entityType, World world, double x, double y, double z, LivingEntity owner) {
        super(entityType, world);
        this.owner = owner;
        this.ignoreCameraFrustum = true;
        this.ambientTick = 2;
        this.seed = this.random.nextLong();
        this.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.WEATHER;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ambientTick == 2) {
            if (this.getWorld().isClient) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER,
                    10000.0F, 0.8F + this.random.nextFloat() * 0.2F, false);
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER,
                    2.0F, 0.5F + this.random.nextFloat() * 0.2F, false);
            } else {
                Difficulty difficulty = this.getWorld().getDifficulty();
                if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
                    this.spawnFire();
                }
                this.damageEntities();
            }
        }

        --this.ambientTick;

        if (this.ambientTick < 0) {
            if (this.remainingActions == 0) {
                this.discard();
            } else if (this.ambientTick < -this.random.nextInt(10)) {
                --this.remainingActions;
                this.ambientTick = 1;

                if (!this.getWorld().isClient) {
                    this.damageEntities();
                }
            }
        }

        if (this.ambientTick >= 0 && !this.getWorld().isClient) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.setLightningTicksLeft(2);
            }
        }
    }

    private void damageEntities() {
        if (this.getWorld().isClient || !(this.getWorld() instanceof ServerWorld)) {
            return;
        }


        float damageAmount = 4.0F + (float) CustomMethods.getHighestDamageAttribute(owner);
        double radius = 3.0;
        List<Entity> entities = this.getWorld().getOtherEntities(this,
            new Box(this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                    this.getX() + radius, this.getY() + 6.0 + radius, this.getZ() + radius));

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            if (this.owner != null && entity.equals(this.owner)) {
                continue;
            }

            if (this.owner != null && isFriendly(this.owner, livingEntity)) {
                continue;
            }

            entity.damage(this.getDamageSources().lightningBolt(), damageAmount);
        }
    }

    private boolean isFriendly(LivingEntity owner, LivingEntity target) {
        if (owner == null || target == null) {
            return false;
        }

        try {
            var relation = EntityRelations.getRelation(owner, target);
            return relation == EntityRelation.ALLY;
        } catch (Exception e) {
            return !EntityRelations.allowedToHurt(owner, target);
        }
    }


    private double getHighestDamage(double meleeDamage, double rangedDamage, double spellPower) {
        return Math.max(meleeDamage, Math.max(rangedDamage, spellPower));
    }

    private void spawnFire() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public boolean shouldRender(double distance) {
        double renderDistance = 64.0 * getRenderDistanceMultiplier();
        return distance < renderDistance * renderDistance;
    }

    public static FriendlyLightningEntity spawn(ServerWorld world, Vec3d pos, LivingEntity owner, EntityType<FriendlyLightningEntity> entityType) {
        FriendlyLightningEntity lightning = new FriendlyLightningEntity(
            entityType, world, pos.x, pos.y, pos.z, owner
        );
        world.spawnEntity(lightning);
        return lightning;
    }

    public static FriendlyLightningEntity spawnAtBlock(ServerWorld world, BlockPos pos, LivingEntity owner, EntityType<FriendlyLightningEntity> entityType) {
        return spawn(world, Vec3d.ofBottomCenter(pos), owner, entityType);
    }
}
