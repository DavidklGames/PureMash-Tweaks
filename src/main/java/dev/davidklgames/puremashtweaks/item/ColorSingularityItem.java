package dev.davidklgames.puremashtweaks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class ColorSingularityItem extends Item {
    private final int color0;
    private final int color1;
    private final @Nullable String customDisplayName;

    public ColorSingularityItem(Properties properties, int color0, int color1) {
        this(properties, color0, color1, null);
    }

    public ColorSingularityItem(Properties properties, int color0, int color1, @Nullable String customDisplayName) {
        super(properties);
        this.color0 = color0;
        this.color1 = color1;
        this.customDisplayName = customDisplayName;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (this.customDisplayName != null) {
            return Component.literal(this.customDisplayName);
        }
        return super.getName(stack);
    }

    public int getColor0() {
        return this.color0;
    }

    public int getColor1() {
        return this.color1;
    }
}