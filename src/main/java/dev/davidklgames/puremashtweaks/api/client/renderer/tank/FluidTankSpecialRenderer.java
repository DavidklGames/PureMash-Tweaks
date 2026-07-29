package dev.davidklgames.puremashtweaks.api.client.renderer.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.davidklgames.puremashtweaks.client.renderer.FluidRenderHelper;
import dev.davidklgames.puremashtweaks.client.renderer.box.FluidBox;
import dev.davidklgames.puremashtweaks.util.TankNbtHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class FluidTankSpecialRenderer implements SpecialModelRenderer<FluidTankSpecialRenderer.TankRenderContext> {

    private final long capacity;
    private final boolean isCreative;

    public FluidTankSpecialRenderer(long capacity, boolean isCreative) {
        this.capacity = capacity;
        this.isCreative = isCreative;
    }

    /**
     * Context record holding extracted fluid properties matching ItemDecorator logic.
     */
    public record TankRenderContext(Fluid fluid, long amount) {}

    @Override
    public @Nullable TankRenderContext extractArgument(@NonNull ItemStack stack) {
        if (stack.isEmpty()) return null;

        CompoundTag tag = TankNbtHelper.getTagFromStack(stack);
        if (tag == null || tag.isEmpty()) return null;

        net.minecraft.core.HolderLookup.Provider provider = Minecraft.getInstance().level != null ?
                Minecraft.getInstance().level.registryAccess() : null;

        // Uses the exact direct extraction logic that proved 100% successful in ItemDecorator
        FluidStack fluidStack = TankNbtHelper.readFluidFromTag(tag, provider);
        if (fluidStack == null || fluidStack.isEmpty() || fluidStack.getAmount() <= 0 || fluidStack.getFluid() == Fluids.EMPTY) {
            return null;
        }

        return new TankRenderContext(fluidStack.getFluid(), fluidStack.getAmount());
    }

    @Override
    public void submit(
            @Nullable TankRenderContext context,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
    ) {
        if (context == null || context.fluid() == null || context.fluid() == Fluids.EMPTY || context.amount() <= 0) {
            return;
        }

        // Retrieves exact texture and color proven working in ItemDecorator
        TextureAtlasSprite texture = FluidRenderHelper.getFluidTexture(context.fluid());
        if (texture == null) return;

        int finalColor = FluidRenderHelper.getFluidColor(context.fluid(), context.amount());
        double ratio = this.isCreative ? 1.0 : Mth.clamp((double) context.amount() / (double) this.capacity, 0.05, 1.0);

        // 3D coordinates fitting inside the glass container
        // Coordinates matched precisely inside the new glass container (from X/Z: 3 to 13, Y: 2 to 16)
        double minY = 0.126; // Y = 2.01 block units
        double maxY = 0.126 + (0.999 - 0.126) * ratio; // Y up to 15.98 block units

        double minX = 0.1885; // X = 3.01 block units
        double maxX = 0.8115; // X = 12.99 block units
        double minZ = 0.1885; // Z = 3.01 block units
        double maxZ = 0.8115; // Z = 12.99 block units

        // Renders in 3D inside the item model using the itemTranslucent pipeline
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.itemTranslucent(texture.atlasLocation()),
                (pose, buffer) -> FluidBox.render(buffer, pose, texture, finalColor, lightCoords, minX, minY, minZ, maxX, maxY, maxZ)
        );
    }

    @Override
    public void getExtents(@NonNull Consumer<Vector3fc> output) {
    }
}