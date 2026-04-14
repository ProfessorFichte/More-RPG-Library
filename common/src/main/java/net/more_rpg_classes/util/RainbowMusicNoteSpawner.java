package net.more_rpg_classes.util;

import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.more_rpg_classes.client.particle.MoreParticles;

public class RainbowMusicNoteSpawner {

    public static void spawnCircle(ServerWorld world, LivingEntity entity, double radius, SimpleParticleType particleType, int particlesCountPerTick, float particleSpeedMultiplier) {
        if (entity.age % 3 != 0) {
            return;
        }

        double timeOffset = entity.age * 0.02;

        for (int i = 0; i < particlesCountPerTick; i++) {
            double angle = (2 * Math.PI * i / particlesCountPerTick) + (timeOffset * 2 * Math.PI);

            double offsetX = Math.cos(angle) * radius * 1.5;
            double offsetZ = Math.sin(angle) * radius * 1.5;

            double x = entity.getX() + offsetX;
            double y = entity.getY() + entity.getHeight() * 0.5 + (world.random.nextDouble() - 0.5) * 0.3;
            double z = entity.getZ() + offsetZ;

            double velocityX = -Math.sin(angle) * 0.08;
            double velocityY = 0.0;
            double velocityZ = Math.cos(angle) * 0.08;

            world.spawnParticles(
                    particleType,
                x, y, z,
                0,
                velocityX, velocityY, velocityZ,
                    particleSpeedMultiplier
            );
        }
    }

    public static void spawnBurst(ServerWorld world, LivingEntity entity, int particleCount, double radius) {
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            double x = entity.getX() + offsetX;
            double y = entity.getY() + entity.getHeight() * 0.5 + (world.random.nextDouble() - 0.5) * 0.6;
            double z = entity.getZ() + offsetZ;

            double velocityX = -Math.sin(angle) * 0.05 + (world.random.nextDouble() - 0.5) * 0.1;
            double velocityY = 0.01 + world.random.nextDouble() * 0.02;
            double velocityZ = Math.cos(angle) * 0.05 + (world.random.nextDouble() - 0.5) * 0.1;

            world.spawnParticles(
                MoreParticles.RAINBOW_MUSIC_NOTE,
                x, y, z,
                1,
                0, 0, 0,
                0.0
            );
        }
    }
}
