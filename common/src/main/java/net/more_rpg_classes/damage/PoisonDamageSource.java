package net.more_rpg_classes.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PoisonDamageSource extends DamageSource {
    public PoisonDamageSource(RegistryEntry<DamageType> type) {
        super(type);
    }

    private final String KEY = "death.attack.mrpgc.fatal_poison";

    private static final TagKey<Item> MUSHROOMS = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "mushrooms"));

    @Override
    public Text getDeathMessage(LivingEntity killed) {
        if(killed instanceof PlayerEntity player && player.getMainHandStack().isIn(MUSHROOMS)) {
            return Text.translatable(KEY + ".teemo", killed.getDisplayName());
        }
        return Text.translatable(KEY, killed.getDisplayName());
    }
}
