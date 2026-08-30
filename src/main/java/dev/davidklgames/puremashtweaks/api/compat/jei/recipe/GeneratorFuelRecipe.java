package dev.davidklgames.puremashtweaks.api.compat.jei.recipe;

import net.minecraft.world.item.ItemStack;

public record GeneratorFuelRecipe(
        ItemStack fuelStack,
        int burnTicks,
        int fePerTick,
        double maxTemp
) {
    public long getTotalEnergy() {
        return (long) burnTicks * fePerTick;
    }
}