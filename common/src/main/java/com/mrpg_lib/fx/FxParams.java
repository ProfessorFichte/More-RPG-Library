package com.mrpg_lib.fx;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class FxParams {
    private Vec3d origin;
    @Nullable private Vec3d direction;
    @Nullable private Vec3d target;
    private int entityId = -1;
    private float radius = Float.NaN;
    private float length = Float.NaN;
    private float scale = 1.0F;
    private float intensity = 1.0F;
    private int color = -1;
    private int duration = -1;
    private int startAge = 0;
    private long seed;
    private FxVariant variant = FxVariant.AUTO;

    private FxParams(Vec3d origin) {
        this.origin = origin;
        this.seed = Double.doubleToLongBits(origin.x * 31 + origin.y * 17 + origin.z) ^ System.nanoTime();
    }

    public static FxParams at(Vec3d origin) {
        return new FxParams(origin);
    }

    public static FxParams on(Entity entity) {
        return new FxParams(entity.getPos()).attach(entity);
    }

    public FxParams direction(Vec3d direction) {
        this.direction = direction.lengthSquared() > 1.0E-8 ? direction.normalize() : null;
        return this;
    }

    public FxParams target(Vec3d target) {
        this.target = target;
        return this;
    }

    public FxParams attach(Entity entity) {
        this.entityId = entity.getId();
        return this;
    }

    public FxParams radius(float radius) {
        this.radius = radius;
        return this;
    }

    public FxParams length(float length) {
        this.length = length;
        return this;
    }

    public FxParams scale(float scale) {
        this.scale = scale;
        return this;
    }

    public FxParams intensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    public FxParams color(int rgb) {
        this.color = rgb & 0xFFFFFF;
        return this;
    }

    public FxParams duration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public FxParams startAge(int ticks) {
        this.startAge = Math.max(0, ticks);
        return this;
    }

    public FxParams seed(long seed) {
        this.seed = seed;
        return this;
    }

    public FxParams variant(FxVariant variant) {
        this.variant = variant;
        return this;
    }

    public Vec3d origin() { return origin; }
    @Nullable public Vec3d direction() { return direction; }
    @Nullable public Vec3d target() { return target; }
    public int entityId() { return entityId; }
    public float radius() { return radius; }
    public float length() { return length; }
    public float scale() { return scale; }
    public float intensity() { return intensity; }
    public int color() { return color; }
    public int duration() { return duration; }
    public int startAge() { return startAge; }
    public long seed() { return seed; }
    public FxVariant variant() { return variant; }

    public void write(PacketByteBuf buf) {
        buf.writeDouble(origin.x);
        buf.writeDouble(origin.y);
        buf.writeDouble(origin.z);
        writeOptionalVec(buf, direction);
        writeOptionalVec(buf, target);
        buf.writeVarInt(entityId);
        buf.writeFloat(radius);
        buf.writeFloat(length);
        buf.writeFloat(scale);
        buf.writeFloat(intensity);
        buf.writeInt(color);
        buf.writeVarInt(duration + 1);
        buf.writeVarInt(startAge);
        buf.writeLong(seed);
        buf.writeByte(variant.ordinal());
    }

    public static FxParams read(PacketByteBuf buf) {
        var params = new FxParams(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        params.direction = readOptionalVec(buf);
        params.target = readOptionalVec(buf);
        params.entityId = buf.readVarInt();
        params.radius = buf.readFloat();
        params.length = buf.readFloat();
        params.scale = buf.readFloat();
        params.intensity = buf.readFloat();
        params.color = buf.readInt();
        params.duration = buf.readVarInt() - 1;
        params.startAge = buf.readVarInt();
        params.seed = buf.readLong();
        int variant = buf.readByte();
        params.variant = variant >= 0 && variant < FxVariant.values().length ? FxVariant.values()[variant] : FxVariant.AUTO;
        return params;
    }

    private static void writeOptionalVec(PacketByteBuf buf, @Nullable Vec3d vec) {
        buf.writeBoolean(vec != null);
        if (vec != null) {
            buf.writeDouble(vec.x);
            buf.writeDouble(vec.y);
            buf.writeDouble(vec.z);
        }
    }

    @Nullable
    private static Vec3d readOptionalVec(PacketByteBuf buf) {
        return buf.readBoolean() ? new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()) : null;
    }
}
