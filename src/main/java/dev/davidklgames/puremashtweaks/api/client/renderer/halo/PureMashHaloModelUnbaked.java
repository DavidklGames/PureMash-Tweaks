package dev.davidklgames.puremashtweaks.api.client.renderer.halo;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record PureMashHaloModelUnbaked(
        Identifier baseModelId,
        Identifier haloModelId,
        HaloSetting haloSetting,
        Optional<Transformation> transformation
) implements ItemModel.Unbaked {

    public static final MapCodec<PureMashHaloModelUnbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("base").forGetter(PureMashHaloModelUnbaked::baseModelId),
                    Identifier.CODEC.fieldOf("halo_model").forGetter(PureMashHaloModelUnbaked::haloModelId),
                    HaloSetting.CODEC.fieldOf("halo").forGetter(PureMashHaloModelUnbaked::haloSetting),
                    Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(PureMashHaloModelUnbaked::transformation)
            ).apply(instance, PureMashHaloModelUnbaked::new)
    );

    @Override
    public @NonNull MapCodec<? extends ItemModel.Unbaked> type() {
        return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.baseModelId);
        resolver.markDependency(this.haloModelId);
    }

    @Override
    public @NonNull ItemModel bake(ItemModel.BakingContext context, @NonNull Matrix4fc transformation) {
        ModelBaker baker = context.blockModelBaker();

        // 1. Bakes the 2-layer singularity base model
        ResolvedModel baseResolved = baker.getModel(this.baseModelId);
        TextureSlots baseSlots = baseResolved.getTopTextureSlots();
        QuadCollection baseQuads = baseResolved.bakeTopGeometry(baseSlots, baker, BlockModelRotation.IDENTITY);

        // 2. Bakes the Halo model (a clean 2D plane)
        ResolvedModel haloResolved = baker.getModel(this.haloModelId);
        TextureSlots haloSlots = haloResolved.getTopTextureSlots();
        QuadCollection haloQuads = haloResolved.bakeTopGeometry(haloSlots, baker, BlockModelRotation.IDENTITY);

        // Combines the transformation matrices from the JSON
        Matrix4fc composedTransform = com.mojang.math.Transformation.compose(transformation, this.transformation);

        // Creates the mod's registered tint sources to color layer0 and layer1
        net.minecraft.client.color.item.ItemTintSource layer0Tint = new dev.davidklgames.puremashtweaks.api.client.renderer.halo.SingularityTintSource(0);
        net.minecraft.client.color.item.ItemTintSource layer1Tint = new dev.davidklgames.puremashtweaks.api.client.renderer.halo.SingularityTintSource(1);

        ModelRenderProperties baseProperties = ModelRenderProperties.fromResolvedModel(baker, baseResolved, baseSlots);
        ModelRenderProperties haloProperties = ModelRenderProperties.fromResolvedModel(baker, haloResolved, haloSlots);

        // Creates the traditional base model instance with the configured tint sources
        ItemModel bakedBase = new CuboidItemModelWrapper(
                java.util.List.of(layer0Tint, layer1Tint),
                baseQuads,
                baseProperties,
                composedTransform
        );

        // Returns our dynamic compound Halo model
        return new PureMashHaloModel(bakedBase, haloQuads, this.haloSetting, composedTransform, haloProperties);
    }
}