package com.mrpg_lib.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MrpgMixinPlugin implements IMixinConfigPlugin {
    private static final String SPELL_ENGINE_PACKAGE = ".compat.spell_engine.";
    private static final String SPELL_POWER_PACKAGE = ".compat.spell_power.";
    private static final String PLAYER_ANIMATOR_PACKAGE = ".compat.player_animator.";

    private final ClassLoader loader = MrpgMixinPlugin.class.getClassLoader();

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(SPELL_ENGINE_PACKAGE)) {
            return isPresent("net/spell_engine/Platform.class");
        }
        if (mixinClassName.contains(SPELL_POWER_PACKAGE)) {
            return isPresent("net/spell_power/SpellPowerMod.class");
        }
        if (mixinClassName.contains(PLAYER_ANIMATOR_PACKAGE)) {
            return isPresent("dev/kosmx/playerAnim/api/layered/AnimationStack.class");
        }
        return true;
    }

    private boolean isPresent(String resource) {
        return loader.getResource(resource) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
