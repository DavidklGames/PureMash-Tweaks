package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.HolderLookup;
import dev.davidklgames.puremashtweaks.recipe.ShapedSynthesisRecipe;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    private final HolderLookup.Provider lookupProvider; // Safely stores the registries!

    // Updated constructor to save the reference
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.lookupProvider = registries;
    }

    @Override
    protected void buildRecipes() {

        // =========================================================================
        // SYNTHORIUM ARMOR (Native and clean using instance methods)
        // =========================================================================

        this.shaped(RecipeCategory.COMBAT, ModItems.SYNTHORIUM_HELMET.get())
                .pattern("III")
                .pattern("I I")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.COMBAT, ModItems.SYNTHORIUM_CHESTPLATE.get())
                .pattern("I I")
                .pattern("III")
                .pattern("III")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.COMBAT, ModItems.SYNTHORIUM_LEGGINGS.get())
                .pattern("III")
                .pattern("I I")
                .pattern("I I")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.COMBAT, ModItems.SYNTHORIUM_BOOTS.get())
                .pattern("I I")
                .pattern("I I")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        // =========================================================================
        // NEW RECIPES: SMELT SYNTHORIUM DUST TO GENERATE 2x INGOTS
        // =========================================================================

        // Smelt Synthorium Dust in a normal furnace (Yield: 2x Ingots)
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.SYNTHORIUM_DUST.get()),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        new net.minecraft.world.item.ItemStackTemplate(ModItems.SYNTHORIUM_INGOT.get(), 1),
                        1.0f,
                        200
                )
                .unlockedBy("has_synthorium_dust", this.has(ModItems.SYNTHORIUM_DUST.get()))
                .save(this.output, "synthorium_ingots_from_smelting_dust");

        // Smelt Synthorium Dust in a blast furnace (Yield: 2x Ingots in half the time)
        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.SYNTHORIUM_DUST.get()),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        new net.minecraft.world.item.ItemStackTemplate(ModItems.SYNTHORIUM_INGOT.get(), 1),
                        1.0f,
                        100
                )
                .unlockedBy("has_synthorium_dust", this.has(ModItems.SYNTHORIUM_DUST.get()))
                .save(this.output, "synthorium_ingots_from_blasting_dust");

        // =========================================================================
        // COMPRESSOR SPEED UPGRADES (Speed Upgrades)
        // =========================================================================

        // Speed Upgrade Tier 1 (R=Repeater, P=Paper, E=Redstone, S=Sugar)
        this.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_1.get())
                .pattern("RPR")
                .pattern("PEP")
                .pattern("SPR")
                .define('R', Items.REPEATER)
                .define('P', Items.PAPER)
                .define('E', Items.REDSTONE)
                .define('S', Items.SUGAR)
                .unlockedBy("has_repeater", this.has(Items.REPEATER))
                .save(this.output);

        // Speed Upgrade Tier 2 (R=Repeater, I=Synthorium Ingot, U=Speed Upgrade 1, D=Diamond)
        this.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_2.get())
                .pattern("DID")
                .pattern("IUI")
                .pattern("DID")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('U', ModItems.SPEED_UPGRADE_1.get())
                .define('D', Items.DIAMOND)
                .unlockedBy("has_speed_upgrade_1", this.has(ModItems.SPEED_UPGRADE_1.get()))
                .save(this.output);

        // Speed Upgrade Tier 3 (New Recipe: M=Moldelonian Ingot, C=PureMash Core, U=Speed Upgrade 2)
        this.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_3.get())
                .pattern("UMU")
                .pattern("MCM")
                .pattern("UMU")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('C', ModItems.PUREMASH_CORE.get())
                .define('U', ModItems.SPEED_UPGRADE_2.get())
                .unlockedBy("has_speed_upgrade_2", this.has(ModItems.SPEED_UPGRADE_2.get()))
                .save(this.output);

        // =========================================================================
        // SYNTHORIUM TOOLS
        // =========================================================================

        this.shaped(RecipeCategory.COMBAT, ModItems.SYNTHORIUM_SWORD.get())
                .pattern("I")
                .pattern("I")
                .pattern("R")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('R', ModItems.SYNTHORIUM_ROD.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.TOOLS, ModItems.SYNTHORIUM_PICKAXE.get())
                .pattern("III")
                .pattern(" R ")
                .pattern(" R ")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('R', ModItems.SYNTHORIUM_ROD.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.TOOLS, ModItems.SYNTHORIUM_SHOVEL.get())
                .pattern("I")
                .pattern("R")
                .pattern("R")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('R', ModItems.SYNTHORIUM_ROD.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.TOOLS, ModItems.SYNTHORIUM_AXE.get())
                .pattern("II")
                .pattern("IR")
                .pattern(" R")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('R', ModItems.SYNTHORIUM_ROD.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.TOOLS, ModItems.SYNTHORIUM_HOE.get())
                .pattern("II")
                .pattern(" R")
                .pattern(" R")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('R', ModItems.SYNTHORIUM_ROD.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        // =========================================================================
        // SYNTHORIUM PAXEL (Pickaxe, Shovel, Axe + Bar + Rod)
        // =========================================================================

        this.shaped(RecipeCategory.TOOLS, ModItems.SYNTHORIUM_PAXEL.get())
                .pattern("PSA")
                .pattern(" I ")
                .pattern(" R ")
                .define('P', ModItems.SYNTHORIUM_PICKAXE.get())
                .define('S', ModItems.SYNTHORIUM_SHOVEL.get())
                .define('A', ModItems.SYNTHORIUM_AXE.get())
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('R', ModItems.SYNTHORIUM_ROD.get())
                .unlockedBy("has_synthorium_tools", this.has(ModItems.SYNTHORIUM_PICKAXE.get()))
                .save(this.output);

        // =========================================================================
        // ORE AND METAL PROCESSING (Smelting, Blasting, Nugget <-> Ingot)
        // =========================================================================

        // Smelt Synthorium Debris in a normal furnace to generate Scrap
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SYNTHORIUM_DEBRIS.get()), RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.SYNTHORIUM_SCRAP.get(), 2.0f, 200)
                .unlockedBy("has_synthorium_debris", this.has(ModBlocks.SYNTHORIUM_DEBRIS.get()))
                .save(this.output, "synthorium_scrap_from_smelting");

        // Smelt Synthorium Debris in a blast furnace (Blasting)
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModBlocks.SYNTHORIUM_DEBRIS.get()), RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.SYNTHORIUM_SCRAP.get(), 2.0f, 100)
                .unlockedBy("has_synthorium_debris", this.has(ModBlocks.SYNTHORIUM_DEBRIS.get()))
                .save(this.output, "synthorium_scrap_from_blasting");

        // Shapeless craft for Synthorium Ingot (4 Scrap + 4 Diamonds, Netherite style)
        this.shapeless(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get(), 1)
                .requires(ModItems.SYNTHORIUM_SCRAP.get(), 4)
                .requires(Items.DIAMOND, 4)
                .unlockedBy("has_synthorium_scrap", this.has(ModItems.SYNTHORIUM_SCRAP.get()))
                .save(this.output, net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.RECIPE,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthorium_ingot")
                ));

        // =========================================================================
        // ALCHEMICAL SYNTHESIZER (Alchemical Synthesizer)
        // =========================================================================
        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.ALCHEMICAL_SYNTHESIZER.get())
                .pattern("MBM")
                .pattern("SCS")
                .pattern("MRM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('B', Items.BUCKET)
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('C', Items.CAULDRON)
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output);

        // Unpack Synthorium Ingot -> 9 Nuggets
        this.shapeless(RecipeCategory.MISC, ModItems.SYNTHORIUM_NUGGET.get(), 9)
                .requires(ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output, "synthorium_nuggets_from_ingot");

        // Pack 9 Synthorium Nuggets -> 1 Ingot
        this.shaped(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.SYNTHORIUM_NUGGET.get())
                .unlockedBy("has_synthorium_nugget", this.has(ModItems.SYNTHORIUM_NUGGET.get()))
                .save(this.output, "synthorium_ingot_from_nuggets");

        // Pack 9 Synthorium Ingots -> 1 Block
        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SYNTHORIUM_BLOCK.get())
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output, "synthorium_block_from_ingots");

        // Unpack Synthorium Block -> 9 Ingots
        this.shapeless(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get(), 9)
                .requires(ModBlocks.SYNTHORIUM_BLOCK.get())
                .unlockedBy("has_synthorium_block", this.has(ModBlocks.SYNTHORIUM_BLOCK.get()))
                .save(this.output, "synthorium_ingots_from_block");

        // =========================================================================
        // DYNAMIC MYSTICAL AGRICULTURE COMPATIBILITY
        // =========================================================================
        net.minecraft.world.item.Item synthoriumEssence = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("mysticalagriculture", "synthorium_essence")
        ).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);

        if (synthoriumEssence != net.minecraft.world.item.Items.AIR) {
            RecipeOutput mysticalAgriOutput = this.output.withConditions(
                    new net.neoforged.neoforge.common.conditions.ModLoadedCondition("mysticalagriculture")
            );

            // Shaped recipe generation via Essence - ID hardcoded as puremashtweaks:synthorium_ingot_from_essence
            this.shaped(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get())
                    .pattern("EEE")
                    .pattern("EEE")
                    .pattern("EEE")
                    .define('E', synthoriumEssence)
                    .unlockedBy("has_synthorium_essence", this.has(synthoriumEssence))
                    .save(mysticalAgriOutput, net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.RECIPE,
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthorium_ingot_from_essence")
                    ));
        }

        // PureMash Core Block (M = Moldelonian Ingot, C = PureMash Core, S = Synthorium Block)
        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PUREMASH_CORE_BLOCK.get())
                .pattern("MCM")
                .pattern("CSC")
                .pattern("MCM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('C', ModItems.PUREMASH_CORE.get())
                .define('S', ModBlocks.SYNTHORIUM_BLOCK.get())
                .unlockedBy("has_puremash_core", this.has(ModItems.PUREMASH_CORE.get()))
                .save(this.output);

        // =========================================================================
        // MOLDELONIAN PACKING
        // =========================================================================

        // Pack 9 Moldelonian Ingots -> 1 Block
        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MOLDELONIAN_BLOCK.get())
                .pattern("MMM")
                .pattern("MMM")
                .pattern("MMM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "moldelonian_block_from_ingots");

        // Unpack Moldelonian Block -> 9 Ingots
        this.shapeless(RecipeCategory.MISC, ModItems.MOLDELONIAN_INGOT.get(), 9)
                .requires(ModBlocks.MOLDELONIAN_BLOCK.get())
                .unlockedBy("has_moldelonian_block", this.has(ModBlocks.MOLDELONIAN_BLOCK.get()))
                .save(this.output, "moldelonian_ingots_from_block");

        // Unpack Moldelonian Ingot -> 9 Nuggets
        this.shapeless(RecipeCategory.MISC, ModItems.MOLDELONIAN_NUGGET.get(), 9)
                .requires(ModItems.MOLDELONIAN_INGOT.get())
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "moldelonian_nuggets_from_ingot");

        // Pack 9 Moldelonian Nuggets -> 1 Ingot
        this.shaped(RecipeCategory.MISC, ModItems.MOLDELONIAN_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.MOLDELONIAN_NUGGET.get())
                .unlockedBy("has_moldelonian_nugget", this.has(ModItems.MOLDELONIAN_NUGGET.get()))
                .save(this.output, "moldelonian_ingot_from_nuggets");

        // =========================================================================
        // OTHER COMPONENTS AND AUTOMATION COMPONENTS
        // =========================================================================

        // Memory Card (R=Redstone, N=Synthorium Nugget, P=Paper)
        this.shaped(RecipeCategory.MISC, ModItems.MEMORY_CARD.get())
                .pattern(" R ")
                .pattern("RNR")
                .pattern("PPP")
                .define('R', Items.REDSTONE)
                .define('N', ModItems.SYNTHORIUM_NUGGET.get())
                .define('P', Items.PAPER)
                .unlockedBy("has_synthorium_nugget", this.has(ModItems.SYNTHORIUM_NUGGET.get()))
                .save(this.output);

        // Synthorium Rod (3 Synthorium Ingots vertically)
        this.shaped(RecipeCategory.MISC, ModItems.SYNTHORIUM_ROD.get(), 4)
                .pattern("I")
                .pattern("I")
                .pattern("I")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        // Synthesis Table (X=DIAMOND BLOCK, Y=BEACON, Z=Synthorium Block, C=Crafter)
        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.SYNTHESIS_TABLE.get())
                .pattern("XYX")
                .pattern("ZCZ")
                .pattern("XZX")
                .define('X', Blocks.DIAMOND_BLOCK)
                .define('Y', Items.BEACON)
                .define('Z', ModBlocks.SYNTHORIUM_BLOCK.get())
                .define('C', Blocks.CRAFTER)
                .unlockedBy("has_puremash_core", this.has(Items.DIAMOND))
                .save(this.output);

        // Compressor (M=Moldelonian Ingot, S=Synthorium Block, P=Paxel)
        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get())
                .pattern("MSM")
                .pattern("SPS")
                .pattern("MSM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('S', ModBlocks.SYNTHORIUM_BLOCK.get())
                .define('P', ModItems.SYNTHORIUM_PAXEL.get())
                .unlockedBy("has_synthorium_paxel", this.has(ModItems.SYNTHORIUM_PAXEL.get()))
                .save(this.output);

        // --- CREATIVE ESSENCE FALLBACK RECIPE ---
        this.registerCreativeEssenceFallback(this.output);

        // =========================================================================
        // SYNTHESIS TABLE RECIPES (9x9)
        // =========================================================================

        // --- PUREMASH CORE RECIPE ---
        registerPureMashCore(this.output);

        // --- MOLDELONIAN CORE RECIPE ---
        registerMoldelonianCore(this.output);

        // --- DYNAMIC MOLDELONIAN INGOT RECIPE ---
        this.registerMoldelonianIngotRecipe(this.output);

        // --- SUPREME SINGULARITY RECIPE (COSMIC SINGULARITY) ---
        this.registerCosmicSingularityRecipe(this.output);

        // --- CREATIVE FLUID TANK RECIPE ---
        this.registerCreativeFluidTank(this.output);

        // --- NORMAL FLUID TANK RECIPE ---
        this.registerFluidTank(this.output);

        // --- CHUNK LOADER RECIPE ---
        this.registerChunkLoader(this.output);
    }

    private void saveShapedSynthesis(
            RecipeOutput recipeOutput,
            ResourceKey<Recipe<?>> keyId,
            String[] pattern,
            Map<Character, Ingredient> keys,
            ItemLike resultItem
    ) {
        java.util.List<String> patternList = java.util.List.of(pattern);
        java.util.Map<String, Ingredient> keyMap = new java.util.HashMap<>();
        for (var entry : keys.entrySet()) {
            keyMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        net.minecraft.world.item.ItemStackTemplate safeResult = new net.minecraft.world.item.ItemStackTemplate(resultItem.asItem(), 1);

        ShapedSynthesisRecipe recipe = ShapedSynthesisRecipe.newFromCodec("", patternList, keyMap, safeResult);
        recipeOutput.accept(keyId, recipe, null);
    }

    // Look up a partner mod item or return the fallback if not found
    private Ingredient getModItem(String modId, String path, net.minecraft.world.item.Item fallback) {
        return Ingredient.of(BuiltInRegistries.ITEM.get(
                Identifier.fromNamespaceAndPath(modId, path)
        ).map(net.minecraft.core.Holder::value).orElse(fallback));
    }

    // Register the Moldelonian Core recipe
    private void registerMoldelonianCore(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                ".MMSDSMM.",
                "MSSDIDSSM",
                "MSDIMIDSM",
                "SDIDSDIDS",
                "DIMSNSMID",
                "SDIDSDIDS",
                "MSDIMIDSM",
                "MSSDIDSSM",
                ".MMSDSMM."
        };

        java.util.Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('M', Ingredient.of(ModBlocks.MOLDELONIAN_BLOCK.get()));
        keys.put('S', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('I', Ingredient.of(ModItems.SYNTHORIUM_INGOT.get()));
        keys.put('N', Ingredient.of(Items.NETHER_STAR));
        keys.put('D', Ingredient.of(Blocks.DIAMOND_BLOCK));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthesis/moldelonian_core")
                ),
                pattern,
                keys,
                ModItems.MOLDELONIAN_CORE.get()
        );
    }

    // Register the PureMash Core recipe
    private void registerPureMashCore(RecipeOutput recipeOutput) {
        // Resolve modded ingredients locally to clean up the method signature
        Ingredient creativeEssence = getModItem("mysticalagradditions", "creative_essence", Items.NETHER_STAR);
        Ingredient controller = getModItem("ae2", "controller", Blocks.NETHERITE_BLOCK.asItem());

        String[] pattern = new String[] {
                "SSSSYSSSS",
                "SMMINIMMS",
                "SMYNSNYMS",
                "SINGCGNIS",
                "YNSCKCSNY",
                "SINGCGNIS",
                "SMYNSNYMS",
                "SMMINIMMS",
                "SSSSYSSSS"
        };

        java.util.Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('S', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('M', Ingredient.of(ModBlocks.MOLDELONIAN_BLOCK.get()));
        keys.put('I', Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()));
        assert ModSingularities.SYNTHORIUM_SINGULARITY != null;
        keys.put('G', Ingredient.of(ModSingularities.SYNTHORIUM_SINGULARITY.get()));
        keys.put('C', controller);
        keys.put('Y', creativeEssence);
        keys.put('N', Ingredient.of(Blocks.DIAMOND_BLOCK));
        keys.put('K', Ingredient.of(ModItems.MOLDELONIAN_CORE.get()));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthesis/puremash_core")
                ),
                pattern,
                keys,
                ModItems.PUREMASH_CORE.get()
        );
    }

    // Utility method to register Shapeless recipes on the 9x9 Synthesis Table
    private void saveShapelessSynthesis(
            RecipeOutput recipeOutput,
            ResourceKey<Recipe<?>> keyId,
            List<Ingredient> ingredients,
            ItemLike resultItem
    ) {
        net.minecraft.world.item.ItemStackTemplate safeResult = new net.minecraft.world.item.ItemStackTemplate(resultItem.asItem(), 1);

        dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe recipe =
                new dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe("", ingredients, safeResult);

        recipeOutput.accept(keyId, recipe, null);
    }

    // Look up official Tag ingredient via DataGen registries in a secure way
    private Ingredient getTagIngredient(String metal) {
        net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "ingots/" + metal)
        );
        return Ingredient.of(this.lookupProvider.lookupOrThrow(Registries.ITEM).getOrThrow(tagKey));
    }

    // Register the dynamic and moldable Moldelonian Ingot recipe using official Tags
    private void registerMoldelonianIngotRecipe(RecipeOutput recipeOutput) {
        java.util.List<Ingredient> ingredients = new java.util.ArrayList<>();

        // 1. Base Ingredients (Always included)
        ingredients.add(Ingredient.of(ModItems.SYNTHORIUM_INGOT.get()));
        ingredients.add(Ingredient.of(Items.IRON_INGOT));
        ingredients.add(Ingredient.of(Items.GOLD_INGOT));
        ingredients.add(Ingredient.of(Items.COPPER_INGOT));
        ingredients.add(Ingredient.of(Items.NETHERITE_INGOT));

        // 2. Dynamic ingredients retrieved by official "c:ingots/" Tags (The game only requires them if the corresponding mod is active!)
        String[] atoIngots = new String[] {
                // Common mining metals from AllTheOres (already included in your environment)
                "aluminum", "lead", "nickel", "osmium", "platinum", "silver", "tin", "zinc", "uranium",

                // Metal alloys and advanced industrial metals
                "steel", "bronze", "brass", "electrum", "invar", "constantan", "iridium", "titanium", "tungsten",

                // Mystic, magical, or special metals from popular mods (Forbidden & Arcanus, Advanced AE, Mystical Agriculture, etc.)
                "cobalt", "ardite", "manyullyn", "refined_glowstone", "refined_obsidian", "rose_gold",
                "enderium", "lumium", "signalum", "deorum", "quantum_alloy", "prosperity", "inferium", "prudentium", "tertium", "imperium", "supremium"
        };

        for (String metal : atoIngots) {
            ingredients.add(getTagIngredient(metal));
        }

        saveShapelessSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthesis/moldelonian_ingot")
                ),
                ingredients,
                ModItems.MOLDELONIAN_INGOT.get()
        );
    }

    // Look up individual singularity Tag ingredient securely against nulls
    private Ingredient getSingularityTagIngredient(String name) {
        net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "singularities/" + name)
        );
        return Ingredient.of(this.lookupProvider.lookupOrThrow(Registries.ITEM).getOrThrow(tagKey));
    }

    // Register the dynamic unification recipe of all singularities for the Cosmic Singularity
    private void registerCosmicSingularityRecipe(RecipeOutput recipeOutput) {
        java.util.List<Ingredient> ingredients = new java.util.ArrayList<>();

        // Dynamically iterate over the mod's singularity registry list
        for (var itemHolder : dev.davidklgames.puremashtweaks.registry.ModSingularities.REGISTERED_SINGULARITIES) {
            // Avoid adding the Cosmic Singularity itself to its synthesis recipe!
            if (itemHolder == dev.davidklgames.puremashtweaks.registry.ModSingularities.COSMIC_SINGULARITY) {
                continue;
            }

            String pathName = itemHolder.getId().getPath();
            // Remove the "_singularity" suffix to extract the clean tag (e.g., "coal_singularity" -> "coal")
            String cleanName = pathName.replace("_singularity", "");

            // Collect the corresponding tag ingredient from the list of placeholders or registries
            ingredients.add(getSingularityTagIngredient(cleanName));
        }

        saveShapelessSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthesis/cosmic_singularity")
                ),
                ingredients,
                ModSingularities.COSMIC_SINGULARITY.get()
        );
    }

    // Registry of Creative Fluid Tank recipe
    private void registerCreativeFluidTank(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                "BBRBCBRBB",
                "BRBRMRBRB",
                "RBRMBMRBR",
                "BRMBBBMRB",
                "CMBBTBBMC",
                "BRMBBBMRB",
                "RBRMBMRBR",
                "BRBRMRBRB",
                "BBRBCBRBB"
        };

        java.util.Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('B', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('R', Ingredient.of(Blocks.REDSTONE_BLOCK));
        keys.put('M', Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()));
        keys.put('C', Ingredient.of(ModItems.PUREMASH_CORE.get()));
        keys.put('T', Ingredient.of(ModBlocks.FLUID_TANK.get()));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthesis/creative_fluid_tank")
                ),
                pattern,
                keys,
                ModBlocks.CREATIVE_FLUID_TANK.get()
        );
    }

    // Registry of Fluid Tank recipe
    private void registerFluidTank(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                ".........",
                "..BBBBB..",
                "..BGGGB..",
                "..BGGGB..",
                "..BGKGB..",
                "..BGGGB..",
                "..BGGGB..",
                "..BBBBB..",
                "........."
        };

        java.util.Map<Character, Ingredient> keys = new java.util.HashMap<>();

        // Safe use of global tag "c:glass_panes" in 26.1.2
        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> glassPanesTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "glass_panes")
        );
        keys.put('G', Ingredient.of(this.lookupProvider.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM).getOrThrow(glassPanesTag)));
        keys.put('B', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('K', Ingredient.of(Items.BUCKET));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthesis/fluid_tank")
                ),
                pattern,
                keys,
                ModBlocks.FLUID_TANK.get()
        );
    }

    // Registry od Chunk Loader recipe
    private void registerChunkLoader(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                ".........",
                "..PPSPP..",
                ".PSDMDSP.",
                ".PDRSRDP.",
                ".SMSCSMS.",
                ".PDRSRDP.",
                ".PSDMDSP.",
                "..PPSPP..",
                "........."
        };

        java.util.Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('R', Ingredient.of(ModItems.SYNTHORIUM_ROD.get()));
        keys.put('D', Ingredient.of(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK));
        keys.put('M', Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()));
        keys.put('S', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('C', Ingredient.of(ModItems.PUREMASH_CORE.get()));
        keys.put('P', Ingredient.of(ModItems.SYNTHORIUM_NUGGET.get()));

        saveShapedSynthesis(
                recipeOutput,
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.RECIPE,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "synthesis/chunk_loader")
                ),
                pattern,
                keys,
                ModBlocks.CHUNK_LOADER.get()
        );
    }

    // Registry of Conditional Recipe to Creative Essence (Mystical Agradditions)
    private void registerCreativeEssenceFallback(RecipeOutput recipeOutput) {
        // Retrieves the result item safely during DataGen.
        net.minecraft.world.item.Item creativeEssence = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("mysticalagradditions", "creative_essence")
        ).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);

        if (creativeEssence != net.minecraft.world.item.Items.AIR) {

            // Packages the recipe output with our two bulletproof conditions.
            RecipeOutput conditionalOutput = recipeOutput.withConditions(
                    new net.neoforged.neoforge.common.conditions.ModLoadedCondition("mysticalagradditions"),
                    new dev.davidklgames.puremashtweaks.recipe.condition.FallbackConfigCondition()
            );

            this.shaped(net.minecraft.data.recipes.RecipeCategory.MISC, creativeEssence)
                    .pattern("PIT")
                    .pattern("INI")
                    .pattern("MIS")

                    .define('P', getModItem("mysticalagriculture", "prudentium_block", net.minecraft.world.item.Items.AIR))
                    .define('I', getModItem("mysticalagradditions", "insanium_block", net.minecraft.world.item.Items.AIR))
                    .define('T', getModItem("mysticalagriculture", "tertium_block", net.minecraft.world.item.Items.AIR))
                    .define('N', net.minecraft.world.item.Items.NETHER_STAR)
                    .define('M', getModItem("mysticalagriculture", "imperium_block", net.minecraft.world.item.Items.AIR))
                    .define('S', getModItem("mysticalagriculture", "supremium_block", net.minecraft.world.item.Items.AIR))
                    .unlockedBy("has_nether_star", this.has(net.minecraft.world.item.Items.NETHER_STAR))
                    .save(conditionalOutput, net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.RECIPE,
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "creative_essence")
                    ));
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // OFFICIAL INTERNAL RUNNER CLASS OF 26.1.2 (Concrete, implements all abstract methods)
    // ----------------------------------------------------------------------------------------------------
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, @NonNull RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "PureMash Tweaks Recipes";
        }
    }
}