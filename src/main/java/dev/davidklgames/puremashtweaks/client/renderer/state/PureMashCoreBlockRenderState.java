package dev.davidklgames.puremashtweaks.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

public class PureMashCoreBlockRenderState extends BlockEntityRenderState {
    public boolean showArea = false;
    public boolean active = true;
    public int overloadLevel = 0;
    public BlockPos blockPos = BlockPos.ZERO;
    public float gameTime = 0.0F;
}