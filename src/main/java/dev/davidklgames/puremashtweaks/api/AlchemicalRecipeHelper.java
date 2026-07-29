package dev.davidklgames.puremashtweaks.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AlchemicalRecipeHelper {
    private static final Gson GSON = new Gson();
    private static final List<ParsedRecipe> RECIPES = new ArrayList<>();
    private static final List<ParsedRecipe> AUTO_SMELTING_RECIPES = new ArrayList<>();
    private static boolean loaded = false;
    private static boolean smeltingScanned = false;

    public record ParsedRecipe(
            Item input,
            @Nullable Fluid fluid,
            int fluidAmount,
            String toolType, // "pickaxe", "shovel", "axe", "none"
            ItemStack output,
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

    public static void addDefaultRecipes() {

        // =========================================================================
        // MECHANICAL RECYCLING OF ARMOR AND TOOLS (DISASSEMBLY METHOD)
        // =========================================================================

        // --- Iron Equipment Recycling (Returns iron nuggets) ---
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.IRON_CHESTPLATE, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET, 4), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.IRON_LEGGINGS, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET, 3), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.IRON_HELMET, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET, 2), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.IRON_BOOTS, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET, 1), false));

        // --- Gold Equipment Recycling (Returns gold nuggets) ---
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.GOLDEN_CHESTPLATE, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET, 4), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.GOLDEN_LEGGINGS, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET, 3), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.GOLDEN_HELMET, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET, 2), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.GOLDEN_BOOTS, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET, 1), false));

        // --- Synthorium Equipment Recycling (Returns Synthorium Scrap) ---
        RECIPES.add(new ParsedRecipe(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_CHESTPLATE.get(), null, 0, "pickaxe", new ItemStack(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_SCRAP.get(), 4), false));
        RECIPES.add(new ParsedRecipe(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_LEGGINGS.get(), null, 0, "pickaxe", new ItemStack(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_SCRAP.get(), 3), false));
        RECIPES.add(new ParsedRecipe(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_HELMET.get(), null, 0, "pickaxe", new ItemStack(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_SCRAP.get(), 2), false));
        RECIPES.add(new ParsedRecipe(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_BOOTS.get(), null, 0, "pickaxe", new ItemStack(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_SCRAP.get(), 1), false));

        // --- STANDARD FACTORY RECIPES (KINETIC & WASHING) ---

        // Gravel + Water + No Tool (none) -> Sand
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.GRAVEL, Fluids.WATER, 250, "none", new ItemStack(net.minecraft.world.item.Items.SAND), false));

        // Gravel + Water + Shovel -> Flint (Route A)
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.GRAVEL, Fluids.WATER, 250, "shovel", new ItemStack(net.minecraft.world.item.Items.FLINT), false));
        // Sand + Water + Shovel -> Clay Ball (Route A)
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.SAND, Fluids.WATER, 250, "shovel", new ItemStack(net.minecraft.world.item.Items.CLAY_BALL), false));

        // Cobblestone + Pickaxe -> Gravel (Route B)
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.COBBLESTONE, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.GRAVEL), false));
        // Stone + Pickaxe -> Cobblestone (Route B)
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.STONE, null, 0, "pickaxe", new ItemStack(net.minecraft.world.item.Items.COBBLESTONE), false));

        // Wood Logs + Axe -> 6x Corresponding Planks (Route B)
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.OAK_LOG, null, 0, "axe", new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS, 6), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.SPRUCE_LOG, null, 0, "axe", new ItemStack(net.minecraft.world.item.Items.SPRUCE_PLANKS, 6), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.BIRCH_LOG, null, 0, "axe", new ItemStack(net.minecraft.world.item.Items.BIRCH_PLANKS, 6), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.JUNGLE_LOG, null, 0, "axe", new ItemStack(net.minecraft.world.item.Items.JUNGLE_PLANKS, 6), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.ACACIA_LOG, null, 0, "axe", new ItemStack(net.minecraft.world.item.Items.ACACIA_PLANKS, 6), false));
        RECIPES.add(new ParsedRecipe(net.minecraft.world.item.Items.DARK_OAK_LOG, null, 0, "axe", new ItemStack(net.minecraft.world.item.Items.DARK_OAK_PLANKS, 6), false));
    }

    public static void loadRecipes() {
        if (loaded) return;
        loaded = true;

        RECIPES.clear();
        addDefaultRecipes();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks/alchemical_recipes");
        if (!Files.exists(configDir)) return;

        try (var stream = Files.list(configDir)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (FileReader reader = new FileReader(path.toFile())) {
                    JsonElement rootElement = JsonParser.parseReader(reader);
                    if (rootElement.isJsonArray()) {
                        JsonArray array = rootElement.getAsJsonArray();
                        for (JsonElement element : array) {
                            if (element.isJsonObject()) {
                                parseRecipe(element.getAsJsonObject());
                            }
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
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Custom Alchemical Synthesizer recipes loaded.");
    }

    private static void parseRecipe(JsonObject obj) {
        boolean enabled = !obj.has("enable_recipe") || obj.get("enable_recipe").getAsBoolean();
        if (!enabled) return;

        if (!obj.has("input") || !obj.has("output")) return;

        Identifier inputId = Identifier.tryParse(obj.get("input").getAsString());
        Identifier outputId = Identifier.tryParse(obj.get("output").getAsString());

        if (inputId == null || outputId == null) return;

        Item inputItem = BuiltInRegistries.ITEM.get(inputId).map(net.minecraft.core.Holder::value).orElse(null);
        Item outputItem = BuiltInRegistries.ITEM.get(outputId).map(net.minecraft.core.Holder::value).orElse(null);

        if (inputItem == null || outputItem == null || inputItem == net.minecraft.world.item.Items.AIR || outputItem == net.minecraft.world.item.Items.AIR) {
            return;
        }

        Fluid requiredFluid = null;
        if (obj.has("fluid")) {
            String fluidStr = obj.get("fluid").getAsString();
            if (!fluidStr.equalsIgnoreCase("none")) {
                Identifier fluidId = Identifier.tryParse(fluidStr);
                if (fluidId != null) {
                    requiredFluid = BuiltInRegistries.FLUID.get(fluidId).map(net.minecraft.core.Holder::value).orElse(null);
                }
            }
        }

        int fluidAmount = obj.has("fluid_amount") ? obj.get("fluid_amount").getAsInt() : 0;
        String toolType = obj.has("tool_type") ? obj.get("tool_type").getAsString().toLowerCase() : "none";
        int outputCount = obj.has("output_count") ? obj.get("output_count").getAsInt() : 1;
        boolean doubleOutput = obj.has("double_output") && obj.get("double_output").getAsBoolean();

        RECIPES.add(new ParsedRecipe(
                inputItem,
                requiredFluid,
                fluidAmount,
                toolType,
                new ItemStack(outputItem, outputCount),
                doubleOutput
        ));
    }

    @SuppressWarnings("deprecation")
    public static void scanAutoSmeltingRecipes(RecipeManager recipeManager) {
        if (smeltingScanned) return;
        smeltingScanned = true;
        AUTO_SMELTING_RECIPES.clear();

        if (recipeManager == null) return;

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof net.minecraft.world.item.crafting.SmeltingRecipe smeltingRecipe) {
                List<Ingredient> ingredients = smeltingRecipe.placementInfo().ingredients();
                if (ingredients.size() == 1) {
                    Ingredient inputIng = ingredients.getFirst();
                    if (!inputIng.isEmpty()) {
                        ItemStack finalResult = smeltingRecipe.assemble(null);
                        inputIng.items().forEach(holderItem -> {
                            Item item = holderItem.value();
                            if (item != net.minecraft.world.item.Items.AIR && !finalResult.isEmpty()) {
                                addScannedRecipe(item, finalResult);
                            }
                        });
                    }
                }
            } else if (holder.value() instanceof net.minecraft.world.item.crafting.BlastingRecipe blastingRecipe) {
                List<Ingredient> ingredients = blastingRecipe.placementInfo().ingredients();
                if (ingredients.size() == 1) {
                    Ingredient inputIng = ingredients.getFirst();
                    if (!inputIng.isEmpty()) {
                        ItemStack finalResult = blastingRecipe.assemble(null);
                        inputIng.items().forEach(holderItem -> {
                            Item item = holderItem.value();
                            if (item != net.minecraft.world.item.Items.AIR && !finalResult.isEmpty()) {
                                addScannedRecipe(item, finalResult);
                            }
                        });
                    }
                }
            }
        }
    }

    private static void addScannedRecipe(Item item, ItemStack resultStack) {
        boolean duplicate = false;
        for (ParsedRecipe existing : AUTO_SMELTING_RECIPES) {
            if (existing.input() == item && !existing.doubleOutput() && existing.toolType().equals("none")) {
                duplicate = true;
                break;
            }
        }

        if (!duplicate) {
            // Route C: Normal Smelting with Lava (Available out of the box for any fuel item)
            // No physical tools associated with this route.
            AUTO_SMELTING_RECIPES.add(new ParsedRecipe(
                    item,
                    Fluids.LAVA,
                    250,
                    "none",
                    resultStack.copy(),
                    false
            ));
        }
    }

    @Nullable
    public static ParsedRecipe getRecipe(Fluid activeFluid, ItemStack inputStack, ItemStack toolStack, @Nullable RecipeManager recipeManager) {
        loadRecipes();
        if (recipeManager != null) {
            scanAutoSmeltingRecipes(recipeManager);
        }

        for (ParsedRecipe recipe : RECIPES) {
            if (recipe.matches(activeFluid, inputStack, toolStack)) {
                return recipe;
            }
        }

        for (ParsedRecipe recipe : AUTO_SMELTING_RECIPES) {
            if (recipe.matches(activeFluid, inputStack, toolStack)) {
                return recipe;
            }
        }
        return null;
    }

    public static List<ParsedRecipe> getRecipes() {
        loadRecipes();
        List<ParsedRecipe> all = new ArrayList<>(RECIPES);
        all.addAll(AUTO_SMELTING_RECIPES);
        return all;
    }
}