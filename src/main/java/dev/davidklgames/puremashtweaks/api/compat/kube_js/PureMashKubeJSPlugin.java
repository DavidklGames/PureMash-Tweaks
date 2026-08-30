package dev.davidklgames.puremashtweaks.api.compat.kube_js;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeOptional;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * KubeJS Plugin for PureMash Tweaks in Minecraft 26.1.2.
 * Exposes 9x9 Synthesis Table, Compression, Singularity, Dust, and Alchemical recipe schemas to KubeJS scripts.
 */
public class PureMashKubeJSPlugin implements KubeJSPlugin {

    // --- BOOLEAN COMPONENT FOR KUBEJS (SEM AVISO DE @Internal) ---
    private static final RecipeComponent<Boolean> BOOLEAN = new SimpleRecipeComponent<>(
            RecipeComponentType.key(net.minecraft.resources.Identifier.fromNamespaceAndPath("kubejs", "boolean")),
            com.mojang.serialization.Codec.BOOL,
            dev.latvian.mods.rhino.type.TypeInfo.BOOLEAN
    );

    // --- HELPER TO CREATE OPTIONAL KEYS WITH DEFAULT VALUES ---
    private static <T> RecipeKey<T> optionalKey(RecipeComponent<T> component, String name, T defaultValue) {
        RecipeKey<T> key = component.key(name, ComponentRole.OTHER);
        key.optional = RecipeOptional.unit(defaultValue);
        return key;
    }

    // =========================================================================
    // 1. COMPRESSION SCHEMA (Required keys first, then optional keys)
    // =========================================================================
    private static final RecipeKey<Ingredient> COMPRESS_INPUT = IngredientComponent.INGREDIENT.inputKey("input");
    private static final RecipeKey<ItemStack> COMPRESS_OUTPUT = ItemStackComponent.ITEM_STACK.outputKey("output");
    private static final RecipeKey<Integer> COMPRESS_COUNT = optionalKey(NumberComponent.INT, "input_count", 9);
    private static final RecipeKey<Integer> COMPRESS_TIME = optionalKey(NumberComponent.INT, "time_cost", 20);

    public static final RecipeSchema COMPRESSION_SCHEMA = new RecipeSchema(
            COMPRESS_INPUT, COMPRESS_OUTPUT, COMPRESS_COUNT, COMPRESS_TIME
    );

    // =========================================================================
    // 2. SINGULARITY SCHEMA (Required keys first, then optional keys)
    // =========================================================================
    private static final RecipeKey<Ingredient> SINGULARITY_INPUT = IngredientComponent.INGREDIENT.inputKey("item");
    private static final RecipeKey<ItemStack> SINGULARITY_OUTPUT = ItemStackComponent.ITEM_STACK.outputKey("output");
    private static final RecipeKey<Integer> SINGULARITY_COST = optionalKey(NumberComponent.INT, "cost", 1000);
    private static final RecipeKey<Integer> SINGULARITY_TIME = optionalKey(NumberComponent.INT, "time_cost", 40);

    public static final RecipeSchema SINGULARITY_SCHEMA = new RecipeSchema(
            SINGULARITY_INPUT, SINGULARITY_OUTPUT, SINGULARITY_COST, SINGULARITY_TIME
    );

    // =========================================================================
    // 3. DUST CRUSHER SCHEMA (Required keys first, then optional keys)
    // =========================================================================
    private static final RecipeKey<Ingredient> DUST_INPUT = IngredientComponent.INGREDIENT.inputKey("input");
    private static final RecipeKey<ItemStack> DUST_OUTPUT = ItemStackComponent.ITEM_STACK.outputKey("output");
    private static final RecipeKey<Integer> DUST_TIME = optionalKey(NumberComponent.INT, "time_cost", 20);

    public static final RecipeSchema DUST_SCHEMA = new RecipeSchema(
            DUST_INPUT, DUST_OUTPUT, DUST_TIME
    );

    // =========================================================================
    // 4. ALCHEMICAL SYNTHESIZER SCHEMA (Required keys first, then optional keys)
    // =========================================================================
    private static final RecipeKey<Ingredient> ALCHEMICAL_INPUT = IngredientComponent.INGREDIENT.inputKey("input");
    private static final RecipeKey<ItemStack> ALCHEMICAL_OUTPUT = ItemStackComponent.ITEM_STACK.outputKey("output");
    private static final RecipeKey<Integer> ALCHEMICAL_OUTPUT_COUNT = optionalKey(NumberComponent.INT, "output_count", 1);
    private static final RecipeKey<String> ALCHEMICAL_FLUID = optionalKey(StringComponent.STRING, "fluid", "none");
    private static final RecipeKey<Integer> ALCHEMICAL_FLUID_AMOUNT = optionalKey(NumberComponent.INT, "fluid_amount", 0);
    private static final RecipeKey<String> ALCHEMICAL_TOOL = optionalKey(StringComponent.STRING, "tool_type", "none");
    private static final RecipeKey<Integer> ALCHEMICAL_TIME = optionalKey(NumberComponent.INT, "time", 60);
    private static final RecipeKey<Integer> ALCHEMICAL_ENERGY = optionalKey(NumberComponent.INT, "energy", 100);
    private static final RecipeKey<Boolean> ALCHEMICAL_DOUBLE = optionalKey(BOOLEAN, "double_output", false);

    public static final RecipeSchema ALCHEMICAL_SCHEMA = new RecipeSchema(
            ALCHEMICAL_INPUT, ALCHEMICAL_OUTPUT, ALCHEMICAL_OUTPUT_COUNT,
            ALCHEMICAL_FLUID, ALCHEMICAL_FLUID_AMOUNT, ALCHEMICAL_TOOL,
            ALCHEMICAL_TIME, ALCHEMICAL_ENERGY, ALCHEMICAL_DOUBLE
    );

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        PureMashTweaks.LOGGER.info("[KubeJS x PureMash Tweaks]: Handshake initiated! Linking custom recipe protocols...");

        var pmt = registry.namespace(PureMashTweaks.MODID);

        // 1. 9x9 Synthesis Table (Custom Shaped & Shapeless 9x9 Matrix)
        pmt.shaped("shaped_synthesis");
        pmt.shapeless("shapeless_synthesis");
        PureMashTweaks.LOGGER.info("[KubeJS x PureMash Tweaks]: Registered 9x9 Synthesis Table schemas (shaped_synthesis & shapeless_synthesis).");

        // 2. Multifunctional Compressor (Compression, Singularity, Dust)
        pmt.register("compression", COMPRESSION_SCHEMA);
        pmt.register("singularity", SINGULARITY_SCHEMA);
        pmt.register("dust", DUST_SCHEMA);
        PureMashTweaks.LOGGER.info("[KubeJS x PureMash Tweaks]: Registered Multifunctional Compressor schemas (compression, singularity, dust).");

        // 3. Alchemical Synthesizer
        pmt.register("alchemical", ALCHEMICAL_SCHEMA);
        PureMashTweaks.LOGGER.info("[KubeJS x PureMash Tweaks]: Registered Alchemical Synthesizer schema (alchemical).");

        PureMashTweaks.LOGGER.info("[KubeJS x PureMash Tweaks]: All PureMash Tweaks recipe pipelines are linked and ready for scripts!");
    }
}