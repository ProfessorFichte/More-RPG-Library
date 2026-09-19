package net.more_rpg_classes.entity.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MRPGCEntityAttributes{

    /// Every attribute this mod owns, in declaration order, keyed by the id it registers under.
    /// Built by `<clinit>`; nothing is written to the registry until {@link #registerAttributes()}
    /// (Fabric) or the Forge `ATTRIBUTE` window iterates {@link #attributesToRegister()}.
    private static final Map<Identifier, EntityAttribute> TO_REGISTER = new LinkedHashMap<>();

    public static final EntityAttribute DAMAGE_REFLECT_MODIFIER = register("damage_reflect_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute LIFESTEAL_MODIFIER = register("lifesteal_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute RAGE_MODIFIER = register("rage_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute SPELL_VAMPIRE = register("spell_vampire", 100.0, 100.0, 1024.0);
    public static final EntityAttribute AIR_FUSE_MODIFIER= register("air_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute ARCANE_FUSE_MODIFIER= register("arcane_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute EARTH_FUSE_MODIFIER= register("earth_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute FIRE_FUSE_MODIFIER= register("fire_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute FROST_FUSE_MODIFIER= register("frost_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute HEALING_FUSE_MODIFIER= register("healing_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute WATER_FUSE_MODIFIER= register("water_fuse_modifier", 100.0, 100.0, 1024.0);
    public static final EntityAttribute BURNING_CHANCE = register("burning_chance", 100.0, 100.0, 200.0);
    public static final EntityAttribute STAGGER_CHANCE = register("stagger_chance", 100.0, 100.0, 200.0);
    public static final EntityAttribute STUN_CHANCE = register("stun_chance", 100.0, 100.0, 200.0);
    public static final EntityAttribute POISON_CHANCE = register("poison_chance", 100.0, 100.0, 200.0);
    public static final EntityAttribute FREEZE_CHANCE = register("freeze_chance", 100.0, 100.0, 200.0);
    public static final EntityAttribute BLEEDING_CHANCE = register("bleeding_chance", 100.0, 100.0, 200.0);
    public static final EntityAttribute ARMOR_PIERCING = register("armor_piercing", 100.0, 100.0, 200.0);
    public static final EntityAttribute TENACITY = register("tenacity", 100.0, 100.0, 200.0);

    private static EntityAttribute register(final String name, double base, double min, double max) {
        EntityAttribute attribute = new ClampedEntityAttribute("attribute.name." + MOD_ID + '.' + name, base, min, max).setTracked(true);
        TO_REGISTER.put(new Identifier(MOD_ID, name), attribute);
        return attribute;
    }

    /// Creation only — the attributes keyed by the id they register under. Forge iterates this from its
    /// `ATTRIBUTE` `RegisterEvent` window, where the vanilla registry is only writable through the
    /// event's helper. Skips ids already present, so it is idempotent.
    public static Map<Identifier, EntityAttribute> attributesToRegister() {
        var toRegister = new LinkedHashMap<Identifier, EntityAttribute>();
        TO_REGISTER.forEach((id, attribute) -> {
            if (Registries.ATTRIBUTE.containsId(id)) { return; }
            toRegister.put(id, attribute);
        });
        return Collections.unmodifiableMap(toRegister);
    }

    /// The vanilla registration path, used on Fabric (from the `EntityAttributes` `<clinit>`-TAIL mixin).
    public static void registerAttributes(){
        attributesToRegister().forEach((id, attribute) -> Registry.register(Registries.ATTRIBUTE, id, attribute));
    }
}
