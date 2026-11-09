package com.mrpg_lib.fabric.worldgen;

import com.mrpg_lib.worldgen.modifier.StructureBlockModifiers;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public class FabricStructureBlockModifiers extends StructureBlockModifiers implements IdentifiableResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
