package dev.davidklgames.puremashtweaks.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidRenderHelper {

    /**
     * Retrieves fluid texture sprite using FluidStateModelSet approach.
     */
    public static TextureAtlasSprite getFluidTexture(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) return null;

        try {
            var fluidState = fluid.defaultFluidState();
            var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
            return model.stillMaterial().sprite();
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Retrieves fluid tint color using FluidTintSource approach with ARGB fallbacks.
     */
    public static int getFluidColor(Fluid fluid, long amount) {
        if (fluid == null || fluid == Fluids.EMPTY) return -1;

        if (fluid.isSame(Fluids.WATER) || fluid.isSame(Fluids.FLOWING_WATER)) {
            return 0xFF3F76E4;
        }
        if (fluid.isSame(Fluids.LAVA) || fluid.isSame(Fluids.FLOWING_LAVA)) {
            return 0xFFFFFFFF;
        }

        try {
            var fluidState = fluid.defaultFluidState();
            var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
            var tintSource = model.fluidTintSource();
            if (tintSource != null) {
                int color = tintSource.colorAsStack(new FluidStack(fluid, (int) Math.min(amount, Integer.MAX_VALUE)));
                if (color != 0 && color != -1) {
                    if ((color & 0xFF000000) == 0) {
                        color |= 0xFF000000;
                    }
                    return color;
                }
            }
        } catch (Exception ignored) {}

        return 0xFFFFFFFF;
    }
}