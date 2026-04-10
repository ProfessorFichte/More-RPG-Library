package com.mrpg_lib.neoforge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrpg_lib.neoforge.NeoForgeMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.effect.MRPGCEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(InGameHud.class)
public abstract class RenderHeartMixin {
    @Shadow @Nullable protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "drawHeart", at = @At(value = "HEAD"), cancellable = true)
    private void drawHeart(DrawContext pGuiGraphics, InGameHud.HeartType type, int x, int y, boolean pHardcore, boolean blinking, boolean half, CallbackInfo ci) {
        // ^^ BLINKING AND HALF ARE FLIPPED FROM THE SOURCE BECAUSE THEY ARE MISNAMED THERE
        // only run if normal heart or a container heart
        if (!(type.equals(InGameHud.HeartType.NORMAL) || type.equals(InGameHud.HeartType.CONTAINER))) return;
        // add a container boolean for when the heart type is a container
        boolean container = type.equals(InGameHud.HeartType.CONTAINER);

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        // fatal poison
        if (player.hasStatusEffect(MRPGCEffects.FATAL_POISON.entry)) {
            render(ci,pGuiGraphics,x,y,half,blinking,container,"fatal_poison",true);
        }
    }

    /**
     * renders the heart
     * @param name the name for the heart texture
     * @param renderContainer if there is a custom container for the heart
     */
    @Unique
    private static void render(CallbackInfo ci, DrawContext pGuiGraphics, int x, int y, boolean half, boolean blinking, boolean container, String name, boolean renderContainer) {
        // get the textures
        Identifier texture = Identifier.ofVanilla("hud/heart/"+name+"_full");
        if (half) texture = Identifier.ofVanilla("hud/heart/"+name+"_half");

        // if container texture
        if (container) {
            // quit if theres no custom container texture
            if (!renderContainer) return;
            // get the container textures
            texture = Identifier.ofVanilla("hud/heart/"+name+"_container");
            if (blinking) texture = Identifier.ofVanilla("hud/heart/"+name+"_container_blinking");
        }

        // draw the texture
        RenderSystem.enableBlend();
        pGuiGraphics.drawGuiTexture(texture,x,y,9,9);
        RenderSystem.disableBlend();

        // cancel the drawing of the other texture
        ci.cancel();
    }
}
