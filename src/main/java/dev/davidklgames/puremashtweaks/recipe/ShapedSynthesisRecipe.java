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
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class ShapedSynthesisRecipe implements Recipe<CraftingInput> {
    private final String group;
    private final int width;
    private final int height;
    private final List<Optional<Ingredient>> ingredients;
    private final ItemStackTemplate result;

    private final List<String> pattern;
    private final Map<String, Ingredient> key;

    public ShapedSynthesisRecipe(String group, int width, int height, List<Optional<Ingredient>> ingredients, ItemStackTemplate result, List<String> pattern, Map<String, Ingredient> key) {
        this.group = group;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
        this.pattern = pattern;
        this.key = key;
    }

    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }
    public List<Optional<Ingredient>> getIngredients() { return this.ingredients; }
    public ItemStackTemplate getResult() { return this.result; }
    public List<String> getPattern() { return this.pattern; }
    public Map<String, Ingredient> getKeys() { return this.key; }

    @Override
    public boolean matches(CraftingInput input, @NonNull Level level) {
        if (input.width() < this.width || input.height() < this.height) {
            return false;
        }

        int maxStartY = input.height() - this.height;
        int maxStartX = input.width() - this.width;

        for (int startY = 0; startY <= maxStartY; startY++) {
            for (int startX = 0; startX <= maxStartX; startX++) {
                if (this.matches(input, startX, startY, true)) return true;
                if (this.matches(input, startX, startY, false)) return true;
            }
        }
        return false;
    }

    private boolean matches(CraftingInput input, int startX, int startY, boolean mirror) {
        int inputWidth = input.width();
        int inputHeight = input.height();

        for (int y = 0; y < inputHeight; y++) {
            for (int x = 0; x < inputWidth; x++) {
                int rx = x - startX;
                int ry = y - startY;
                Optional<Ingredient> expected = Optional.empty();

                if (rx >= 0 && ry >= 0 && rx < this.width && ry < this.height) {
                    int index = mirror ? (this.width - 1 - rx + ry * this.width) : (rx + ry * this.width);
                    expected = this.ingredients.get(index);
                }

                ItemStack actual = input.getItem(x + y * inputWidth);

                if (expected.isPresent()) {
                    if (!expected.get().test(actual)) return false;
                } else {
                    if (!actual.isEmpty()) return false;
                }
            }
        }
        return true;
    }

    @Override
    public @NonNull ItemStack assemble(@NonNull CraftingInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() { return true; }

    @Override
    public @NonNull String group() { return this.group; }

    @Override
    public @NonNull RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPED_SYNTHESIS_SERIALIZER.get();
    }

    @Override
    public @NonNull RecipeType<? extends Recipe<CraftingInput>> getType() {
        return dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPED_SYNTHESIS_TYPE.get();
    }

    @Override
    public @NonNull PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(this.ingredients);
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() { return null; }

    public static ShapedSynthesisRecipe newFromCodec(String group, List<String> pattern, Map<String, Ingredient> key, ItemStackTemplate result) {
        int firstRow = -1;
        int lastRow = -1;
        int firstCol = Integer.MAX_VALUE;
        int lastCol = -1;

        for (int r = 0; r < pattern.size(); r++) {
            String row = pattern.get(r);
            boolean rowEmpty = true;
            for (int c = 0; c < row.length(); c++) {
                char ch = row.charAt(c);
                if (ch != ' ' && ch != '.') {
                    rowEmpty = false;
                    if (c < firstCol) firstCol = c;
                    if (c > lastCol) lastCol = c;
                }
            }
            if (!rowEmpty) {
                if (firstRow == -1) firstRow = r;
                lastRow = r;
            }
        }

        if (firstRow == -1) {
            return new ShapedSynthesisRecipe(group, 0, 0, List.of(), result, pattern, key);
        }

        int trimmedHeight = lastRow - firstRow + 1;
        int trimmedWidth = lastCol - firstCol + 1;

        List<Optional<Ingredient>> trimmedIngredients = new java.util.ArrayList<>(trimmedWidth * trimmedHeight);
        for (int r = firstRow; r <= lastRow; r++) {
            String row = pattern.get(r);
            for (int c = firstCol; c <= lastCol; c++) {
                char ch = row.charAt(c);
                if (ch == ' ' || ch == '.') {
                    trimmedIngredients.add(Optional.empty());
                } else {
                    trimmedIngredients.add(Optional.ofNullable(key.get(String.valueOf(ch))));
                }
            }
        }

        return new ShapedSynthesisRecipe(group, trimmedWidth, trimmedHeight, trimmedIngredients, result, pattern, key);
    }

    public static final MapCodec<ShapedSynthesisRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    Codec.STRING.listOf().fieldOf("pattern").forGetter(ShapedSynthesisRecipe::getPattern),
                    Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(ShapedSynthesisRecipe::getKeys),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShapedSynthesisRecipe::getResult)
            ).apply(instance, ShapedSynthesisRecipe::newFromCodec)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedSynthesisRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ShapedSynthesisRecipe::group,
            ByteBufCodecs.VAR_INT, ShapedSynthesisRecipe::getWidth,
            ByteBufCodecs.VAR_INT, ShapedSynthesisRecipe::getHeight,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ShapedSynthesisRecipe::getIngredients,
            ItemStackTemplate.STREAM_CODEC, ShapedSynthesisRecipe::getResult,
            (group, width, height, ingredients, result) -> new ShapedSynthesisRecipe(group, width, height, ingredients, result, List.of(), Map.of())
    );
}