package dev.davidklgames.puremashtweaks.api.compat.jei.category;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.compat.jei.recipe.GeneratorFuelRecipe;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GeneratorFuelCategory implements IRecipeCategory<GeneratorFuelRecipe> {
    public static final IRecipeType<GeneratorFuelRecipe> RECIPE_TYPE =
            IRecipeType.create(PureMashTweaks.MODID, "generator_fuel", GeneratorFuelRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public GeneratorFuelCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(160, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PUREMASH_GENERATOR.get()));
    }

    @Override
    public @NotNull IRecipeType<GeneratorFuelRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("container.puremashtweaks.puremash_generator");
    }

    @Override
    public int getWidth() { return 160; }

    @Override
    public int getHeight() { return 60; }

    @Override
    public void draw(@NotNull GeneratorFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        graphics.fill(10, 18, 28, 36, 0xFF555555);
        graphics.fill(11, 19, 27, 35, 0xFF222222);

        int seconds = recipe.burnTicks() / 20;
        Component timeComp = Component.literal("Burn Time: " + seconds + "s").withStyle(ChatFormatting.DARK_GRAY);
        Component genComp = Component.literal("Generation: +" + String.format("%,d", recipe.fePerTick()) + " FE/t").withStyle(ChatFormatting.GREEN);
        Component totalComp = Component.literal("Total: " + String.format("%,d", recipe.getTotalEnergy()) + " FE").withStyle(ChatFormatting.GOLD);
        Component tempComp = Component.literal("Max Temp: " + (int)recipe.maxTemp() + " °C").withStyle(ChatFormatting.RED);

        graphics.text(net.minecraft.client.Minecraft.getInstance().font, timeComp, 36, 6, 0xFF404040, false);
        graphics.text(net.minecraft.client.Minecraft.getInstance().font, genComp, 36, 18, 0xFF00AA00, false);
        graphics.text(net.minecraft.client.Minecraft.getInstance().font, totalComp, 36, 30, 0xFFA17300, false);
        graphics.text(net.minecraft.client.Minecraft.getInstance().font, tempComp, 36, 42, 0xFFAA0000, false);
    }

    @Override
    public @NotNull IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GeneratorFuelRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 11, 19).addIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, recipe.fuelStack());
    }
}