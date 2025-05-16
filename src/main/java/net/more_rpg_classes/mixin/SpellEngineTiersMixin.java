package net.more_rpg_classes.mixin;

import net.spell_engine.api.item.Tiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Tiers.class)
public class SpellEngineTiersMixin {

    @Inject(method = "unsafe(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true,remap=false)
    private static void mrpgc_tiers(String name, CallbackInfoReturnable<Integer> cir){
        if         (name.contains("elder_guardian")
                || name.contains("ender_dragon")
                || name.contains("glacial")
                || name.contains("wither")
                    ) {cir.setReturnValue(4);
        }
    }
}
