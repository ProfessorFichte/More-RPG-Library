package com.mrpg_lib.compat.spell_engine;

import com.google.common.base.Suppliers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.spell_engine.Platform;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.internals.SpellExecution;
import net.spell_engine.internals.delivery.SpellStash;
import net.spell_engine.internals.delivery.arrow.ArrowExtension;
import net.spell_engine.internals.delivery.arrow.ArrowHelper;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.PatternMatching;
import net.spell_engine.utils.StatusEffectUtil;
import net.spell_power.api.SpellPower;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MobStashHelper {
    private MobStashHelper() {
    }

    public static void onMobArrowShot(LivingEntity shooter, PersistentProjectileEntity arrow, boolean firedBySpell) {
        if (shooter instanceof PlayerEntity || !(shooter.getWorld() instanceof ServerWorld world)) return;
        if (!(arrow instanceof ArrowExtension extension)) return;

        Map<StatusEffectInstance, StatusEffectUtil.Diff> changes = new HashMap<>();
        var trackers = Suppliers.memoize(() -> Platform.tracking(shooter));
        for (var instance : List.copyOf(shooter.getActiveStatusEffects().values())) {
            if (!(instance.getEffectType().value() instanceof SpellStash stash)) continue;
            for (var stashed : stash.getStashedSpells()) {
                var spellEntry = stashed.spell();
                if (spellEntry == null || stashed.triggers() == null) continue;
                for (var trigger : stashed.triggers()) {
                    if (trigger == null || !arrowShotTriggerMatches(trigger, spellEntry.getKey().get().getValue(), shooter, arrow, firedBySpell)) continue;

                    int consume = stashed.consume();
                    var pending = changes.get(instance);
                    int available = pending != null ? pending.newAmplifier() : instance.getAmplifier();
                    if (!stashed.consume_any_stacks() && (available + 1) < consume) continue;

                    switch (stashed.impactMode()) {
                        case TRANSFER -> ArrowHelper.onArrowShot(extension, shooter, spellEntry, trackers);
                        case PERFORM -> {
                            var power = SpellPower.getSpellPower(spellEntry.value().school, shooter);
                            var context = new SpellExecution.ImpactContext(1F, 1F, null, power, SpellTarget.FocusMode.DIRECT, 0)
                                    .position(shooter.getPos());
                            SpellImpacts.performImpacts(world, shooter, shooter, shooter, spellEntry, spellEntry.value().impacts, context);
                        }
                    }

                    if (consume != 0) {
                        changes.put(instance, new StatusEffectUtil.Diff(instance, available - consume, stashed.delayConsume() ? 1 : 0));
                    }
                    break;
                }
            }
        }
        StatusEffectUtil.applyChanges(shooter, List.copyOf(changes.values()));
    }

    private static boolean arrowShotTriggerMatches(Spell.Trigger trigger, Identifier spellId, LivingEntity shooter,
                                                   PersistentProjectileEntity arrow, boolean firedBySpell) {
        if (trigger.type != Spell.Trigger.Type.ARROW_SHOT || trigger.stage != Spell.Trigger.Stage.POST) return false;
        if (trigger.chance < 1 && shooter.getRandom().nextFloat() > trigger.chance) return false;
        if (trigger.caster_conditions != null) {
            for (var condition : trigger.caster_conditions) {
                if (!SpellTarget.evaluate(shooter, null, condition)) return false;
            }
        }
        if (trigger.equipment_condition != null) {
            var container = SpellContainerHelper.containerFromItemStack(shooter.getEquippedStack(trigger.equipment_condition));
            if (container == null || !container.contains(spellId)) return false;
        }
        if (trigger.weapon_condition != null) {
            var weapon = arrow.getWeaponStack();
            if (weapon == null || weapon.isEmpty()
                    || !PatternMatching.matches(weapon.getItem().getRegistryEntry(), RegistryKeys.ITEM, trigger.weapon_condition)) {
                return false;
            }
        }
        var condition = trigger.arrow_shot;
        return condition == null || condition.from_spell == null || condition.from_spell == firedBySpell;
    }
}
