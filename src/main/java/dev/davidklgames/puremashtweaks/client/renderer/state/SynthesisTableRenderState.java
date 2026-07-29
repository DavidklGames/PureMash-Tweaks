package dev.davidklgames.puremashtweaks.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class SynthesisTableRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState itemState = new ItemStackRenderState();

    public boolean isAutomationActive = false;
    public boolean hasHologram = false;
    public float gameTime = 0.0f;

    public SynthesisTableRenderState() {
    }
}