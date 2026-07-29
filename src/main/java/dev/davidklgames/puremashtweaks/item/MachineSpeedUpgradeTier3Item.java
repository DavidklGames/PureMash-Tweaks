package dev.davidklgames.puremashtweaks.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MachineSpeedUpgradeTier3Item extends Item {
    public MachineSpeedUpgradeTier3Item(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable(this.getDescriptionId())
                .withStyle(style -> style.withColor(TextColor.fromRgb(0x527A80)));
    }
}