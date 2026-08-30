package dev.davidklgames.puremashtweaks.api.compat.jei.category;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AlchemicalSynthesizerCategory implements IRecipeCategory<AlchemicalRecipeHelper.ParsedRecipe> {
    public static final IRecipeType<AlchemicalRecipeHelper.ParsedRecipe> RECIPE_TYPE =
            IRecipeType.create(PureMashTweaks.MODID, "alchemical", AlchemicalRecipeHelper.ParsedRecipe.class);

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            PureMashTweaks.MODID,
            "textures/gui/jei/tables/alchemical/alchemical_gui.png"
    );

    private static final Identifier ALCHEMICAL_SILHOUETTES = Identifier.fromNamespaceAndPath(
            PureMashTweaks.MODID,
            "textures/gui/jei/colorful_silhouettes/jei_alchemical_colorful_silhouettes.png"
    );

    private final IDrawable background;
    private final IDrawable icon;

    public AlchemicalSynthesizerCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 151, 81);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ALCHEMICAL_SYNTHESIZER.get()));
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

        long totalCycle = Math.max(500L, recipe.time() * 50L);
        long time = System.currentTimeMillis();
        float progress = (time % totalCycle) / (float) totalCycle;

        // 1. Seta Central de Reação
        int straightW = (int) (progress * 22);
        if (straightW > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ALCHEMICAL_SILHOUETTES, 30, 38, 0.0F, 0.0F, straightW, 17, 24, 50);
        }

        // 2. Seta de Fluido (Topo)
        if (recipe.fluid() != null) {
            int curveDownH = (int) (progress * 16);
            if (curveDownH > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ALCHEMICAL_SILHOUETTES, 34, 17, 4.0F, 18.0F, 18, curveDownH, 24, 50);
            }
        }

        // 3. Seta de Ferramenta (Base)
        if (recipe.toolType() != null && !recipe.toolType().equals("none")) {
            int curveUpH = (int) (progress * 15);
            if (curveUpH > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ALCHEMICAL_SILHOUETTES, 33, 75 - curveUpH, 4.0F, 35.0F + (15 - curveUpH), 18, curveUpH, 24, 50);
            }
        }

        // 4. Silhuetas Cicláveis de Ferramentas no Slot de Ferramentas do JEI (X=9, Y=59)
        if (recipe.toolType() == null || recipe.toolType().equals("none")) {
            int cycleIndex = (int) ((System.currentTimeMillis() / 1000L) % 4);
            float u = 224.0F;
            float v = cycleIndex * 16.0F;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 9, 59, u, v, 16, 16, 256, 256);
        }
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull AlchemicalRecipeHelper.ParsedRecipe recipe, @NotNull IFocusGroup focuses) {
        // Slot de Entrada (X=9, Y=38)
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 38)
                .add(new ItemStack(recipe.input()));

        // Slot de Fluido (X=9, Y=17)
        if (recipe.fluid() != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 9, 17)
                    .add(recipe.fluid(), recipe.fluidAmount());
        }

        // Slot de Ferramentas / Catalisador (X=9, Y=59)
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
            case "paxel" -> {
                tools.add(new ItemStack(ModItems.SYNTHORIUM_PAXEL.get()));
                tools.add(new ItemStack(ModItems.MOLDELONIAN_PAXEL.get()));
            }
        }
        if (!tools.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 9, 59)
                    .addItemStacks(tools);
        }

        // Slot de Saída (X=59, Y=6)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 6)
                .add(recipe.output());
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull AlchemicalRecipeHelper.ParsedRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 30.0 && mouseX <= 54.0 && mouseY >= 35.0 && mouseY <= 55.0) {
            float seconds = (float) recipe.time() / 20.0F;
            tooltip.add(Component.literal("Time: " + String.format(java.util.Locale.US, "%.1fs", seconds)).withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal("Energy: " + recipe.energyCost() + " FE/t").withStyle(ChatFormatting.RED));
        }

        if (mouseX >= 59.0 && mouseX <= 75.0 && mouseY >= 6.0 && mouseY <= 22.0) {
            if (recipe.doubleOutput()) {
                tooltip.add(Component.literal("Special Route: Dual Output").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }
}