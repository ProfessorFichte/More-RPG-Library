package net.more_rpg_classes.client.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.more_rpg_classes.entity.FriendlyLightningEntity;
import org.joml.Matrix4f;

/**
 * Renderer for the FriendlyLightningEntity
 * Based on Minecraft's vanilla LightningEntityRenderer
 */
public class FriendlyLightningEntityRenderer extends EntityRenderer<FriendlyLightningEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/lightning_bolt.png");

    public FriendlyLightningEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(FriendlyLightningEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        float[] fs = new float[8];
        float[] gs = new float[8];
        float f = 0.0F;
        float g = 0.0F;
        Random random = Random.create(entity.getId());

        for (int i = 7; i >= 0; --i) {
            fs[i] = f;
            gs[i] = g;
            f += (float)(random.nextInt(11) - 5);
            g += (float)(random.nextInt(11) - 5);
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLightning());
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        for (int j = 0; j < 4; ++j) {
            Random random2 = Random.create(entity.getId());

            for (int k = 0; k < 3; ++k) {
                int l = 7;
                int m = 0;
                if (k > 0) {
                    l = 7 - k;
                }

                if (k > 0) {
                    m = l - 2;
                }

                float h = fs[l] - f;
                float n = gs[l] - g;

                for (int o = l; o >= m; --o) {
                    float p = h;
                    float q = n;
                    if (k == 0) {
                        h += (float)(random2.nextInt(11) - 5);
                        n += (float)(random2.nextInt(11) - 5);
                    } else {
                        h += (float)(random2.nextInt(31) - 15);
                        n += (float)(random2.nextInt(31) - 15);
                    }

                    float r = 0.1F + (float)j * 0.2F;
                    if (k == 0) {
                        r *= (float)o * 0.1F + 1.0F;
                    }

                    float s = 0.1F + (float)j * 0.2F;
                    if (k == 0) {
                        s *= ((float)o - 1.0F) * 0.1F + 1.0F;
                    }

                    drawBranch(matrix4f, vertexConsumer, h, n, o, p, q, 0.45F, 0.45F, 0.5F, r, s, false, false, true, false);
                    drawBranch(matrix4f, vertexConsumer, h, n, o, p, q, 0.45F, 0.45F, 0.5F, r, s, true, false, true, true);
                    drawBranch(matrix4f, vertexConsumer, h, n, o, p, q, 0.45F, 0.45F, 0.5F, r, s, true, true, false, true);
                    drawBranch(matrix4f, vertexConsumer, h, n, o, p, q, 0.45F, 0.45F, 0.5F, r, s, false, true, false, false);
                }
            }
        }
    }

    private static void drawBranch(
        Matrix4f matrix,
        VertexConsumer buffer,
        float x1,
        float z1,
        int y,
        float x2,
        float z2,
        float red,
        float green,
        float blue,
        float offset2,
        float offset1,
        boolean reverseX,
        boolean reverseZ,
        boolean reverseY,
        boolean reverseUV
    ) {
        buffer.vertex(matrix, x1 + (reverseX ? offset1 : -offset1), (float)(y * 16), z1 + (reverseZ ? offset1 : -offset1))
            .color(red, green, blue, 0.3F)
            .texture(reverseUV ? 1.0F : 0.0F, reverseY ? 1.0F : 0.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);

        buffer.vertex(matrix, x2 + (reverseX ? offset2 : -offset2), (float)((y + 1) * 16), z2 + (reverseZ ? offset2 : -offset2))
            .color(red, green, blue, 0.3F)
            .texture(reverseUV ? 0.0F : 1.0F, reverseY ? 1.0F : 0.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public Identifier getTexture(FriendlyLightningEntity entity) {
        return TEXTURE;
    }
}
