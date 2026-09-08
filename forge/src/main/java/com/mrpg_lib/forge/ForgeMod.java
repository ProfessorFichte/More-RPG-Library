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
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.entity.attribute.MRPGCEntityAttributes;
import net.more_rpg_classes.compat.armory_rpgs.SmithingIngredients;
import net.more_rpg_classes.item.MRPGCItemGroups;
import net.more_rpg_classes.item.MRPGCItems;

@Mod(MRPGCMod.MOD_ID)
public final class ForgeMod {
    public ForgeMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Networking first: common init may already want to send.
        MrpgForgeNetwork.register();

        MRPGCMod.init();
        MRPGCMod.registerEvents();

        // Fabric drives these from `<clinit>`-TAIL mixins; on Forge a registration mixin would hit a
        // locked registry, so the schools are added here (before Spell Power's ATTRIBUTES window) and
        // the attributes inside their own window below.
        MoreSpellSchools.registerSchools();

        modBus.addListener(ForgeMod::register);
        modBus.addListener(ForgeMod::buildTabContents);
    }

    /// Forge 47 hands out one registration window per registry; each `register` call must stay inside
    /// the window for the registry it touches (the registries are locked outside it).
    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.ATTRIBUTE, reg -> MRPGCEntityAttributes.registerAttributes());
        event.register(RegistryKeys.LOOT_FUNCTION_TYPE, reg -> MRPGCMod.registerLootFunction());
        event.register(RegistryKeys.SOUND_EVENT, reg -> MRPGCMod.registerSounds());
        event.register(RegistryKeys.ITEM, reg -> MRPGCMod.registerItems());
        event.register(RegistryKeys.STATUS_EFFECT, reg -> MRPGCMod.registerEffects());
        event.register(RegistryKeys.ENCHANTMENT, reg -> MRPGCMod.registerEnchantments());
        event.register(RegistryKeys.PARTICLE_TYPE, reg -> MoreParticles.register());
        event.register(RegistryKeys.ENTITY_TYPE, reg -> MRPGCMod.registerEntities());
        event.register(RegistryKeys.STRUCTURE_TYPE, reg -> MRPGCMod.registerStructures());
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
