package net.more_rpg_classes.damage;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class PoisonDamageSource extends DamageSource {
    public PoisonDamageSource(RegistryEntry<DamageType> type) {
        super(type);
    }

    private final String KEY = "death.attack.mrpgc.fatal_poison";

    @Override
    public Text getDeathMessage(LivingEntity killed) {
        if(killed instanceof PlayerEntity player && player.getMainHandStack().isIn(ConventionalItemTags.MUSHROOMS)) {
            return Text.translatable(KEY + ".teemo", killed.getDisplayName());
        }
        return Text.translatable(KEY, killed.getDisplayName());
    }
}
