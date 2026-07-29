package dev.davidklgames.puremashtweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;
import dev.davidklgames.puremashtweaks.client.renderer.state.SynthesisTableRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@SuppressWarnings({"DataFlowIssue", "removal"})
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
    public void extractRenderState(dev.davidklgames.puremashtweaks.block.entity.@NonNull SynthesisTableBlockEntity blockEntity, @NonNull SynthesisTableRenderState state, float partialTicks, net.minecraft.world.phys.@NonNull Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.isAutomationActive = blockEntity.isAutomationActive();
        state.itemState.clear();
        state.hasHologram = false;

        net.minecraft.world.level.Level level = blockEntity.getLevel();
        if (level == null || !state.isAutomationActive) return;

        state.gameTime = (float) level.getGameTime() + partialTicks;

        net.minecraft.world.item.ItemStack card = blockEntity.inventory.getStackInSlot(82);
        if (!card.isEmpty() && card.has(dev.davidklgames.puremashtweaks.component.ModDataComponents.RECIPE_CARD_DATA.get())) {
            CompoundTag data = card.get(dev.davidklgames.puremashtweaks.component.ModDataComponents.RECIPE_CARD_DATA.get());
            if (data != null && data.contains("OutputItem")) {
                net.minecraft.world.item.ItemStack result = net.minecraft.world.item.ItemStack.CODEC.parse(
                        level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                        data.getCompoundOrEmpty("OutputItem")
                ).result().orElse(net.minecraft.world.item.ItemStack.EMPTY);

                if (!result.isEmpty()) {
                    this.itemModelResolver.updateForTopItem(state.itemState, result, net.minecraft.world.item.ItemDisplayContext.GROUND, level, null, 0);
                    state.hasHologram = true;
                }
            }
        }
    }

    @Override
    public void submit(SynthesisTableRenderState state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        if (!state.isAutomationActive) return;

        if (state.hasHologram) {
            poseStack.pushPose();

            double bob = Math.sin(state.gameTime / 15.0F) * 0.05;
            float rotation = (state.gameTime * 2.5F) % 360.0F;

            poseStack.translate(0.5, 1.30 + bob, 0.5);

            poseStack.scale(0.82F, 0.82F, 0.82F);

            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            state.itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}