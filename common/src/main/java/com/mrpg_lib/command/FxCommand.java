package com.mrpg_lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mrpg_lib.fx.FxParams;
import com.mrpg_lib.fx.FxShape;
import com.mrpg_lib.fx.MrpgFx;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class FxCommand {
    private static final int PERMISSION_LEVEL = 2;
    private static final double REACH = 32.0;

    private FxCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mrpg_fx")
                .requires(source -> source.hasPermissionLevel(PERMISSION_LEVEL))
                .then(literal("list").executes(FxCommand::list))
                .then(literal("play")
                        .then(argument("effect", IdentifierArgumentType.identifier())
                                .suggests((context, builder) -> CommandSource.suggestIdentifiers(MrpgFx.ids(), builder))
                                .executes(context -> play(context, Float.NaN, 1.0F, 1.0F, null))
                                .then(argument("radius", FloatArgumentType.floatArg(0.05F, 64.0F))
                                        .executes(context -> play(context, FloatArgumentType.getFloat(context, "radius"), 1.0F, 1.0F, null))
                                        .then(argument("scale", FloatArgumentType.floatArg(0.05F, 10.0F))
                                                .executes(context -> play(context, FloatArgumentType.getFloat(context, "radius"),
                                                        FloatArgumentType.getFloat(context, "scale"), 1.0F, null))
                                                .then(argument("intensity", FloatArgumentType.floatArg(0.0F, 10.0F))
                                                        .executes(context -> play(context, FloatArgumentType.getFloat(context, "radius"),
                                                                FloatArgumentType.getFloat(context, "scale"), FloatArgumentType.getFloat(context, "intensity"), null))
                                                        .then(argument("color", StringArgumentType.word())
                                                                .executes(context -> play(context, FloatArgumentType.getFloat(context, "radius"),
                                                                        FloatArgumentType.getFloat(context, "scale"), FloatArgumentType.getFloat(context, "intensity"),
                                                                        StringArgumentType.getString(context, "color"))))))))));
    }

    private static int list(CommandContext<ServerCommandSource> context) {
        var ids = MrpgFx.ids();
        context.getSource().sendFeedback(() -> Text.literal(ids.size() + " MRPG FX effects: " + String.join(", ", ids.stream().map(Object::toString).toList())), false);
        return ids.size();
    }

    private static int play(CommandContext<ServerCommandSource> context, float radius, float scale, float intensity, String color) {
        var source = context.getSource();
        var id = IdentifierArgumentType.getIdentifier(context, "effect");
        var effect = MrpgFx.get(id);
        if (effect == null) {
            source.sendError(Text.literal("Unknown MRPG FX effect " + id));
            return 0;
        }
        var entity = source.getEntity();
        Vec3d origin = source.getPosition();
        FxParams params;
        if (entity != null && !effect.follow()) {
            Vec3d eye = entity.getEyePos();
            Vec3d look = entity.getRotationVector();
            var hit = source.getWorld().raycast(new RaycastContext(eye, eye.add(look.multiply(REACH)),
                    RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));
            Vec3d point = hit.getPos().subtract(look.multiply(0.2));
            boolean beam = effect.layers().stream().anyMatch(layer -> layer.shape() == FxShape.BEAM);
            params = beam
                    ? FxParams.at(eye.add(look.multiply(0.6)).add(0, -0.25, 0)).target(point).direction(look)
                    : FxParams.at(point).direction(look);
        } else {
            params = entity != null ? FxParams.on(entity).direction(entity.getRotationVector()) : FxParams.at(origin);
        }
        if (!Float.isNaN(radius)) params.radius(radius);
        params.scale(scale).intensity(intensity);
        if (color != null) {
            try {
                params.color(Integer.parseInt(color.startsWith("#") ? color.substring(1) : color, 16));
            } catch (NumberFormatException e) {
                source.sendError(Text.literal("Color must be hex, for example #B040F0"));
                return 0;
            }
        }
        MrpgFx.play(source.getWorld(), effect, params);
        source.sendFeedback(() -> Text.literal("Played " + id), false);
        return 1;
    }
}
