package com.mrpg_lib.mixin;

import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.client.heart.HeartRegistry;
import com.mrpg_lib.client.heart.HeartSetting;
import com.mrpg_lib.client.heart.HeartTypes;
import com.mrpg_lib.effect.MRPGCEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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
        if (!(type.equals(InGameHud.HeartType.NORMAL) || type.equals(InGameHud.HeartType.CONTAINER))) return;
        boolean container = type.equals(InGameHud.HeartType.CONTAINER);

        PlayerEntity player = this.client.player;
        if (player == null) return;

        if (player.hasStatusEffect(MRPGCEffects.FATAL_POISON.entry)) {
            render(ci,context,x,y,half,blinking,container, HeartRegistry.getHeartSetting(HeartTypes.FATAL_POISON_ID));
        }
    }

    @Unique
    private static void render(CallbackInfo ci, DrawContext context, int x, int y, boolean half, boolean blinking, boolean container, HeartSetting heartSetting) {
        Identifier texture = heartSetting.getIdentifier(blinking,half,false,container);
        if (texture == null) return;

        context.drawGuiTexture(texture, x, y, 9, 9);
        ci.cancel();
    }
}
