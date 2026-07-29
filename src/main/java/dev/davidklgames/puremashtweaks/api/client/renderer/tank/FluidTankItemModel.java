package dev.davidklgames.puremashtweaks.api.client.renderer.tank;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FluidTankItemModel implements ItemModel {
    private final ItemModel baseModel;
    private final ModelRenderProperties baseProperties;
    private final FluidTankSpecialRenderer specialRenderer;

    public FluidTankItemModel(ItemModel baseModel, ModelRenderProperties baseProperties, long capacity, boolean isCreative) {
        this.baseModel = baseModel;
        this.baseProperties = baseProperties;
        this.specialRenderer = new FluidTankSpecialRenderer(capacity, isCreative);
    }

    @Override
    public void update(
            @NonNull ItemStackRenderState output,
            @NonNull ItemStack item,
            @NonNull ItemModelResolver resolver,
            @NonNull ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        // 1. Registers model instance identity in 26.1.2 render state pipeline
        output.appendModelIdentityElement(this);

        // 2. Renders the base block model (transparent glass tank frame)
        this.baseModel.update(output, item, resolver, displayContext, level, owner, seed);

        // 3. Extracts fluid arguments
        var arg = this.specialRenderer.extractArgument(item);
        if (arg != null) {

            // Prevents Minecraft from caching and reusing Lava render state on Water/Modded tanks!
            output.appendModelIdentityElement(arg.fluid());
            output.appendModelIdentityElement(arg.amount());

            ItemStackRenderState.LayerRenderState fluidLayer = output.newLayer();

            // Applies 3D display transformations (GUI, Hand, Ground)
            this.baseProperties.applyToLayer(fluidLayer, displayContext);

            // Configures the special model renderer with extracted fluid arguments
            fluidLayer.setupSpecialModel(this.specialRenderer, arg);
        }
    }
}