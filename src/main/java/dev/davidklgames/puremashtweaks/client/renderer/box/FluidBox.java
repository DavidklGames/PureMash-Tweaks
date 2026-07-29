package dev.davidklgames.puremashtweaks.client.renderer.box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class FluidBox {
    private static final Vector4f vector4f = new Vector4f();

    public static void render(VertexConsumer buffer, PoseStack.Pose matrix, TextureAtlasSprite sprite, int color, int light, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
        if (sprite == null) return;

        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 255;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face
        pos(buffer, matrix, xMin, yMax, zMin, red, green, blue, alpha, u0, v0, light, 0, 1, 0);
        pos(buffer, matrix, xMin, yMax, zMax, red, green, blue, alpha, u0, v1, light, 0, 1, 0);
        pos(buffer, matrix, xMax, yMax, zMax, red, green, blue, alpha, u1, v1, light, 0, 1, 0);
        pos(buffer, matrix, xMax, yMax, zMin, red, green, blue, alpha, u1, v0, light, 0, 1, 0);

        // Bottom face
        pos(buffer, matrix, xMin, yMin, zMax, red, green, blue, alpha, u0, v0, light, 0, -1, 0);
        pos(buffer, matrix, xMin, yMin, zMin, red, green, blue, alpha, u0, v1, light, 0, -1, 0);
        pos(buffer, matrix, xMax, yMin, zMin, red, green, blue, alpha, u1, v1, light, 0, -1, 0);
        pos(buffer, matrix, xMax, yMin, zMax, red, green, blue, alpha, u1, v0, light, 0, -1, 0);

        // North face
        pos(buffer, matrix, xMax, yMax, zMin, red, green, blue, alpha, u1, v0, light, 0, 0, -1);
        pos(buffer, matrix, xMax, yMin, zMin, red, green, blue, alpha, u1, v1, light, 0, 0, -1);
        pos(buffer, matrix, xMin, yMin, zMin, red, green, blue, alpha, u0, v1, light, 0, 0, -1);
        pos(buffer, matrix, xMin, yMax, zMin, red, green, blue, alpha, u0, v0, light, 0, 0, -1);

        // South face
        pos(buffer, matrix, xMin, yMax, zMax, red, green, blue, alpha, u0, v0, light, 0, 0, 1);
        pos(buffer, matrix, xMin, yMin, zMax, red, green, blue, alpha, u0, v1, light, 0, 0, 1);
        pos(buffer, matrix, xMax, yMin, zMax, red, green, blue, alpha, u1, v1, light, 0, 0, 1);
        pos(buffer, matrix, xMax, yMax, zMax, red, green, blue, alpha, u1, v0, light, 0, 0, 1);

        // West face
        pos(buffer, matrix, xMin, yMax, zMin, red, green, blue, alpha, u0, v0, light, -1, 0, 0);
        pos(buffer, matrix, xMin, yMin, zMin, red, green, blue, alpha, u0, v1, light, -1, 0, 0);
        pos(buffer, matrix, xMin, yMin, zMax, red, green, blue, alpha, u1, v1, light, -1, 0, 0);
        pos(buffer, matrix, xMin, yMax, zMax, red, green, blue, alpha, u1, v0, light, -1, 0, 0);

        // East face
        pos(buffer, matrix, xMax, yMax, zMax, red, green, blue, alpha, u0, v0, light, 1, 0, 0);
        pos(buffer, matrix, xMax, yMin, zMax, red, green, blue, alpha, u0, v1, light, 1, 0, 0);
        pos(buffer, matrix, xMax, yMin, zMin, red, green, blue, alpha, u1, v1, light, 1, 0, 0);
        pos(buffer, matrix, xMax, yMax, zMin, red, green, blue, alpha, u1, v0, light, 1, 0, 0);
    }

    private static void pos(VertexConsumer buffer, PoseStack.Pose matrix, double x, double y, double z, int r, int g, int b, int a, float u, float v, int light, float nx, float ny, float nz) {
        Matrix4f matrix4f = matrix.pose();
        vector4f.set((float) x, (float) y, (float) z, 1.0F);
        vector4f.mul(matrix4f);
        buffer.addVertex(vector4f.x(), vector4f.y(), vector4f.z())
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }
}