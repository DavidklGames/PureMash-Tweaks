package dev.davidklgames.puremashtweaks.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTankRenderState extends BlockEntityRenderState {
    public boolean isCreative = false;
    public boolean hasFluid = false;
    public Fluid fluid = Fluids.EMPTY;
    public long amount = 0;
    public long capacity = 1;
}