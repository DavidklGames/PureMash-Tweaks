package dev.davidklgames.puremashtweaks.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.recipe.CompressionRecipe;
import dev.davidklgames.puremashtweaks.recipe.DustRecipe;
import dev.davidklgames.puremashtweaks.recipe.SingularityRecipe;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("deprecation")
public class CompressorRecipeHelper {
    private static final Gson GSON = new Gson();

    private static final List<CustomRecipeData> CUSTOM_COMPRESS_RECIPES = new ArrayList<>();
    private static final List<CustomRecipeData> CUSTOM_SINGULARITY_RECIPES = new ArrayList<>();
    private static final List<CustomRecipeData> CUSTOM_DUST_RECIPES = new ArrayList<>();

    private static final Map<Item, CustomRecipeData> COMPRESS_CACHE = new HashMap<>();
    private static final Map<Item, CustomRecipeData> SINGULARITY_CACHE = new HashMap<>();
    private static final Map<Item, CustomRecipeData> DUST_CACHE = new HashMap<>();

    private static boolean dustScanned = false;
    private static boolean loaded = false;
    private static boolean datapackScanned = false;

    public record CustomRecipeData(Item ingredient, ItemStack result, int cost, int time) {}

    public static void reset() {
        loaded = false;
        dustScanned = false;
        datapackScanned = false;
        COMPRESS_CACHE.clear();
        SINGULARITY_CACHE.clear();
        DUST_CACHE.clear();
        CUSTOM_COMPRESS_RECIPES.clear();
        CUSTOM_SINGULARITY_RECIPES.clear();
        CUSTOM_DUST_RECIPES.clear();
    }

    public static void loadCustomRecipes() {
        if (loaded) return;
        loaded = true;

        CUSTOM_COMPRESS_RECIPES.clear();
        CUSTOM_SINGULARITY_RECIPES.clear();
        CUSTOM_DUST_RECIPES.clear();

        COMPRESS_CACHE.clear();
        SINGULARITY_CACHE.clear();
        DUST_CACHE.clear();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks/compressor_recipes");

        loadFolder(configDir.resolve("compressor"), 0);
        loadFolder(configDir.resolve("singularity"), 1);
        loadFolder(configDir.resolve("dust"), 2);

        for (CustomRecipeData s : getSingularityRecipes()) {
            SINGULARITY_CACHE.put(s.ingredient(), s);
        }

        for (CustomRecipeData c : CUSTOM_COMPRESS_RECIPES) {
            COMPRESS_CACHE.put(c.ingredient(), c);
        }
        COMPRESS_CACHE.put(ModItems.SYNTHORIUM_INGOT.get(), new CustomRecipeData(ModItems.SYNTHORIUM_INGOT.get(), new ItemStack(ModBlocks.SYNTHORIUM_BLOCK.get()), 9, 20));
        COMPRESS_CACHE.put(ModItems.MOLDELONIAN_INGOT.get(), new CustomRecipeData(ModItems.MOLDELONIAN_INGOT.get(), new ItemStack(ModBlocks.MOLDELONIAN_BLOCK.get()), 9, 20));
        COMPRESS_CACHE.put(ModItems.SYNTHORIUM_SCRAP.get(), new CustomRecipeData(ModItems.SYNTHORIUM_SCRAP.get(), new ItemStack(ModItems.SYNTHORIUM_INGOT.get()), 9, 20));

        scanAutoDustRecipes();
    }

    public static void scanDatapackAndKubeJSRecipes(@Nullable RecipeManager manager) {
        if (datapackScanned || manager == null) return;
        datapackScanned = true;

        for (RecipeHolder<?> holder : manager.getRecipes()) {
            // 1. KubeJS / Datapack Compression Recipes
            if (holder.value() instanceof CompressionRecipe comp) {
                for (Holder<Item> itemHolder : comp.getInput().items().toList()) {
                    Item item = itemHolder.value();
                    if (item != Items.AIR) {
                        ItemStack output = comp.getResult().create();
                        CustomRecipeData data = new CustomRecipeData(item, output, comp.getInputCount(), comp.getTimeCost());
                        COMPRESS_CACHE.put(item, data);
                        if (!CUSTOM_COMPRESS_RECIPES.contains(data)) {
                            CUSTOM_COMPRESS_RECIPES.add(data);
                        }
                    }
                }
            }
            // 2. KubeJS / Datapack Singularity Recipes
            else if (holder.value() instanceof SingularityRecipe sing) {
                for (Holder<Item> itemHolder : sing.getInput().items().toList()) {
                    Item item = itemHolder.value();
                    if (item != Items.AIR) {
                        ItemStack output = sing.getResult().create();
                        CustomRecipeData data = new CustomRecipeData(item, output, sing.getCost(), sing.getTimeCost());
                        SINGULARITY_CACHE.put(item, data);
                        if (!CUSTOM_SINGULARITY_RECIPES.contains(data)) {
                            CUSTOM_SINGULARITY_RECIPES.add(data);
                        }
                    }
                }
            }
            // 3. KubeJS / Datapack Dust Recipes
            else if (holder.value() instanceof DustRecipe dust) {
                for (Holder<Item> itemHolder : dust.getInput().items().toList()) {
                    Item item = itemHolder.value();
                    if (item != Items.AIR) {
                        ItemStack output = dust.getResult().create();
                        CustomRecipeData data = new CustomRecipeData(item, output, 1, dust.getTimeCost());
                        DUST_CACHE.put(item, data);
                        if (!CUSTOM_DUST_RECIPES.contains(data)) {
                            CUSTOM_DUST_RECIPES.add(data);
                        }
                    }
                }
            }
        }
    }

    public static void scanAutoDustRecipes() {
        if (dustScanned) return;
        dustScanned = true;

        Map<String, Item> dustNameMap = new HashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String path = id.getPath().toLowerCase();
            if (path.endsWith("_dust") || path.endsWith("_powder")) {
                String base = path.endsWith("_dust") ? path.substring(0, path.length() - 5) : path.substring(0, path.length() - 7);
                dustNameMap.putIfAbsent(base, item);
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String path = id.getPath().toLowerCase();

            for (Map.Entry<String, Item> entry : dustNameMap.entrySet()) {
                String metal = entry.getKey();
                Item dust = entry.getValue();

                if (path.equals(metal + "_ingot") || path.equals(metal + "_gem") || path.equals(metal)) {
                    registerDustMapping(item, dust, 1);
                } else if (path.equals("raw_" + metal)) {
                    registerDustMapping(item, dust, 1);
                } else if (path.equals("raw_" + metal + "_block")) {
                    registerDustMapping(item, dust, 9);
                } else if (path.equals(metal + "_block") && !path.startsWith("raw_")) {
                    registerDustMapping(item, dust, 9);
                } else if (path.equals(metal + "_ore") || path.equals("deepslate_" + metal + "_ore") ||
                        path.equals("nether_" + metal + "_ore") || path.equals("end_" + metal + "_ore") ||
                        path.equals("ore_" + metal)) {
                    registerDustMapping(item, dust, 2);
                }
            }
        }

        addModDustFallback("ae2:certus_quartz_crystal", "ae2:certus_quartz_dust");
        addModDustFallback("ae2:charged_certus_quartz_crystal", "ae2:certus_quartz_dust");
        addModDustFallback("ae2:fluix_crystal", "ae2:fluix_dust");
        addModDustFallback("advanced_ae:shattered_singularity", "advanced_ae:quantum_infused_dust");

        registerDustMapping(ModItems.SYNTHORIUM_INGOT.get(), ModItems.SYNTHORIUM_DUST.get(), 1);
        registerDustMapping(ModItems.MOLDELONIAN_INGOT.get(), ModItems.MOLDELONIAN_DUST.get(), 1);
        registerDustMapping(ModBlocks.SYNTHORIUM_DEBRIS.get().asItem(), ModItems.SYNTHORIUM_DUST.get(), 2);
    }

    private static void registerDustMapping(Item input, Item outputDust, int outputCount) {
        if (input != Items.AIR && outputDust != Items.AIR && input != outputDust) {
            CustomRecipeData data = new CustomRecipeData(input, new ItemStack(outputDust, outputCount), 1, 20);
            DUST_CACHE.putIfAbsent(input, data);
            if (!CUSTOM_DUST_RECIPES.contains(data)) {
                CUSTOM_DUST_RECIPES.add(data);
            }
        }
    }

    private static void addModDustFallback(String inputIdStr, String outputIdStr) {
        Identifier inId = Identifier.tryParse(inputIdStr);
        Identifier outId = Identifier.tryParse(outputIdStr);
        if (inId != null && outId != null) {
            Item in = BuiltInRegistries.ITEM.get(inId).map(Holder::value).orElse(Items.AIR);
            Item out = BuiltInRegistries.ITEM.get(outId).map(Holder::value).orElse(Items.AIR);
            if (in != Items.AIR && out != Items.AIR) {
                registerDustMapping(in, out, 1);
            }
        }
    }

    public static List<CustomRecipeData> getCompressionRecipes(@Nullable RecipeManager manager) {
        loadCustomRecipes();
        scanDatapackAndKubeJSRecipes(manager);
        List<CustomRecipeData> list = new ArrayList<>(CUSTOM_COMPRESS_RECIPES);

        list.add(new CustomRecipeData(ModItems.SYNTHORIUM_INGOT.get(), new ItemStack(ModBlocks.SYNTHORIUM_BLOCK.get()), 9, 20));
        list.add(new CustomRecipeData(ModItems.MOLDELONIAN_INGOT.get(), new ItemStack(ModBlocks.MOLDELONIAN_BLOCK.get()), 9, 20));
        list.add(new CustomRecipeData(ModItems.SYNTHORIUM_SCRAP.get(), new ItemStack(ModItems.SYNTHORIUM_INGOT.get()), 9, 20));

        if (manager != null) {
            List<CustomRecipeData> scanned = scanAutoCompressionRecipes(manager);
            for (CustomRecipeData data : scanned) {
                if (!COMPRESS_CACHE.containsKey(data.ingredient())) {
                    COMPRESS_CACHE.put(data.ingredient(), data);
                    list.add(data);
                }
            }
        }
        return list;
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
                        Item current = ing.items().findFirst().map(Holder::value).orElse(Items.AIR);
                        if (current == Items.AIR) {
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
                                list.add(new CustomRecipeData(matchItem, resultStack.copy(), 9, 20));
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return list;
    }

    public static List<CustomRecipeData> getSingularityRecipes() {
        loadCustomRecipes();
        List<CustomRecipeData> list = new ArrayList<>(CUSTOM_SINGULARITY_RECIPES);

        int baseCost = (PureMashTweaksConfig.COMMON_SPEC != null && PureMashTweaksConfig.COMMON_SPEC.isLoaded()) ?
                PureMashTweaksConfig.COMMON.compressorSingularityBaseCost.get() : 1000;
        int time = (PureMashTweaksConfig.COMMON_SPEC != null && PureMashTweaksConfig.COMMON_SPEC.isLoaded()) ?
                PureMashTweaksConfig.COMMON.compressorSingularitySpeed.get() : 40;

        addInternalSingularity(list, ModItems.SYNTHORIUM_INGOT.get(), ModSingularities.SYNTHORIUM_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.SCULK, ModSingularities.SCULK_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.NETHER_STAR, ModSingularities.NETHER_STAR_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.COAL, ModSingularities.COAL_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.COPPER_INGOT, ModSingularities.COPPER_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.IRON_INGOT, ModSingularities.IRON_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.GOLD_INGOT, ModSingularities.GOLD_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.DIAMOND, ModSingularities.DIAMOND_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.EMERALD, ModSingularities.EMERALD_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.REDSTONE, ModSingularities.REDSTONE_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.AMETHYST_SHARD, ModSingularities.AMETHYST_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.QUARTZ, ModSingularities.QUARTZ_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.GLOWSTONE, ModSingularities.GLOWSTONE_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.NETHERITE_INGOT, ModSingularities.NETHERITE_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.ENDER_PEARL, ModSingularities.ENDER_PEARL_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.SUGAR, ModSingularities.SUGAR_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.GUNPOWDER, ModSingularities.GUNPOWDER_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.CLAY_BALL, ModSingularities.CLAY_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.LAPIS_LAZULI, ModSingularities.LAPIS_SINGULARITY, baseCost, time);
        addInternalSingularity(list, Items.SLIME_BALL, ModSingularities.SLIME_SINGULARITY, baseCost, time);

        addModSingularity(list, "mysticalagriculture:inferium_essence", ModSingularities.INFERIUM_SINGULARITY, baseCost, time);
        addModSingularity(list, "mysticalagriculture:prudentium_essence", ModSingularities.PRUDENTIUM_SINGULARITY, baseCost, time);
        addModSingularity(list, "mysticalagriculture:tertium_essence", ModSingularities.TERTIUM_SINGULARITY, baseCost, time);
        addModSingularity(list, "mysticalagriculture:imperium_essence", ModSingularities.IMPERIUM_SINGULARITY, baseCost, time);
        addModSingularity(list, "mysticalagriculture:supremium_essence", ModSingularities.SUPREMIUM_SINGULARITY, baseCost, time);
        addModSingularity(list, "mysticalagradditions:insanium_essence", ModSingularities.INSANIUM_SINGULARITY, baseCost, time);
        addModSingularity(list, "ae2:certus_quartz_crystal", ModSingularities.CERTUS_QUARTZ_SINGULARITY, baseCost, time);
        addModSingularity(list, "appliedenergistics2:certus_quartz_crystal", ModSingularities.CERTUS_QUARTZ_SINGULARITY, baseCost, time);
        addModSingularity(list, "advancedae:quantum_alloy", ModSingularities.QUANTUM_ALLOY_SINGULARITY, baseCost, time);
        addModSingularity(list, "advanced_ae:quantum_alloy", ModSingularities.QUANTUM_ALLOY_SINGULARITY, baseCost, time);
        addModSingularity(list, "forbidden_arcanus:deorum_ingot", ModSingularities.DEORUM_SINGULARITY, baseCost, time);
        addModSingularity(list, "advancednetherite:netherite_iron_ingot", ModSingularities.NETHERITE_IRON_SINGULARITY, baseCost, time);
        addModSingularity(list, "advancednetherite:netherite_gold_ingot", ModSingularities.NETHERITE_GOLD_SINGULARITY, baseCost, time);
        addModSingularity(list, "advancednetherite:netherite_emerald_ingot", ModSingularities.NETHERITE_EMERALD_SINGULARITY, baseCost, time);
        addModSingularity(list, "advancednetherite:netherite_diamond_ingot", ModSingularities.NETHERITE_DIAMOND_SINGULARITY, baseCost, time);

        return list;
    }

    public static List<CustomRecipeData> getDustRecipes() {
        loadCustomRecipes();
        return new ArrayList<>(DUST_CACHE.values());
    }

    @Nullable
    public static CustomRecipeData getRecipe(Level level, ItemStack inputStack, int activeMode) {
        if (inputStack.isEmpty() || level == null) return null;
        Item inputItem = inputStack.getItem();

        loadCustomRecipes();

        RecipeManager recipeManager = level.recipeAccess() instanceof RecipeManager rm ? rm :
                (ServerLifecycleHooks.getCurrentServer() != null ? ServerLifecycleHooks.getCurrentServer().getRecipeManager() : null);

        if (recipeManager != null) {
            scanDatapackAndKubeJSRecipes(recipeManager);
        }

        // 1. Singularity Mode (Mode 1)
        if (activeMode == 1) {
            return SINGULARITY_CACHE.get(inputItem);
        }

        // 2. Compression Mode (Mode 0)
        if (activeMode == 0) {
            CustomRecipeData cached = COMPRESS_CACHE.get(inputItem);
            if (cached != null) return cached;

            if (recipeManager != null) {
                for (CustomRecipeData data : scanAutoCompressionRecipes(recipeManager)) {
                    COMPRESS_CACHE.put(data.ingredient(), data);
                    if (data.ingredient() == inputItem) return data;
                }
            }
        }

        // 3. Dust Crushing Mode (Mode 2)
        if (activeMode == 2) {
            return DUST_CACHE.get(inputItem);
        }

        return null;
    }

    private static void parseRecipeElement(JsonObject obj, int mode) {
        boolean enabled = !obj.has("enable_recipe") || obj.get("enable_recipe").getAsBoolean();
        if (!enabled) return;

        if (mode == 0 || mode == 2) {
            if (!obj.has("input") || !obj.has("output")) return;
            Identifier inputId = Identifier.tryParse(obj.get("input").getAsString());
            Identifier outputId = Identifier.tryParse(obj.get("output").getAsString());

            Item inputItem = inputId != null ? BuiltInRegistries.ITEM.get(inputId).map(Holder::value).orElse(Items.AIR) : Items.AIR;
            Item outputItem = outputId != null ? BuiltInRegistries.ITEM.get(outputId).map(Holder::value).orElse(Items.AIR) : Items.AIR;

            int count = obj.has("input_count") ? obj.get("input_count").getAsInt() : (mode == 0 ? 9 : 1);
            int time = obj.has("time_cost") ? obj.get("time_cost").getAsInt() : 20;

            if (inputItem != Items.AIR && outputItem != Items.AIR) {
                CustomRecipeData recipeData = new CustomRecipeData(inputItem, new ItemStack(outputItem), count, time);
                if (mode == 0) {
                    CUSTOM_COMPRESS_RECIPES.add(recipeData);
                    COMPRESS_CACHE.put(inputItem, recipeData);
                } else {
                    CUSTOM_DUST_RECIPES.add(recipeData);
                    DUST_CACHE.put(inputItem, recipeData);
                }
            }
        } else if (mode == 1) {
            if (!obj.has("item")) return;
            Identifier inputId = Identifier.tryParse(obj.get("item").getAsString());
            Item inputItem = inputId != null ? BuiltInRegistries.ITEM.get(inputId).map(Holder::value).orElse(Items.AIR) : Items.AIR;

            Item outputItem = Items.AIR;
            if (obj.has("output")) {
                Identifier outputId = Identifier.tryParse(obj.get("output").getAsString());
                outputItem = outputId != null ? BuiltInRegistries.ITEM.get(outputId).map(Holder::value).orElse(Items.AIR) : Items.AIR;
            } else if (obj.has("name")) {
                String rawName = obj.get("name").getAsString();
                String name = rawName.toLowerCase().replace(" ", "_").replace("'", "").replace("-", "_");
                outputItem = BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, name)).map(Holder::value).orElse(Items.AIR);
            }

            if (outputItem == Items.AIR) {
                outputItem = ModItems.PUREMASH_CORE.get();
            }

            int cost = obj.has("cost") ? obj.get("cost").getAsInt() : PureMashTweaksConfig.COMMON.compressorSingularityBaseCost.get();
            int time = obj.has("time_cost") ? obj.get("time_cost").getAsInt() : PureMashTweaksConfig.COMMON.compressorSingularitySpeed.get();

            if (inputItem != Items.AIR && outputItem != Items.AIR) {
                CustomRecipeData recipeData = new CustomRecipeData(inputItem, new ItemStack(outputItem), cost, time);
                CUSTOM_SINGULARITY_RECIPES.add(recipeData);
                SINGULARITY_CACHE.put(inputItem, recipeData);
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
                            if (element.isJsonObject()) parseRecipeElement(element.getAsJsonObject(), mode);
                        }
                    } else if (rootElement.isJsonObject()) {
                        parseRecipeElement(rootElement.getAsJsonObject(), mode);
                    }
                } catch (Exception ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private static void addInternalSingularity(List<CustomRecipeData> list, Item input, @Nullable net.neoforged.neoforge.registries.DeferredItem<dev.davidklgames.puremashtweaks.item.ColorSingularityItem> outputHolder, int cost, int time) {
        if (outputHolder != null) {
            CustomRecipeData data = new CustomRecipeData(input, new ItemStack(outputHolder.get()), cost, time);
            list.add(data);
            SINGULARITY_CACHE.put(input, data);
        }
    }

    private static void addModSingularity(List<CustomRecipeData> list, String inputIdStr, @Nullable net.neoforged.neoforge.registries.DeferredItem<dev.davidklgames.puremashtweaks.item.ColorSingularityItem> outputHolder, int cost, int time) {
        if (outputHolder != null) {
            Identifier inputId = Identifier.tryParse(inputIdStr);
            if (inputId != null) {
                Item inputItem = BuiltInRegistries.ITEM.get(inputId).map(Holder::value).orElse(Items.AIR);
                if (inputItem != Items.AIR) {
                    CustomRecipeData data = new CustomRecipeData(inputItem, new ItemStack(outputHolder.get()), cost, time);
                    list.add(data);
                    SINGULARITY_CACHE.put(inputItem, data);
                }
            }
        }
    }
}