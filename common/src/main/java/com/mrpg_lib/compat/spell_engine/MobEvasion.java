package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import com.mrpg_lib.compat.player_animator.api.MobAnimationOptions;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.spell_engine.api.entity.SpellEngineAttributes;
import net.spell_engine.api.event.CombatEvents;

import java.util.function.Predicate;

public final class MobEvasion {
    public static final Identifier DODGE_ANIMATION = Identifier.of("spell_engine", "dodge");

    private static Predicate<LivingEntity> animationFilter = entity -> true;

    private MobEvasion() {
    }

    static void register() {
        CombatEvents.ENTITY_EVASION.register(args -> onEvade(args.entity()));
    }

    public static void setAnimationFilter(Predicate<LivingEntity> filter) {
        animationFilter = filter != null ? filter : entity -> true;
    }

    public static boolean setEvasionChance(LivingEntity entity, double chance) {
        if (!MrpgCompat.SPELL_ENGINE) return false;
        var entry = SpellEngineAttributes.EVASION_CHANCE;
        EntityAttributeInstance instance = entity.getAttributeInstance(entry.entry);
        if (instance == null) return false;
        instance.setBaseValue(entry.baseValue + chance * entry.baseValue);
        return true;
    }

    public static double getEvasionChance(LivingEntity entity) {
        if (!MrpgCompat.SPELL_ENGINE) return 0.0;
        var entry = SpellEngineAttributes.EVASION_CHANCE;
        EntityAttributeInstance instance = entity.getAttributeInstance(entry.entry);
        return instance == null ? 0.0 : entry.asChance(instance.getValue());
    }

    private static void onEvade(LivingEntity entity) {
        if (entity instanceof PlayerEntity || !animationFilter.test(entity)) return;
        MobAnimations.play(entity, DODGE_ANIMATION, MobAnimationOptions.DEFAULT.withLayer(MobAnimationLayer.DODGE));
    }
}
