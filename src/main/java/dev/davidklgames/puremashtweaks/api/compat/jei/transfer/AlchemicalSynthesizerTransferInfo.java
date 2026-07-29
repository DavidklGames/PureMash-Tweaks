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

        slots.add(container.getSlot(1));

        if (recipe.fluid() != null) {
            slots.add(container.getSlot(0));
        }

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