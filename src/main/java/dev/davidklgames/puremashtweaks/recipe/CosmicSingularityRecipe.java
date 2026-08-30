package dev.davidklgames.puremashtweaks.recipe;

import com.mojang.serialization.MapCodec;
import dev.davidklgames.puremashtweaks.registry.ModRecipes;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CosmicSingularityRecipe extends ShapelessSynthesisRecipe {

    public static final MapCodec<CosmicSingularityRecipe> CODEC = MapCodec.unit(CosmicSingularityRecipe::new);

    // StreamCodec manual sem checagem estrita de referência singleton
    public static final StreamCodec<RegistryFriendlyByteBuf, CosmicSingularityRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {},
            buf -> new CosmicSingularityRecipe()
    );

    public CosmicSingularityRecipe() {
        super("", List.of(), new ItemStackTemplate(ModSingularities.COSMIC_SINGULARITY.get(), 1));
    }

    public static List<Ingredient> getDynamicIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        for (var holder : ModSingularities.REGISTERED_SINGULARITIES) {
            if (holder != null && holder != ModSingularities.COSMIC_SINGULARITY) {
                ingredients.add(Ingredient.of(holder.get()));
            }
        }
        return ingredients;
    }

    @Override
    public List<Ingredient> getIngredients() {
        return getDynamicIngredients();
    }

    @Override
    public List<Ingredient> getActiveIngredients() {
        return getDynamicIngredients();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.create(getActiveIngredients());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> activeItems = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                activeItems.add(stack);
            }
        }

        List<Ingredient> expectedIngredients = getActiveIngredients();
        if (activeItems.size() != expectedIngredients.size()) {
            return false;
        }

        List<Ingredient> expected = new ArrayList<>(expectedIngredients);
        for (ItemStack actual : activeItems) {
            boolean matched = false;
            for (int i = 0; i < expected.size(); i++) {
                if (expected.get(i).test(actual)) {
                    expected.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        return expected.isEmpty();
    }

    @Override
    public @NonNull RecipeSerializer<? extends ShapelessSynthesisRecipe> getSerializer() {
        return ModRecipes.COSMIC_SYNTHESIS_SERIALIZER.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}