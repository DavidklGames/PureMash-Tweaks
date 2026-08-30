package dev.davidklgames.puremashtweaks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.davidklgames.puremashtweaks.registry.ModRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class AlchemicalRecipe implements Recipe<SingleRecipeInput> {
    private final String group;
    private final Ingredient input;
    private final ItemStackTemplate output;
    private final int outputCount;
    private final String fluidId;
    private final int fluidAmount;
    private final String toolType;
    private final int time;
    private final int energy;
    private final boolean doubleOutput;

    public AlchemicalRecipe(String group, Ingredient input, ItemStackTemplate output, int outputCount, String fluidId, int fluidAmount, String toolType, int time, int energy, boolean doubleOutput) {
        this.group = group;
        this.input = input;
        this.output = output;
        this.outputCount = outputCount;
        this.fluidId = fluidId != null ? fluidId : "none";
        this.fluidAmount = fluidAmount;
        this.toolType = toolType != null ? toolType.toLowerCase() : "none";
        this.time = time;
        this.energy = energy;
        this.doubleOutput = doubleOutput;
    }

    public Ingredient getInput() { return this.input; }
    public ItemStackTemplate getOutput() { return this.output; }
    public int getOutputCount() { return this.outputCount; }
    public String getFluidId() { return this.fluidId; }
    public int getFluidAmount() { return this.fluidAmount; }
    public String getToolType() { return this.toolType; }
    public int getTime() { return this.time; }
    public int getEnergy() { return this.energy; }
    public boolean isDoubleOutput() { return this.doubleOutput; }

    public @Nullable Fluid getRequiredFluid() {
        if (this.fluidId.equalsIgnoreCase("none") || this.fluidId.isEmpty()) return null;
        Identifier id = Identifier.tryParse(this.fluidId);
        if (id == null) return null;
        return BuiltInRegistries.FLUID.get(id).map(net.minecraft.core.Holder::value).orElse(null);
    }

    public boolean matches(Fluid activeFluid, ItemStack inputStack, ItemStack toolStack) {
        if (inputStack.isEmpty() || !this.input.test(inputStack)) {
            return false;
        }

        Fluid reqFluid = getRequiredFluid();
        if (reqFluid != null) {
            if (activeFluid == null || activeFluid == Fluids.EMPTY || activeFluid != reqFluid) {
                return false;
            }
        }

        return switch (this.toolType) {
            case "pickaxe" -> toolStack.is(ItemTags.PICKAXES);
            case "shovel" -> toolStack.is(ItemTags.SHOVELS);
            case "axe" -> toolStack.is(ItemTags.AXES);
            default -> true;
        };
    }

    @Override
    public boolean matches(SingleRecipeInput input, @NonNull Level level) {
        return this.input.test(input.item());
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull SingleRecipeInput input) {
        ItemStack stack = this.output.create();
        if (this.outputCount > 1) {
            stack.setCount(this.outputCount);
        }
        return stack;
    }

    @Override
    public boolean showNotification() { return true; }

    @Override
    public @NonNull String group() { return this.group; }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.ALCHEMICAL_SERIALIZER.get();
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.ALCHEMICAL_TYPE.get();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.input);
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() { return null; }

    public static final MapCodec<AlchemicalRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    Ingredient.CODEC.fieldOf("input").forGetter(AlchemicalRecipe::getInput),
                    ItemStackTemplate.CODEC.fieldOf("output").forGetter(AlchemicalRecipe::getOutput),
                    Codec.INT.optionalFieldOf("output_count", 1).forGetter(AlchemicalRecipe::getOutputCount),
                    Codec.STRING.optionalFieldOf("fluid", "none").forGetter(AlchemicalRecipe::getFluidId),
                    Codec.INT.optionalFieldOf("fluid_amount", 0).forGetter(AlchemicalRecipe::getFluidAmount),
                    Codec.STRING.optionalFieldOf("tool_type", "none").forGetter(AlchemicalRecipe::getToolType),
                    Codec.INT.optionalFieldOf("time", 60).forGetter(AlchemicalRecipe::getTime),
                    Codec.INT.optionalFieldOf("energy", 100).forGetter(AlchemicalRecipe::getEnergy),
                    Codec.BOOL.optionalFieldOf("double_output", false).forGetter(AlchemicalRecipe::isDoubleOutput)
            ).apply(instance, AlchemicalRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AlchemicalRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, recipe.group());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
                ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getOutput());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getOutputCount());
                ByteBufCodecs.STRING_UTF8.encode(buf, recipe.getFluidId());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getFluidAmount());
                ByteBufCodecs.STRING_UTF8.encode(buf, recipe.getToolType());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getTime());
                ByteBufCodecs.VAR_INT.encode(buf, recipe.getEnergy());
                ByteBufCodecs.BOOL.encode(buf, recipe.isDoubleOutput());
            },
            buf -> new AlchemicalRecipe(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                    ItemStackTemplate.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );
}