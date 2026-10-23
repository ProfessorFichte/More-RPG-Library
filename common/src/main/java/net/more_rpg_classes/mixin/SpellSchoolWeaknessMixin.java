package net.more_rpg_classes.mixin;

import net.minecraft.util.Identifier;
import net.more_rpg_classes.custom.MoreSpellSchoolWeakness;
import net.spell_engine.api.spell.weakness.ScopedWeakness;
import net.spell_engine.api.spell.weakness.SpellSchoolWeakness;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SpellSchoolWeakness.class)
public class SpellSchoolWeaknessMixin {

    @Inject(method = "getWeaknesses(Lnet/minecraft/util/Identifier;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void mrpgLib$appendWeaknesses(Identifier schoolId, CallbackInfoReturnable<List<ScopedWeakness>> cir) {
        var extra = MoreSpellSchoolWeakness.getWeaknesses(schoolId);
        if (extra.isEmpty()) return;
        var merged = new ArrayList<>(cir.getReturnValue());
        merged.addAll(extra);
        cir.setReturnValue(merged);
    }
}
