package net.more_rpg_classes.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect;


public class SoakedEffect extends SpellVulnerabilityStatusEffect {
    public SoakedEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public boolean applyUpdateEffect(LivingEntity pLivingEntity, int pAmplifier) {
        World world = pLivingEntity.getEntityWorld();

        if(pLivingEntity.isOnFire()){
            if(world.isClient){
                world.addParticle(MoreParticles.WATER_MIST.type(),1,1,1,2,2,2);
                world.addParticle(ParticleTypes.LARGE_SMOKE,1,1,1,2,2,2);
            }else{
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(MoreParticles.WATER_MIST.type(),1,1,1,4,2,2,2,2);
                    serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,1,1,1,4,2,2,2,2);
                }
            }
            pLivingEntity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH,2,1);
            pLivingEntity.extinguish();
            pLivingEntity.removeStatusEffect(MRPGCEffects.SOAKED.entry);
        }
        if(pLivingEntity.isInLava()){
            if(world.isClient){
                pLivingEntity.getEntityWorld().addParticle(MoreParticles.WATER_MIST.type(),1,1,1,2,2,2);
                pLivingEntity.getEntityWorld().addParticle(ParticleTypes.LARGE_SMOKE,1,1,1,2,2,2);
            }else{
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(MoreParticles.WATER_MIST.type(),1,1,1,4,2,2,2,2);
                    serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,1,1,1,4,2,2,2,2);
                }
            }
            pLivingEntity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH,2,1);
            pLivingEntity.removeStatusEffect(MRPGCEffects.SOAKED.entry);
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
