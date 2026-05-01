package net.more_rpg_classes.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpellBehaviorRegistry {

    @FunctionalInterface
    public interface SpellCastCondition {
        boolean shouldCast(MobEntity caster, @Nullable LivingEntity target, Identifier spellId);
    }

    private static final Map<Identifier, SpellCastCondition> conditions = new HashMap<>();

    public static void register(Identifier spellId, SpellCastCondition condition) {
        conditions.put(spellId, condition);
    }

    public static boolean shouldCast(MobEntity caster, @Nullable LivingEntity target, Identifier spellId) {
        SpellCastCondition condition = conditions.get(spellId);
        return condition == null || condition.shouldCast(caster, target, spellId);
    }
}
