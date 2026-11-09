package com.mrpg_lib.client.fx;

import com.mrpg_lib.fx.FxEffect;
import com.mrpg_lib.fx.FxLayer;
import com.mrpg_lib.fx.FxOrientation;
import com.mrpg_lib.fx.FxParams;
import com.mrpg_lib.fx.FxShape;
import com.mrpg_lib.fx.FxSound;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

final class FxEmitter {
    private static final Vec3d WORLD_UP = new Vec3d(0, 1, 0);

    private FxEmitter() {
    }

    record Frame(Vec3d right, Vec3d up, Vec3d forward) {
        Vec3d toWorld(double x, double y, double z) {
            return right.multiply(x).add(up.multiply(y)).add(forward.multiply(z));
        }

        Vec3d toWorld(Vec3d local) {
            return toWorld(local.x, local.y, local.z);
        }
    }

    static int emit(ClientWorld world, FxEffect effect, FxLayer layer, FxParams params, int age, float progress,
                    Random random, int budget, double density) {
        if (budget <= 0) return 0;
        if (layer.chance() < 1.0F && random.nextFloat() >= layer.chance()) return 0;
        double wanted = layer.count() * params.intensity() * density;
        int count = (int) Math.floor(wanted);
        if (random.nextDouble() < wanted - count) count++;
        count = Math.min(count, budget);
        if (count <= 0) return 0;

        Entity entity = params.entityId() >= 0 ? world.getEntityById(params.entityId()) : null;
        float effectRadius = Float.isNaN(params.radius()) ? effect.defaultRadius() : params.radius();
        float radiusRatio = effect.defaultRadius() > 0 ? effectRadius / effect.defaultRadius() : 1.0F;
        float grow = MathHelper.lerp(progress, layer.growFrom(), 1.0F);
        double radius = (layer.radiusFromParams() ? layer.radius() * effectRadius : layer.radius()) * params.scale() * grow;
        float effectLength = Float.isNaN(params.length()) ? effect.defaultLength() : params.length();
        double length = (layer.lengthFromParams() ? layer.length() * effectLength : layer.length()) * params.scale();
        double height = layer.height() * params.scale();
        double speedScale = (layer.scalesSpeedWithRadius() ? radiusRatio : 1.0) * params.scale();

        Vec3d base = anchor(world, layer, params, entity, effect.follow());
        Frame frame = frame(layer.orientation(), params, entity);
        double spin = Math.toRadians(layer.spinPerTick() * age);
        double phase = random.nextDouble() * MathHelper.TAU;
        var particle = layer.particle(params.color());

        Vec3d beamEnd = null;
        if (layer.shape() == FxShape.BEAM) {
            beamEnd = params.target() != null ? params.target()
                    : base.add((params.direction() != null ? params.direction() : frame.forward()).multiply(length));
        }

        for (int i = 0; i < count; i++) {
            double t = count > 1 ? (double) i / (count - 1) : random.nextDouble();
            double even = (double) i / count;
            Vec3d local;
            Vec3d axisDirection = null;
            switch (layer.shape()) {
                case SPHERE -> local = randomUnit(random).multiply(radius * Math.cbrt(random.nextDouble()));
                case SHELL -> local = randomUnit(random).multiply(radius);
                case RING -> local = polar(phase + even * MathHelper.TAU, radius, 0);
                case DISC -> local = polar(random.nextDouble() * MathHelper.TAU, radius * Math.sqrt(random.nextDouble()), 0);
                case ARC -> {
                    double half = Math.toRadians(layer.arcDegrees()) / 2.0;
                    double angle = -half + t * half * 2.0;
                    local = new Vec3d(Math.sin(angle) * radius, 0, Math.cos(angle) * radius);
                }
                case CYLINDER -> local = polar(random.nextDouble() * MathHelper.TAU, radius, random.nextDouble() * height);
                case COLUMN -> local = polar(random.nextDouble() * MathHelper.TAU, radius * Math.sqrt(random.nextDouble()), random.nextDouble() * height);
                case CONE -> {
                    double half = Math.toRadians(layer.coneDegrees()) / 2.0;
                    double cosTheta = 1.0 - random.nextDouble() * (1.0 - Math.cos(half));
                    double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
                    double phi = random.nextDouble() * MathHelper.TAU;
                    axisDirection = new Vec3d(Math.cos(phi) * sinTheta, Math.sin(phi) * sinTheta, cosTheta);
                    local = axisDirection.multiply(random.nextDouble() * length);
                }
                case LINE -> local = new Vec3d(0, 0, t * length);
                case HELIX -> {
                    double angle = MathHelper.TAU * layer.turns() * t + phase + random.nextDouble() * layer.spread();
                    local = polar(angle, radius, t * height);
                }
                case SPIRAL -> local = polar(MathHelper.TAU * layer.turns() * t + phase, radius * t, 0);
                case BOX -> local = new Vec3d((random.nextDouble() * 2 - 1) * radius, random.nextDouble() * height, (random.nextDouble() * 2 - 1) * radius);
                default -> local = Vec3d.ZERO;
            }
            if (spin != 0) {
                local = rotateY(local, spin);
                if (axisDirection != null) axisDirection = rotateY(axisDirection, spin);
            }
            if (layer.jitter() > 0) {
                double j = layer.jitter();
                local = local.add((random.nextDouble() * 2 - 1) * j, (random.nextDouble() * 2 - 1) * j, (random.nextDouble() * 2 - 1) * j);
            }

            Vec3d position;
            Vec3d velocityDirection;
            if (beamEnd != null) {
                position = base.lerp(beamEnd, t);
                velocityDirection = velocityForLine(layer, beamEnd.subtract(base), random);
            } else {
                position = base.add(frame.toWorld(local.x, local.y, local.z));
                velocityDirection = frame.toWorld(localVelocity(layer, local, axisDirection, random));
            }
            double speed = MathHelper.lerp(random.nextDouble(), layer.minSpeed(), layer.maxSpeed()) * speedScale;
            Vec3d velocity = applyMotion(layer, velocityDirection.multiply(speed), random);
            if (layer.lifetime() > 0) {
                var spawned = MinecraftClient.getInstance().particleManager.addParticle(particle, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
                if (spawned != null) spawned.setMaxAge(layer.lifetime());
            } else {
                world.addParticle(particle, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            }
        }
        return count;
    }

    static void playSound(ClientWorld world, FxSound sound, Vec3d pos, Random random) {
        float pitch = sound.pitch() + (random.nextFloat() * 2.0F - 1.0F) * sound.pitchVariance();
        world.playSound(pos.x, pos.y, pos.z, sound.sound(), SoundCategory.PLAYERS, sound.volume(), pitch, false);
    }

    static Vec3d soundPosition(ClientWorld world, FxParams params) {
        Entity entity = params.entityId() >= 0 ? world.getEntityById(params.entityId()) : null;
        return entity != null ? entity.getPos().add(0, entity.getHeight() * 0.5, 0) : params.origin();
    }

    private static Vec3d anchor(ClientWorld world, FxLayer layer, FxParams params, @Nullable Entity entity, boolean follow) {
        Vec3d origin = follow && entity != null ? entity.getPos() : params.origin();
        Vec3d result = switch (layer.anchor()) {
            case ENTITY -> entity != null ? entity.getPos().add(0, entity.getHeight() * layer.verticalOrigin(), 0) : origin;
            case EYES -> entity != null ? entity.getEyePos() : origin;
            case GROUND -> ground(world, origin);
            case TARGET -> params.target() != null ? params.target() : origin;
            case POSITION -> origin;
        };
        return layer.yOffset() == 0 ? result : result.add(0, layer.yOffset(), 0);
    }

    private static Vec3d ground(ClientWorld world, Vec3d from) {
        BlockPos.Mutable pos = BlockPos.ofFloored(from.x, from.y + 1.0, from.z).mutableCopy();
        int floor = MathHelper.floor(from.y) - 4;
        while (pos.getY() >= floor) {
            var shape = world.getBlockState(pos).getCollisionShape(world, pos);
            if (!shape.isEmpty() && pos.getY() + shape.getMax(Direction.Axis.Y) <= from.y + 1.0) {
                return new Vec3d(from.x, pos.getY() + shape.getMax(Direction.Axis.Y) + 0.05, from.z);
            }
            pos.move(Direction.DOWN);
        }
        return from.add(0, 0.05, 0);
    }

    private static Frame frame(FxOrientation orientation, FxParams params, @Nullable Entity entity) {
        Vec3d direction = params.direction() != null ? params.direction()
                : entity != null ? entity.getRotationVector() : new Vec3d(0, 0, 1);
        return switch (orientation) {
            case WORLD -> {
                Vec3d forward = new Vec3d(direction.x, 0, direction.z);
                forward = forward.lengthSquared() < 1.0E-6 ? new Vec3d(0, 0, 1) : forward.normalize();
                yield new Frame(forward.crossProduct(WORLD_UP).normalize(), WORLD_UP, forward);
            }
            case LOOK -> {
                Vec3d forward = direction.normalize();
                Vec3d reference = Math.abs(forward.y) > 0.99 ? new Vec3d(1, 0, 0) : WORLD_UP;
                Vec3d right = forward.crossProduct(reference).normalize();
                yield new Frame(right, right.crossProduct(forward).normalize(), forward);
            }
            case AXIS -> {
                Vec3d up = direction.normalize();
                Vec3d reference = Math.abs(up.y) > 0.99 ? new Vec3d(1, 0, 0) : WORLD_UP;
                Vec3d forward = reference.crossProduct(up).normalize();
                yield new Frame(forward.crossProduct(up).normalize(), up, forward);
            }
        };
    }

    private static Vec3d localVelocity(FxLayer layer, Vec3d local, @Nullable Vec3d axisDirection, Random random) {
        return switch (layer.velocity()) {
            case NONE -> Vec3d.ZERO;
            case OUTWARD -> radial(local, random);
            case INWARD -> radial(local, random).negate();
            case UP -> new Vec3d(0, 1, 0);
            case DOWN -> new Vec3d(0, -1, 0);
            case ALONG_AXIS -> axisDirection != null ? axisDirection : new Vec3d(0, 0, 1);
            case TANGENT -> {
                Vec3d flat = new Vec3d(-local.z, 0, local.x);
                yield flat.lengthSquared() < 1.0E-6 ? randomUnit(random) : flat.normalize();
            }
            case RANDOM -> randomUnit(random);
        };
    }

    private static Vec3d velocityForLine(FxLayer layer, Vec3d line, Random random) {
        return switch (layer.velocity()) {
            case NONE -> Vec3d.ZERO;
            case ALONG_AXIS -> line.normalize();
            case UP -> WORLD_UP;
            case DOWN -> WORLD_UP.negate();
            default -> randomUnit(random);
        };
    }

    private static Vec3d applyMotion(FxLayer layer, Vec3d velocity, Random random) {
        return switch (layer.motion()) {
            case DEFAULT -> velocity;
            case STATIC -> Vec3d.ZERO;
            case FLOAT -> velocity.add(0, 0.01 + random.nextDouble() * 0.02, 0);
            case ASCEND -> velocity.add(0, 0.06, 0);
            case DRIFT -> velocity.add((random.nextDouble() - 0.5) * 0.05, 0, (random.nextDouble() - 0.5) * 0.05);
            case BURST -> velocity.multiply(2.5);
        };
    }

    private static Vec3d radial(Vec3d local, Random random) {
        return local.lengthSquared() < 1.0E-6 ? randomUnit(random) : local.normalize();
    }

    private static Vec3d polar(double angle, double radius, double y) {
        return new Vec3d(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private static Vec3d rotateY(Vec3d vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3d(vec.x * cos - vec.z * sin, vec.y, vec.x * sin + vec.z * cos);
    }

    private static Vec3d randomUnit(Random random) {
        double z = random.nextDouble() * 2 - 1;
        double angle = random.nextDouble() * MathHelper.TAU;
        double r = Math.sqrt(1 - z * z);
        return new Vec3d(r * Math.cos(angle), z, r * Math.sin(angle));
    }
}
