package net.more_rpg_classes.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.spell_engine.api.effect.EntityActionsAllowed;
import net.spell_engine.client.gui.HudMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HudMessages.class, remap = false)
public class HudMessagesMixin {

    @Inject(method = "actionImpaired", at = @At("HEAD"), cancellable = true)
    private void mrpgc$customActionImpairedMessages(EntityActionsAllowed.SemanticType reason, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            PlayerEntity player = client.player;

            if (player.hasStatusEffect(MRPGCEffects.FROZEN_SOLID.entry)) {
                ((HudMessages)(Object)this).error(Text.translatable("hud.more_rpg_classes.frozen").formatted(Formatting.RED));
                ci.cancel();
                return;
            }

            if (player.hasStatusEffect(MRPGCEffects.IGNITED.entry)) {
                ((HudMessages)(Object)this).error(Text.translatable("hud.more_rpg_classes.ignited").formatted(Formatting.RED));
                ci.cancel();
                return;
            }

            if (player.hasStatusEffect(MRPGCEffects.FEAR.entry)) {
                ((HudMessages)(Object)this).error(Text.translatable("hud.more_rpg_classes.feared").formatted(Formatting.RED));
                ci.cancel();
                return;
            }

            if (player.hasStatusEffect(MRPGCEffects.STAGGER.entry)) {
                ((HudMessages)(Object)this).error(Text.translatable("hud.more_rpg_classes.stagger").formatted(Formatting.RED));
                ci.cancel();
                return;
            }
        }
    }
}
