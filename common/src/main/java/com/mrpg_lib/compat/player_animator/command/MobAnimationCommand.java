package com.mrpg_lib.compat.player_animator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrpg_lib.compat.player_animator.api.MobAnimationLayer;
import com.mrpg_lib.compat.player_animator.api.MobAnimationOptions;
import com.mrpg_lib.compat.player_animator.api.MobAnimations;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MobAnimationCommand {
    private static final int PERMISSION_LEVEL = 2;
    private static final int STOP_FADE_TICKS = 5;

    private MobAnimationCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mrpg_anim")
                .requires(source -> source.hasPermissionLevel(PERMISSION_LEVEL))
                .then(argument("targets", EntityArgumentType.entities())
                        .then(literal("stop").executes(MobAnimationCommand::stop))
                        .then(argument("animation", IdentifierArgumentType.identifier())
                                .executes(context -> play(context, 1.0F))
                                .then(argument("speed", FloatArgumentType.floatArg(0.05F, 10.0F))
                                        .executes(context -> play(context, FloatArgumentType.getFloat(context, "speed")))))));
    }

    private static int play(CommandContext<ServerCommandSource> context, float speed) throws CommandSyntaxException {
        if (!checkAvailable(context.getSource())) return 0;
        Identifier animation = IdentifierArgumentType.getIdentifier(context, "animation");
        var options = MobAnimationOptions.DEFAULT
                .withLayer(MobAnimationLayer.CASTING)
                .withSpeed(speed)
                .withPersistent(true);
        int count = 0;
        for (Entity entity : EntityArgumentType.getEntities(context, "targets")) {
            if (entity instanceof LivingEntity living) {
                MobAnimations.play(living, animation, options);
                count++;
            }
        }
        return report(context.getSource(), count, "Playing " + animation + " on ");
    }

    private static int stop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!checkAvailable(context.getSource())) return 0;
        int count = 0;
        for (Entity entity : EntityArgumentType.getEntities(context, "targets")) {
            if (entity instanceof LivingEntity living) {
                MobAnimations.stopAll(living, STOP_FADE_TICKS);
                count++;
            }
        }
        return report(context.getSource(), count, "Stopped animations on ");
    }

    private static boolean checkAvailable(ServerCommandSource source) {
        if (MobAnimations.isAvailable()) return true;
        source.sendError(Text.literal("playerAnimator is not loaded, mob animations are unavailable"));
        return false;
    }

    private static int report(ServerCommandSource source, int count, String prefix) {
        if (count == 0) {
            source.sendError(Text.literal("No living entities matched"));
            return 0;
        }
        source.sendFeedback(() -> Text.literal(prefix + count + " entities"), true);
        return count;
    }
}
