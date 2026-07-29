package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.recipe.ShapedSynthesisRecipe;
import dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe;
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

    // --- OFFICIAL 9X9 RECIPE TYPE REGISTERS IN 26.1.2 ---
    public static final DeferredHolder<RecipeType<?>, RecipeType<ShapedSynthesisRecipe>> SHAPED_SYNTHESIS_TYPE =
            RECIPE_TABS.register("shaped_synthesis", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "shaped_synthesis")));

    public static final DeferredHolder<RecipeType<?>, RecipeType<ShapelessSynthesisRecipe>> SHAPELESS_SYNTHESIS_TYPE =
            RECIPE_TABS.register("shapeless_synthesis", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "shapeless_synthesis")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedSynthesisRecipe>> SHAPED_SYNTHESIS_SERIALIZER =
            RECIPE_SERIALIZERS.register("shaped_synthesis", () -> new RecipeSerializer<>(ShapedSynthesisRecipe.CODEC, ShapedSynthesisRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapelessSynthesisRecipe>> SHAPELESS_SYNTHESIS_SERIALIZER =
            RECIPE_SERIALIZERS.register("shapeless_synthesis", () -> new RecipeSerializer<>(ShapelessSynthesisRecipe.CODEC, ShapelessSynthesisRecipe.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        RECIPE_TABS.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Recipe types and serializers registered successfully.");
    }
}