package net.more_rpg_classes.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import static net.more_rpg_classes.MRPGCMod.MOD_ID;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MRPGCEntities {

    public static final Identifier FRIENDLY_LIGHTNING_ID = new Identifier(MOD_ID, "friendly_lightning");

    public static final EntityType<FriendlyLightningEntity> FRIENDLY_LIGHTNING =
        EntityType.Builder.<FriendlyLightningEntity>create(FriendlyLightningEntity::new, SpawnGroup.MISC)
            .setDimensions(0.0F, 0.0F)
            .maxTrackingRange(16)
            .trackingTickInterval(Integer.MAX_VALUE)
            .build("friendly_lightning");

    /// Creation only — the entity types keyed by the id they register under. Forge iterates this from its
    /// `ENTITY_TYPE` `RegisterEvent` window. Skips ids already present, so it is idempotent.
    public static Map<Identifier, EntityType<?>> entitiesToRegister() {
        var toRegister = new LinkedHashMap<Identifier, EntityType<?>>();
        if (!Registries.ENTITY_TYPE.containsId(FRIENDLY_LIGHTNING_ID)) {
            toRegister.put(FRIENDLY_LIGHTNING_ID, FRIENDLY_LIGHTNING);
        }
        return Collections.unmodifiableMap(toRegister);
    }

    /// The vanilla registration path, used on Fabric.
    public static void register() {
        entitiesToRegister().forEach((id, type) -> Registry.register(Registries.ENTITY_TYPE, id, type));
    }
}
