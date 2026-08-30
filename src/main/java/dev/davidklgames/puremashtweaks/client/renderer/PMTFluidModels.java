package dev.davidklgames.puremashtweaks.client.renderer;

import dev.davidklgames.puremashtweaks.api.PMT;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;

public class PMTFluidModels {
    public static final FluidModel.Unbaked MOLTEN_SYNTHORIUM_MODEL = new FluidModel.Unbaked(
            new Material(PMT.id("block/fluid/molten_synthorium_still")),
            new Material(PMT.id("block/fluid/molten_synthorium_flow")),
            null,
            null
    );

    public static final FluidModel.Unbaked MOLTEN_MOLDELONIAN_MODEL = new FluidModel.Unbaked(
            new Material(PMT.id("block/fluid/molten_moldelonian_still")),
            new Material(PMT.id("block/fluid/molten_moldelonian_flow")),
            null,
            null
    );

    public static final FluidModel.Unbaked STEAM_MODEL = new FluidModel.Unbaked(
            new Material(PMT.id("block/fluid/steam/steam_still")),
            new Material(PMT.id("block/fluid/steam/steam_flow")),
            null,
            null
    );
}