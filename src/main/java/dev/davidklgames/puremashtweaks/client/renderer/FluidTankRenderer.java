package dev.davidklgames.puremashtweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.davidklgames.puremashtweaks.block.entity.FluidTankBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.CreativeFluidTankBlockEntity;
import dev.davidklgames.puremashtweaks.client.renderer.box.FluidBox;
import dev.davidklgames.puremashtweaks.client.renderer.state.FluidTankRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FluidTankRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, FluidTankRenderState> {

    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public @NonNull FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void extractRenderState(@NonNull T blockEntity, @NonNull FluidTankRenderState state, float partialTicks, net.minecraft.world.phys.@NonNull Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.isCreative = blockEntity instanceof CreativeFluidTankBlockEntity;

        if (blockEntity instanceof FluidTankBlockEntity normalBe) {
            state.amount = normalBe.fluidTank.getAmountAsLong(0);
            state.capacity = 32000L;
            state.fluid = normalBe.fluidTank.getResource(0).getFluid();
            state.hasFluid = state.amount > 0 && state.fluid != Fluids.EMPTY;
        } else if (blockEntity instanceof CreativeFluidTankBlockEntity creativeBe) {
            state.amount = creativeBe.fluidTank.getAmountAsLong(0);
            state.capacity = 1000000L;
            state.fluid = creativeBe.fluidTank.getResource(0).getFluid();
            state.hasFluid = state.amount > 0 && state.fluid != Fluids.EMPTY;
        } else {
            state.hasFluid = false;
        }
    }

    @Override
    public void submit(@NonNull FluidTankRenderState state, @NonNull PoseStack poseStack, net.minecraft.client.renderer.@NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        if (!state.hasFluid || state.fluid == null || state.fluid == Fluids.EMPTY || state.amount <= 0) return;

        TextureAtlasSprite texture = FluidRenderHelper.getFluidTexture(state.fluid);
        if (texture == null) return;

        int finalColor = FluidRenderHelper.getFluidColor(state.fluid, state.amount);

        // Calculate height ratio dynamically based on current amount (restores smooth 1-second filling animation for creative tanks)
        double ratio = Mth.clamp((double) state.amount / (double) state.capacity, 0.05, 1.0);

        // Coordinates matched precisely inside the new glass container (from X/Z: 3 to 13, Y: 2 to 16)
        double minY = 0.126; // Y = 2.01 block units
        double maxY = 0.126 + (0.999 - 0.126) * ratio; // Y up to 15.98 block units

        double minX = 0.1885; // X = 3.01 block units
        double maxX = 0.8115; // X = 12.99 block units
        double minZ = 0.1885; // Z = 3.01 block units
        double maxZ = 0.8115; // Z = 12.99 block units

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, buffer) -> FluidBox.render(buffer, pose, texture, finalColor, state.lightCoords, minX, minY, minZ, maxX, maxY, maxZ));
    }
}