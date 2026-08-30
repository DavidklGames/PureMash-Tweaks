package dev.davidklgames.puremashtweaks.api.compat.jei.transfer;

import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper.ParsedRecipe;
import dev.davidklgames.puremashtweaks.api.compat.jei.category.AlchemicalSynthesizerCategory;
import dev.davidklgames.puremashtweaks.menu.AlchemicalSynthesizerMenu;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlchemicalSynthesizerTransferInfo implements IRecipeTransferInfo<AlchemicalSynthesizerMenu, ParsedRecipe> {

    @Override
    public @NonNull Class<? extends AlchemicalSynthesizerMenu> getContainerClass() {
        return AlchemicalSynthesizerMenu.class;
    }

    @Override
    public @NonNull Optional<MenuType<AlchemicalSynthesizerMenu>> getMenuType() {
        return Optional.of(ModMenus.ALCHEMICAL_SYNTHESIZER_MENU.get());
    }

    @Override
    public @NonNull IRecipeType<ParsedRecipe> getRecipeType() {
        return AlchemicalSynthesizerCategory.RECIPE_TYPE;
    }

    @Override
    public boolean canHandle(@NonNull AlchemicalSynthesizerMenu container, @NonNull ParsedRecipe recipe) {
        return true;
    }

    @Override
    public @NotNull List<Slot> getRecipeSlots(AlchemicalSynthesizerMenu container, @NonNull ParsedRecipe recipe) {
        List<Slot> slots = new ArrayList<>();

        // Slot 1: Material Sample
        slots.add(container.getSlot(1));

        // Slot 0: Fluid Bucket Input (if recipe requires fluid)
        if (recipe.fluid() != null) {
            slots.add(container.getSlot(0));
        }

        // Slot 2: Tool Catalyst Input (if recipe requires tool)
        if (recipe.toolType() != null && !recipe.toolType().equals("none")) {
            slots.add(container.getSlot(2));
        }

        return slots;
    }

    @Override
    public @NotNull List<Slot> getInventorySlots(@NonNull AlchemicalSynthesizerMenu container, @NonNull ParsedRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 26; i < 62; i++) {
            slots.add(container.getSlot(i));
        }
        return slots;
    }
}