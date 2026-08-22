package net.more_rpg_classes.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.spell_engine.client.particle.SpellParticle;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.ParticleGroupType;
import net.spell_engine.fx.SpellEngineParticles;
import org.jetbrains.annotations.Nullable;

/// The hand-written music note, restored from V1.
///
/// A note leaves the instrument on its *own* lift, not the batch's: the spawn
/// velocity's vertical component is discarded outright and replaced with a small
/// fixed rise, while the horizontal component is damped to a tenth and jittered.
/// From there a raw (deliberately un-vanilla-scaled) gravity of `0.002` bleeds the
/// rise away, a per-tick random walk nudges it sideways, `0.98` damping settles the
/// drift, and the last 15 ticks fade it out.
///
/// None of that is expressible through [net.spell_engine.api.spell.fx.ParticleGroup.Motion]:
/// every preset either keeps the authored Y velocity or scales it, and no preset
/// combines a fixed launch, a raw sub-vanilla gravity, a random walk and a
/// tail-end alpha fade. Porting it onto the generic [SpellParticle] in the 1.9 -> 1.10
/// migration flattened it into `gravity(0.002F)` alone, which let the notes keep the
/// batch's full launch speed and shoot straight up.
///
/// Unlike [RainbowMusicNoteParticle] and [PopupParticle], this one stays a
/// [SpellEngineParticles.Entry]: only its *motion* is custom, and Bards colours its
/// notes per song. Registering the entry against this factory instead of
/// [SpellParticle.Factory] keeps `ParticleGroupBuilder.of(MoreParticles.MUSIC_NOTE)`
/// and its `.color(...)` payload working exactly as before, which is also what V1 did
/// through `TemplateParticleType`.
@Environment(EnvType.CLIENT)
public class MusicNoteParticle extends SpriteBillboardParticle {

    MusicNoteParticle(ClientWorld world, double x, double y, double z,
                      double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);
        this.scale(1.25F);
        this.setBoundingBoxSpacing(0.25F, 0.25F);
        this.maxAge = this.random.nextInt(20) + 40;

        // The batch's Y is intentionally unused: every note rises at its own gentle pace.
        this.velocityY = 0.06F + this.random.nextFloat() * 0.02F;

        this.velocityX = velocityX * 0.1 + (this.random.nextFloat() - 0.5) * 0.02;
        this.velocityZ = velocityZ * 0.1 + (this.random.nextFloat() - 0.5) * 0.02;

        this.gravityStrength = 0.002F;

        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge || this.alpha <= 0.0F) {
            this.markDead();
            return;
        }

        // Raw, not vanilla's `gravityStrength * 0.04` - the deceleration is meant to be
        // barely there, so the note coasts to a stop instead of arcing.
        this.velocityY -= this.gravityStrength;

        this.velocityX += (this.random.nextFloat() - 0.5) * 0.001;
        this.velocityZ += (this.random.nextFloat() - 0.5) * 0.001;

        this.velocityX *= 0.98;
        this.velocityZ *= 0.98;

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        if (this.age >= this.maxAge - 15 && this.alpha > 0.01F) {
            this.alpha -= 0.06F;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    /// Takes the [ParticleGroupType] payload like every other entry, but reads only the
    /// fields V1's `MusicNoteFactory` read - tint, alpha and a scale multiplier. Motion,
    /// lifetime and the fade are the class's own and are not overridable, which is the
    /// point of keeping it hand-written.
    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<ParticleGroupType> {
        private final SpriteProvider spriteProvider;
        private final SpellEngineParticles.Entry entry;

        public Factory(SpriteProvider spriteProvider, SpellEngineParticles.Entry entry) {
            this.spriteProvider = spriteProvider;
            this.entry = entry;
        }

        @Nullable
        @Override
        public Particle createParticle(ParticleGroupType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            var particle = new MusicNoteParticle(world, x, y, z, velocityX, velocityY, velocityZ);

            var appearance = SpellParticle.Factory.resolve(entry, type.payload());
            if (appearance.color >= 0) {
                var color = Color.fromRGBA(appearance.color);
                particle.setColor(color.red(), color.green(), color.blue());
                particle.alpha *= color.alpha();
            }
            particle.alpha *= appearance.opacity;
            particle.scale *= appearance.scale;

            // One of the eight note glyphs, held for the particle's whole life - the
            // sheet is a set of alternatives, not an animation.
            particle.setSprite(this.spriteProvider);
            return particle;
        }
    }
}
