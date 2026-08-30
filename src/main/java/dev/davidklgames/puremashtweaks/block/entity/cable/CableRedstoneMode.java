package dev.davidklgames.puremashtweaks.block.entity.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Redstone operating modes for PureMash Universal Cables.
 * Active (High signal): U 208, V 0
 * Inactive (Low signal): U 224, V 0
 * Ignored: U 240, V 0
 */
public enum CableRedstoneMode {
    IGNORED("ignored", 240, 0),
    HIGH("high", 208, 0),
    LOW("low", 224, 0);

    private final String name;
    private final int u;
    private final int v;

    CableRedstoneMode(String name, int u, int v) {
        this.name = name;
        this.u = u;
        this.v = v;
    }

    public String getName() {
        return this.name;
    }

    public int getU() {
        return this.u;
    }

    public int getV() {
        return this.v;
    }

    public Component getDisplayName() {
        return Component.translatable("tooltip.puremashtweaks.redstone_mode." + this.name);
    }

    public CableRedstoneMode cycle() {
        CableRedstoneMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public boolean isRedstoneRequirementMet(Level level, BlockPos pos) {
        if (this == IGNORED) {
            return true;
        }
        boolean hasSignal = level.hasNeighborSignal(pos);
        if (this == HIGH) {
            return hasSignal;
        } else {
            return !hasSignal;
        }
    }
}