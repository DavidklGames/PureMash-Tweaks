package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.recipe.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TABS = DeferredRegister.create(Registries.RECIPE_TYPE, PureMashTweaks.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PureMashTweaks.MODID);

    // --- 9X9 SYNTHESIS RECIPE TYPES ---
    public static final DeferredHolder<RecipeType<?>, RecipeType<ShapedSynthesisRecipe>> SHAPED_SYNTHESIS_TYPE =
            RECIPE_TABS.register("shaped_synthesis", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "shaped_synthesis")));

    public static final DeferredHolder<RecipeType<?>, RecipeType<ShapelessSynthesisRecipe>> SHAPELESS_SYNTHESIS_TYPE =
            RECIPE_TABS.register("shapeless_synthesis", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "shapeless_synthesis")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedSynthesisRecipe>> SHAPED_SYNTHESIS_SERIALIZER =
            RECIPE_SERIALIZERS.register("shaped_synthesis", () -> new RecipeSerializer<>(ShapedSynthesisRecipe.CODEC, ShapedSynthesisRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapelessSynthesisRecipe>> SHAPELESS_SYNTHESIS_SERIALIZER =
            RECIPE_SERIALIZERS.register("shapeless_synthesis", () -> new RecipeSerializer<>(ShapelessSynthesisRecipe.CODEC, ShapelessSynthesisRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CosmicSingularityRecipe>> COSMIC_SYNTHESIS_SERIALIZER =
            RECIPE_SERIALIZERS.register("cosmic_synthesis", () -> new RecipeSerializer<>(CosmicSingularityRecipe.CODEC, CosmicSingularityRecipe.STREAM_CODEC));

    // --- MULTIFUNCTIONAL COMPRESSOR RECIPE TYPES (KubeJS / Datapacks) ---
    public static final DeferredHolder<RecipeType<?>, RecipeType<CompressionRecipe>> COMPRESSION_TYPE =
            RECIPE_TABS.register("compression", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "compression")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressionRecipe>> COMPRESSION_SERIALIZER =
            RECIPE_SERIALIZERS.register("compression", () -> new RecipeSerializer<>(CompressionRecipe.CODEC, CompressionRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeType<?>, RecipeType<SingularityRecipe>> SINGULARITY_TYPE =
            RECIPE_TABS.register("singularity", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "singularity")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SingularityRecipe>> SINGULARITY_SERIALIZER =
            RECIPE_SERIALIZERS.register("singularity", () -> new RecipeSerializer<>(SingularityRecipe.CODEC, SingularityRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeType<?>, RecipeType<DustRecipe>> DUST_TYPE =
            RECIPE_TABS.register("dust", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "dust")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DustRecipe>> DUST_SERIALIZER =
            RECIPE_SERIALIZERS.register("dust", () -> new RecipeSerializer<>(DustRecipe.CODEC, DustRecipe.STREAM_CODEC));

    // --- ALCHEMICAL SYNTHESIZER RECIPE TYPE (KubeJS / Datapacks) ---
    public static final DeferredHolder<RecipeType<?>, RecipeType<AlchemicalRecipe>> ALCHEMICAL_TYPE =
            RECIPE_TABS.register("alchemical", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "alchemical")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemicalRecipe>> ALCHEMICAL_SERIALIZER =
            RECIPE_SERIALIZERS.register("alchemical", () -> new RecipeSerializer<>(AlchemicalRecipe.CODEC, AlchemicalRecipe.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        RECIPE_TABS.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Recipe types and serializers registered successfully.");
    }
}