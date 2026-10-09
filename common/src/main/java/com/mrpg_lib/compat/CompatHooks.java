package com.mrpg_lib.compat;

import com.mrpg_lib.util.damage.SchoolPowerProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public final class CompatHooks {
    public interface Fuse {
        void apply(LivingEntity attacker, LivingEntity target, boolean particles);
    }

    public interface Fx {
        void lifesteal(LivingEntity entity);

        void fuse(LivingEntity target, int color);

        void freeze(LivingEntity target);

        void poison(LivingEntity target);

        void bleed(LivingEntity target);
    }

    private static final Fx NO_FX = new Fx() {
        @Override
        public void lifesteal(LivingEntity entity) {
        }

        @Override
        public void fuse(LivingEntity target, int color) {
        }

        @Override
        public void freeze(LivingEntity target) {
        }

        @Override
        public void poison(LivingEntity target) {
        }

        @Override
        public void bleed(LivingEntity target) {
        }
    };

    private static Fuse fuse = (attacker, target, particles) -> {
    };
    private static Fx fx = NO_FX;
    private static Supplier<RegistryEntry<StatusEffect>> stunEffect = () -> null;
    private static Supplier<RegistryEntry<StatusEffect>> bleedEffect = () -> null;
    private static Supplier<RegistryEntry<EntityAttribute>> rangedDamageAttribute = () -> null;
    private static ToDoubleFunction<LivingEntity> highestSpellSchoolPower = entity -> 0.0;
    private static SchoolPowerProvider schoolPower = (entity, schoolId) -> 0.0;

    private CompatHooks() {
    }

    public static void setFuse(Fuse fuse) {
        CompatHooks.fuse = fuse;
    }

    public static void setFx(Fx fx) {
        CompatHooks.fx = fx;
    }

    public static void setStunEffect(Supplier<RegistryEntry<StatusEffect>> stunEffect) {
        CompatHooks.stunEffect = stunEffect;
    }

    public static void setBleedEffect(Supplier<RegistryEntry<StatusEffect>> bleedEffect) {
        CompatHooks.bleedEffect = bleedEffect;
    }

    public static void setRangedDamageAttribute(Supplier<RegistryEntry<EntityAttribute>> rangedDamageAttribute) {
        CompatHooks.rangedDamageAttribute = rangedDamageAttribute;
    }

    public static void setHighestSpellSchoolPower(ToDoubleFunction<LivingEntity> highestSpellSchoolPower) {
        CompatHooks.highestSpellSchoolPower = highestSpellSchoolPower;
    }

    public static void setSchoolPower(SchoolPowerProvider schoolPower) {
        CompatHooks.schoolPower = schoolPower;
    }

    public static void applyFuse(LivingEntity attacker, LivingEntity target, boolean particles) {
        fuse.apply(attacker, target, particles);
    }

    public static Fx fx() {
        return fx;
    }

    public static RegistryEntry<StatusEffect> stunEffect() {
        return stunEffect.get();
    }

    public static RegistryEntry<StatusEffect> bleedEffect() {
        return bleedEffect.get();
    }

    public static RegistryEntry<EntityAttribute> rangedDamageAttribute() {
        return rangedDamageAttribute.get();
    }

    public static double highestSpellSchoolPower(LivingEntity entity) {
        return highestSpellSchoolPower.applyAsDouble(entity);
    }

    public static double schoolPower(LivingEntity entity, Identifier schoolId) {
        return schoolPower.power(entity, schoolId);
    }
}
