package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.util.AllyHelper;
import com.mrpg_lib.MRPGCMod;
import com.mrpg_lib.compat.CompatHooks;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.spell_engine.loot.BindSpellFromPoolsLootFunction;
import com.mrpg_lib.compat.spell_engine.loot.SpecificSpellScrollPoolLootFunction;
import com.mrpg_lib.compat.spell_power.SpellPowerCompat;
import com.mrpg_lib.effect.ControlEnemyStatusEffect;
import com.mrpg_lib.effect.StealthStatusEffect;
import com.mrpg_lib.entity.FriendlyLightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.spell_engine.api.effect.Synchronized;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.internals.target.EntityRelation;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.SoundHelper;
import net.tiny_config.ConfigManager;

import java.util.List;

public final class SpellEngineCompat {
    public static final ConfigManager<WeaknessConfig> weaknessConfig = new ConfigManager<>
            ("elemental_weaknesses", MoreSpellSchoolWeakness.createDefault())
            .builder()
            .setDirectory(MRPGCMod.MOD_ID)
            .sanitize(true)
            .validate(WeaknessConfig::isValid)
            .build();

    private SpellEngineCompat() {
    }

    public static void init() {
        weaknessConfig.refresh();
        installBridges();
        CustomSpellImpacts.registerCustomImpacts();
        MrpgEntityRelationMatcher.register();
        if (MrpgCompat.SPELL_POWER) {
            SpellPowerCompat.init();
        }
        CustomSpellEntityPredicate.registerCustomPredicates();
    }

    public static void registerLootFunctions() {
        Registry.register(Registries.LOOT_FUNCTION_TYPE, SpecificSpellScrollPoolLootFunction.ID, SpecificSpellScrollPoolLootFunction.TYPE);
        Registry.register(Registries.LOOT_FUNCTION_TYPE, BindSpellFromPoolsLootFunction.ID, BindSpellFromPoolsLootFunction.TYPE);
    }

    private static void installBridges() {
        AllyHelper.setRelationBridge((caster, target, harmful) -> EntityRelations.actionAllowed(
                SpellTarget.FocusMode.DIRECT,
                harmful ? SpellTarget.Intent.HARMFUL : SpellTarget.Intent.HELPFUL,
                caster, target));
        ControlEnemyStatusEffect.setAllyCheck((owner, candidate) -> {
            var relation = EntityRelations.getRelation(owner, candidate);
            return relation == EntityRelation.ALLY || relation == EntityRelation.FRIENDLY;
        });
        FriendlyLightningEntity.setFriendlyCheck((owner, target) -> {
            try {
                var relation = EntityRelations.getRelation(owner, target);
                return relation == EntityRelation.ALLY;
            } catch (Exception e) {
                return !EntityRelations.allowedToHurt(owner, target);
            }
        });
        StealthStatusEffect.setActiveCheck(entity -> {
            for (var effect : ((Synchronized.Provider) entity).SpellEngine_syncedStatusEffects()) {
                if (effect.effect() instanceof StealthStatusEffect) {
                    return true;
                }
            }
            return false;
        });
        StealthStatusEffect.setFx(new StealthStatusEffect.Fx() {
            @Override
            public void playSound(LivingEntity entity, Identifier soundId) {
                SoundHelper.playSoundEvent(entity.getWorld(), entity, SoundEvent.of(soundId));
            }

            @Override
            public void popParticles(LivingEntity entity, Object particleGroup) {
                ParticleHelper.sendBatches(entity, List.of((ParticleGroup) particleGroup));
            }
        });
        CompatHooks.setFx(new SpellEngineFx());
        CompatHooks.setStunEffect(() -> net.spell_engine.api.effect.SpellEngineEffects.STUN.entry);
        CompatHooks.setBleedEffect(() -> net.spell_engine.api.effect.SpellEngineEffects.BLEED.entry);
    }
}
