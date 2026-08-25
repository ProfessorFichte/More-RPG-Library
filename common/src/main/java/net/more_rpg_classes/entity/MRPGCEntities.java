package net.more_rpg_classes.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MRPGCEntities {

    public static final EntityType<FriendlyLightningEntity> FRIENDLY_LIGHTNING = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "friendly_lightning"),
        EntityType.Builder.<FriendlyLightningEntity>create(FriendlyLightningEntity::new, SpawnGroup.MISC)
            .dimensions(0.0F, 0.0F)
            .maxTrackingRange(16)
            .trackingTickInterval(Integer.MAX_VALUE)
            .build("friendly_lightning")
    );

    public static void register() {
    }
}
