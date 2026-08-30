package dev.davidklgames.puremashtweaks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SpeedUpgradeTier3Item extends Item {
    public SpeedUpgradeTier3Item(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

}