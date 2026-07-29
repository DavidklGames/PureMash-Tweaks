package dev.davidklgames.puremashtweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.entity.ChunkLoaderBlockEntity;
import dev.davidklgames.puremashtweaks.client.renderer.state.ChunkLoaderRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"all", "unused"})
public class ChunkLoaderRenderer implements BlockEntityRenderer<ChunkLoaderBlockEntity, ChunkLoaderRenderState> {

    private static final Identifier BORDER_TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/border/chunk_loader_border.png");

    public static final RenderType BORDER_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(BORDER_TEXTURE);

    public ChunkLoaderRenderer(BlockEntityRendererProvider.@NonNull Context context) {}

    @Override
    public @NonNull AABB getRenderBoundingBox(ChunkLoaderBlockEntity blockEntity) {
        if (!blockEntity.isShowingBoundary()) {
            return new AABB(blockEntity.getBlockPos());
        }
        return AABB.INFINITE;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public @NonNull ChunkLoaderRenderState createRenderState() {
        return new ChunkLoaderRenderState();
    }

    @Override
    public void extractRenderState(@NonNull ChunkLoaderBlockEntity blockEntity, @NonNull ChunkLoaderRenderState state, float partialTicks, net.minecraft.world.phys.@NonNull Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.isShowingBoundary = blockEntity.isShowingBoundary();
        state.activeLevel = blockEntity.getActiveLevel();
        state.blockPos = blockEntity.getBlockPos();

        net.minecraft.world.level.Level level = blockEntity.getLevel();
        if (level == null) return;

        state.gameTime = (float) level.getGameTime() + partialTicks;
    }

    @Override
    public void submit(@NonNull ChunkLoaderRenderState state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        if (!state.isShowingBoundary) return;

        int radiusIndex = ChunkLoaderBlockEntity.getRadiusByLevel(state.activeLevel);
        BlockPos pos = state.blockPos;

        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;

        int minX = (centerChunkX - radiusIndex) << 4;
        int maxX = ((centerChunkX + radiusIndex) << 4) + 16;
        int minZ = (centerChunkZ - radiusIndex) << 4;
        int maxZ = ((centerChunkZ + radiusIndex) << 4) + 16;

        float relMinX = minX - pos.getX();
        float relMaxX = maxX - pos.getX();
        float relMinZ = minZ - pos.getZ();
        float relMaxZ = maxZ - pos.getZ();

        float relMinY = -32.0F;
        float relMaxY = 32.0F;

        int fullBrightLight = 15728880;

        submitNodeCollector.submitCustomGeometry(poseStack, BORDER_RENDER_TYPE, (pose, buffer) -> {

            float pulse = 0.35F + 0.1F * Mth.sin(state.gameTime * 0.08F);
            float uScroll = state.gameTime * 0.006F;
            float vScroll = state.gameTime * 0.006F;

            renderWalls(pose, buffer, relMinX, relMaxX, relMinZ, relMaxZ, relMinY, relMaxY, 1.0F, 1.0F, 1.0F, pulse, uScroll, vScroll, fullBrightLight);
        });
    }

    private void renderWalls(PoseStack.Pose pose, VertexConsumer buffer, float minX, float maxX, float minZ, float maxZ, float minY, float maxY, float r, float g, float b, float a, float uScroll, float vScroll, int light) {
        drawSingleWall(pose, buffer, minX, minZ, maxX, minZ, minY, maxY, r, g, b, a, uScroll, vScroll, light);
        drawSingleWall(pose, buffer, maxX, maxZ, minX, maxZ, minY, maxY, r, g, b, a, uScroll, vScroll, light);
        drawSingleWall(pose, buffer, maxX, minZ, maxX, maxZ, minY, maxY, r, g, b, a, uScroll, vScroll, light);
        drawSingleWall(pose, buffer, minX, maxZ, minX, minZ, minY, maxY, r, g, b, a, uScroll, vScroll, light);
    }

    private void drawSingleWall(PoseStack.Pose pose, VertexConsumer buffer, float x1, float z1, float x2, float z2, float yMin, float yMax, float r, float g, float b, float a, float uScroll, float vScroll, int light) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dz * dz);
        float height = yMax - yMin;

        float uEnd = uScroll + (length / 2.0F);
        float vEnd = vScroll + (height / 2.0F);

        float nx = -dz / length;
        float nz = dx / length;

        buffer.addVertex(pose, x1, yMin, z1).setColor(r, g, b, a).setUv(uScroll, vScroll).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, 0.0F, nz);
        buffer.addVertex(pose, x2, yMin, z2).setColor(r, g, b, a).setUv(uEnd, vScroll).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, 0.0F, nz);
        buffer.addVertex(pose, x2, yMax, z2).setColor(r, g, b, a).setUv(uEnd, vEnd).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, 0.0F, nz);
        buffer.addVertex(pose, x1, yMax, z1).setColor(r, g, b, a).setUv(uScroll, vEnd).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, 0.0F, nz);

        buffer.addVertex(pose, x2, yMin, z2).setColor(r, g, b, a).setUv(uEnd, vScroll).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(-nx, 0.0F, -nz);
        buffer.addVertex(pose, x1, yMin, z1).setColor(r, g, b, a).setUv(uScroll, vScroll).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(-nx, 0.0F, -nz);
        buffer.addVertex(pose, x1, yMax, z1).setColor(r, g, b, a).setUv(uScroll, vEnd).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(-nx, 0.0F, -nz);
        buffer.addVertex(pose, x2, yMax, z2).setColor(r, g, b, a).setUv(uEnd, vEnd).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(-nx, 0.0F, -nz);
    }
}