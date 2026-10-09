package com.mrpg_lib.fabric;

import com.mrpg_lib.platform.MrpgPlatform;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricPlatformUtil implements MrpgPlatform.Util {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
