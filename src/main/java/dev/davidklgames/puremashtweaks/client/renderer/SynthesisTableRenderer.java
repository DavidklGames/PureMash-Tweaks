package dev.davidklgames.puremashtweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;
import dev.davidklgames.puremashtweaks.client.renderer.state.SynthesisTableRenderState;
import dev.davidklgames.puremashtweaks.util.SynthesisTableHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"removal", "rawtypes"})
public class SynthesisTableRenderer implements BlockEntityRenderer<SynthesisTableBlockEntity, SynthesisTableRenderState> {
    private final ItemModelResolver itemModelResolver;

    public SynthesisTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NonNull SynthesisTableRenderState createRenderState() {
        return new SynthesisTableRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull SynthesisTableBlockEntity blockEntity,
            @NonNull SynthesisTableRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.isAutomationActive = blockEntity.isAutomationActive();
        state.itemState.clear();
        state.hasHologram = false;

        Level level = blockEntity.getLevel();
        if (level != null && state.isAutomationActive) {
            state.gameTime = (float) level.getGameTime() + partialTicks;

            ItemStack card = blockEntity.inventory.getStackInSlot(82);
            if (SynthesisTableHelper.hasEncodedRecipe(card)) {
                ItemStack result = SynthesisTableHelper.readOutputFromCard(card, level.registryAccess());
                if (!result.isEmpty()) {
                    this.itemModelResolver.updateForTopItem(state.itemState, result, ItemDisplayContext.GROUND, level, null, 0);
                    state.hasHologram = true;
                }
            }
        }
    }

    @Override
    public void submit(
            @NonNull SynthesisTableRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            @NonNull CameraRenderState camera
    ) {
        if (state.isAutomationActive && state.hasHologram) {
            poseStack.pushPose();
            double bob = Math.sin(state.gameTime / 15.0F) * 0.05;
            float rotation = (state.gameTime * 2.5F) % 360.0F;

            poseStack.translate(0.5F, 1.3 + bob, 0.5F);
            poseStack.scale(0.82F, 0.82F, 0.82F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            state.itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}