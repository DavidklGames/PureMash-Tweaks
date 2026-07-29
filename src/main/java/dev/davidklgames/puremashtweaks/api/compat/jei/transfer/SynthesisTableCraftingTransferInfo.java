package dev.davidklgames.puremashtweaks.api.compat.jei.transfer;

import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SynthesisTableCraftingTransferInfo implements IRecipeTransferInfo<SynthesisTableMenu, RecipeHolder<CraftingRecipe>> {

    @Override
    public @NonNull Class<? extends SynthesisTableMenu> getContainerClass() {
        return SynthesisTableMenu.class;
    }

    @Override
    public @NonNull Optional<MenuType<SynthesisTableMenu>> getMenuType() {
        return Optional.of(dev.davidklgames.puremashtweaks.registry.ModMenus.SYNTHESIS_TABLE_MENU.get());
    }

    @Override
    public @NonNull IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public boolean canHandle(@NonNull SynthesisTableMenu container, @NonNull RecipeHolder<CraftingRecipe> recipe) {
        return true;
    }

    @Override
    public @NotNull List<Slot> getRecipeSlots(SynthesisTableMenu container, @NonNull RecipeHolder<CraftingRecipe> recipe) {
        List<Slot> slots = new ArrayList<>();
        // Maps the 9 slots of the 3x3 JEI recipe directly to the CENTRAL 3x3 of the 9x9 crafting grid
        slots.add(container.getSlot(30)); // Row 3, Column 3
        slots.add(container.getSlot(31)); // Row 3, Column 4
        slots.add(container.getSlot(32)); // Row 3, Column 5

        slots.add(container.getSlot(39)); // Row 4, Column 3
        slots.add(container.getSlot(40)); // Row 4, Column 4 (Absolute center)
        slots.add(container.getSlot(41)); // Row 4, Column 5

        slots.add(container.getSlot(48)); // Row 5, Column 3
        slots.add(container.getSlot(49)); // Row 5, Column 4
        slots.add(container.getSlot(50)); // Row 5, Column 5

        return slots;
    }

    @Override
    public @NotNull List<Slot> getInventorySlots(@NonNull SynthesisTableMenu container, @NonNull RecipeHolder<CraftingRecipe> recipe) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 83; i < 83 + 36; i++) {
            slots.add(container.getSlot(i));
        }
        return slots;
    }
}