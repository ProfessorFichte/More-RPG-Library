package net.more_rpg_classes.entity.attribute;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.spell_power.api.SpellSchools;

public class ProjectileAttributeData {
    /**
     * Data class storing shooter's fuse and chance-based attribute values
     */
    // Fuse modifiers
    public final float airFuse;
    public final float arcaneFuse;
    public final float earthFuse;
    public final float fireFuse;
    public final float frostFuse;
    public final float healingFuse;
    public final float waterFuse;
    // Spell power values
    public final float airPower;
    public final float arcanePower;
    public final float earthPower;
    public final float firePower;
    public final float frostPower;
    public final float healingPower;
    public final float waterPower;
    // Chance-based effects
    public final float burningChance;
    public final float staggerChance;
    public final float stunChance;
    public final float poisonChance;
    public final float freezeChance;
    public final float bleedingChance;
    // Attack damage for amplifier calculation
    public final float attackDamage;

    public ProjectileAttributeData(
        float airFuse, float arcaneFuse, float earthFuse, float fireFuse,
        float frostFuse, float healingFuse, float waterFuse,
        float airPower, float arcanePower, float earthPower, float firePower,
        float frostPower, float healingPower, float waterPower,
        float burningChance, float staggerChance, float stunChance,
        float poisonChance, float freezeChance, float bleedingChance,
        float attackDamage
    ) {
        this.airFuse = airFuse;
        this.arcaneFuse = arcaneFuse;
        this.earthFuse = earthFuse;
        this.fireFuse = fireFuse;
        this.frostFuse = frostFuse;
        this.healingFuse = healingFuse;
        this.waterFuse = waterFuse;

        this.airPower = airPower;
        this.arcanePower = arcanePower;
        this.earthPower = earthPower;
        this.firePower = firePower;
        this.frostPower = frostPower;
        this.healingPower = healingPower;
        this.waterPower = waterPower;

        this.burningChance = burningChance;
        this.staggerChance = staggerChance;
        this.stunChance = stunChance;
        this.poisonChance = poisonChance;
        this.freezeChance = freezeChance;
        this.bleedingChance = bleedingChance;

        this.attackDamage = attackDamage;
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.putFloat("mrpgc_air_fuse", airFuse);
        nbt.putFloat("mrpgc_arcane_fuse", arcaneFuse);
        nbt.putFloat("mrpgc_earth_fuse", earthFuse);
        nbt.putFloat("mrpgc_fire_fuse", fireFuse);
        nbt.putFloat("mrpgc_frost_fuse", frostFuse);
        nbt.putFloat("mrpgc_healing_fuse", healingFuse);
        nbt.putFloat("mrpgc_water_fuse", waterFuse);

        nbt.putFloat("mrpgc_air_power", airPower);
        nbt.putFloat("mrpgc_arcane_power", arcanePower);
        nbt.putFloat("mrpgc_earth_power", earthPower);
        nbt.putFloat("mrpgc_fire_power", firePower);
        nbt.putFloat("mrpgc_frost_power", frostPower);
        nbt.putFloat("mrpgc_healing_power", healingPower);
        nbt.putFloat("mrpgc_water_power", waterPower);

        nbt.putFloat("mrpgc_burning_chance", burningChance);
        nbt.putFloat("mrpgc_stagger_chance", staggerChance);
        nbt.putFloat("mrpgc_stun_chance", stunChance);
        nbt.putFloat("mrpgc_poison_chance", poisonChance);
        nbt.putFloat("mrpgc_freeze_chance", freezeChance);
        nbt.putFloat("mrpgc_bleeding_chance", bleedingChance);

        nbt.putFloat("mrpgc_attack_damage", attackDamage);
    }

    public static ProjectileAttributeData readFromNbt(NbtCompound nbt) {
        return new ProjectileAttributeData(
            nbt.getFloat("mrpgc_air_fuse"),
            nbt.getFloat("mrpgc_arcane_fuse"),
            nbt.getFloat("mrpgc_earth_fuse"),
            nbt.getFloat("mrpgc_fire_fuse"),
            nbt.getFloat("mrpgc_frost_fuse"),
            nbt.getFloat("mrpgc_healing_fuse"),
            nbt.getFloat("mrpgc_water_fuse"),

            nbt.getFloat("mrpgc_air_power"),
            nbt.getFloat("mrpgc_arcane_power"),
            nbt.getFloat("mrpgc_earth_power"),
            nbt.getFloat("mrpgc_fire_power"),
            nbt.getFloat("mrpgc_frost_power"),
            nbt.getFloat("mrpgc_healing_power"),
            nbt.getFloat("mrpgc_water_power"),

            nbt.getFloat("mrpgc_burning_chance"),
            nbt.getFloat("mrpgc_stagger_chance"),
            nbt.getFloat("mrpgc_stun_chance"),
            nbt.getFloat("mrpgc_poison_chance"),
            nbt.getFloat("mrpgc_freeze_chance"),
            nbt.getFloat("mrpgc_bleeding_chance"),

            nbt.getFloat("mrpgc_attack_damage")
        );
    }

    private static RegistryEntry<EntityAttribute> getRangedDamageAttribute() {
        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            return EntityAttributes_RangedWeapon.DAMAGE.entry;
        } else {
            return EntityAttributes.GENERIC_ATTACK_DAMAGE;
        }
    }

    public static ProjectileAttributeData fromPlayer(PlayerEntity player) {
        return new ProjectileAttributeData(
            getAttributeValue(player, MRPGCEntityAttributes.AIR_FUSE_MODIFIER),
            getAttributeValue(player, MRPGCEntityAttributes.ARCANE_FUSE_MODIFIER),
            getAttributeValue(player, MRPGCEntityAttributes.EARTH_FUSE_MODIFIER),
            getAttributeValue(player, MRPGCEntityAttributes.FIRE_FUSE_MODIFIER),
            getAttributeValue(player, MRPGCEntityAttributes.FROST_FUSE_MODIFIER),
            getAttributeValue(player, MRPGCEntityAttributes.HEALING_FUSE_MODIFIER),
            getAttributeValue(player, MRPGCEntityAttributes.WATER_FUSE_MODIFIER),

            (float) player.getAttributeValue(MoreSpellSchools.AIR.attributeEntry),
            (float) player.getAttributeValue(SpellSchools.ARCANE.attributeEntry),
            (float) player.getAttributeValue(MoreSpellSchools.EARTH.attributeEntry),
            (float) player.getAttributeValue(SpellSchools.FIRE.attributeEntry),
            (float) player.getAttributeValue(SpellSchools.FROST.attributeEntry),
            (float) player.getAttributeValue(SpellSchools.HEALING.attributeEntry),
            (float) player.getAttributeValue(MoreSpellSchools.WATER.attributeEntry),

            getAttributeValue(player, MRPGCEntityAttributes.BURNING_CHANCE),
            getAttributeValue(player, MRPGCEntityAttributes.STAGGER_CHANCE),
            getAttributeValue(player, MRPGCEntityAttributes.STUN_CHANCE),
            getAttributeValue(player, MRPGCEntityAttributes.POISON_CHANCE),
            getAttributeValue(player, MRPGCEntityAttributes.FREEZE_CHANCE),
            getAttributeValue(player, MRPGCEntityAttributes.BLEEDING_CHANCE),

            (float) player.getAttributeValue(getRangedDamageAttribute())
        );
    }

    private static float getAttributeValue(PlayerEntity player, RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        return instance != null ? (float) instance.getValue() : 100.0f;
    }
}
