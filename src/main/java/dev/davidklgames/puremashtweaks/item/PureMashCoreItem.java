package dev.davidklgames.puremashtweaks.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PureMashCoreItem extends Item {
    public PureMashCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }


    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        long time = System.currentTimeMillis();

        // 1. We slowed down the total cycle using the divisor 1200.0 (a complete cycle takes ~7.5 seconds).
        double raw = Math.sin(time / 1000.0);

        // 2. We created the plateau: any value above 0.4 is capped at 0.4.
        // This causes the brilliant cyan to be retained for approximately 37% of the total cycle duration.
        double wave = Math.min(0.4, raw);

        // 3. We normalized the wave scale from [-1.0, 0.4] to [0.0f, 1.0f].
        float sine = (float) ((wave + 1.0) / 1.4);

        // Bright Cyan (Cyan): RGB (0, 255, 255).
        // Dark Cyan: RGB (0, 75, 75).
        int r = 0;
        int g = (int) (75 + (255 - 75) * sine);
        int b = (int) (75 + (255 - 75) * sine);
        int color = (0) | (g << 8) | b;

        return Component.translatable(this.getDescriptionId())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
    }
}