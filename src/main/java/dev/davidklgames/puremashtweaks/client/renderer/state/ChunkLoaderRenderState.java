package dev.davidklgames.puremashtweaks.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

public class ChunkLoaderRenderState extends BlockEntityRenderState {
    public boolean isShowingBoundary = false;
    public int activeLevel = 0;
    public BlockPos blockPos = BlockPos.ZERO;
    public float gameTime = 0.0f;
}