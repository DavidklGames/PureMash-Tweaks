package dev.davidklgames.puremashtweaks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CapacityUpgradeTier2Item extends Item {
    public CapacityUpgradeTier2Item(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }
}
