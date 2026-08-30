package dev.davidklgames.puremashtweaks.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.recipe.AlchemicalRecipe;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModRecipes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AlchemicalRecipeHelper {
    private static final Gson GSON = new Gson();
    private static final List<ParsedRecipe> RECIPES = new ArrayList<>();
    private static final List<ParsedRecipe> AUTO_SMELTING_RECIPES = new ArrayList<>();

    private static final Map<Item, List<ParsedRecipe>> RECIPE_CACHE = new HashMap<>();

    public static void reset() {
        loaded = false;
        smeltingScanned = false;
        RECIPES.clear();
        RECIPE_CACHE.clear();
        AUTO_SMELTING_RECIPES.clear();
    }

    private static boolean loaded = false;
    private static boolean smeltingScanned = false;

    public record ParsedRecipe(
            Item input,
            @Nullable Fluid fluid,
            int fluidAmount,
            String toolType, // "pickaxe", "shovel", "axe", "none"
            ItemStack output,
            int time,        // In ticks
            int energyCost,  // In FE/t
            boolean doubleOutput
    ) {
        public boolean matches(Fluid activeFluid, ItemStack inputStack, ItemStack toolStack) {
            if (inputStack.isEmpty() || inputStack.getItem() != this.input) {
                return false;
            }

            if (this.fluid != null) {
                if (activeFluid != this.fluid) {
                    return false;
                }
            }

            return switch (this.toolType) {
                case "pickaxe" -> toolStack.is(ItemTags.PICKAXES);
                case "shovel" -> toolStack.is(ItemTags.SHOVELS);
                case "axe" -> toolStack.is(ItemTags.AXES);
                default -> true;
            };
        }
    }

    public static int calculateSmeltingTime(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase();

        if (path.contains("synthorium") || path.contains("moldelonian") ||
                path.contains("unobtainium") || path.contains("vibranium") ||
                path.contains("allthemodium") || path.contains("insanium") ||
                path.contains("quantum") || path.contains("alloy")) {
            return 35;
        }
        if (path.contains("netherite") || path.contains("ancient_debris")) {
            return 30;
        }
        if (path.contains("diamond") || path.contains("emerald") ||
                path.contains("uranium") || path.contains("platinum") ||
                path.contains("deorum") || path.contains("osmium") ||
                path.contains("sapphire") || path.contains("ruby")) {
            return 25;
        }
        return 20;
    }

    public static int calculateSmeltingEnergy(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase();

        if (path.contains("synthorium") || path.contains("moldelonian") ||
                path.contains("unobtainium") || path.contains("vibranium") ||
                path.contains("allthemodium") || path.contains("insanium") ||
                path.contains("quantum") || path.contains("alloy")) {
            return 500;
        }
        if (path.contains("netherite") || path.contains("ancient_debris")) {
            return 300;
        }
        if (path.contains("diamond") || path.contains("emerald") ||
                path.contains("uranium") || path.contains("platinum") ||
                path.contains("deorum") || path.contains("osmium") ||
                path.contains("sapphire") || path.contains("ruby")) {
            return 180;
        }
        return 100;
    }

    public static void addDefaultRecipes() {
        addRecipeEntry(new ParsedRecipe(Items.IRON_CHESTPLATE, null, 0, "pickaxe", new ItemStack(Items.IRON_NUGGET, 4), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.IRON_LEGGINGS, null, 0, "pickaxe", new ItemStack(Items.IRON_NUGGET, 3), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.IRON_HELMET, null, 0, "pickaxe", new ItemStack(Items.IRON_NUGGET, 2), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.IRON_BOOTS, null, 0, "pickaxe", new ItemStack(Items.IRON_NUGGET, 1), 20, 100, false));

        addRecipeEntry(new ParsedRecipe(Items.GOLDEN_CHESTPLATE, null, 0, "pickaxe", new ItemStack(Items.GOLD_NUGGET, 4), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.GOLDEN_LEGGINGS, null, 0, "pickaxe", new ItemStack(Items.GOLD_NUGGET, 3), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.GOLDEN_HELMET, null, 0, "pickaxe", new ItemStack(Items.GOLD_NUGGET, 2), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.GOLDEN_BOOTS, null, 0, "pickaxe", new ItemStack(Items.GOLD_NUGGET, 1), 20, 100, false));

        addRecipeEntry(new ParsedRecipe(ModItems.SYNTHORIUM_CHESTPLATE.get(), null, 0, "pickaxe", new ItemStack(ModItems.SYNTHORIUM_SCRAP.get(), 4), 35, 500, false));
        addRecipeEntry(new ParsedRecipe(ModItems.SYNTHORIUM_LEGGINGS.get(), null, 0, "pickaxe", new ItemStack(ModItems.SYNTHORIUM_SCRAP.get(), 3), 35, 500, false));
        addRecipeEntry(new ParsedRecipe(ModItems.SYNTHORIUM_HELMET.get(), null, 0, "pickaxe", new ItemStack(ModItems.SYNTHORIUM_SCRAP.get(), 2), 35, 500, false));
        addRecipeEntry(new ParsedRecipe(ModItems.SYNTHORIUM_BOOTS.get(), null, 0, "pickaxe", new ItemStack(ModItems.SYNTHORIUM_SCRAP.get(), 1), 35, 500, false));

        addRecipeEntry(new ParsedRecipe(Items.GRAVEL, Fluids.WATER, 250, "none", new ItemStack(Items.SAND), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.GRAVEL, Fluids.WATER, 250, "shovel", new ItemStack(Items.FLINT), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.SAND, Fluids.WATER, 250, "shovel", new ItemStack(Items.CLAY_BALL), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.COBBLESTONE, null, 0, "pickaxe", new ItemStack(Items.GRAVEL), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.STONE, null, 0, "pickaxe", new ItemStack(Items.COBBLESTONE), 20, 100, false));

        addRecipeEntry(new ParsedRecipe(Items.OAK_LOG, null, 0, "axe", new ItemStack(Items.OAK_PLANKS, 6), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.SPRUCE_LOG, null, 0, "axe", new ItemStack(Items.SPRUCE_PLANKS, 6), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.BIRCH_LOG, null, 0, "axe", new ItemStack(Items.BIRCH_PLANKS, 6), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.JUNGLE_LOG, null, 0, "axe", new ItemStack(Items.JUNGLE_PLANKS, 6), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.ACACIA_LOG, null, 0, "axe", new ItemStack(Items.ACACIA_PLANKS, 6), 20, 100, false));
        addRecipeEntry(new ParsedRecipe(Items.DARK_OAK_LOG, null, 0, "axe", new ItemStack(Items.DARK_OAK_PLANKS, 6), 20, 100, false));
    }

    private static void addRecipeEntry(ParsedRecipe recipe) {
        List<ParsedRecipe> list = RECIPE_CACHE.computeIfAbsent(recipe.input(), k -> new ArrayList<>());
        for (ParsedRecipe existing : list) {
            if (existing.input() == recipe.input() &&
                    Objects.equals(existing.fluid(), recipe.fluid()) &&
                    existing.toolType().equals(recipe.toolType()) &&
                    ItemStack.isSameItemSameComponents(existing.output(), recipe.output())) {
                return;
            }
        }
        RECIPES.add(recipe);
        list.add(recipe);
    }

    public static boolean isRecipeRegisteredFor(Item item) {
        List<ParsedRecipe> list = RECIPE_CACHE.get(item);
        return list != null && !list.isEmpty();
    }

    public static void loadRecipes() {
        if (loaded) return;
        loaded = true;

        RECIPES.clear();
        RECIPE_CACHE.clear();
        AUTO_SMELTING_RECIPES.clear();
        addDefaultRecipes();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            String p = BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase();
            if (p.endsWith("_dust") || p.endsWith("_powder") || p.startsWith("dust_")) {
                if (!isRecipeRegisteredFor(item)) {
                    Item processed = resolveProcessedMaterialFromDust(item);
                    if (processed != null && processed != Items.AIR && processed != item) {
                        int time = calculateSmeltingTime(item);
                        int energy = calculateSmeltingEnergy(item);
                        ParsedRecipe autoRecipe = new ParsedRecipe(item, null, 0, "none", new ItemStack(processed), time, energy, false);
                        addRecipeEntry(autoRecipe);
                    }
                }
            }
        }

        Path configDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks/alchemical_recipes");
        if (!Files.exists(configDir)) return;

        try (var stream = Files.list(configDir)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (FileReader reader = new FileReader(path.toFile())) {
                    JsonElement rootElement = JsonParser.parseReader(reader);
                    if (rootElement.isJsonArray()) {
                        JsonArray array = rootElement.getAsJsonArray();
                        for (JsonElement element : array) {
                            if (element.isJsonObject()) parseRecipe(element.getAsJsonObject());
                        }
                    } else if (rootElement.isJsonObject()) {
                        parseRecipe(rootElement.getAsJsonObject());
                    }
                } catch (Exception e) {
                    PureMashTweaks.LOGGER.error("[PureMash]: Failed to load alchemical recipe file: {}", path, e);
                }
            });
        } catch (IOException e) {
            PureMashTweaks.LOGGER.error("[PureMash]: Failed to list alchemical recipes folder: {}", configDir, e);
        }
    }

    private static void parseRecipe(JsonObject obj) {
        boolean enabled = !obj.has("enable_recipe") || obj.get("enable_recipe").getAsBoolean();
        if (!enabled) return;

        if (!obj.has("input") || !obj.has("output")) return;

        Identifier inputId = Identifier.tryParse(obj.get("input").getAsString());
        Identifier outputId = Identifier.tryParse(obj.get("output").getAsString());

        if (inputId == null || outputId == null) return;

        Item inputItem = BuiltInRegistries.ITEM.get(inputId).map(Holder::value).orElse(null);
        Item outputItem = BuiltInRegistries.ITEM.get(outputId).map(Holder::value).orElse(null);

        if (inputItem == null || outputItem == null || inputItem == Items.AIR || outputItem == Items.AIR) {
            return;
        }

        Fluid requiredFluid = null;
        if (obj.has("fluid")) {
            String fluidStr = obj.get("fluid").getAsString();
            if (!fluidStr.equalsIgnoreCase("none") && !fluidStr.isEmpty()) {
                Identifier fluidId = Identifier.tryParse(fluidStr);
                if (fluidId != null) {
                    requiredFluid = BuiltInRegistries.FLUID.get(fluidId).map(Holder::value).orElse(null);
                }
            }
        }

        int fluidAmount = obj.has("fluid_amount") ? obj.get("fluid_amount").getAsInt() : 0;
        String toolType = obj.has("tool_type") ? obj.get("tool_type").getAsString().toLowerCase() : "none";
        int outputCount = obj.has("output_count") ? obj.get("output_count").getAsInt() : 1;
        boolean doubleOutput = obj.has("double_output") && obj.get("double_output").getAsBoolean();

        int time = obj.has("time") ? obj.get("time").getAsInt() : calculateSmeltingTime(inputItem);
        int energy = obj.has("energy") ? obj.get("energy").getAsInt() : calculateSmeltingEnergy(inputItem);

        addRecipeEntry(new ParsedRecipe(
                inputItem,
                requiredFluid,
                fluidAmount,
                toolType,
                new ItemStack(outputItem, outputCount),
                time,
                energy,
                doubleOutput
        ));
    }

    public static void scanAutoSmeltingRecipes(RecipeManager recipeManager) {
        if (smeltingScanned || recipeManager == null) return;
        smeltingScanned = true;
        AUTO_SMELTING_RECIPES.clear();

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            // Scan KubeJS / Datapack Alchemical Recipes
            if (holder.value() instanceof AlchemicalRecipe alchemical) {
                for (Holder<Item> itemHolder : alchemical.getInput().items().toList()) {
                    Item inItem = itemHolder.value();
                    if (inItem != Items.AIR) {
                        ItemStack output = alchemical.assemble(new SingleRecipeInput(new ItemStack(inItem)));
                        ParsedRecipe parsed = new ParsedRecipe(
                                inItem,
                                alchemical.getRequiredFluid(),
                                alchemical.getFluidAmount(),
                                alchemical.getToolType(),
                                output,
                                alchemical.getTime(),
                                alchemical.getEnergy(),
                                alchemical.isDoubleOutput()
                        );
                        addRecipeEntry(parsed);
                    }
                }
            }
            // Auto-smelting scanning
            else if (holder.value() instanceof SmeltingRecipe smeltingRecipe) {
                List<Ingredient> ingredients = smeltingRecipe.placementInfo().ingredients();
                if (ingredients.size() == 1) {
                    Ingredient inputIng = ingredients.getFirst();
                    if (!inputIng.isEmpty()) {
                        inputIng.items().forEach(holderItem -> {
                            Item item = holderItem.value();
                            if (item != Items.AIR) {
                                ItemStack finalResult = smeltingRecipe.assemble(new SingleRecipeInput(new ItemStack(item)));
                                if (!finalResult.isEmpty()) addScannedRecipe(item, finalResult);
                            }
                        });
                    }
                }
            } else if (holder.value() instanceof BlastingRecipe blastingRecipe) {
                List<Ingredient> ingredients = blastingRecipe.placementInfo().ingredients();
                if (ingredients.size() == 1) {
                    Ingredient inputIng = ingredients.getFirst();
                    if (!inputIng.isEmpty()) {
                        inputIng.items().forEach(holderItem -> {
                            Item item = holderItem.value();
                            if (item != Items.AIR) {
                                ItemStack finalResult = blastingRecipe.assemble(new SingleRecipeInput(new ItemStack(item)));
                                if (!finalResult.isEmpty()) addScannedRecipe(item, finalResult);
                            }
                        });
                    }
                }
            }
        }
    }

    private static void addScannedRecipe(Item item, ItemStack resultStack) {
        if (isRecipeRegisteredFor(item)) {
            return;
        }

        for (ParsedRecipe existing : AUTO_SMELTING_RECIPES) {
            if (existing.input() == item) {
                return;
            }
        }

        int time = calculateSmeltingTime(item);
        int energy = calculateSmeltingEnergy(item);

        ParsedRecipe normalSmelt = new ParsedRecipe(
                item,
                null,
                0,
                "none",
                resultStack.copy(),
                time,
                energy,
                false
        );
        AUTO_SMELTING_RECIPES.add(normalSmelt);
        RECIPE_CACHE.computeIfAbsent(item, k -> new ArrayList<>()).add(normalSmelt);
    }

    @Nullable
    private static Item resolveProcessedMaterialFromDust(Item dustItem) {
        Identifier dustId = BuiltInRegistries.ITEM.getKey(dustItem);
        String path = dustId.getPath().toLowerCase();
        String namespace = dustId.getNamespace();

        String baseName = null;
        if (path.endsWith("_dust")) {
            baseName = path.substring(0, path.length() - 5);
        } else if (path.endsWith("_powder")) {
            baseName = path.substring(0, path.length() - 7);
        } else if (path.startsWith("dust_")) {
            baseName = path.substring(5);
        }

        if (baseName == null || baseName.isEmpty()) return null;

        TagKey<Item> ingotTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/" + baseName));
        var ingotHolders = BuiltInRegistries.ITEM.get(ingotTag);
        if (ingotHolders.isPresent()) {
            var first = ingotHolders.get().stream().findFirst();
            if (first.isPresent() && first.get().value() != Items.AIR && first.get().value() != dustItem) {
                return first.get().value();
            }
        }

        TagKey<Item> gemTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "gems/" + baseName));
        var gemHolders = BuiltInRegistries.ITEM.get(gemTag);
        if (gemHolders.isPresent()) {
            var first = gemHolders.get().stream().findFirst();
            if (first.isPresent() && first.get().value() != Items.AIR && first.get().value() != dustItem) {
                return first.get().value();
            }
        }

        List<Identifier> candidateIds = List.of(
                Identifier.fromNamespaceAndPath(namespace, baseName + "_ingot"),
                Identifier.fromNamespaceAndPath(namespace, baseName + "_gem"),
                Identifier.fromNamespaceAndPath(namespace, baseName),
                Identifier.fromNamespaceAndPath("minecraft", baseName + "_ingot"),
                Identifier.fromNamespaceAndPath("minecraft", baseName)
        );

        for (Identifier id : candidateIds) {
            var holder = BuiltInRegistries.ITEM.get(id);
            if (holder.isPresent() && holder.get().value() != Items.AIR && holder.get().value() != dustItem) {
                return holder.get().value();
            }
        }

        if (baseName.equals("diamond")) return Items.DIAMOND;
        if (baseName.equals("emerald")) return Items.EMERALD;
        if (baseName.equals("lapis")) return Items.LAPIS_LAZULI;
        if (baseName.equals("amethyst")) return Items.AMETHYST_SHARD;
        if (baseName.equals("quartz")) return Items.QUARTZ;

        return null;
    }

    @Nullable
    public static ParsedRecipe getRecipe(Fluid activeFluid, ItemStack inputStack, ItemStack toolStack, @Nullable RecipeManager recipeManager) {
        if (inputStack.isEmpty()) return null;

        loadRecipes();

        if (recipeManager != null) {
            scanAutoSmeltingRecipes(recipeManager);
        }

        List<ParsedRecipe> candidates = RECIPE_CACHE.get(inputStack.getItem());
        if (candidates != null) {
            for (ParsedRecipe recipe : candidates) {
                if (recipe.matches(activeFluid, inputStack, toolStack)) {
                    return recipe;
                }
            }
        }

        if (recipeManager != null && (activeFluid == null || activeFluid == Fluids.EMPTY)) {
            SingleRecipeInput singleInput = new SingleRecipeInput(inputStack);

            Optional<RecipeHolder<BlastingRecipe>> blastingMatch = recipeManager.getRecipeFor(RecipeType.BLASTING, singleInput, null);
            if (blastingMatch.isPresent()) {
                ItemStack result = blastingMatch.get().value().assemble(singleInput);
                if (!result.isEmpty()) {
                    int time = calculateSmeltingTime(inputStack.getItem());
                    int energy = calculateSmeltingEnergy(inputStack.getItem());
                    ParsedRecipe dynamicRecipe = new ParsedRecipe(inputStack.getItem(), null, 0, "none", result.copy(), time, energy, false);
                    addRecipeEntry(dynamicRecipe);
                    return dynamicRecipe;
                }
            }

            Optional<RecipeHolder<SmeltingRecipe>> smeltingMatch = recipeManager.getRecipeFor(RecipeType.SMELTING, singleInput, null);
            if (smeltingMatch.isPresent()) {
                ItemStack result = smeltingMatch.get().value().assemble(singleInput);
                if (!result.isEmpty()) {
                    int time = calculateSmeltingTime(inputStack.getItem());
                    int energy = calculateSmeltingEnergy(inputStack.getItem());
                    ParsedRecipe dynamicRecipe = new ParsedRecipe(inputStack.getItem(), null, 0, "none", result.copy(), time, energy, false);
                    addRecipeEntry(dynamicRecipe);
                    return dynamicRecipe;
                }
            }
        }

        Item processedItem = resolveProcessedMaterialFromDust(inputStack.getItem());
        if (processedItem != null && processedItem != Items.AIR) {
            int time = calculateSmeltingTime(inputStack.getItem());
            int energy = calculateSmeltingEnergy(inputStack.getItem());
            ParsedRecipe dynamicRecipe = new ParsedRecipe(
                    inputStack.getItem(),
                    null,
                    0,
                    "none",
                    new ItemStack(processedItem),
                    time,
                    energy,
                    false
            );
            addRecipeEntry(dynamicRecipe);
            return dynamicRecipe;
        }

        return null;
    }

    public static List<ParsedRecipe> getRecipes() {
        loadRecipes();
        Map<String, ParsedRecipe> uniqueMap = new LinkedHashMap<>();
        for (ParsedRecipe r : RECIPES) {
            String key = BuiltInRegistries.ITEM.getKey(r.input()) + "->" + BuiltInRegistries.ITEM.getKey(r.output().getItem()) + ":" + r.toolType() + ":" + (r.fluid() != null ? BuiltInRegistries.FLUID.getKey(r.fluid()) : "none");
            uniqueMap.putIfAbsent(key, r);
        }
        for (ParsedRecipe r : AUTO_SMELTING_RECIPES) {
            String key = BuiltInRegistries.ITEM.getKey(r.input()) + "->" + BuiltInRegistries.ITEM.getKey(r.output().getItem()) + ":" + r.toolType() + ":" + (r.fluid() != null ? BuiltInRegistries.FLUID.getKey(r.fluid()) : "none");
            uniqueMap.putIfAbsent(key, r);
        }
        return new ArrayList<>(uniqueMap.values());
    }
}