package dev.davidklgames.puremashtweaks.block.entity.cable;

import net.minecraft.network.chat.Component;

/**
 * Distribution modes for the PureMash Universal Cables.
 * Features Round-Robin, Random, and the signature 'Dynamically' distribution mode.
 */
public enum DistributionMode {
    ROUND_ROBIN("round_robin", 208, 16),
    RANDOM("random", 224, 16),
    DYNAMICALLY("dynamically", 240, 16);

    private final String name;
    private final int u;
    private final int v;

    DistributionMode(String name, int u, int v) {
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
        return Component.translatable("tooltip.puremashtweaks.distribution." + this.name);
    }

    public DistributionMode cycle() {
        DistributionMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}