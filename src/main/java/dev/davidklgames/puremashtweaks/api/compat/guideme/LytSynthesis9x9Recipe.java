package dev.davidklgames.puremashtweaks.api.compat.guideme;

import dev.davidklgames.puremashtweaks.recipe.ShapedSynthesisRecipe;
import dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytVBox;
import guideme.scene.LytItemImage;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;

public class LytSynthesis9x9Recipe {

    public static LytBlock create(RecipeHolder<? extends Recipe<CraftingInput>> holder) {
        Recipe<CraftingInput> recipe = holder.value();
        ItemStack resultStack = recipe.assemble(CraftingInput.EMPTY);

        ItemStack[] grid = new ItemStack[81];
        for (int i = 0; i < 81; i++) {
            grid[i] = ItemStack.EMPTY;
        }

        if (recipe instanceof ShapedSynthesisRecipe shaped) {
            int width = shaped.getWidth();
            int height = shaped.getHeight();
            int startX = (9 - width) / 2;
            int startY = (9 - height) / 2;
            List<Optional<Ingredient>> ingredients = shaped.getIngredients();

            int idx = 0;
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    if (idx < ingredients.size()) {
                        Optional<Ingredient> opt = ingredients.get(idx++);
                        if (opt.isPresent()) {
                            grid[(startY + r) * 9 + (startX + c)] = getFirstStack(opt.get());
                        }
                    }
                }
            }
        } else if (recipe instanceof ShapelessSynthesisRecipe shapeless) {
            List<Ingredient> ingredients = shapeless.getIngredients();
            for (int i = 0; i < ingredients.size() && i < 81; i++) {
                grid[i] = getFirstStack(ingredients.get(i));
            }
        }

        // Bloco Raiz Vertical
        LytVBox rootBox = new LytVBox();
        rootBox.setGap(4);

        // 1. Cabeçalho com o ícone da Mesa de Síntese e Título
        LytHBox header = new LytHBox();
        header.setGap(4);
        header.setAlignItems(AlignItems.CENTER);

        LytItemImage tableIcon = new LytItemImage();
        tableIcon.setItem(new ItemStack(ModBlocks.SYNTHESIS_TABLE.get()));
        header.append(tableIcon);
        header.append(LytParagraph.of("9x9 Synthesis Table"));
        rootBox.append(header);

        // 2. Fluxo Principal Horizontal: [ Grade 9x9 ] -> [ Seta ] -> [ Saída ]
        LytHBox mainFlow = new LytHBox();
        mainFlow.setGap(6);
        mainFlow.setAlignItems(AlignItems.CENTER);

        // Grade de 9 Linhas x 9 Colunas
        LytVBox gridBox = new LytVBox();
        gridBox.setGap(1);

        for (int row = 0; row < 9; row++) {
            LytHBox rowBox = new LytHBox();
            rowBox.setGap(1);
            rowBox.setAlignItems(AlignItems.CENTER);

            for (int col = 0; col < 9; col++) {
                ItemStack slotItem = grid[row * 9 + col];
                LytItemImage itemImage = new LytItemImage();
                itemImage.setItem(slotItem.isEmpty() ? new ItemStack(Items.AIR) : slotItem);
                rowBox.append(itemImage);
            }
            gridBox.append(rowBox);
        }
        mainFlow.append(gridBox);

        // Seta de Progresso
        mainFlow.append(LytParagraph.of("➔"));

        // Slot de Saída
        LytVBox outputBox = new LytVBox();
        outputBox.setAlignItems(AlignItems.CENTER);
        LytItemImage outputImage = new LytItemImage();
        outputImage.setItem(resultStack);
        outputBox.append(outputImage);

        mainFlow.append(outputBox);
        rootBox.append(mainFlow);

        return rootBox;
    }

    private static ItemStack getFirstStack(Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return ItemStack.EMPTY;
        return ingredient.items()
                .findFirst()
                .map(Holder::value)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }
}