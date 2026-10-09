package com.mrpg_lib.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mrpg_lib.MRPGCMod.MOD_ID;

public class MrpgParticles implements DataProvider {
    private final DataOutput.PathResolver resolver;

    public MrpgParticles(FabricDataOutput output) {
        this.resolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "particles");
    }

    public static Map<String, List<String>> definitions() {
        Map<String, List<String>> particles = new LinkedHashMap<>();
        particles.put("big_splash", frames("big_splash", 0, 3));
        particles.put("blood_drop", frames("blood_drop_particle"));
        particles.put("bubble", frames("bubble", 0, 5));
        particles.put("bubble_pop", external("minecraft:bubble_pop", 0, 4));
        particles.put("dragon_claw", pingPong("dragon_claw", 0, 3));
        particles.put("dripping_water", frames("splash", 0, 3));
        particles.put("fatal_poison", frames("fatal_poison", 0, 6));
        particles.put("freezing_snowflake", frames("freezing_snowflake"));
        particles.put("fading_mote", frames("freezing_snowflake"));
        particles.put("gust", frames("gust", 0, 11));
        particles.put("gust_emitter", frames("gust", 0, 11));
        particles.put("hot_splash", frames("hot_splash", 0, 3));
        particles.put("ice_trap", frames("ice_trap", 0, 11));
        particles.put("leaf", frames("leaf", 0, 11));
        particles.put("molten_armor", List.of("minecraft:lava"));
        particles.put("music_note", frames("note", 1, 8));
        particles.put("rage_particle", frames("rage", 1, 3));
        particles.put("rainbow_music_note", frames("note", 1, 8));
        particles.put("slash_claw", pingPong("slash_claw", 0, 3));
        particles.put("small_gust", frames("small_gust", 0, 6));
        particles.put("small_thunder", frames("small_thunder", 0, 6));
        particles.put("splash", frames("splash", 0, 3));
        particles.put("star", frames("star"));
        particles.put("stone_explosion", frames("stone_explosion", 0, 4));
        particles.put("stone_particle", frames("stone_particle", 0, 2));
        particles.put("stone_trap", frames("stone_trap", 0, 3));
        particles.put("water_circle", join(frames("watercircle", 1, 1), frames("watercircle", 1, 6)));
        particles.put("water_drop", frames("water_drop_particle"));
        particles.put("water_heal", List.of("spell_engine:magic/heal"));
        particles.put("water_mist", frames("water_mist", 0, 9));
        particles.put("water_splash", join(frames("water_splash", 0, 5), frames("water_splash", 4, 2)));
        particles.put("water_whip", frames("water_whip", 0, 6));
        particles.put("wave", frames("wave", 0, 7));
        particles.put("wind_vacuum", frames("wind_vacuum", 0, 7));
        return particles;
    }

    private static List<String> frames(String name) {
        return List.of(MOD_ID + ":" + name);
    }

    private static List<String> frames(String prefix, int from, int to) {
        var result = new ArrayList<String>();
        int step = from <= to ? 1 : -1;
        for (int i = from; i != to + step; i += step) {
            result.add(MOD_ID + ":" + prefix + "_" + i);
        }
        return result;
    }

    private static List<String> external(String prefix, int from, int to) {
        var result = new ArrayList<String>();
        for (int i = from; i <= to; i++) {
            result.add(prefix + "_" + i);
        }
        return result;
    }

    private static List<String> pingPong(String prefix, int from, int to) {
        return join(frames(prefix, from, to), frames(prefix, to - 1, from));
    }

    @SafeVarargs
    private static List<String> join(List<String>... parts) {
        var result = new ArrayList<String>();
        for (var part : parts) {
            result.addAll(part);
        }
        return result;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.allOf(definitions().entrySet().stream().map(entry -> {
            var json = new JsonObject();
            var textures = new JsonArray();
            entry.getValue().forEach(textures::add);
            json.add("textures", textures);
            return DataProvider.writeToPath(writer, json, resolver.resolveJson(Identifier.of(MOD_ID, entry.getKey())));
        }).toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Particle Definitions (" + MOD_ID + ")";
    }
}
