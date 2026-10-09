package com.mrpg_lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrpg_lib.compat.MrpgCompat;
import com.mrpg_lib.compat.better_combat.BetterCombatCompat;
import com.mrpg_lib.compat.combat_roll.CombatRollCompat;
import com.mrpg_lib.compat.spell_engine.SpellCastTestGoals;
import com.mrpg_lib.entity.TestSpellCasters;
import com.mrpg_lib.entity.goal.MobGoals;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MobGoalCommand {
    private static final int PERMISSION_LEVEL = 2;
    private static final Map<MobEntity, List<Goal>> ADDED = new WeakHashMap<>();
    private static final int MAX_SHOTS = 8;
    private static final double SHOT_DISTANCE = 8.0;
    private static final float SHOT_SPEED = 1.6F;
    private static final CombatRollCompat.RollSettings ROLL_DODGE_SETTINGS = CombatRollCompat.RollSettings.DEFAULT
            .withDefensiveHealthThreshold(0.0F)
            .withDodgeProjectiles(true);
    private static final CombatRollCompat.RollSettings ROLL_TEST_SETTINGS = CombatRollCompat.RollSettings.DEFAULT
            .withEngage(true, 5.0, 0.3F)
            .withDodgeProjectiles(true);

    private MobGoalCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mrpg_goal")
                .requires(source -> source.hasPermissionLevel(PERMISSION_LEVEL))
                .then(argument("targets", EntityArgumentType.entities())
                        .then(literal("bettercombat").executes(context -> addToTargets(context, "Better Combat melee", 1, MobGoalCommand::betterCombat)))
                        .then(literal("combatroll")
                                .executes(context -> addToTargets(context, "Combat Roll", 0, MobGoalCommand::combatRoll))
                                .then(literal("dodge").executes(context -> addToTargets(context, "Combat Roll dodge", 0, MobGoalCommand::combatRollDodge))))
                        .then(literal("shoot")
                                .executes(context -> shoot(context, 1))
                                .then(argument("count", IntegerArgumentType.integer(1, MAX_SHOTS))
                                        .executes(context -> shoot(context, IntegerArgumentType.getInteger(context, "count")))))
                        .then(literal("spellcast")
                                .then(argument("spell", IdentifierArgumentType.identifier())
                                        .executes(context -> addToTargets(context, "spell cast", 1, MobGoalCommand::spellCast))))
                        .then(literal("clear").executes(MobGoalCommand::clear))));
    }

    private static Goal betterCombat(CommandContext<ServerCommandSource> context, MobEntity mob) {
        if (!BetterCombatCompat.isLoaded()) {
            context.getSource().sendError(Text.literal("Better Combat is not loaded, a plain melee goal is used"));
        }
        return mob instanceof PathAwareEntity pathAware ? BetterCombatCompat.createMeleeGoal(pathAware, 1.0, false) : null;
    }

    private static Goal combatRoll(CommandContext<ServerCommandSource> context, MobEntity mob) {
        if (!CombatRollCompat.isLoaded()) {
            context.getSource().sendError(Text.literal("Combat Roll is not loaded"));
            return null;
        }
        return mob instanceof PathAwareEntity pathAware ? CombatRollCompat.createRollGoal(pathAware, ROLL_TEST_SETTINGS).orElse(null) : null;
    }

    private static Goal combatRollDodge(CommandContext<ServerCommandSource> context, MobEntity mob) {
        if (!CombatRollCompat.isLoaded()) {
            context.getSource().sendError(Text.literal("Combat Roll is not loaded"));
            return null;
        }
        return mob instanceof PathAwareEntity pathAware ? CombatRollCompat.createRollGoal(pathAware, ROLL_DODGE_SETTINGS).orElse(null) : null;
    }

    private static int shoot(CommandContext<ServerCommandSource> context, int count) throws CommandSyntaxException {
        int mobs = 0;
        for (Entity entity : EntityArgumentType.getEntities(context, "targets")) {
            if (!(entity instanceof LivingEntity target) || !(target.getWorld() instanceof ServerWorld world)) continue;
            Vec3d aim = target.getPos().add(0.0, target.getHeight() * 0.6, 0.0);
            for (int i = 0; i < count; i++) {
                double angle = Math.toRadians(360.0 * i / count);
                Vec3d origin = aim.add(Math.cos(angle) * SHOT_DISTANCE, 0.5, Math.sin(angle) * SHOT_DISTANCE);
                Vec3d direction = aim.subtract(origin);
                ArrowEntity arrow = new ArrowEntity(world, origin.x, origin.y, origin.z, new ItemStack(Items.ARROW), null);
                arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
                arrow.setVelocity(direction.x, direction.y, direction.z, SHOT_SPEED, 0.0F);
                world.spawnEntity(arrow);
            }
            mobs++;
        }
        return report(context.getSource(), mobs, "Shot " + count + " arrows at ");
    }

    private static Goal spellCast(CommandContext<ServerCommandSource> context, MobEntity mob) {
        if (!MrpgCompat.SPELL_ENGINE) {
            context.getSource().sendError(Text.literal("Spell Engine is not loaded"));
            return null;
        }
        return SpellCastTestGoals.create(mob, IdentifierArgumentType.getIdentifier(context, "spell").toString());
    }

    private static int addToTargets(CommandContext<ServerCommandSource> context, String name, int priority,
                                    GoalFactory factory) throws CommandSyntaxException {
        int count = 0;
        for (Entity entity : EntityArgumentType.getEntities(context, "targets")) {
            if (!(entity instanceof MobEntity mob)) continue;
            Goal goal = factory.apply(context, mob);
            if (goal == null) continue;
            MobGoals.addGoal(mob, priority, goal);
            ADDED.computeIfAbsent(mob, key -> new ArrayList<>()).add(goal);
            count++;
        }
        return report(context.getSource(), count, "Added " + name + " goal to ");
    }

    private static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int count = 0;
        for (Entity entity : EntityArgumentType.getEntities(context, "targets")) {
            if (!(entity instanceof MobEntity mob)) continue;
            List<Goal> goals = ADDED.remove(mob);
            if (goals == null) continue;
            goals.forEach(goal -> {
                MobGoals.removeGoal(mob, goal);
                BetterCombatCompat.onGoalRemoved(goal);
            });
            TestSpellCasters.unmark(mob);
            count++;
        }
        return report(context.getSource(), count, "Removed test goals from ");
    }

    private static int report(ServerCommandSource source, int count, String prefix) {
        if (count == 0) {
            source.sendError(Text.literal("No matching mobs"));
            return 0;
        }
        source.sendFeedback(() -> Text.literal(prefix + count + " mobs"), true);
        return count;
    }

    @FunctionalInterface
    private interface GoalFactory {
        Goal apply(CommandContext<ServerCommandSource> context, MobEntity mob);
    }
}
