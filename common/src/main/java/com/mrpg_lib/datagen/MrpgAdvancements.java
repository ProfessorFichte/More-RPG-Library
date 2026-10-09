package com.mrpg_lib.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public class MrpgAdvancements extends FabricAdvancementProvider {
    public static final String ROOT_TITLE_KEY = "advancements." + MOD_ID + ".root.title";
    public static final String ROOT_DESCRIPTION_KEY = "advancements." + MOD_ID + ".root.description";

    public MrpgAdvancements(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup registryLookup, Consumer<AdvancementEntry> consumer) {
        Advancement.Builder.createUntelemetered()
                .display(
                        Items.ENCHANTED_BOOK,
                        Text.translatable(ROOT_TITLE_KEY),
                        Text.translatable(ROOT_DESCRIPTION_KEY),
                        Identifier.ofVanilla("textures/block/calcite.png"),
                        AdvancementFrame.TASK,
                        false,
                        false,
                        false
                )
                .criterion("always", Criteria.TICK.create(new TickCriterion.Conditions(Optional.empty())))
                .build(consumer, Identifier.of(MOD_ID, "root").toString());
    }
}
