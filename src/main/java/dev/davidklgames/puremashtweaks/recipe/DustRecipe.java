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

public class DustRecipe implements Recipe<SingleRecipeInput> {
    private final String group;
    private final Ingredient input;
    private final ItemStackTemplate result;
    private final int timeCost;

    public DustRecipe(String group, Ingredient input, ItemStackTemplate result, int timeCost) {
        this.group = group;
        this.input = input;
        this.result = result;
        this.timeCost = timeCost;
    }

    public Ingredient getInput() { return this.input; }
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
        return ModRecipes.DUST_SERIALIZER.get();
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.DUST_TYPE.get();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.input);
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() { return null; }

    public static final MapCodec<DustRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    Ingredient.CODEC.fieldOf("input").forGetter(DustRecipe::getInput),
                    ItemStackTemplate.CODEC.fieldOf("output").forGetter(DustRecipe::getResult),
                    Codec.INT.optionalFieldOf("time_cost", 20).forGetter(DustRecipe::getTimeCost)
            ).apply(instance, DustRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DustRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            Ingredient.CONTENTS_STREAM_CODEC, DustRecipe::getInput,
            ItemStackTemplate.STREAM_CODEC, DustRecipe::getResult,
            ByteBufCodecs.VAR_INT, DustRecipe::getTimeCost,
            DustRecipe::new
    );
}