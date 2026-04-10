package net.more_rpg_classes.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.client.heart.HeartRegistry;
import net.more_rpg_classes.client.heart.HeartSetting;
import net.more_rpg_classes.client.heart.HeartTypes;
import net.more_rpg_classes.effect.MRPGCEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class DrawHeartsMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "drawHeart", at = @At(value = "HEAD"), cancellable = true)
    private void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo ci) {
        // only run if normal heart or a container heart
        if (!(type.equals(InGameHud.HeartType.NORMAL) || type.equals(InGameHud.HeartType.CONTAINER))) return;
        // add a container boolean for when the heart type is a container
        boolean container = type.equals(InGameHud.HeartType.CONTAINER);

        // make sure the player is valid
        PlayerEntity player = this.client.player;
        if (player == null) return;

        // fatal poison
        if (player.hasStatusEffect(MRPGCEffects.FATAL_POISON.entry)) {
            render(ci,context,x,y,half,blinking,container, HeartRegistry.getHeartSetting(HeartTypes.FATAL_POISON_ID));
        }
    }

    /**
     * Renders the heart
     * @param heartSetting the heart setting to use for rendering
     */
    @Unique
    private static void render(CallbackInfo ci, DrawContext context, int x, int y, boolean half, boolean blinking, boolean container, HeartSetting heartSetting) {
        // get the textures
        Identifier texture = heartSetting.getIdentifier(blinking,half,false,container);
        if (texture == null) return; // if the texture is null, do not render

        // draw the texture
        context.drawGuiTexture(texture, x, y, 9, 9);
        // cancel the drawing of the other texture
        ci.cancel();
    }
}