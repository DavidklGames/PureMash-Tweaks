package dev.davidklgames.puremashtweaks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class ShapelessSynthesisRecipe implements Recipe<CraftingInput> {
    private final String group;
    private final List<Ingredient> ingredients;
    private final ItemStackTemplate result;
    private PlacementInfo placementInfo; // Lazy cache for correct synchronization with the client.

    public ShapelessSynthesisRecipe(String group, List<Ingredient> ingredients, ItemStackTemplate result) {
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
    }

    public List<Ingredient> getIngredients() { return this.ingredients; }
    public ItemStackTemplate getResult() { return this.result; }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> activeItems = new java.util.ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) activeItems.add(stack);
        }

        // Filters out non-empty ingredients (ignores empty tags for ores from uninstalled mods).
        List<Ingredient> nonEmptyIngredients = this.ingredients.stream()
                .filter(ing -> !ing.isEmpty())
                .toList();

        if (activeItems.size() != nonEmptyIngredients.size()) return false;

        List<Ingredient> expected = new java.util.ArrayList<>(nonEmptyIngredients);
        for (ItemStack actual : activeItems) {
            boolean matched = false;
            for (int i = 0; i < expected.size(); i++) {
                if (expected.get(i).test(actual)) {
                    expected.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return expected.isEmpty();
    }

    @Override
    public @NonNull ItemStack assemble(CraftingInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() { return true; }

    @Override
    public @NonNull String group() { return this.group; }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPELESS_SYNTHESIS_SERIALIZER.get();
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<CraftingInput>> getType() {
        return dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPELESS_SYNTHESIS_TYPE.get();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            // Filters and synchronizes only the ingredients that actually exist in the session.
            List<Ingredient> nonEmptyIngredients = this.ingredients.stream()
                    .filter(ing -> !ing.isEmpty())
                    .toList();
            this.placementInfo = PlacementInfo.create(nonEmptyIngredients);
        }
        return this.placementInfo;
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() { return null; }

    public static final MapCodec<ShapelessSynthesisRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.ingredients),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShapelessSynthesisRecipe::getResult)
            ).apply(instance, ShapelessSynthesisRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessSynthesisRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.ingredients,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ShapelessSynthesisRecipe::new
    );
}