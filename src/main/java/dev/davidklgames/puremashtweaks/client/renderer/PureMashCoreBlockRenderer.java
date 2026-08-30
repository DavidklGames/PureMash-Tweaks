package dev.davidklgames.puremashtweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.davidklgames.puremashtweaks.block.entity.PureMashCoreBlockEntity;
import dev.davidklgames.puremashtweaks.client.renderer.state.PureMashCoreBlockRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.Color;

/**
 * Actuation area renderer for PureMash Core Block using JustDireThings-style AABB geometry in 26.1.2.
 */
public class PureMashCoreBlockRenderer implements BlockEntityRenderer<PureMashCoreBlockEntity, PureMashCoreBlockRenderState> {

    private static final Color BORDER_GREEN = new Color(85, 255, 85, 255);

    public PureMashCoreBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public @NonNull AABB getRenderBoundingBox(PureMashCoreBlockEntity blockEntity) {
        if (!blockEntity.isShowArea() || blockEntity.getOverloadLevel() <= 0) {
            return new AABB(blockEntity.getBlockPos());
        }
        int radius = switch (blockEntity.getOverloadLevel()) {
            case 3 -> 3;
            case 2 -> 2;
            default -> 1;
        };
        return new AABB(blockEntity.getBlockPos()).inflate(radius + 1.0);
    }

    @Override
    public void submit(
            @NonNull PureMashCoreBlockRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            @NonNull CameraRenderState camera
    ) {
        if (!state.showArea || state.overloadLevel <= 0) return;

        int radius = switch (state.overloadLevel) {
            case 3 -> 3;
            case 2 -> 2;
            default -> 1;
        };

        AABB area = new AABB(-radius, -radius, -radius, radius + 1.0, radius + 1.0, radius + 1.0);

        // 1. Bordas em Linhas Verdes (JustDireThings Style)
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.lines(),
                (pose, buffer) -> drawAABBLines(pose, buffer, area, BORDER_GREEN)
        );

        // 2. Paredes Avermelhadas Sólidas/Translúcidas (Tom de vermelho mais claro e suave)
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugQuads(),
                (pose, buffer) -> drawAABBSolid(pose, buffer, area, 1.0F, 0.32F, 0.32F, 0.25F)
        );
    }

    protected static void drawAABBLines(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Color color) {
        float x = (float) aabb.minX;
        float y = (float) aabb.minY;
        float z = (float) aabb.minZ;
        float dx = (float) aabb.maxX;
        float dy = (float) aabb.maxY;
        float dz = (float) aabb.maxZ;
        int c = color.getRGB();

        edge(pose, buffer, c, x, y, z, dx, y, z, 1.0F, 0.0F, 0.0F);
        edge(pose, buffer, c, x, y, z, x, dy, z, 0.0F, 1.0F, 0.0F);
        edge(pose, buffer, c, x, y, z, x, y, dz, 0.0F, 0.0F, 1.0F);
        edge(pose, buffer, c, dx, y, z, dx, dy, z, 0.0F, 1.0F, 0.0F);
        edge(pose, buffer, c, dx, dy, z, x, dy, z, -1.0F, 0.0F, 0.0F);
        edge(pose, buffer, c, x, dy, z, x, dy, dz, 0.0F, 0.0F, 1.0F);
        edge(pose, buffer, c, x, dy, dz, x, y, dz, 0.0F, -1.0F, 0.0F);
        edge(pose, buffer, c, x, y, dz, dx, y, dz, 1.0F, 0.0F, 0.0F);
        edge(pose, buffer, c, dx, y, dz, dx, y, z, 0.0F, 0.0F, -1.0F);
        edge(pose, buffer, c, x, dy, dz, dx, dy, dz, 1.0F, 0.0F, 0.0F);
        edge(pose, buffer, c, dx, y, dz, dx, dy, dz, 0.0F, 1.0F, 0.0F);
        edge(pose, buffer, c, dx, dy, z, dx, dy, dz, 0.0F, 0.0F, 1.0F);
    }

    private static void edge(PoseStack.Pose pose, VertexConsumer buffer, int color, float x0, float y0, float z0, float x1, float y1, float z1, float nx, float ny, float nz) {
        buffer.addVertex(pose.pose(), x0, y0, z0).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
        buffer.addVertex(pose.pose(), x1, y1, z1).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
    }

    public static void drawAABBSolid(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, float r, float g, float b, float alpha) {
        float sx = (float) aabb.minX;
        float sy = (float) aabb.minY;
        float sz = (float) aabb.minZ;
        float ex = (float) aabb.maxX;
        float ey = (float) aabb.maxY;
        float ez = (float) aabb.maxZ;

        // Bottom
        quad(pose, buffer, r, g, b, alpha, sx, sy, sz, ex, sy, sz, ex, sy, ez, sx, sy, ez);
        // Top
        quad(pose, buffer, r, g, b, alpha, sx, ey, sz, sx, ey, ez, ex, ey, ez, ex, ey, sz);
        // North
        quad(pose, buffer, r, g, b, alpha, sx, sy, sz, sx, ey, sz, ex, ey, sz, ex, sy, sz);
        // South
        quad(pose, buffer, r, g, b, alpha, sx, sy, ez, ex, sy, ez, ex, ey, ez, sx, ey, ez);
        // East
        quad(pose, buffer, r, g, b, alpha, ex, sy, sz, ex, ey, sz, ex, ey, ez, ex, sy, ez);
        // West
        quad(pose, buffer, r, g, b, alpha, sx, sy, sz, sx, sy, ez, sx, ey, ez, sx, ey, sz);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float r, float g, float b, float alpha, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        // Face frontal
        buffer.addVertex(pose.pose(), x0, y0, z0).setColor(r, g, b, alpha);
        buffer.addVertex(pose.pose(), x1, y1, z1).setColor(r, g, b, alpha);
        buffer.addVertex(pose.pose(), x2, y2, z2).setColor(r, g, b, alpha);
        buffer.addVertex(pose.pose(), x3, y3, z3).setColor(r, g, b, alpha);

        // Face traseira (visibilidade interior)
        buffer.addVertex(pose.pose(), x3, y3, z3).setColor(r, g, b, alpha);
        buffer.addVertex(pose.pose(), x2, y2, z2).setColor(r, g, b, alpha);
        buffer.addVertex(pose.pose(), x1, y1, z1).setColor(r, g, b, alpha);
        buffer.addVertex(pose.pose(), x0, y0, z0).setColor(r, g, b, alpha);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public @NonNull PureMashCoreBlockRenderState createRenderState() {
        return new PureMashCoreBlockRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull PureMashCoreBlockEntity blockEntity,
            @NonNull PureMashCoreBlockRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.showArea = blockEntity.isShowArea();
        state.active = blockEntity.isActive();
        state.overloadLevel = blockEntity.getOverloadLevel();
        state.blockPos = blockEntity.getBlockPos();

        if (blockEntity.getLevel() != null) {
            state.gameTime = (float) blockEntity.getLevel().getGameTime() + partialTicks;
        }
    }
}