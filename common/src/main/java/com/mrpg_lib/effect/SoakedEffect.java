package com.mrpg_lib.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;


public class SoakedEffect extends StatusEffect {
    public SoakedEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public static boolean handleUpdate(LivingEntity pLivingEntity, ParticleEffect mist) {
        World world = pLivingEntity.getEntityWorld();

        if(pLivingEntity.isOnFire()){
            spawnSteam(world, mist);
            pLivingEntity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH,2,1);
            pLivingEntity.extinguish();
            pLivingEntity.removeStatusEffect(MRPGCEffects.SOAKED.entry);
        }
        if(pLivingEntity.isInLava()){
            spawnSteam(world, mist);
            pLivingEntity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH,2,1);
            pLivingEntity.removeStatusEffect(MRPGCEffects.SOAKED.entry);
        }
        return true;
    }

    private static void spawnSteam(World world, ParticleEffect mist) {
        if(world.isClient){
            world.addParticle(mist,1,1,1,2,2,2);
            world.addParticle(ParticleTypes.LARGE_SMOKE,1,1,1,2,2,2);
        }else{
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(mist,1,1,1,4,2,2,2,2);
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,1,1,1,4,2,2,2,2);
            }
        }
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity pLivingEntity, int pAmplifier) {
        return handleUpdate(pLivingEntity, ParticleTypes.CLOUD);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
