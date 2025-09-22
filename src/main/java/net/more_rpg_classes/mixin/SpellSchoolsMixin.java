package net.more_rpg_classes.mixin;

import net.more_rpg_classes.custom.MoreSpellSchools;
import net.spell_power.api.SpellSchools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpellSchools.class)
public class SpellSchoolsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_MoreMagic(CallbackInfo ci) {
        SpellSchools.register(MoreSpellSchools.WATER); // Trigger registration
        SpellSchools.register(MoreSpellSchools.AIR); // Trigger registration
        SpellSchools.register(MoreSpellSchools.EARTH); // Trigger registration
        SpellSchools.register(MoreSpellSchools.NATURE); // Trigger registration
    }
}
