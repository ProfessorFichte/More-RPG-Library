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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

        MrpgForgeNetwork.register();
        MRPGCNetworking.setSender(MrpgForgeNetwork::sendToPlayer);

        MRPGCMod.init();
        MRPGCMod.registerEvents();

        MoreSpellSchools.registerSchools();

        modBus.addListener(ForgeMod::register);
        modBus.addListener(ForgeMod::commonSetup);
        modBus.addListener(ForgeMod::buildTabContents);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MoreSpellSchools::refreshRangedAttribute);
    }

    // Goes through the helper on purpose, on Forge 47.0-47.3 a plain Registry.register throws "Can not register to a locked registry".
    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.ATTRIBUTE, helper ->
                MRPGCEntityAttributes.attributesToRegister().forEach(helper::register));

        event.register(RegistryKeys.SOUND_EVENT, helper -> {
            MRPGLibSounds.soundsToRegister().forEach(helper::register);
            MRPGLibSounds.linkEntries();
        });

        event.register(RegistryKeys.PARTICLE_TYPE, helper ->
                MoreParticles.particlesToRegister().forEach(helper::register));

        event.register(RegistryKeys.ITEM, helper -> {
            MRPGCItems.itemsToRegister().forEach(helper::register);
            if (MRPGCMod.smithingIngredientsEnabled()) {
                SmithingIngredients.itemsToRegister().forEach(helper::register);
            }
        });

        event.register(RegistryKeys.STATUS_EFFECT, helper -> {
            MRPGCEffects.effectsToRegister(MRPGCMod.effectsConfig.value).forEach(helper::register);
            Effects.linkEntries(MRPGCEffects.entries);
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

        event.register(RegistryKeys.ITEM_GROUP, helper ->
                MRPGCItemGroups.groupsToRegister().forEach(helper::register));
    }

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
