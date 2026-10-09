package com.mrpg_lib.compat.spell_power.mixin;

import com.mrpg_lib.compat.spell_power.MoreSpellSchools;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpellSchools.class)
public class SpellSchoolsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_MoreMagic(CallbackInfo ci) {
        SpellSchools.register(MoreSpellSchools.WATER);
        SpellSchools.register(MoreSpellSchools.AIR);
        SpellSchools.register(MoreSpellSchools.EARTH);
        SpellSchools.register(MoreSpellSchools.NATURE);
    }
}
