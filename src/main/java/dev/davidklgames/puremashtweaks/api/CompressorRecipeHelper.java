package dev.davidklgames.puremashtweaks.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CompressorRecipeHelper {
    private static final Gson GSON = new Gson();

    private static final List<CustomRecipeData> CUSTOM_COMPRESS_RECIPES = new ArrayList<>();
    private static final List<CustomRecipeData> CUSTOM_SINGULARITY_RECIPES = new ArrayList<>();
    private static final List<CustomRecipeData> CUSTOM_DUST_RECIPES = new ArrayList<>();

    private static final List<CustomRecipeData> AUTO_DUST_RECIPES = new ArrayList<>();
    private static boolean dustScanned = false;
    private static boolean loaded = false;

    public record CustomRecipeData(Item ingredient, ItemStack result, int cost, int time) {}

    public static void loadCustomRecipes() {
        if (loaded) return;
        loaded = true;

        CUSTOM_COMPRESS_RECIPES.clear();
        CUSTOM_SINGULARITY_RECIPES.clear();
        CUSTOM_DUST_RECIPES.clear();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks/compressor_recipes");

        loadFolder(configDir.resolve("compressor"), 0);
        loadFolder(configDir.resolve("singularity"), 1);
        loadFolder(configDir.resolve("dust"), 2);

        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Custom Multifunctional Compressor recipes loaded.");
    }

    public static List<CustomRecipeData> getCustomRecipes() {
        loadCustomRecipes();
        List<CustomRecipeData> all = new ArrayList<>();
        all.addAll(CUSTOM_COMPRESS_RECIPES);
        all.addAll(CUSTOM_SINGULARITY_RECIPES);
        all.addAll(CUSTOM_DUST_RECIPES);
        return all;
    }

    public static void scanAutoDustRecipes() {
        if (dustScanned) return;
        dustScanned = true;
        AUTO_DUST_RECIPES.clear();

        java.util.Map<String, Item> dustMap = new java.util.HashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String path = id.getPath();
            if (path.endsWith("_dust") || path.endsWith("_powder")) {
                String metal = path.endsWith("_dust") ? path.replace("_dust", "") : path.replace("_powder", "");
                dustMap.put(metal, item);
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String path = id.getPath();

            for (java.util.Map.Entry<String, Item> entry : dustMap.entrySet()) {
                String metal = entry.getKey();
                Item dustItem = entry.getValue();

                if (path.equals(metal + "_ingot")) {
                    AUTO_DUST_RECIPES.add(new CustomRecipeData(item, new ItemStack(dustItem, 1), 1, 100));
                } else if (path.equals("raw_" + metal)) {
                    AUTO_DUST_RECIPES.add(new CustomRecipeData(item, new ItemStack(dustItem, 1), 1, 100));
                } else if (path.equals("raw_" + metal + "_block")) {
                    AUTO_DUST_RECIPES.add(new CustomRecipeData(item, new ItemStack(dustItem, 9), 1, 110));
                } else if (path.equals(metal + "_ore")) {
                    AUTO_DUST_RECIPES.add(new CustomRecipeData(item, new ItemStack(dustItem, 2), 1, 110));
                } else if (path.equals("deepslate_" + metal + "_ore")) {
                    AUTO_DUST_RECIPES.add(new CustomRecipeData(item, new ItemStack(dustItem, 2), 1, 110));
                } else if (path.equals("nether_" + metal + "_ore")) {
                    AUTO_DUST_RECIPES.add(new CustomRecipeData(item, new ItemStack(dustItem, 2), 1, 110));
                }
            }
        }
    }

    public static List<CustomRecipeData> scanAutoCompressionRecipes(@Nullable RecipeManager recipeManager) {
        List<CustomRecipeData> list = new ArrayList<>();
        if (recipeManager == null) return list;

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof CraftingRecipe craftingRecipe) {
                List<Ingredient> ingredients = craftingRecipe.placementInfo().ingredients();
                if (ingredients.size() == 9) {
                    Item matchItem = null;
                    boolean valid = true;
                    for (Ingredient ing : ingredients) {
                        if (ing.isEmpty()) {
                            valid = false;
                            break;
                        }

                        Item current = ing.items().findFirst().map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
                        if (current == net.minecraft.world.item.Items.AIR) {
                            valid = false;
                            break;
                        }
                        if (matchItem == null) {
                            matchItem = current;
                        } else if (matchItem != current) {
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        NonNullList<ItemStack> itemsList = NonNullList.withSize(9, new ItemStack(matchItem));
                        CraftingInput craftingInput = CraftingInput.of(3, 3, itemsList);

                        try {
                            ItemStack resultStack = craftingRecipe.assemble(craftingInput);
                            if (!resultStack.isEmpty()) {
                                list.add(new CustomRecipeData(matchItem, resultStack.copy(), 9, 100));
                            }
                        } catch (Exception _) {}
                    }
                }
            }
        }
        return list;
    }

    public static List<CustomRecipeData> getCompressionRecipes(@Nullable RecipeManager manager) {
        loadCustomRecipes();
        List<CustomRecipeData> list = new java.util.ArrayList<>(CUSTOM_COMPRESS_RECIPES);

        list.add(new CustomRecipeData(ModItems.SYNTHORIUM_INGOT.get(), new ItemStack(ModBlocks.SYNTHORIUM_BLOCK.get()), 9, 100));
        list.add(new CustomRecipeData(ModItems.MOLDELONIAN_INGOT.get(), new ItemStack(ModBlocks.MOLDELONIAN_BLOCK.get()), 9, 100));
        list.add(new CustomRecipeData(ModItems.SYNTHORIUM_SCRAP.get(), new ItemStack(ModItems.SYNTHORIUM_INGOT.get()), 9, 100));

        if (manager != null) {
            List<CustomRecipeData> scanned = scanAutoCompressionRecipes(manager);
            for (CustomRecipeData data : scanned) {
                boolean exists = false;
                for (CustomRecipeData existing : list) {
                    if (existing.ingredient() == data.ingredient()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) list.add(data);
            }
        }

        return list;
    }

    public static List<CustomRecipeData> getSingularityRecipes() {
        loadCustomRecipes();
        List<CustomRecipeData> list = new java.util.ArrayList<>(CUSTOM_SINGULARITY_RECIPES);

        int baseCost = dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.COMPRESSOR_SINGULARITY_BASE_COST.get();

        addInternalSingularity(list, dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_INGOT.get(), ModSingularities.SYNTHORIUM_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.SCULK, ModSingularities.SCULK_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.NETHER_STAR, ModSingularities.NETHER_STAR_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.COAL, ModSingularities.COAL_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.COPPER_INGOT, ModSingularities.COPPER_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.IRON_INGOT, ModSingularities.IRON_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.GOLD_INGOT, ModSingularities.GOLD_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.DIAMOND, ModSingularities.DIAMOND_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.EMERALD, ModSingularities.EMERALD_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.REDSTONE, ModSingularities.REDSTONE_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.AMETHYST_SHARD, ModSingularities.AMETHYST_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.QUARTZ, ModSingularities.QUARTZ_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.LAPIS_LAZULI, ModSingularities.LAPIS_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.SLIME_BALL, ModSingularities.SLIME_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.GLOWSTONE, ModSingularities.GLOWSTONE_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.NETHERITE_INGOT, ModSingularities.NETHERITE_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.ENDER_PEARL, ModSingularities.ENDER_PEARL_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.SUGAR, ModSingularities.SUGAR_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.GUNPOWDER, ModSingularities.GUNPOWDER_SINGULARITY, baseCost);
        addInternalSingularity(list, net.minecraft.world.item.Items.CLAY_BALL, ModSingularities.CLAY_SINGULARITY, baseCost);

        addModSingularity(list, "mysticalagriculture:inferium_essence", ModSingularities.INFERIUM_SINGULARITY, baseCost);
        addModSingularity(list, "mysticalagriculture:prudentium_essence", ModSingularities.PRUDENTIUM_SINGULARITY, baseCost);
        addModSingularity(list, "mysticalagriculture:tertium_essence", ModSingularities.TERTIUM_SINGULARITY, baseCost);
        addModSingularity(list, "mysticalagriculture:imperium_essence", ModSingularities.IMPERIUM_SINGULARITY, baseCost);
        addModSingularity(list, "mysticalagriculture:supremium_essence", ModSingularities.SUPREMIUM_SINGULARITY, baseCost);
        addModSingularity(list, "mysticalagradditions:insanium_essence", ModSingularities.INSANIUM_SINGULARITY, baseCost);
        addModSingularity(list, "ae2:certus_quartz_crystal", ModSingularities.CERTUS_QUARTZ_SINGULARITY, baseCost);
        addModSingularity(list, "advanced_ae:quantum_alloy", ModSingularities.QUANTUM_ALLOY_SINGULARITY, baseCost);
        addModSingularity(list, "forbidden_arcanus:deorum_ingot", ModSingularities.DEORUM_SINGULARITY, baseCost);
        addModSingularity(list, "advancednetherite:netherite_iron_ingot", ModSingularities.NETHERITE_IRON_SINGULARITY, baseCost);
        addModSingularity(list, "advancednetherite:netherite_gold_ingot", ModSingularities.NETHERITE_GOLD_SINGULARITY, baseCost);
        addModSingularity(list, "advancednetherite:netherite_emerald_ingot", ModSingularities.NETHERITE_EMERALD_SINGULARITY, baseCost);
        addModSingularity(list, "advancednetherite:netherite_diamond_ingot", ModSingularities.NETHERITE_DIAMOND_SINGULARITY, baseCost);

        return list;
    }

    public static List<CustomRecipeData> getDustRecipes() {
        loadCustomRecipes();
        scanAutoDustRecipes();

        List<CustomRecipeData> list = new java.util.ArrayList<>(CUSTOM_DUST_RECIPES);

        list.add(new CustomRecipeData(ModItems.SYNTHORIUM_INGOT.get(), new ItemStack(ModItems.SYNTHORIUM_DUST.get()), 1, 100));

        addModDustRecipe(list, "ae2:certus_quartz_crystal", "ae2:certus_quartz_dust");
        addModDustRecipe(list, "ae2:charged_certus_quartz_crystal", "ae2:certus_quartz_dust");
        addModDustRecipe(list, "ae2:fluix_crystal", "ae2:fluix_dust");
        addModDustRecipe(list, "advanced_ae:shattered_singularity", "advanced_ae:quantum_infused_dust");

        for (CustomRecipeData data : AUTO_DUST_RECIPES) {
            boolean exists = false;
            for (CustomRecipeData existing : list) {
                if (existing.ingredient() == data.ingredient()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) list.add(data);
        }

        return list;
    }

    private static void parseRecipeElement(JsonObject obj, int mode) {
        boolean enabled = !obj.has("enable_recipe") || obj.get("enable_recipe").getAsBoolean();
        if (!enabled) return;

        if (mode == 0 || mode == 2) {
            if (!obj.has("input") || !obj.has("output")) return;
            Identifier inputId = Identifier.tryParse(obj.get("input").getAsString());
            Identifier outputId = Identifier.tryParse(obj.get("output").getAsString());

            Item inputItem = inputId != null ? BuiltInRegistries.ITEM.get(inputId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
            Item outputItem = outputId != null ? BuiltInRegistries.ITEM.get(outputId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;

            int count = obj.has("input_count") ? obj.get("input_count").getAsInt() : (mode == 0 ? 9 : 1);
            int time = obj.has("time_cost") ? obj.get("time_cost").getAsInt() : 100;

            if (inputItem != net.minecraft.world.item.Items.AIR && outputItem != net.minecraft.world.item.Items.AIR) {
                CustomRecipeData recipeData = new CustomRecipeData(inputItem, new ItemStack(outputItem), count, time);
                if (mode == 0) {
                    CUSTOM_COMPRESS_RECIPES.add(recipeData);
                } else {
                    CUSTOM_DUST_RECIPES.add(recipeData);
                }
            }
        } else if (mode == 1) {
            if (!obj.has("item")) return;
            Identifier inputId = Identifier.tryParse(obj.get("item").getAsString());
            Item inputItem = inputId != null ? BuiltInRegistries.ITEM.get(inputId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;

            Item outputItem = net.minecraft.world.item.Items.AIR;
            if (obj.has("output")) {
                Identifier outputId = Identifier.tryParse(obj.get("output").getAsString());
                outputItem = outputId != null ? BuiltInRegistries.ITEM.get(outputId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR) : net.minecraft.world.item.Items.AIR;
            } else if (obj.has("name")) {
                String rawName = obj.get("name").getAsString();
                String name = rawName.toLowerCase()
                        .replace(" ", "_")
                        .replace("'", "")
                        .replace("-", "_");
                Identifier generatedSingularityId = Identifier.fromNamespaceAndPath("puremashtweaks", name);
                outputItem = BuiltInRegistries.ITEM.get(generatedSingularityId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
            }

            if (outputItem == net.minecraft.world.item.Items.AIR) {
                outputItem = BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("puremashtweaks", "puremash_core")).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
            }

            int cost = obj.has("cost") ? obj.get("cost").getAsInt() : PureMashTweaksConfig.COMPRESSOR_SINGULARITY_BASE_COST.get();
            int time = obj.has("time_cost") ? obj.get("time_cost").getAsInt() : 400;

            if (inputItem != net.minecraft.world.item.Items.AIR && outputItem != net.minecraft.world.item.Items.AIR) {
                CustomRecipeData recipeData = new CustomRecipeData(inputItem, new ItemStack(outputItem), cost, time);
                CUSTOM_SINGULARITY_RECIPES.add(recipeData);
            }
        }
    }

    private static void loadFolder(Path folder, int mode) {
        if (!Files.exists(folder)) return;
        try (var stream = Files.list(folder)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (FileReader reader = new FileReader(path.toFile())) {
                    JsonElement rootElement = com.google.gson.JsonParser.parseReader(reader);
                    if (rootElement.isJsonArray()) {
                        JsonArray array = rootElement.getAsJsonArray();
                        for (JsonElement element : array) {
                            if (element.isJsonObject()) {
                                parseRecipeElement(element.getAsJsonObject(), mode);
                            }
                        }
                    } else if (rootElement.isJsonObject()) {
                        parseRecipeElement(rootElement.getAsJsonObject(), mode);
                    }
                } catch (Exception _) {}
            });
        } catch (IOException _) {}
    }

    @Nullable
    public static CustomRecipeData getRecipe(Level level, ItemStack inputStack, int activeMode) {
        if (inputStack.isEmpty() || level == null) return null;
        Item inputItem = inputStack.getItem();

        loadCustomRecipes();

        if (activeMode == 1) {
            List<CustomRecipeData> allSingularities = getSingularityRecipes();
            for (CustomRecipeData data : allSingularities) {
                if (data.ingredient() == inputItem) return data;
            }
        }

        if (activeMode == 0) {
            for (CustomRecipeData data : CUSTOM_COMPRESS_RECIPES) {
                if (data.ingredient() == inputItem) return data;
            }

            if (inputItem == ModItems.SYNTHORIUM_INGOT.get()) {
                return new CustomRecipeData(inputItem, new ItemStack(ModBlocks.SYNTHORIUM_BLOCK.get()), 9, 100);
            }
            if (inputItem == ModItems.MOLDELONIAN_INGOT.get()) {
                return new CustomRecipeData(inputItem, new ItemStack(ModBlocks.MOLDELONIAN_BLOCK.get()), 9, 100);
            }
            if (inputItem == ModItems.SYNTHORIUM_SCRAP.get()) {
                return new CustomRecipeData(inputItem, new ItemStack(ModItems.SYNTHORIUM_INGOT.get()), 9, 100);
            }

            net.minecraft.world.item.crafting.RecipeManager recipeManager = null;
            if (level.recipeAccess() instanceof net.minecraft.world.item.crafting.RecipeManager) {
                recipeManager = (net.minecraft.world.item.crafting.RecipeManager) level.recipeAccess();
            } else if (ServerLifecycleHooks.getCurrentServer() != null) {
                recipeManager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
            }

            if (recipeManager != null) {
                for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
                    if (holder.value() instanceof CraftingRecipe craftingRecipe) {
                        List<Ingredient> ingredients = craftingRecipe.placementInfo().ingredients();
                        if (ingredients.size() == 9) {
                            Item matchItem = null;
                            boolean valid = true;
                            for (Ingredient ing : ingredients) {
                                if (ing.isEmpty()) {
                                    valid = false;
                                    break;
                                }

                                Item current = ing.items().findFirst().map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
                                if (current == net.minecraft.world.item.Items.AIR) {
                                    valid = false;
                                    break;
                                }
                                if (matchItem == null) {
                                    matchItem = current;
                                } else if (matchItem != current) {
                                    valid = false;
                                    break;
                                }
                            }

                            if (valid && matchItem == inputItem) {
                                NonNullList<ItemStack> itemsList = NonNullList.withSize(9, new ItemStack(inputItem));
                                CraftingInput craftingInput = CraftingInput.of(3, 3, itemsList);

                                ItemStack resultStack = craftingRecipe.assemble(craftingInput);
                                if (!resultStack.isEmpty()) {
                                    return new CustomRecipeData(inputItem, resultStack.copy(), 9, 100);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeMode == 2) {
            for (CustomRecipeData data : CUSTOM_DUST_RECIPES) {
                if (data.ingredient() == inputItem) return data;
            }

            scanAutoDustRecipes();
            for (CustomRecipeData data : AUTO_DUST_RECIPES) {
                if (data.ingredient() == inputItem) return data;
            }

            if (inputItem == ModItems.SYNTHORIUM_INGOT.get()) {
                return new CustomRecipeData(inputItem, new ItemStack(ModItems.SYNTHORIUM_DUST.get()), 1, 100);
            }
        }
        return null;
    }

    private static void addInternalSingularity(List<CustomRecipeData> list, Item input, @Nullable net.neoforged.neoforge.registries.DeferredItem<dev.davidklgames.puremashtweaks.item.ColorSingularityItem> outputHolder, int cost) {
        if (outputHolder != null) {
            list.add(new CustomRecipeData(input, new ItemStack(outputHolder.get()), cost, 400));
        }
    }

    private static void addModSingularity(List<CustomRecipeData> list, String inputIdStr, @Nullable net.neoforged.neoforge.registries.DeferredItem<dev.davidklgames.puremashtweaks.item.ColorSingularityItem> outputHolder, int cost) {
        if (outputHolder != null) {
            Identifier inputId = Identifier.tryParse(inputIdStr);
            if (inputId != null) {
                Item inputItem = BuiltInRegistries.ITEM.get(inputId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
                if (inputItem != net.minecraft.world.item.Items.AIR) {
                    list.add(new CustomRecipeData(inputItem, new ItemStack(outputHolder.get()), cost, 400));
                }
            }
        }
    }

    private static void addModDustRecipe(List<CustomRecipeData> list, String inputIdStr, String outputIdStr) {
        Identifier inputId = Identifier.tryParse(inputIdStr);
        Identifier outputId = Identifier.tryParse(outputIdStr);
        if (inputId != null && outputId != null) {
            Item inputItem = BuiltInRegistries.ITEM.get(inputId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
            Item outputItem = BuiltInRegistries.ITEM.get(outputId).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);

            if (inputItem != net.minecraft.world.item.Items.AIR && outputItem != net.minecraft.world.item.Items.AIR) {
                list.add(new CustomRecipeData(inputItem, new ItemStack(outputItem), 1, 100));
            }
        }
    }
}