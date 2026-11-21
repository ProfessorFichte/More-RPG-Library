package com.mrpg_lib.util;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public final class UnderwaterProjectiles {
    public static final TagKey<Item> WEAPONS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "underwater_ranged_weapons"));
    public static final String TOOLTIP_KEY = "tooltip." + MOD_ID + ".underwater_shooting";
    public static final float WATER_DRAG = 0.99F;

    private UnderwaterProjectiles() {
    }

    public interface Access {
        boolean mrpg$isUnderwaterProjectile();

        void mrpg$setUnderwaterProjectile(boolean underwater);
    }

    public static void enable(PersistentProjectileEntity projectile) {
        ((Access) projectile).mrpg$setUnderwaterProjectile(true);
    }

    public static boolean isEnabled(PersistentProjectileEntity projectile) {
        return ((Access) projectile).mrpg$isUnderwaterProjectile();
    }

    public static boolean isWeapon(@Nullable ItemStack weapon) {
        return weapon != null && !weapon.isEmpty() && weapon.isIn(WEAPONS);
    }

    public static Text tooltip() {
        return Text.translatable(TOOLTIP_KEY).formatted(Formatting.AQUA);
    }
}
