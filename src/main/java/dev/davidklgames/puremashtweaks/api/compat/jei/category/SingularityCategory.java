package dev.davidklgames.puremashtweaks.api.compat.jei.category;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SingularityCategory implements IRecipeCategory<CompressorRecipeHelper.CustomRecipeData> {
    public static final IRecipeType<CompressorRecipeHelper.CustomRecipeData> RECIPE_TYPE =
            IRecipeType.create(PureMashTweaks.MODID, "singularity", CompressorRecipeHelper.CustomRecipeData.class);

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/jei/tables/multifunctional_compressor/singularity_gui.png");
    private static final Identifier SILHOUETTES_TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/jei/colorful_silhouettes/jei_colorful_silhouettes.png");

    private final IDrawable icon;
    private final IDrawable background;

    public SingularityCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 63);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()));
    }

    @Override
    public @NotNull IRecipeType<CompressorRecipeHelper.CustomRecipeData> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.puremashtweaks.singularity");
    }

    @Override
    public int getWidth() { return 170; }

    @Override
    public int getHeight() { return 63; }

    @Override
    public void draw(@NotNull CompressorRecipeHelper.CustomRecipeData recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull net.minecraft.client.gui.GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        this.background.draw(graphics);

        long durationS = 1500L;
        long durationBP = 1500L;
        long totalCycle = durationS + durationBP;

        long time = System.currentTimeMillis();
        long elapsed = time % totalCycle;

        float progressS;
        float progressBP;

        if (elapsed < durationS) {
            progressS = (float) elapsed / durationS;
            progressBP = 0.0F;
        } else {
            progressS = 1.0F;
            progressBP = (float) (elapsed - durationS) / durationBP;
        }

        int fillWidth = (int) (progressS * 16);
        if (fillWidth > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, SILHOUETTES_TEXTURE, 62, 21, 17.0F, 0.0F, fillWidth, 16, 50, 35);
        }

        int arrowWidth = (int) (progressBP * 24);
        if (arrowWidth > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, SILHOUETTES_TEXTURE, 86, 21, 13.0F, 18.0F, arrowWidth, 17, 50, 35);
        }
    }

    @Override
    public @NotNull IDrawable getIcon() { return this.icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CompressorRecipeHelper.CustomRecipeData recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 21).addIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, List.of(new ItemStack(recipe.ingredient())));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 21).addIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, List.of(recipe.result()));
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull CompressorRecipeHelper.CustomRecipeData recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX > 61.0 && mouseX < 79.0 && mouseY > 20.0 && mouseY < 38.0) {
            tooltip.add(Component.literal("Requires: " + recipe.cost()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (mouseX > 85.0 && mouseX < 111.0 && mouseY > 20.0 && mouseY < 38.0) {
            tooltip.add(Component.literal("Time: " + recipe.time() + " ticks").withStyle(ChatFormatting.BLUE));
        }
    }
}