package net.more_rpg_classes.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class BetterCombatWeaponAttributeGenerator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Generate Better Combat weapon attribute files for a list of weapon entries.
     * @param outputPath Base output path for data files
     * @param modId The mod ID for folder path
     * @param weaponEntries List of weapon registry entries (must have getId() and item() or get() methods)
     * @param nameFilter Optional filter string - only items with this string in their ID will be processed
     * @param parent The Better Combat parent attribute (e.g., "bettercombat:staff", "bettercombat:sword")
     */
    public static <T> void generateBetterCombatWeaponAttributes(
            Path outputPath,
            String modId,
            List<T> weaponEntries,
            String nameFilter,
            String parent) throws IOException {

        for (T entry : weaponEntries) {
            Identifier itemId = getItemId(entry);
            if (itemId == null) {
                continue;
            }

            if (nameFilter != null && !nameFilter.isEmpty()) {
                if (!itemId.getPath().contains(nameFilter)) {
                    continue;
                }
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("parent", parent);

            Path modDataPath = outputPath.resolve("data").resolve(modId);
            Path weaponAttributesPath = modDataPath.resolve("weapon_attributes");
            Files.createDirectories(weaponAttributesPath);
            Path filePath = weaponAttributesPath.resolve(itemId.getPath() + ".json");

            String json = GSON.toJson(jsonObject);
            Files.writeString(filePath, json);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Identifier getItemId(T entry) {
        try {
            var getIdMethod = entry.getClass().getMethod("getId");
            Object result = getIdMethod.invoke(entry);
            if (result instanceof Identifier) {
                return (Identifier) result;
            }
        } catch (Exception ignored) {
        }

        try {
            var idMethod = entry.getClass().getMethod("id");
            Object result = idMethod.invoke(entry);
            if (result instanceof Identifier) {
                return (Identifier) result;
            }
        } catch (Exception ignored) {
        }

        try {
            Object itemResult = null;

            try {
                var itemMethod = entry.getClass().getMethod("item");
                itemResult = itemMethod.invoke(entry);
            } catch (Exception e) {
                var getMethod = entry.getClass().getMethod("get");
                itemResult = getMethod.invoke(entry);
            }

            if (itemResult instanceof Supplier<?>) {
                itemResult = ((Supplier<?>) itemResult).get();
            }

            if (itemResult instanceof Item item) {
                return Registries.ITEM.getId(item);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public static <T> void generateBetterCombatWeaponAttributes(
            Path outputPath,
            String modId,
            List<T> weaponEntries,
            String parent) throws IOException {
        generateBetterCombatWeaponAttributes(outputPath, modId, weaponEntries, null, parent);
    }
}
