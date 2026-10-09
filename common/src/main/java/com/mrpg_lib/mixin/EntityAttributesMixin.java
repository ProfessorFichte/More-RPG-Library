package com.mrpg_lib.mixin;

import com.mrpg_lib.entity.attribute.MRPGCEntityAttributes;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityAttributes.class)
public class EntityAttributesMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_MRPGC_attributes(CallbackInfo ci) {
        MRPGCEntityAttributes.registerAttributes();
    }
}
