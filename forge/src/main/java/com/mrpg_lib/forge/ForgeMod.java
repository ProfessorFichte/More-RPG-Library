package com.mrpg_lib.forge;

import com.mrpg_lib.forge.network.MrpgForgeNetwork;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.network.MRPGCNetworking;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.enchantment.MRPGCEnchantments;
import net.more_rpg_classes.entity.MRPGCEntities;
import net.more_rpg_classes.item.MRPGCItemGroups;
import net.more_rpg_classes.item.MRPGCItems;
import net.more_rpg_classes.sounds.MRPGLibSounds;
import net.more_rpg_classes.worldgen.ModStructureProcessorTypes;
import net.more_rpg_classes.worldgen.ModStructureTypes;
import net.spell_engine.api.effect.Effects;

@Mod(MRPGCMod.MOD_ID)
public final class ForgeMod {
    public ForgeMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Networking first: common init may already want to send.
        MrpgForgeNetwork.register();
        MRPGCNetworking.setSender(MrpgForgeNetwork::sendToPlayer);

        MRPGCMod.init();
        MRPGCMod.registerEvents();

        // Fabric drives these from `<clinit>`-TAIL mixins; on Forge a registration mixin would hit a
        // locked registry, so the schools are added here (before Spell Power's ATTRIBUTES window) and
        // the attributes inside their own window below.
        MoreSpellSchools.registerSchools();

        modBus.addListener(ForgeMod::register);
        modBus.addListener(ForgeMod::buildTabContents);
    }

    /// Forge 47 hands out one registration window per registry, and the vanilla registry behind each
    /// Forge-wrapped one stays **locked** outside that window on Forge 47.0–47.3 (the lock is only cleared
    /// for the plain `Registry.register` path from 47.4.0 on). `mods.toml` declares `[47,)`, so every write
    /// has to go through the helper this event hands out, iterating the same content `common` exposes for
    /// the Fabric path. The duplication is deliberate — the workaround stays inside `forge/`.
    ///
    /// Every block is declared unconditionally: `event.register` is a no-op unless its key matches the
    /// event's registry, and a mismatched key is silent, so each registry gets its own block by name.
    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.ATTRIBUTE, helper ->
                MRPGCEntityAttributes.attributesToRegister().forEach(helper::register));

        event.register(RegistryKeys.SOUND_EVENT, helper -> {
            MRPGLibSounds.soundsToRegister().forEach(helper::register);
            // The helper returns void where `Registry.registerReference` returned the entry `Entry#entry()`
            // exposes — read them back so both loaders end up in the same state.
            MRPGLibSounds.linkEntries();
        });

        event.register(RegistryKeys.PARTICLE_TYPE, helper ->
                MoreParticles.particlesToRegister().forEach(helper::register));

        event.register(RegistryKeys.ITEM, helper -> {
            MRPGCItems.itemsToRegister().forEach(helper::register);
            if (MRPGCMod.smithingIngredientsEnabled()) {
                // `itemsToRegister` also does the conditional entry building the loop alone would miss.
                SmithingIngredients.itemsToRegister().forEach(helper::register);
            }
        });

        event.register(RegistryKeys.STATUS_EFFECT, helper -> {
            MRPGCEffects.effectsToRegister(MRPGCMod.effectsConfig.value).forEach(helper::register);
            Effects.linkEntries(MRPGCEffects.entries);
            // `registerEffects()` saves the config after registering; that is part of the contract.
            MRPGCMod.effectsConfig.save();
        });

        event.register(RegistryKeys.ENCHANTMENT, helper ->
                MRPGCEnchantments.enchantmentsToRegister().forEach(helper::register));

        event.register(RegistryKeys.ENTITY_TYPE, helper ->
                MRPGCEntities.entitiesToRegister().forEach(helper::register));

        event.register(RegistryKeys.LOOT_FUNCTION_TYPE, helper ->
                MRPGCMod.lootFunctionsToRegister().forEach(helper::register));

        event.register(RegistryKeys.LOOT_POOL_ENTRY_TYPE, helper ->
                MRPGCMod.lootPoolEntryTypesToRegister().forEach(helper::register));

        event.register(RegistryKeys.STRUCTURE_TYPE, helper ->
                ModStructureTypes.typesToRegister().forEach(helper::register));

        event.register(RegistryKeys.STRUCTURE_PROCESSOR, helper ->
                ModStructureProcessorTypes.typesToRegister().forEach(helper::register));

        // NOT in the ITEM block: `creative_mode_tab` is event 65, `item` is event 7.
        event.register(RegistryKeys.ITEM_GROUP, helper ->
                MRPGCItemGroups.groupsToRegister().forEach(helper::register));
    }

    /// Forge 47 has no insertAfter/insertFirst helpers on the event; the backing map does the ordering.
    private static void buildTabContents(BuildCreativeModeTabContentsEvent event) {
        var tabKey = event.getTabKey();
        if (tabKey.equals(ItemGroups.INGREDIENTS)) {
            ItemStack anchor = new ItemStack(Items.RABBIT_HIDE);
            for (Item item : MRPGCItems.INGREDIENTS_GROUP_ITEMS) {
                ItemStack stack = new ItemStack(item);
                event.getEntries().putAfter(anchor, stack, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                anchor = stack;
            }
        } else if (tabKey.equals(ItemGroups.COMBAT)) {
            for (Item item : MRPGCItems.COMBAT_GROUP_ITEMS) {
                event.accept(() -> item);
            }
        } else if (tabKey.equals(MRPGCItemGroups.ARMORY_KEY)) {
            var items = SmithingIngredients.armoryGroupItems();
            for (int i = items.size() - 1; i >= 0; i--) {
                event.getEntries().putFirst(new ItemStack(items.get(i)), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}
