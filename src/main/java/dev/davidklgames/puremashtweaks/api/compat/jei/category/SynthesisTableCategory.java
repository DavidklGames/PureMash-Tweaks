package dev.davidklgames.puremashtweaks.api.compat.jei.category;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.recipe.ShapedSynthesisRecipe;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
// Update the class header to implement the correct recipe signature for 26.1.2!
public class SynthesisTableCategory implements IRecipeCategory<Object> {
    // 1. Registry of the recipe type using the new IRecipeType (mezz.jei.api.recipe.types.IRecipeType)
    public static final IRecipeType RECIPE_TYPE =
            IRecipeType.create(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis_craft"), (Class) RecipeHolder.class);
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/jei/tables/synthesis/synthesis_gui.png");
    private final IDrawable background;
    private final IDrawable icon;

    public SynthesisTableCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 189, 163);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SYNTHESIS_TABLE.get()));
    }

    @Override
    public @NonNull IRecipeType<Object> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.puremashtweaks.synthesis_table");
    }

    @Override
    public int getWidth() {
        return 189;
    }

    @Override
    public int getHeight() {
        return 163;
    }

    @Override
    public void draw(@NonNull Object recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull net.minecraft.client.gui.GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        this.background.draw(graphics);
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    // 1. Updated setRecipe method with safety cast to prevent the "value" error in Object
    @Override
    @SuppressWarnings("unchecked")
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NonNull Object recipeHolder, @NotNull IFocusGroup focuses) {
        // Safety cast to read the RecipeHolder record in 26.1.2
        if (recipeHolder instanceof RecipeHolder<?> holder) {
            Recipe<CraftingInput> recipe = (Recipe<CraftingInput>) holder.value();
            ClientLevel level = Minecraft.getInstance().level;
            assert level != null;

            List<Ingredient> inputs = recipe.placementInfo().ingredients();
            ItemStack output = recipe.assemble(CraftingInput.EMPTY);

            if (recipe instanceof ShapedSynthesisRecipe shaped) {
                int stackIndex = 0;
                int heightOffset = Math.floorDiv(9 - shaped.getHeight(), 2);
                int widthOffset = Math.floorDiv(9 - shaped.getWidth(), 2);

                for (int i = 0; i < 9; ++i) {
                    for (int j = 0; j < 9; ++j) {
                        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 2, i * 18 + 2);
                        if (i >= heightOffset && i < heightOffset + shaped.getHeight() && j >= widthOffset && j < widthOffset + shaped.getWidth()) {

                            // Type handling: safely unpacks the Optional<Ingredient> from JEI
                            Optional<Ingredient> optIng = shaped.getIngredients().get(stackIndex);
                            if (optIng.isPresent()) {
                                List<ItemStack> itemStacks = optIng.get().items().map(h -> new ItemStack(h.value())).toList();
                                slot.addIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, itemStacks);
                            }

                            ++stackIndex;
                        }
                    }
                }
            } else {
                // Logic for Shapeless 9x9 recipes
                for (int i = 0; i < 9; ++i) {
                    for (int j = 0; j < 9; ++j) {
                        int index = j + i * 9;
                        if (index < inputs.size()) {
                            List<ItemStack> itemStacks = inputs.get(index).items().map(h -> new ItemStack(h.value())).toList();
                            builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 2, i * 18 + 2).addIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, itemStacks);
                        }
                    }
                }
            }

            builder.addSlot(RecipeIngredientRole.OUTPUT, 170, 72).addIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, java.util.List.of(output));
        }
    }

    // 2. Updated getTooltip method with safety cast to prevent the "value" error in Object
    @Override
    @SuppressWarnings("unchecked")
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NonNull Object recipeHolder, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (recipeHolder instanceof RecipeHolder<?> holder) {
            Recipe<CraftingInput> recipe = (Recipe<CraftingInput>) holder.value();
            boolean shapeless = !(recipe instanceof ShapedSynthesisRecipe);
            int sX = (shapeless ? 340 : 306) / 2;
            int sY = 100;
            if (shapeless && mouseX > (double)(sX + 10) && mouseX < (double)(sX + 20) && mouseY > (double)(sY - 1) && mouseY < (double)(sY + 8)) {
                tooltip.add(Component.translatable("jei.tooltip.shapeless.recipe").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}