package net.more_rpg_classes.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class BleedingDamageSource extends DamageSource {

    private final String KEY = "death.attack.mrpgc.bleeding";

    public BleedingDamageSource(RegistryEntry<DamageType> type) {
        super(type);
    }

    @Override
    public Text getDeathMessage(LivingEntity killed) {
        return Text.translatable(KEY, killed.getDisplayName());
    }
}
