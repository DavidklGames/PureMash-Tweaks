package dev.davidklgames.puremashtweaks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.davidklgames.puremashtweaks.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SingularityRecipe implements Recipe<SingleRecipeInput> {
    private final String group;
    private final Ingredient input;
    private final int cost;
    private final ItemStackTemplate result;
    private final int timeCost;

    public SingularityRecipe(String group, Ingredient input, int cost, ItemStackTemplate result, int timeCost) {
        this.group = group;
        this.input = input;
        this.cost = cost;
        this.result = result;
        this.timeCost = timeCost;
    }

    public Ingredient getInput() { return this.input; }
    public int getCost() { return this.cost; }
    public ItemStackTemplate getResult() { return this.result; }
    public int getTimeCost() { return this.timeCost; }

    @Override
    public boolean matches(SingleRecipeInput input, @NonNull Level level) {
        return this.input.test(input.item());
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull SingleRecipeInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() { return true; }

    @Override
    public @NonNull String group() { return this.group; }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.SINGULARITY_SERIALIZER.get();
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.SINGULARITY_TYPE.get();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.input);
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() { return null; }

    public static final MapCodec<SingularityRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    Ingredient.CODEC.fieldOf("item").forGetter(SingularityRecipe::getInput),
                    Codec.INT.optionalFieldOf("cost", 1000).forGetter(SingularityRecipe::getCost),
                    ItemStackTemplate.CODEC.fieldOf("output").forGetter(SingularityRecipe::getResult),
                    Codec.INT.optionalFieldOf("time_cost", 40).forGetter(SingularityRecipe::getTimeCost)
            ).apply(instance, SingularityRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SingularityRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            Ingredient.CONTENTS_STREAM_CODEC, SingularityRecipe::getInput,
            ByteBufCodecs.VAR_INT, SingularityRecipe::getCost,
            ItemStackTemplate.STREAM_CODEC, SingularityRecipe::getResult,
            ByteBufCodecs.VAR_INT, SingularityRecipe::getTimeCost,
            SingularityRecipe::new
    );
}