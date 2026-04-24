package net.more_rpg_classes.client.effect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.more_rpg_classes.MRPGCMod;
import net.spell_engine.api.effect.CustomModelStatusEffect;
import net.spell_engine.api.render.CustomLayers;
import net.spell_engine.api.render.CustomModels;
import net.spell_engine.api.render.LightEmission;

public class DuelistsFocusRenderer implements CustomModelStatusEffect.Renderer {

    private static final RenderLayer RENDER_LAYER = CustomLayers.spellEffect(LightEmission.RADIATE, false);
    private static final float ORBIT_RADIUS = 0.75f;
    private static final float ROTATION_SPEED = 2.25f;
    private static final float SCALE = 0.5f;

    public static final Identifier OWNER_MODEL = Identifier.of(MRPGCMod.MOD_ID, "effect/duelists_focus_owner");
    public static final Identifier TARGET_MODEL = Identifier.of(MRPGCMod.MOD_ID, "effect/duelists_focus_target");

    private final Identifier modelId;

    public DuelistsFocusRenderer(Identifier modelId) {
        this.modelId = modelId;
    }

    @Override
    public void renderEffect(int amplifier, LivingEntity livingEntity, float delta,
                             MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light) {
        var itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        float time = livingEntity.getWorld().getTime() + delta;
        float baseAngle = time * ROTATION_SPEED;
        float midHeight = livingEntity.getHeight() / 2.0f;
        float radius = ORBIT_RADIUS * livingEntity.getScaleFactor();

        matrixStack.push();
        for (int i = 0; i < 3; i++) {
            float angle = baseAngle + i * 120f;
            matrixStack.push();
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
            matrixStack.translate(0, midHeight, -radius);
            // Rotate tip from +Y to +Z so it points toward the entity center
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            matrixStack.scale(SCALE, SCALE, SCALE);
            CustomModels.render(RENDER_LAYER, itemRenderer, modelId,
                    matrixStack, vertexConsumers, light, livingEntity.getId() + i);
            matrixStack.pop();
        }
        matrixStack.pop();
    }
}
