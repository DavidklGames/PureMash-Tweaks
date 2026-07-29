package dev.davidklgames.puremashtweaks.api.client.renderer.halo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PureMashHaloModel implements ItemModel {
    private final ItemModel baseModel;
    private final QuadCollection haloQuads;
    private final HaloSetting setting;
    private final Matrix4fc baseTransformation;
    private final ModelRenderProperties haloProperties;

    public PureMashHaloModel(ItemModel baseModel, QuadCollection haloQuads, HaloSetting setting, Matrix4fc baseTransformation, ModelRenderProperties haloProperties) {
        this.baseModel = baseModel;
        this.haloQuads = haloQuads;
        this.setting = setting;
        this.baseTransformation = baseTransformation;
        this.haloProperties = haloProperties;
    }

    @Override
    public void update(
            ItemStackRenderState output,
            @NonNull ItemStack item,
            @NonNull ItemModelResolver resolver,
            @NonNull ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        // 1. Registers the model identity in the game engine pipeline
        output.appendModelIdentityElement(this);

        // Executes the base model FIRST.
        // Since the base model reads the item properties (which contain "oversized_in_gui": true),
        // it notifies Minecraft to expand the scissor box before drawing the Halo.
        this.baseModel.update(output, item, resolver, displayContext, level, owner, seed);

        // Renders the Halo exclusively when displayed in the inventory/GUI (Creative Tab, JEI, Chest, etc.)
        if (displayContext == ItemDisplayContext.GUI) {

            // Ensures capacity for 3 layers.
            // Consisting of: 2 layers for the base singularity + 1 additional layer for the Halo.
            output.ensureCapacity(3);

            // =========================================================================
            // LAYER 3: REAR HALO (STATIC - WITHOUT CLIPPING)
            // =========================================================================
            ItemStackRenderState.LayerRenderState haloLayer = output.newLayer();
            this.haloProperties.applyToLayer(haloLayer, displayContext);

            long time = System.currentTimeMillis();

            // Spread logic and strict scale from 1.21.1
            double spread = (double) this.setting.size() / 16.0F;
            double scale = 1.0 + 2.0 * spread; // Size 2.0 = 1.25 scale (125% of slot size)

            if (this.setting.pulse()) {
                scale += 0.08 * Math.sin(time / 250.0); // Smooth dynamic pulsation if active
            }

            Matrix4f haloMatrix = new Matrix4f(this.baseTransformation);
            haloMatrix.translate(0.5F, 0.5F, 0.5F); // Centered pivot
            haloMatrix.scale((float) scale, (float) scale, 1.0F);
            haloMatrix.translate(-0.5F, -0.5F, -0.5F);

            // Smooth Z offset adjustment.
            // Adjusted from -0.01F to -0.002F to prevent the image from clipping behind the invisible background
            // wall of the inventory interface (container), which also caused a box-like clipping artifact.
            haloMatrix.translate(0.0F, 0.0F, -0.002F);

            int color = this.setting.color(); // Dynamic color from JSON
            haloLayer.tintLayers().add(color);
            output.appendModelIdentityElement(color);

            haloLayer.setLocalTransform(haloMatrix);
            haloLayer.prepareQuadList().addAll(this.haloQuads.getAll());
        }
    }
}