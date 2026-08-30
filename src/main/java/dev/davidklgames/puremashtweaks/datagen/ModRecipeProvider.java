package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import dev.davidklgames.puremashtweaks.recipe.ShapedSynthesisRecipe;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public class ModRecipeProvider extends RecipeProvider {

    private final HolderLookup.Provider lookupProvider;

    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.lookupProvider = registries;
    }

    @Override
    protected void buildRecipes() {

        // =========================================================================
        // PUREMASH GUIDE BOOK RECIPE (GUIDEME)
        // =========================================================================
        Item guideItem = BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("guideme", "guide"))
                .map(net.minecraft.core.Holder::value)
                .orElse(Items.AIR);

        if (guideItem != Items.AIR) {
            var guideIdComponent = BuiltInRegistries.DATA_COMPONENT_TYPE.get(Identifier.fromNamespaceAndPath("guideme", "guide_id"));
            DataComponentPatch patch = DataComponentPatch.EMPTY;
            if (guideIdComponent.isPresent()) {
                patch = DataComponentPatch.builder()
                        .set((net.minecraft.core.component.DataComponentType<Identifier>) guideIdComponent.get().value(), Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "guide"))
                        .build();
            }

            ItemStackTemplate guideTemplate = new ItemStackTemplate(guideItem, 1, patch);
            RecipeOutput guidemeOutput = this.output.withConditions(
                    new net.neoforged.neoforge.common.conditions.ModLoadedCondition("guideme")
            );

            TagKey<Item> glassPanesTag = TagKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("c", "glass_panes")
            );

            this.shaped(RecipeCategory.MISC, guideTemplate)
                    .pattern("SGS")
                    .pattern("GBG")
                    .pattern("SGS")
                    .define('S', ModItems.SYNTHORIUM_INGOT.get())
                    .define('G', glassPanesTag)
                    .define('B', Items.BOOK)
                    .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                    .save(guidemeOutput, ResourceKey.create(
                            Registries.RECIPE,
                            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "guide_book")
                    ));
        }

        // =========================================================================
        // SYNTHORIUM ARMOR
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
        // SMELTING SYNTHORIUM DUST
        // =========================================================================
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
        // MOLDELONIAN DUST SMELTING
        // =========================================================================
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.MOLDELONIAN_DUST.get()),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        new net.minecraft.world.item.ItemStackTemplate(ModItems.MOLDELONIAN_INGOT.get(), 1),
                        1.0f,
                        200
                )
                .unlockedBy("has_moldelonian_dust", this.has(ModItems.MOLDELONIAN_DUST.get()))
                .save(this.output, "moldelonian_ingot_from_smelting_dust");

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.MOLDELONIAN_DUST.get()),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        new net.minecraft.world.item.ItemStackTemplate(ModItems.MOLDELONIAN_INGOT.get(), 1),
                        1.0f,
                        100
                )
                .unlockedBy("has_moldelonian_dust", this.has(ModItems.MOLDELONIAN_DUST.get()))
                .save(this.output, "moldelonian_ingot_from_blasting_dust");

        // =========================================================================
        // COMPRESSOR SPEED UPGRADES
        // =========================================================================
        this.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_1.get())
                .pattern("RPR")
                .pattern("PEP")
                .pattern("RPR")
                .define('R', Items.REPEATER)
                .define('P', Items.PAPER)
                .define('E', Items.REDSTONE)
                .unlockedBy("has_repeater", this.has(Items.REPEATER))
                .save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.SPEED_UPGRADE_2.get())
                .pattern("DID")
                .pattern("IUI")
                .pattern("DID")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .define('U', ModItems.SPEED_UPGRADE_1.get())
                .define('D', Items.DIAMOND)
                .unlockedBy("has_speed_upgrade_1", this.has(ModItems.SPEED_UPGRADE_1.get()))
                .save(this.output);

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

        this.shaped(RecipeCategory.TOOLS, ModItems.SYNTHORIUM_PAXEL.get())
                .pattern("APS")
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
        // PUREMASH APPLES
        // =========================================================================
        this.shaped(RecipeCategory.FOOD, ModItems.SYNTHORIUM_APPLE.get())
                .pattern("SSS")
                .pattern("SAS")
                .pattern("SSS")
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('A', Items.APPLE)
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output, "synthorium_apple");

        this.shaped(RecipeCategory.FOOD, ModItems.MOLDELONIAN_APPLE.get())
                .pattern("MMM")
                .pattern("MAM")
                .pattern("MMM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('A', ModItems.SYNTHORIUM_APPLE.get())
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "moldelonian_apple");

        // =========================================================================
        // ORE AND METAL PROCESSING
        // =========================================================================
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SYNTHORIUM_DEBRIS.get()), RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.SYNTHORIUM_SCRAP.get(), 2.0f, 200)
                .unlockedBy("has_synthorium_debris", this.has(ModBlocks.SYNTHORIUM_DEBRIS.get()))
                .save(this.output, "synthorium_scrap_from_smelting");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModBlocks.SYNTHORIUM_DEBRIS.get()), RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.SYNTHORIUM_SCRAP.get(), 2.0f, 100)
                .unlockedBy("has_synthorium_debris", this.has(ModBlocks.SYNTHORIUM_DEBRIS.get()))
                .save(this.output, "synthorium_scrap_from_blasting");

        this.shapeless(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get(), 1)
                .requires(ModItems.SYNTHORIUM_SCRAP.get(), 4)
                .requires(Items.DIAMOND, 4)
                .unlockedBy("has_synthorium_scrap", this.has(ModItems.SYNTHORIUM_SCRAP.get()))
                .save(this.output, ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthorium_ingot")
                ));

        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.ALCHEMICAL_SYNTHESIZER.get())
                .pattern("MRM")
                .pattern("SCS")
                .pattern("MFM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('C', ModBlocks.FLUID_TANK.get())
                .define('R', Items.REDSTONE_BLOCK)
                .define('F', Blocks.FURNACE)
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output);

        this.shapeless(RecipeCategory.MISC, ModItems.SYNTHORIUM_NUGGET.get(), 9)
                .requires(ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output, "synthorium_nuggets_from_ingot");

        this.shaped(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.SYNTHORIUM_NUGGET.get())
                .unlockedBy("has_synthorium_nugget", this.has(ModItems.SYNTHORIUM_NUGGET.get()))
                .save(this.output, "synthorium_ingot_from_nuggets");

        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SYNTHORIUM_BLOCK.get())
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output, "synthorium_block_from_ingots");

        this.shapeless(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get(), 9)
                .requires(ModBlocks.SYNTHORIUM_BLOCK.get())
                .unlockedBy("has_synthorium_block", this.has(ModBlocks.SYNTHORIUM_BLOCK.get()))
                .save(this.output, "synthorium_ingots_from_block");

        // =========================================================================
        // DYNAMIC MYSTICAL AGRICULTURE COMPATIBILITY
        // =========================================================================
        Item synthoriumEssence = BuiltInRegistries.ITEM.get(
                Identifier.fromNamespaceAndPath("mysticalagriculture", "synthorium_essence")
        ).map(Holder::value).orElse(Items.AIR);

        if (synthoriumEssence != Items.AIR) {
            RecipeOutput mysticalAgriOutput = this.output.withConditions(
                    new net.neoforged.neoforge.common.conditions.ModLoadedCondition("mysticalagriculture")
            );

            this.shaped(RecipeCategory.MISC, ModItems.SYNTHORIUM_INGOT.get())
                    .pattern("EEE")
                    .pattern("EEE")
                    .pattern("EEE")
                    .define('E', synthoriumEssence)
                    .unlockedBy("has_synthorium_essence", this.has(synthoriumEssence))
                    .save(mysticalAgriOutput, ResourceKey.create(
                            Registries.RECIPE,
                            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthorium_ingot_from_essence")
                    ));
        }

        Item moldelonianEssence = BuiltInRegistries.ITEM.get(
                Identifier.fromNamespaceAndPath("mysticalagriculture", "moldelonian_essence")
        ).map(Holder::value).orElse(Items.AIR);

        if (moldelonianEssence != Items.AIR) {
            RecipeOutput mysticalAgriOutput = this.output.withConditions(
                    new net.neoforged.neoforge.common.conditions.ModLoadedCondition("mysticalagriculture")
            );

            this.shaped(RecipeCategory.MISC, ModItems.MOLDELONIAN_INGOT.get())
                    .pattern("EEE")
                    .pattern("EEE")
                    .pattern("EEE")
                    .define('E', moldelonianEssence)
                    .unlockedBy("has_moldelonian_essence", this.has(moldelonianEssence))
                    .save(mysticalAgriOutput, ResourceKey.create(
                            Registries.RECIPE,
                            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "moldelonian_ingot_from_essence")
                    ));
        }

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
        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MOLDELONIAN_BLOCK.get())
                .pattern("MMM")
                .pattern("MMM")
                .pattern("MMM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "moldelonian_block_from_ingots");

        this.shapeless(RecipeCategory.MISC, ModItems.MOLDELONIAN_INGOT.get(), 9)
                .requires(ModBlocks.MOLDELONIAN_BLOCK.get())
                .unlockedBy("has_moldelonian_block", this.has(ModBlocks.MOLDELONIAN_BLOCK.get()))
                .save(this.output, "moldelonian_ingots_from_block");

        this.shapeless(RecipeCategory.MISC, ModItems.MOLDELONIAN_NUGGET.get(), 9)
                .requires(ModItems.MOLDELONIAN_INGOT.get())
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "moldelonian_nuggets_from_ingot");

        this.shaped(RecipeCategory.MISC, ModItems.MOLDELONIAN_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.MOLDELONIAN_NUGGET.get())
                .unlockedBy("has_moldelonian_nugget", this.has(ModItems.MOLDELONIAN_NUGGET.get()))
                .save(this.output, "moldelonian_ingot_from_nuggets");

        // =========================================================================
        // OTHER COMPONENTS
        // =========================================================================
        this.shaped(RecipeCategory.MISC, ModItems.MEMORY_CARD.get())
                .pattern(" R ")
                .pattern("RNR")
                .pattern("PPP")
                .define('R', Items.REDSTONE)
                .define('N', ModItems.SYNTHORIUM_NUGGET.get())
                .define('P', Items.PAPER)
                .unlockedBy("has_synthorium_nugget", this.has(ModItems.SYNTHORIUM_NUGGET.get()))
                .save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.SYNTHORIUM_ROD.get(), 4)
                .pattern("I")
                .pattern("I")
                .pattern("I")
                .define('I', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

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

        this.shaped(RecipeCategory.DECORATIONS, ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get())
                .pattern("MSM")
                .pattern("SPS")
                .pattern("MSM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('S', ModBlocks.SYNTHORIUM_BLOCK.get())
                .define('P', ModItems.SYNTHORIUM_PAXEL.get())
                .unlockedBy("has_synthorium_paxel", this.has(ModItems.SYNTHORIUM_PAXEL.get()))
                .save(this.output);

        // --- MOLDELONIAN SMITHING UPGRADES ---
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_SWORD.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.COMBAT,
                        ModItems.MOLDELONIAN_SWORD.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_sword_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_PICKAXE.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.TOOLS,
                        ModItems.MOLDELONIAN_PICKAXE.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_pickaxe_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_SHOVEL.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.TOOLS,
                        ModItems.MOLDELONIAN_SHOVEL.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_shovel_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_AXE.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.TOOLS,
                        ModItems.MOLDELONIAN_AXE.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_axe_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_HOE.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.TOOLS,
                        ModItems.MOLDELONIAN_HOE.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_hoe_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_PAXEL.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.TOOLS,
                        ModItems.MOLDELONIAN_PAXEL.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_paxel_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_HELMET.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.COMBAT,
                        ModItems.MOLDELONIAN_HELMET.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_helmet_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_CHESTPLATE.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.COMBAT,
                        ModItems.MOLDELONIAN_CHESTPLATE.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_chestplate_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_LEGGINGS.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.COMBAT,
                        ModItems.MOLDELONIAN_LEGGINGS.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_leggings_smithing");

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SYNTHORIUM_BOOTS.get()),
                        Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()),
                        RecipeCategory.COMBAT,
                        ModItems.MOLDELONIAN_BOOTS.get()
                ).unlocks("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output, "puremashtweaks:moldelonian_boots_smithing");

        this.shaped(RecipeCategory.MISC, ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get(), 2)
                .pattern("DTD")
                .pattern("DSD")
                .pattern("DDD")
                .define('D', Blocks.END_STONE)
                .define('T', ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get())
                .define('S', ModItems.MOLDELONIAN_INGOT.get())
                .unlockedBy("has_template", this.has(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()))
                .save(this.output, "moldelonian_smithing_template_duplication");

        this.shaped(RecipeCategory.TOOLS, ModItems.CONFIGURATION_WRENCH.get())
                .pattern("I I")
                .pattern(" N ")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
                .save(this.output);

        // =========================================================================
        // UNIVERSAL CABLES RECIPES (16x Yield)
        // =========================================================================
        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get(), 16)
                .pattern("SSS")
                .pattern("RGR")
                .pattern("SSS")
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('R', Items.REDSTONE)
                .define('G', Blocks.GLASS)
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get(), 16)
                .pattern("MMM")
                .pattern("RCR")
                .pattern("MMM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('R', Blocks.REDSTONE_BLOCK)
                .define('C', ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get())
                .unlockedBy("has_moldelonian_ingot", this.has(ModItems.MOLDELONIAN_INGOT.get()))
                .save(this.output);

        var overclockHolder = this.lookupProvider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.OVERCLOCK);

        ItemEnchantments.Mutable overclockEnchants1 = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        overclockEnchants1.set(overclockHolder, 1);
        DataComponentPatch patch1 = DataComponentPatch.builder()
                .set(DataComponents.STORED_ENCHANTMENTS, overclockEnchants1.toImmutable())
                .build();
        Ingredient overclock1 = net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(
                false,
                patch1,
                Items.ENCHANTED_BOOK
        );

        ItemEnchantments.Mutable overclockEnchants2 = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        overclockEnchants2.set(overclockHolder, 2);
        DataComponentPatch patch2 = DataComponentPatch.builder()
                .set(DataComponents.STORED_ENCHANTMENTS, overclockEnchants2.toImmutable())
                .build();
        Ingredient overclock2 = net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(
                false,
                patch2,
                Items.ENCHANTED_BOOK
        );

        Ingredient overclockBookIngredient = net.neoforged.neoforge.common.crafting.CompoundIngredient.of(
                overclock1,
                overclock2
        );

        this.shaped(RecipeCategory.MISC, ModItems.DUPLICATION_UPGRADE_1.get())
                .pattern("SNS")
                .pattern("NON")
                .pattern("SNS")
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('N', ModBlocks.SYNTHORIUM_BLOCK.get())
                .define('O', overclockBookIngredient)
                .unlockedBy("has_synthorium_ingot", this.has(ModItems.SYNTHORIUM_INGOT.get()))
                .save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.DUPLICATION_UPGRADE_2.get())
                .pattern("MNM")
                .pattern("NDN")
                .pattern("MNM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('N', Items.NETHER_STAR)
                .define('D', ModItems.DUPLICATION_UPGRADE_1.get())
                .unlockedBy("has_duplication_upgrade_1", this.has(ModItems.DUPLICATION_UPGRADE_1.get()))
                .save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.STACK_PROCESSING_UPGRADE.get())
                .pattern("MSM")
                .pattern("SCS")
                .pattern("MSM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('C', ModItems.MEMORY_CARD.get())
                .unlockedBy("has_memory_card", this.has(ModItems.MEMORY_CARD.get()))
                .save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.CAPACITY_UPGRADE_1.get())
                .pattern("SMS")
                .pattern("MRM")
                .pattern("SMS")
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('M', ModItems.MEMORY_CARD.get())
                .define('R', Blocks.REDSTONE_BLOCK)
                .unlockedBy("has_memory_card", this.has(ModItems.MEMORY_CARD.get()))
                .save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.CAPACITY_UPGRADE_2.get())
                .pattern("MSM")
                .pattern("SCS")
                .pattern("MSM")
                .define('M', ModItems.MOLDELONIAN_INGOT.get())
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .define('C', ModItems.CAPACITY_UPGRADE_1.get())
                .unlockedBy("has_capacity_upgrade_1", this.has(ModItems.CAPACITY_UPGRADE_1.get()))
                .save(this.output);

        this.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PUREMASH_BATTERY.get())
                .pattern("SRS")
                .pattern("RCR")
                .pattern("SRS")
                .define('S', ModBlocks.SYNTHORIUM_BLOCK.get())
                .define('R', Blocks.REDSTONE_BLOCK)
                .define('C', ModItems.MOLDELONIAN_CORE.get())
                .unlockedBy("has_moldelonian_core", this.has(ModItems.MOLDELONIAN_CORE.get()))
                .save(this.output);

        this.shaped(RecipeCategory.MISC, ModItems.DISTRIBUTION_FILTER.get())
                .pattern("ESE")
                .pattern("SPS")
                .pattern("ESE")
                .define('P', Items.PAPER)
                .define('E', Items.REDSTONE)
                .define('S', ModItems.SYNTHORIUM_INGOT.get())
                .unlockedBy("has_redstone", this.has(Items.REDSTONE))
                .save(this.output);

        this.registerCreativeEssenceFallback(this.output);

        // =========================================================================
        // SYNTHESIS TABLE RECIPES (9x9)
        // =========================================================================
        registerPureMashCore(this.output);
        registerMoldelonianCore(this.output);
        this.registerMoldelonianIngotRecipe(this.output);
        this.registerCreativeFluidTank(this.output);
        this.registerFluidTank(this.output);
        this.registerChunkLoader(this.output);
        this.registerCreativeBattery(this.output);
        this.registerPureMashGenerator(this.output);
    }

    private void saveShapedSynthesis(
            RecipeOutput recipeOutput,
            ResourceKey<Recipe<?>> keyId,
            String[] pattern,
            Map<Character, Ingredient> keys,
            ItemLike resultItem
    ) {
        List<String> patternList = List.of(pattern);
        Map<String, Ingredient> keyMap = new java.util.HashMap<>();
        for (var entry : keys.entrySet()) {
            keyMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        ItemStackTemplate safeResult = new ItemStackTemplate(resultItem.asItem(), 1);
        ShapedSynthesisRecipe recipe = ShapedSynthesisRecipe.newFromCodec("", patternList, keyMap, safeResult);
        recipeOutput.accept(keyId, recipe, null);
    }

    private Ingredient getModItem(String modId, String path, Item fallback) {
        return Ingredient.of(BuiltInRegistries.ITEM.get(
                Identifier.fromNamespaceAndPath(modId, path)
        ).map(Holder::value).orElse(fallback));
    }

    private void registerMoldelonianCore(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                ".MMSDSMM.",
                "MSSDIDSSM",
                "MSDIMIDSM",
                "SDIASAIDS",
                "DIMSNSMID",
                "SDIASAIDS",
                "MSDIMIDSM",
                "MSSDIDSSM",
                ".MMSDSMM."
        };

        Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('M', Ingredient.of(ModBlocks.MOLDELONIAN_BLOCK.get()));
        keys.put('S', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('I', Ingredient.of(ModItems.SYNTHORIUM_INGOT.get()));
        keys.put('N', Ingredient.of(Items.NETHER_STAR));
        keys.put('D', Ingredient.of(Blocks.DIAMOND_BLOCK));
        keys.put('A', Ingredient.of(Blocks.REDSTONE_BLOCK));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/moldelonian_core")
                ),
                pattern,
                keys,
                ModItems.MOLDELONIAN_CORE.get()
        );
    }

    private void registerPureMashCore(RecipeOutput recipeOutput) {
        Ingredient creativeEssence = getModItem("mysticalagradditions", "creative_essence", Items.NETHER_STAR);
        Ingredient controller = getModItem("ae2", "controller", Blocks.NETHERITE_BLOCK.asItem());

        String[] pattern = new String[] {
                "SSSSYSSSS",
                "STMINIMTS",
                "SMYNSNYMS",
                "SINGCGNIS",
                "YNSCKCSNY",
                "SINGCGNIS",
                "SMYNSNYMS",
                "STMINIMTS",
                "SSSSYSSSS"
        };

        Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('S', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('M', Ingredient.of(ModBlocks.MOLDELONIAN_BLOCK.get()));
        keys.put('I', Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()));
        if (ModSingularities.SYNTHORIUM_SINGULARITY != null) {
            keys.put('G', Ingredient.of(ModSingularities.SYNTHORIUM_SINGULARITY.get()));
        } else {
            keys.put('G', Ingredient.of(ModItems.SYNTHORIUM_INGOT.get()));
        }
        keys.put('C', controller);
        keys.put('Y', creativeEssence);
        keys.put('N', Ingredient.of(Blocks.DIAMOND_BLOCK));
        keys.put('K', Ingredient.of(ModItems.MOLDELONIAN_CORE.get()));
        keys.put('T', Ingredient.of(ModSingularities.COSMIC_SINGULARITY.get()));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/puremash_core")
                ),
                pattern,
                keys,
                ModItems.PUREMASH_CORE.get()
        );
    }

    private void registerCreativeBattery(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                "IIIIAIIII",
                "IRRSMSRRI",
                "IRSMCMSRI",
                "ISMARAMSI",
                "AMCRBRCMA",
                "ISMARAMSI",
                "IRSMCMSRI",
                "IRRSMSRRI",
                "IIIIAIIII"
        };

        Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('I', Ingredient.of(ModBlocks.MOLDELONIAN_BLOCK.get()));
        keys.put('S', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('R', Ingredient.of(Blocks.REDSTONE_BLOCK));
        keys.put('M', Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()));
        keys.put('C', Ingredient.of(ModItems.MOLDELONIAN_CORE.get()));
        keys.put('B', Ingredient.of(ModBlocks.PUREMASH_BATTERY.get()));
        keys.put('A', Ingredient.of(ModBlocks.PUREMASH_CORE_BLOCK.get()));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/creative_battery")
                ),
                pattern,
                keys,
                ModBlocks.CREATIVE_BATTERY.get()
        );
    }

    private void saveShapelessSynthesis(
            RecipeOutput recipeOutput,
            ResourceKey<Recipe<?>> keyId,
            List<Ingredient> ingredients,
            ItemLike resultItem
    ) {
        ItemStackTemplate safeResult = new ItemStackTemplate(resultItem.asItem(), 1);
        ShapelessSynthesisRecipe recipe = new ShapelessSynthesisRecipe("", ingredients, safeResult);
        recipeOutput.accept(keyId, recipe, null);
    }

    private Ingredient getTagIngredient(String metal) {
        TagKey<Item> tagKey = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "ingots/" + metal)
        );
        return Ingredient.of(this.lookupProvider.lookupOrThrow(Registries.ITEM).getOrThrow(tagKey));
    }

    private void registerMoldelonianIngotRecipe(RecipeOutput recipeOutput) {
        List<Ingredient> ingredients = new ArrayList<>();

        ingredients.add(Ingredient.of(ModItems.SYNTHORIUM_INGOT.get()));
        ingredients.add(Ingredient.of(Items.IRON_INGOT));
        ingredients.add(Ingredient.of(Items.GOLD_INGOT));
        ingredients.add(Ingredient.of(Items.COPPER_INGOT));
        ingredients.add(Ingredient.of(Items.NETHERITE_INGOT));

        String[] atoIngots = new String[] {
                "aluminum", "lead", "nickel", "osmium", "platinum", "silver", "tin", "zinc", "uranium",
                "steel", "bronze", "brass", "electrum", "invar", "constantan", "iridium", "titanium", "tungsten",
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
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/moldelonian_ingot")
                ),
                ingredients,
                ModItems.MOLDELONIAN_INGOT.get()
        );
    }

    private void registerCreativeFluidTank(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                "BBRBCBRBB",
                "BRBRMRBRB",
                "RBRMBMRBR",
                "BRMBRBMRB",
                "CMBRTRBMC",
                "BRMBRBMRB",
                "RBRMBMRBR",
                "BRBRMRBRB",
                "BBRBCBRBB"
        };

        Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('B', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('R', Ingredient.of(Blocks.REDSTONE_BLOCK));
        keys.put('M', Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()));
        keys.put('C', Ingredient.of(ModItems.PUREMASH_CORE.get()));
        keys.put('T', Ingredient.of(ModBlocks.FLUID_TANK.get()));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/creative_fluid_tank")
                ),
                pattern,
                keys,
                ModBlocks.CREATIVE_FLUID_TANK.get()
        );
    }

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

        Map<Character, Ingredient> keys = new java.util.HashMap<>();
        TagKey<Item> glassPanesTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "glass_panes")
        );
        keys.put('G', Ingredient.of(this.lookupProvider.lookupOrThrow(Registries.ITEM).getOrThrow(glassPanesTag)));
        keys.put('B', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('K', Ingredient.of(Items.BUCKET));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/fluid_tank")
                ),
                pattern,
                keys,
                ModBlocks.FLUID_TANK.get()
        );
    }

    private void registerChunkLoader(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                ".........",
                "..PPSPP..",
                ".PSDMDSP.",
                ".PDRORDP.",
                ".SMOCOMS.",
                ".PDRORDP.",
                ".PSDMDSP.",
                "..PPSPP..",
                "........."
        };

        Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('R', Ingredient.of(ModItems.SYNTHORIUM_ROD.get()));
        keys.put('D', Ingredient.of(Blocks.DIAMOND_BLOCK));
        keys.put('M', Ingredient.of(ModItems.MOLDELONIAN_INGOT.get()));
        keys.put('S', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('O', Ingredient.of(Blocks.OBSIDIAN));
        keys.put('C', Ingredient.of(ModItems.PUREMASH_CORE.get()));
        keys.put('P', Ingredient.of(ModItems.SYNTHORIUM_NUGGET.get()));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/chunk_loader")
                ),
                pattern,
                keys,
                ModBlocks.CHUNK_LOADER.get()
        );
    }

    private void registerCreativeEssenceFallback(RecipeOutput recipeOutput) {
        Item creativeEssence = BuiltInRegistries.ITEM.get(
                Identifier.fromNamespaceAndPath("mysticalagradditions", "creative_essence")
        ).map(Holder::value).orElse(Items.AIR);

        if (creativeEssence != Items.AIR) {
            RecipeOutput conditionalOutput = recipeOutput.withConditions(
                    new net.neoforged.neoforge.common.conditions.ModLoadedCondition("mysticalagradditions"),
                    new dev.davidklgames.puremashtweaks.recipe.condition.FallbackConfigCondition()
            );

            this.shaped(RecipeCategory.MISC, creativeEssence)
                    .pattern("PIT")
                    .pattern("INI")
                    .pattern("MIS")
                    .define('P', getModItem("mysticalagriculture", "prudentium_block", Items.AIR))
                    .define('I', getModItem("mysticalagradditions", "insanium_block", Items.AIR))
                    .define('T', getModItem("mysticalagriculture", "tertium_block", Items.AIR))
                    .define('N', Items.NETHER_STAR)
                    .define('M', getModItem("mysticalagriculture", "imperium_block", Items.AIR))
                    .define('S', getModItem("mysticalagriculture", "supremium_block", Items.AIR))
                    .unlockedBy("has_nether_star", this.has(Items.NETHER_STAR))
                    .save(conditionalOutput, ResourceKey.create(
                            Registries.RECIPE,
                            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "creative_essence")
                    ));
        }
    }

    private void registerPureMashGenerator(RecipeOutput recipeOutput) {
        String[] pattern = new String[] {
                ".........",
                "..GCCCG..",
                ".GGGIGGG.",
                ".CGBDBGC.",
                ".CIATAIC.",
                ".CGBDBGC.",
                ".GGGIGGG.",
                "..GCCCG..",
                "........."
        };

        Map<Character, Ingredient> keys = new java.util.HashMap<>();
        keys.put('I', Ingredient.of(Items.NETHERITE_INGOT));
        keys.put('B', Ingredient.of(ModBlocks.SYNTHORIUM_BLOCK.get()));
        keys.put('C', Ingredient.of(ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get()));
        keys.put('A', Ingredient.of(ModBlocks.FLUID_TANK.get()));
        keys.put('R', Ingredient.of(Items.REDSTONE_BLOCK));
        keys.put('G', Ingredient.of(Blocks.DIAMOND_BLOCK));
        keys.put('D', Ingredient.of(Blocks.REDSTONE_BLOCK));
        keys.put('T', Ingredient.of(Blocks.FURNACE));

        saveShapedSynthesis(
                recipeOutput,
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/puremash_generator")
                ),
                pattern,
                keys,
                ModBlocks.PUREMASH_GENERATOR.get()
        );
    }

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