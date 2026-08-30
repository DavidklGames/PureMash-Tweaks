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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class ShapelessSynthesisRecipe implements Recipe<CraftingInput> {
    private final String group;
    private final List<Ingredient> ingredients;
    private final ItemStackTemplate result;

    public ShapelessSynthesisRecipe(String group, List<Ingredient> ingredients, ItemStackTemplate result) {
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public ItemStackTemplate getResult() {
        return this.result;
    }

    /**
     * Filters out ingredients that resolve to empty (e.g. uninstalled optional mod tags).
     */
    public List<Ingredient> getActiveIngredients() {
        List<Ingredient> active = new ArrayList<>();
        for (Ingredient ing : this.ingredients) {
            try {
                if (ing != null && !ing.isEmpty()) {
                    active.add(ing);
                }
            } catch (Exception e) {
                // Safe DataGen fallback when tags are not yet resolved
                active.add(ing);
            }
        }
        return active;
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

        List<Ingredient> activeIngredients = getActiveIngredients();

        if (activeItems.size() != activeIngredients.size()) {
            return false;
        }

        List<Ingredient> expected = new ArrayList<>(activeIngredients);
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
    public @NonNull ItemStack assemble(CraftingInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public @NonNull String group() {
        return this.group;
    }

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
        return PlacementInfo.create(getActiveIngredients());
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() {
        return null;
    }

    public static final MapCodec<ShapelessSynthesisRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.ingredients),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShapelessSynthesisRecipe::getResult)
            ).apply(instance, ShapelessSynthesisRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessSynthesisRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ShapelessSynthesisRecipe::group,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ShapelessSynthesisRecipe::getIngredients,
            ItemStackTemplate.STREAM_CODEC, ShapelessSynthesisRecipe::getResult,
            ShapelessSynthesisRecipe::new
    );
}