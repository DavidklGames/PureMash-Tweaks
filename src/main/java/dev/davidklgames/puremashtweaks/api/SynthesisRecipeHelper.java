package dev.davidklgames.puremashtweaks.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynthesisRecipeHelper {
    private static final Gson GSON = new Gson();
    private static final List<ShapedRecipeData> SHAPED_RECIPES = new ArrayList<>();
    private static final List<ShapelessRecipeData> SHAPELESS_RECIPES = new ArrayList<>();
    private static boolean loaded = false;

    // Shielded 9x9 spatial collision structure
    public record ShapedRecipeData(String[] pattern, Map<Character, Item> keys, ItemStack result, int width, int height) {
        public boolean matches(ItemStack[] grid) {
            // Iterates through all possible offset positions within the 9x9 grid
            for (int startY = 0; startY <= 9 - this.height; startY++) {
                for (int startX = 0; startX <= 9 - this.width; startX++) {
                    if (this.checkMatch(grid, startX, startY, true)) {
                        return true;
                    }
                    if (this.checkMatch(grid, startX, startY, false)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean checkMatch(ItemStack[] grid, int startX, int startY, boolean mirror) {
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    int rx = x - startX;
                    int ry = y - startY;
                    Item expectedItem = null;

                    if (rx >= 0 && ry >= 0 && rx < this.width && ry < this.height) {
                        char ch;
                        if (mirror) {
                            ch = pattern[ry].charAt(this.width - 1 - rx);
                        } else {
                            ch = pattern[ry].charAt(rx);
                        }
                        expectedItem = keys.get(ch);
                    }

                    ItemStack actual = grid[x + y * 9];
                    // If the recipe does not expect an item at this coordinate, the slot on the table MUST be empty
                    if (expectedItem == null || expectedItem == net.minecraft.world.item.Items.AIR) {
                        if (!actual.isEmpty()) return false;
                    } else {
                        if (actual.isEmpty() || actual.getItem() != expectedItem) return false;
                    }
                }
            }
            return true;
        }
    }

    // Formless (Shapeless) collision structure
    public record ShapelessRecipeData(List<Item> ingredients, ItemStack result) {
        public boolean matches(ItemStack[] grid) {
            List<ItemStack> activeItems = new ArrayList<>();
            for (ItemStack stack : grid) {
                if (!stack.isEmpty()) activeItems.add(stack);
            }
            if (activeItems.size() != ingredients.size()) return false;

            List<Item> expected = new ArrayList<>(ingredients);
            for (ItemStack actual : activeItems) {
                Item actualItem = actual.getItem();
                if (!expected.remove(actualItem)) {
                    return false;
                }
            }
            return expected.isEmpty();
        }
    }

    public static void loadCustomRecipes() {
        if (loaded) return;
        loaded = true;

        SHAPED_RECIPES.clear();
        SHAPELESS_RECIPES.clear();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks/synthesis_recipes");

        loadShaped(configDir.resolve("shaped"));
        loadShapeless(configDir.resolve("shapeless"));

        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Custom 9x9 Synthesis Table recipes loaded.");
    }

    private static void loadShaped(Path folder) {
        if (!Files.exists(folder)) return;
        try (var stream = Files.list(folder)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (FileReader reader = new FileReader(path.toFile())) {
                    JsonArray array = GSON.fromJson(reader, JsonArray.class);
                    if (array != null) {
                        for (JsonElement element : array) {
                            JsonObject obj = element.getAsJsonObject();
                            boolean enabled = !obj.has("enable_recipe") || obj.get("enable_recipe").getAsBoolean();
                            if (!enabled) continue;

                            JsonArray patternArray = obj.getAsJsonArray("pattern");
                            String[] pattern = new String[patternArray.size()];
                            for (int i = 0; i < patternArray.size(); i++) {
                                pattern[i] = patternArray.get(i).getAsString();
                            }

                            int patH = pattern.length;
                            int patW = pattern[0].length();

                            JsonObject keyObj = obj.getAsJsonObject("key");
                            Map<Character, Item> keys = new HashMap<>();
                            for (Map.Entry<String, JsonElement> entry : keyObj.entrySet()) {
                                char ch = entry.getKey().charAt(0);
                                Identifier itemLoc = Identifier.tryParse(entry.getValue().getAsString());
                                Item item = itemLoc != null ? BuiltInRegistries.ITEM.get(itemLoc).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
                                keys.put(ch, item);
                            }

                            // ADVANCED COMPONENT SUPPORT: Reads using Minecraft's Codec if it is a complex object!
                            ItemStack resultStack;
                            if (obj.get("result").isJsonObject()) {
                                resultStack = ItemStack.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, obj.get("result")).getOrThrow();
                            } else {
                                Identifier resId = Identifier.tryParse(obj.get("result").getAsString());
                                Item resultItem = resId != null ? BuiltInRegistries.ITEM.get(resId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
                                int count = obj.has("result_count") ? obj.get("result_count").getAsInt() : 1;
                                resultStack = new ItemStack(resultItem, count);
                            }

                            if (!resultStack.isEmpty()) {
                                SHAPED_RECIPES.add(new ShapedRecipeData(pattern, keys, resultStack, patW, patH));
                            }
                        }
                    }
                } catch (Exception e) {
                    PureMashTweaks.LOGGER.error("[PureMash] Failed to load custom shaped recipe: {}", path, e);
                }
            });
        } catch (IOException e) {
            PureMashTweaks.LOGGER.error("[PureMash] Failed to list shaped recipes: {}", folder, e);
        }
    }

    private static void loadShapeless(Path folder) {
        if (!Files.exists(folder)) return;
        try (var stream = Files.list(folder)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (FileReader reader = new FileReader(path.toFile())) {
                    JsonArray array = GSON.fromJson(reader, JsonArray.class);
                    if (array != null) {
                        for (JsonElement element : array) {
                            JsonObject obj = element.getAsJsonObject();
                            boolean enabled = !obj.has("enable_recipe") || obj.get("enable_recipe").getAsBoolean();
                            if (!enabled) continue;

                            JsonArray ingredientsArray = obj.getAsJsonArray("ingredients");
                            List<Item> ingredients = new ArrayList<>();
                            for (int i = 0; i < ingredientsArray.size(); i++) {
                                Identifier itemLoc = Identifier.tryParse(ingredientsArray.get(i).getAsString());
                                Item item = itemLoc != null ? BuiltInRegistries.ITEM.get(itemLoc).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
                                if (item != net.minecraft.world.item.Items.AIR) {
                                    ingredients.add(item);
                                }
                            }

                            // ADVANCED COMPONENT SUPPORT: Reads using Minecraft's Codec if it is a complex object!
                            ItemStack resultStack;
                            if (obj.get("result").isJsonObject()) {
                                resultStack = ItemStack.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, obj.get("result")).getOrThrow();
                            } else {
                                Identifier resId = Identifier.tryParse(obj.get("result").getAsString());
                                Item resultItem = resId != null ? BuiltInRegistries.ITEM.get(resId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
                                int count = obj.has("result_count") ? obj.get("result_count").getAsInt() : 1;
                                resultStack = new ItemStack(resultItem, count);
                            }

                            if (!resultStack.isEmpty()) {
                                SHAPELESS_RECIPES.add(new ShapelessRecipeData(ingredients, resultStack));
                            }
                        }
                    }
                } catch (Exception e) {
                    PureMashTweaks.LOGGER.error("[PureMash] Failed to load custom shapeless recipe: {}", path, e);
                }
            });
        } catch (IOException e) {
            PureMashTweaks.LOGGER.error("[PureMash] Failed to list shapeless recipes: {}", folder, e);
        }
    }

    @Nullable
    public static ItemStack getResult(ItemStack[] grid) {
        loadCustomRecipes();

        // 1. First check if the pattern on the table matches any Shaped 9x9 recipe
        for (ShapedRecipeData recipe : SHAPED_RECIPES) {
            if (recipe.matches(grid)) {
                return recipe.result().copy();
            }
        }

        // 2. If it did not match Shaped, check if it matches any Shapeless 9x9 recipe
        for (ShapelessRecipeData recipe : SHAPELESS_RECIPES) {
            if (recipe.matches(grid)) {
                return recipe.result().copy();
            }
        }

        return null;
    }
}