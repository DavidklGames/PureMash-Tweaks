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

public class CompressionRecipe implements Recipe<SingleRecipeInput> {
    private final String group;
    private final Ingredient input;
    private final int inputCount;
    private final ItemStackTemplate result;
    private final int timeCost;

    public CompressionRecipe(String group, Ingredient input, int inputCount, ItemStackTemplate result, int timeCost) {
        this.group = group;
        this.input = input;
        this.inputCount = inputCount;
        this.result = result;
        this.timeCost = timeCost;
    }

    public Ingredient getInput() { return this.input; }
    public int getInputCount() { return this.inputCount; }
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
        return ModRecipes.COMPRESSION_SERIALIZER.get();
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.COMPRESSION_TYPE.get();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.input);
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() { return null; }

    public static final MapCodec<CompressionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    Ingredient.CODEC.fieldOf("input").forGetter(CompressionRecipe::getInput),
                    Codec.INT.optionalFieldOf("input_count", 9).forGetter(CompressionRecipe::getInputCount),
                    ItemStackTemplate.CODEC.fieldOf("output").forGetter(CompressionRecipe::getResult),
                    Codec.INT.optionalFieldOf("time_cost", 20).forGetter(CompressionRecipe::getTimeCost)
            ).apply(instance, CompressionRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CompressionRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            Ingredient.CONTENTS_STREAM_CODEC, CompressionRecipe::getInput,
            ByteBufCodecs.VAR_INT, CompressionRecipe::getInputCount,
            ItemStackTemplate.STREAM_CODEC, CompressionRecipe::getResult,
            ByteBufCodecs.VAR_INT, CompressionRecipe::getTimeCost,
            CompressionRecipe::new
    );
}