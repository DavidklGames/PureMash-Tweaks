package dev.davidklgames.puremashtweaks.api.compat.jei.category;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AlchemicalSynthesizerCategory implements IRecipeCategory<AlchemicalRecipeHelper.ParsedRecipe> {
    public static final IRecipeType<AlchemicalRecipeHelper.ParsedRecipe> RECIPE_TYPE =
            IRecipeType.create(PureMashTweaks.MODID, "alchemical", AlchemicalRecipeHelper.ParsedRecipe.class);

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/jei/tables/alchemical/alchemical_gui.png");

    private static final Identifier ALCHEMICAL_SILHOUETTES = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/jei/colorful_silhouettes/jei_alchemical_colorful_silhouettes.png");

    private final IDrawable background;
    private final IDrawable icon;

    public AlchemicalSynthesizerCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 151, 81);
        this.icon = helper.createDrawableIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ALCHEMICAL_SYNTHESIZER.get()));
    }

    @Override
    public @NotNull IRecipeType<AlchemicalRecipeHelper.ParsedRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.puremashtweaks.alchemical_synthesizer");
    }

    @Override
    public int getWidth() {
        return 151;
    }

    @Override
    public int getHeight() {
        return 81;
    }

    @Override
    public void draw(@NotNull AlchemicalRecipeHelper.ParsedRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        this.background.draw(graphics);

        long time = System.currentTimeMillis();
        float progress = (time % 2000L) / 2000.0F;

        int straightW = (int) (progress * 22);
        if (straightW > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ALCHEMICAL_SILHOUETTES, 30, 38, 0.0F, 0.0F, straightW, 17, 24, 50);
        }

        if (recipe.fluid() != null) {
            int curveDownH = (int) (progress * 16);
            if (curveDownH > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ALCHEMICAL_SILHOUETTES, 34, 17, 4.0F, 18.0F, 18, curveDownH, 24, 50);
            }
        }

        if (recipe.toolType() != null && !recipe.toolType().equals("none")) {
            int curveUpH = (int) (progress * 15);
            if (curveUpH > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ALCHEMICAL_SILHOUETTES, 33, 75 - curveUpH, 4.0F, 35.0F + (15 - curveUpH), 18, curveUpH, 24, 50);
            }
        }
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull AlchemicalRecipeHelper.ParsedRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 38).add(new ItemStack(recipe.input()));

        if (recipe.fluid() != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 9, 17).add(recipe.fluid(), recipe.fluidAmount());
        }

        List<ItemStack> tools = new ArrayList<>();
        switch (recipe.toolType()) {
            case "pickaxe" -> {
                for (var holder : BuiltInRegistries.ITEM.getOrThrow(ItemTags.PICKAXES)) {
                    tools.add(new ItemStack(holder.value()));
                }
            }
            case "shovel" -> {
                for (var holder : BuiltInRegistries.ITEM.getOrThrow(ItemTags.SHOVELS)) {
                    tools.add(new ItemStack(holder.value()));
                }
            }
            case "axe" -> {
                for (var holder : BuiltInRegistries.ITEM.getOrThrow(ItemTags.AXES)) {
                    tools.add(new ItemStack(holder.value()));
                }
            }
        }
        if (!tools.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 9, 59).addItemStacks(tools);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 6).add(recipe.output());
    }

    @Override
    public void getTooltip(@NotNull mezz.jei.api.gui.builder.ITooltipBuilder tooltip, @NotNull AlchemicalRecipeHelper.ParsedRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 59.0 && mouseX <= 75.0 && mouseY >= 6.0 && mouseY <= 22.0) {
            if (recipe.doubleOutput()) {
                tooltip.add(Component.literal("Route D: Super-Smelting").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(Component.literal("Doubles output when processed with a Pickaxe/Paxel!").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}