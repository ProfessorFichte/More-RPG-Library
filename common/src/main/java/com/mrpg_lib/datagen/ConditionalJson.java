package com.mrpg_lib.datagen;

import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ConditionalJson {
    private ConditionalJson() {
    }

    public static Rules rules() {
        return new Rules();
    }

    public static String dataPath(String directory, Identifier id) {
        return "data/" + id.getNamespace() + "/" + directory + "/" + id.getPath() + ".json";
    }

    public static JsonObject inject(JsonObject json, String... requiredMods) {
        JsonObject result = new JsonObject();
        result.add("fabric:load_conditions", fabricConditions(requiredMods));
        result.add("neoforge:conditions", neoForgeConditions(requiredMods));
        json.entrySet().forEach(entry -> result.add(entry.getKey(), entry.getValue()));
        return result;
    }

    public static DataProvider wrap(FabricDataOutput output, DataProvider inner, Rules rules) {
        return new DataProvider() {
            @Override
            public CompletableFuture<?> run(DataWriter writer) {
                return inner.run(new InjectingWriter(writer, output.getPath(), rules))
                        .thenRun(() -> rules.assertAllMatched(inner.getName()));
            }

            @Override
            public String getName() {
                return inner.getName();
            }
        };
    }

    public static DataProvider raw(FabricDataOutput output, String name, Map<Identifier, JsonObject> files, String directory, Rules rules) {
        var resolver = output.getResolver(DataOutput.OutputType.DATA_PACK, directory);
        DataProvider provider = new DataProvider() {
            @Override
            public CompletableFuture<?> run(DataWriter writer) {
                return CompletableFuture.allOf(files.entrySet().stream()
                        .map(entry -> DataProvider.writeToPath(writer, entry.getValue(), resolver.resolveJson(entry.getKey())))
                        .toArray(CompletableFuture[]::new));
            }

            @Override
            public String getName() {
                return name;
            }
        };
        return wrap(output, provider, rules);
    }

    private static JsonArray fabricConditions(String[] mods) {
        JsonObject condition = new JsonObject();
        condition.addProperty("condition", "fabric:all_mods_loaded");
        JsonArray values = new JsonArray();
        for (String mod : mods) {
            values.add(mod);
        }
        condition.add("values", values);
        JsonArray array = new JsonArray();
        array.add(condition);
        return array;
    }

    private static JsonArray neoForgeConditions(String[] mods) {
        JsonArray array = new JsonArray();
        if (mods.length == 1) {
            array.add(modLoaded(mods[0]));
            return array;
        }
        JsonObject and = new JsonObject();
        and.addProperty("type", "neoforge:and");
        JsonArray inner = new JsonArray();
        for (String mod : mods) {
            inner.add(modLoaded(mod));
        }
        and.add("conditions", inner);
        array.add(and);
        return array;
    }

    private static JsonObject modLoaded(String mod) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", mod);
        return condition;
    }

    public static final class Rules {
        private final Map<String, String[]> byPath = new HashMap<>();
        private final Set<String> matched = ConcurrentHashMap.newKeySet();

        public Rules add(String directory, Identifier id, String... requiredMods) {
            byPath.put(dataPath(directory, id), requiredMods);
            return this;
        }

        String[] modsFor(String relativePath) {
            var mods = byPath.get(relativePath);
            if (mods != null) {
                matched.add(relativePath);
            }
            return mods;
        }

        void assertAllMatched(String provider) {
            var missing = byPath.keySet().stream().filter(path -> !matched.contains(path)).toList();
            if (!missing.isEmpty()) {
                throw new IllegalStateException(provider + " never wrote conditional files: " + missing);
            }
        }
    }

    private static final class InjectingWriter implements DataWriter {
        private final DataWriter delegate;
        private final Path root;
        private final Rules rules;

        private InjectingWriter(DataWriter delegate, Path root, Rules rules) {
            this.delegate = delegate;
            this.root = root;
            this.rules = rules;
        }

        @Override
        public void write(Path path, byte[] data, HashCode hashCode) throws IOException {
            var relative = root.relativize(path).toString().replace('\\', '/');
            var mods = rules.modsFor(relative);
            if (mods == null) {
                delegate.write(path, data, hashCode);
                return;
            }
            JsonElement parsed = JsonParser.parseString(new String(data, StandardCharsets.UTF_8));
            DataProvider.writeToPath(delegate, inject(parsed.getAsJsonObject(), mods), path).join();
        }
    }
}
