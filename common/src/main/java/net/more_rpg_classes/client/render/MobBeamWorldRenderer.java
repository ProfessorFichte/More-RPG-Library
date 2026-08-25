package net.more_rpg_classes.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.compatibility.ShaderCompatibility;
import net.spell_engine.client.render.BeamRenderer;
import net.spell_engine.client.util.Color;

@Environment(EnvType.CLIENT)
public class MobBeamWorldRenderer {

    public static void render(MatrixStack matrices, Camera camera, float delta) {
        var client = MinecraftClient.getInstance();
        var world = client.world;
        if (world == null) return;

        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
        long time = world.getTime();

        Vec3d camPos = camera.getPos();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        boolean rendered = false;
        for (var entity : world.getEntities()) {
            if (!(entity instanceof LivingEntity caster)) continue;
            var activeBeam = MobBeamTracker.get(entity.getId());
            if (activeBeam == null) continue;
            var target = world.getEntityById(activeBeam.targetId());
            if (!(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) continue;

            renderMobBeam(matrices, consumers, caster, livingTarget, activeBeam.beam(), activeBeam.range(), time, delta);
            rendered = true;
        }

        if (rendered) consumers.draw();
        matrices.pop();
    }

    public static void onDisconnect() {
        MobBeamTracker.clear();
    }

    private static void renderMobBeam(MatrixStack matrices, VertexConsumerProvider consumers,
                                      LivingEntity caster, LivingEntity target,
                                      Spell.Target.Beam beam, float spellRange, long time, float delta) {
        Vec3d casterPos = new Vec3d(
                caster.prevX + (caster.getX() - caster.prevX) * delta,
                caster.prevY + (caster.getY() - caster.prevY) * delta,
                caster.prevZ + (caster.getZ() - caster.prevZ) * delta
        );
        Vec3d from = casterPos.add(0, caster.getStandingEyeHeight() * 0.9, 0);
        Vec3d targetEye = target.getEyePos();

        Vec3d beamVector = targetEye.subtract(from);
        float length = (float) beamVector.length();
        if (length < 0.01f) return;
        beamVector = beamVector.normalize();

        float effectiveRange = spellRange > 0 ? spellRange : 32f;
        Vec3d extendedEnd = from.add(beamVector.multiply(effectiveRange));
        var world = MinecraftClient.getInstance().world;
        Vec3d to;
        if (world != null) {
            BlockHitResult blockHit = world.raycast(new RaycastContext(
                    from, extendedEnd,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    caster));
            to = blockHit.getType() == HitResult.Type.MISS ? extendedEnd : blockHit.getPos();
        } else {
            to = extendedEnd;
        }
        beamVector = to.subtract(from).normalize();
        length = (float) to.distanceTo(from);

        float rotY = (float) Math.atan2(beamVector.z, beamVector.x);
        float rotX = (float) Math.acos(beamVector.y);
        float absoluteTime = (float) Math.floorMod(time, 40) + delta;

        var texture = Identifier.of(beam.texture_id);
        var outerColor = Color.IntFormat.fromLongRGBA(beam.color_rgba);
        var innerColor = Color.IntFormat.fromLongRGBA(beam.inner_color_rgba);

        BeamRenderer.LayerSet renderLayers;
        if (ShaderCompatibility.isVanillaRenderSystem()) {
            renderLayers = BeamRenderer.vanilla(texture);
        } else {
            var luminance = ShaderCompatibility.isShaderPackInUse() ? beam.luminance : Spell.Target.Beam.Luminance.LOW;
            renderLayers = BeamRenderer.layerSetFor(texture, luminance);
        }

        matrices.push();
        matrices.translate(from.x, from.y, from.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964F - rotY) * 57.295776F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotX * 57.295776F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(absoluteTime * 2.25F - 45.0F));

        BeamRenderer.renderBeam(matrices, consumers,
                time, delta, beam.flow, true,
                innerColor, outerColor, renderLayers,
                0, length, beam.width);
        matrices.pop();
    }
}
