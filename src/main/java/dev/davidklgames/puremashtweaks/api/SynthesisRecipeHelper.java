package dev.davidklgames.puremashtweaks.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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

    public static void reset() {
        loaded = false;
        SHAPED_RECIPES.clear();
        SHAPELESS_RECIPES.clear();
    }

    public record ShapedRecipeData(String[] pattern, Map<Character, Item> keys, ItemStack result, int width, int height, int totalItemsRequired) {
        public boolean matches(ItemStack[] grid, int activeCount) {
            if (activeCount != this.totalItemsRequired) return false;

            for (int startY = 0; startY <= 9 - this.height; startY++) {
                for (int startX = 0; startX <= 9 - this.width; startX++) {
                    if (this.checkMatch(grid, startX, startY, true)) return true;
                    if (this.checkMatch(grid, startX, startY, false)) return true;
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
                        char ch = mirror ? pattern[ry].charAt(this.width - 1 - rx) : pattern[ry].charAt(rx);
                        expectedItem = keys.get(ch);
                    }

                    ItemStack actual = grid[x + y * 9];
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

    public record ShapelessRecipeData(List<Item> ingredients, ItemStack result) {
        public boolean matches(ItemStack[] grid, int activeCount) {
            if (activeCount != ingredients.size()) return false;

            boolean[] matched = new boolean[ingredients.size()];

            for (ItemStack actual : grid) {
                if (actual.isEmpty()) continue;
                Item actualItem = actual.getItem();

                boolean found = false;
                for (int j = 0; j < ingredients.size(); j++) {
                    if (!matched[j] && ingredients.get(j) == actualItem) {
                        matched[j] = true;
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
        }
    }

    public static List<ShapedRecipeData> getShapedRecipes() {
        loadCustomRecipes();
        return SHAPED_RECIPES;
    }

    public static List<ShapelessRecipeData> getShapelessRecipes() {
        loadCustomRecipes();
        return SHAPELESS_RECIPES;
    }

    public static ItemStack[] getIngredientItems(Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return new ItemStack[0];
        return ingredient.items()
                .map(Holder::value)
                .map(ItemStack::new)
                .toArray(ItemStack[]::new);
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
                                Item item = itemLoc != null ? BuiltInRegistries.ITEM.get(itemLoc).map(Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
                                keys.put(ch, item);
                            }

                            int totalRequired = 0;
                            for (String row : pattern) {
                                for (int c = 0; c < row.length(); c++) {
                                    char ch = row.charAt(c);
                                    if (ch != ' ' && ch != '.' && keys.containsKey(ch) && keys.get(ch) != net.minecraft.world.item.Items.AIR) {
                                        totalRequired++;
                                    }
                                }
                            }

                            ItemStack resultStack;
                            if (obj.get("result").isJsonObject()) {
                                resultStack = ItemStack.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, obj.get("result")).getOrThrow();
                            } else {
                                Identifier resId = Identifier.tryParse(obj.get("result").getAsString());
                                Item resultItem = resId != null ? BuiltInRegistries.ITEM.get(resId).map(Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
                                int count = obj.has("result_count") ? obj.get("result_count").getAsInt() : 1;
                                resultStack = new ItemStack(resultItem, count);
                            }

                            if (!resultStack.isEmpty()) {
                                SHAPED_RECIPES.add(new ShapedRecipeData(pattern, keys, resultStack, patW, patH, totalRequired));
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
                                Item item = itemLoc != null ? BuiltInRegistries.ITEM.get(itemLoc).map(Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
                                if (item != net.minecraft.world.item.Items.AIR) {
                                    ingredients.add(item);
                                }
                            }

                            ItemStack resultStack;
                            if (obj.get("result").isJsonObject()) {
                                resultStack = ItemStack.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, obj.get("result")).getOrThrow();
                            } else {
                                Identifier resId = Identifier.tryParse(obj.get("result").getAsString());
                                Item resultItem = resId != null ? BuiltInRegistries.ITEM.get(resId).map(Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
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
        if (grid == null || grid.length == 0) return null;

        int activeCount = 0;
        for (ItemStack s : grid) {
            if (!s.isEmpty()) activeCount++;
        }
        if (activeCount == 0) return null;

        loadCustomRecipes();

        for (ShapedRecipeData recipe : SHAPED_RECIPES) {
            if (recipe.matches(grid, activeCount)) {
                return recipe.result().copy();
            }
        }

        for (ShapelessRecipeData recipe : SHAPELESS_RECIPES) {
            if (recipe.matches(grid, activeCount)) {
                return recipe.result().copy();
            }
        }

        return null;
    }
}