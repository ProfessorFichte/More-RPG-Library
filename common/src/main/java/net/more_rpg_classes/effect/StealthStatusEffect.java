package net.more_rpg_classes.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.utils.SoundHelper;

public abstract class StealthStatusEffect extends StatusEffect {
    protected StealthStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public abstract ParticleBatch stealthPopParticles();

    public Identifier stealthLeaveSoundId() {
        return null;
    }

    public abstract double stealthFollowRange();

    public abstract double stealthVisibilityMultiplier();

    public void onStealthRemoved(LivingEntity entity) {
        if (entity.getWorld().isClient()) return;
        var soundId = stealthLeaveSoundId();
        if (soundId != null) {
            SoundHelper.playSoundEvent(entity.getWorld(), entity, SoundEvent.of(soundId));
        }
        var particles = stealthPopParticles();
        if (particles != null) {
            ParticleHelper.sendBatches(entity, new ParticleBatch[]{particles});
        }
    }
}
