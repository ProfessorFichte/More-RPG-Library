package com.mrpg_lib.entity;

import com.mrpg_lib.util.AllyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomCloudEntity extends Entity implements Ownable {
    private static final TrackedData<Float> RADIUS = DataTracker.registerData(CustomCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> WAITING = DataTracker.registerData(CustomCloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> COLOR = DataTracker.registerData(CustomCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PARTICLE_ID = DataTracker.registerData(CustomCloudEntity.class, TrackedDataHandlerRegistry.STRING);

    private final Map<Entity, Integer> affectedEntities = new HashMap<>();
    private ParticleEffect particleType = ParticleTypes.CLOUD;

    private int duration;
    private int waitTime;
    private int reapplicationDelay;
    private float radiusGrowth;

    private RegistryEntry<StatusEffect> statusEffect;
    private int effectDuration;
    private int effectAmplifier;
    private int maxAmplifier;
    private boolean canStackAmplifier;

    private boolean canDealDamage;
    private float damageAmount;
    private DamageSource damageSource;

    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUuid;

    public CustomCloudEntity(EntityType<? extends CustomCloudEntity> entityType, World world) {
        super(entityType, world);
        this.duration = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
        this.noClip = true;
        this.maxAmplifier = 4;
        this.canStackAmplifier = false;
        this.canDealDamage = false;
        this.damageAmount = 0.0F;
    }

    public CustomCloudEntity(World world, double x, double y, double z) {
        this(MRPGCEntities.CUSTOM_CLOUD, world);
        this.setPosition(x, y, z);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(RADIUS, 3.0F);
        builder.add(WAITING, false);
        builder.add(COLOR, 0);
        builder.add(PARTICLE_ID, "minecraft:cloud");
    }

    public void setRadius(float radius) {
        if (!this.getWorld().isClient) {
            this.dataTracker.set(RADIUS, MathHelper.clamp(radius, 0.0F, 32.0F));
        }
    }

    public float getRadius() {
        return this.dataTracker.get(RADIUS);
    }

    public void setParticleType(ParticleEffect particle) {
        this.particleType = particle;
        if (!this.getWorld().isClient) {
            Identifier typeId = Registries.PARTICLE_TYPE.getId(particle.getType());
            if (typeId != null) {
                this.dataTracker.set(PARTICLE_ID, typeId.toString());
            }
        }
    }

    public ParticleEffect getParticleType() {
        return this.particleType;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (data.equals(PARTICLE_ID)) {
            Identifier id = Identifier.tryParse(this.dataTracker.get(PARTICLE_ID));
            if (id != null) {
                ParticleType<?> type = Registries.PARTICLE_TYPE.get(id);
                if (type instanceof SimpleParticleType dpt) {
                    this.particleType = dpt;
                }
            }
        }
    }

    public void setColor(int color) {
        this.dataTracker.set(COLOR, color);
    }

    public int getColor() {
        return this.dataTracker.get(COLOR);
    }

    protected void setWaiting(boolean waiting) {
        this.dataTracker.set(WAITING, waiting);
    }

    public boolean isWaiting() {
        return this.dataTracker.get(WAITING);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setRadiusGrowth(float radiusGrowth) {
        this.radiusGrowth = radiusGrowth;
    }

    public void setReapplicationDelay(int delay) {
        this.reapplicationDelay = delay;
    }

    public void setStatusEffect(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        this.statusEffect = effect;
        this.effectDuration = duration;
        this.effectAmplifier = amplifier;
    }

    public void setAmplifierStacking(boolean canStack, int maxAmplifier) {
        this.canStackAmplifier = canStack;
        this.maxAmplifier = maxAmplifier;
    }

    public void setDamageProperties(boolean canDamage, float amount, @Nullable DamageSource source) {
        this.canDealDamage = canDamage;
        this.damageAmount = amount;
        this.damageSource = source;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUuid = owner == null ? null : owner.getUuid();
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUuid != null && this.getWorld() instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(this.ownerUuid);
            if (entity instanceof LivingEntity living) {
                this.owner = living;
            }
        }
        return this.owner;
    }

    private boolean shouldAffectEntity(LivingEntity entity) {
        return AllyHelper.canHurt(this.getOwner(), entity);
    }

    @Override
    public void tick() {
        super.tick();

        boolean isWaiting = this.isWaiting();
        float radius = this.getRadius();

        if (this.getWorld().isClient) {
            if (isWaiting && this.random.nextBoolean()) {
                return;
            }

            int particleCount = isWaiting ? 2 : MathHelper.ceil(3.1415927F * radius * radius);
            float spread = isWaiting ? 0.2F : radius;

            for (int i = 0; i < particleCount; i++) {
                float angle = this.random.nextFloat() * 6.2831855F;
                float distance = MathHelper.sqrt(this.random.nextFloat()) * spread;
                double x = this.getX() + (MathHelper.cos(angle) * distance);
                double y = this.getY();
                double z = this.getZ() + (MathHelper.sin(angle) * distance);

                if (isWaiting) {
                    this.getWorld().addImportantParticle(this.particleType, x, y, z, 0.0, 0.0, 0.0);
                } else {
                    this.getWorld().addImportantParticle(this.particleType, x, y, z,
                        (0.5 - this.random.nextDouble()) * 0.15, 0.01, (0.5 - this.random.nextDouble()) * 0.15);
                }
            }
            return;
        }

        if (this.age >= this.waitTime + this.duration) {
            this.discard();
            return;
        }

        boolean shouldWait = this.age < this.waitTime;
        if (isWaiting != shouldWait) {
            this.setWaiting(shouldWait);
        }

        if (shouldWait) {
            return;
        }

        if (this.radiusGrowth != 0.0F) {
            radius += this.radiusGrowth;
            if (radius < 0.5F) {
                this.discard();
                return;
            }
            this.setRadius(radius);
        }

        if (this.age % 5 == 0) {
            this.affectedEntities.entrySet().removeIf(entry -> this.age >= entry.getValue());

            List<LivingEntity> nearbyEntities = this.getWorld().getNonSpectatingEntities(
                LivingEntity.class, this.getBoundingBox()
            );

            for (LivingEntity entity : nearbyEntities) {
                if (this.affectedEntities.containsKey(entity)) {
                    continue;
                }

                if (!this.shouldAffectEntity(entity)) {
                    continue;
                }

                double dx = entity.getX() - this.getX();
                double dz = entity.getZ() - this.getZ();
                double distanceSq = dx * dx + dz * dz;

                if (distanceSq > radius * radius) {
                    continue;
                }

                this.affectedEntities.put(entity, this.age + this.reapplicationDelay);

                if (this.statusEffect != null) {
                    this.applyStatusEffect(entity);
                }

                if (this.canDealDamage && this.damageAmount > 0.0F) {
                    DamageSource source = this.damageSource != null ?
                        this.damageSource : this.getWorld().getDamageSources().magic();
                    entity.damage(source, this.damageAmount);
                }
            }
        }
    }

    private void applyStatusEffect(LivingEntity entity) {
        if (this.statusEffect == null) {
            return;
        }

        StatusEffectInstance existingEffect = entity.getStatusEffect(this.statusEffect);

        if (existingEffect != null && this.canStackAmplifier) {
            int currentAmplifier = existingEffect.getAmplifier();
            int newAmplifier = Math.min(currentAmplifier + 1, this.maxAmplifier);
            int duration = Math.max(existingEffect.getDuration(), this.effectDuration);

            entity.addStatusEffect(new StatusEffectInstance(
                this.statusEffect, duration, newAmplifier, false, false, true
            ), this);
        } else {
            entity.addStatusEffect(new StatusEffectInstance(
                this.statusEffect, this.effectDuration, this.effectAmplifier, false, false, true
            ), this);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.age = nbt.getInt("Age");
        this.duration = nbt.getInt("Duration");
        this.waitTime = nbt.getInt("WaitTime");
        this.reapplicationDelay = nbt.getInt("ReapplicationDelay");
        this.radiusGrowth = nbt.getFloat("RadiusPerTick");
        this.setRadius(nbt.getFloat("Radius"));

        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }

        this.effectDuration = nbt.getInt("EffectDuration");
        this.effectAmplifier = nbt.getInt("EffectAmplifier");
        this.maxAmplifier = nbt.getInt("MaxAmplifier");
        this.canStackAmplifier = nbt.getBoolean("CanStackAmplifier");
        this.canDealDamage = nbt.getBoolean("CanDealDamage");
        this.damageAmount = nbt.getFloat("DamageAmount");

        if (nbt.contains("ParticleType")) {
            Identifier id = Identifier.tryParse(nbt.getString("ParticleType"));
            if (id != null) {
                ParticleType<?> type = Registries.PARTICLE_TYPE.get(id);
                if (type instanceof SimpleParticleType dpt) {
                    this.particleType = dpt;
                    this.dataTracker.set(PARTICLE_ID, id.toString());
                }
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Age", this.age);
        nbt.putInt("Duration", this.duration);
        nbt.putInt("WaitTime", this.waitTime);
        nbt.putInt("ReapplicationDelay", this.reapplicationDelay);
        nbt.putFloat("RadiusPerTick", this.radiusGrowth);
        nbt.putFloat("Radius", this.getRadius());

        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }

        nbt.putInt("EffectDuration", this.effectDuration);
        nbt.putInt("EffectAmplifier", this.effectAmplifier);
        nbt.putInt("MaxAmplifier", this.maxAmplifier);
        nbt.putBoolean("CanStackAmplifier", this.canStackAmplifier);
        nbt.putBoolean("CanDealDamage", this.canDealDamage);
        nbt.putFloat("DamageAmount", this.damageAmount);

        Identifier typeId = Registries.PARTICLE_TYPE.getId(this.particleType.getType());
        if (typeId != null) {
            nbt.putString("ParticleType", typeId.toString());
        }
    }
}
