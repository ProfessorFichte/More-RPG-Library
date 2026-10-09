package com.mrpg_lib.neoforge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrpg_lib.effect.MRPGCEffects;
import com.mrpg_lib.neoforge.NeoForgeMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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
        // blinking and half are swapped compared to the source, they're misnamed there
        if (!(type.equals(InGameHud.HeartType.NORMAL) || type.equals(InGameHud.HeartType.CONTAINER))) return;
        boolean container = type.equals(InGameHud.HeartType.CONTAINER);

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        if (player.hasStatusEffect(MRPGCEffects.FATAL_POISON.entry)) {
            render(ci,pGuiGraphics,x,y,half,blinking,container,"fatal_poison",true);
        }
    }

    @Unique
    private static void render(CallbackInfo ci, DrawContext pGuiGraphics, int x, int y, boolean half, boolean blinking, boolean container, String name, boolean renderContainer) {
        Identifier texture = Identifier.ofVanilla("hud/heart/"+name+"_full");
        if (half) texture = Identifier.ofVanilla("hud/heart/"+name+"_half");

        if (container) {
            if (!renderContainer) return;
            texture = Identifier.ofVanilla("hud/heart/"+name+"_container");
            if (blinking) texture = Identifier.ofVanilla("hud/heart/"+name+"_container_blinking");
        }

        RenderSystem.enableBlend();
        pGuiGraphics.drawGuiTexture(texture,x,y,9,9);
        RenderSystem.disableBlend();

        ci.cancel();
    }
}
