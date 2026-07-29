package dev.davidklgames.puremashtweaks.api.client.renderer.tank;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public record FluidTankItemModelUnbaked(
        Identifier baseModelId,
        long capacity,
        boolean isCreative,
        Optional<Transformation> transformation
) implements ItemModel.Unbaked {

    public static final MapCodec<FluidTankItemModelUnbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("base").forGetter(FluidTankItemModelUnbaked::baseModelId),
                    Codec.LONG.fieldOf("capacity").forGetter(FluidTankItemModelUnbaked::capacity),
                    Codec.BOOL.fieldOf("is_creative").forGetter(FluidTankItemModelUnbaked::isCreative),
                    Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(FluidTankItemModelUnbaked::transformation)
            ).apply(instance, FluidTankItemModelUnbaked::new)
    );

    @Override
    public @NonNull MapCodec<? extends ItemModel.Unbaked> type() {
        return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.baseModelId);
    }

    @Override
    public @NonNull ItemModel bake(ItemModel.BakingContext context, @NonNull Matrix4fc transformation) {
        ModelBaker baker = context.blockModelBaker();

        ResolvedModel baseResolved = baker.getModel(this.baseModelId);
        TextureSlots baseSlots = baseResolved.getTopTextureSlots();
        QuadCollection baseQuads = baseResolved.bakeTopGeometry(baseSlots, baker, BlockModelRotation.IDENTITY);
        ModelRenderProperties baseProperties = ModelRenderProperties.fromResolvedModel(baker, baseResolved, baseSlots);

        Matrix4fc composedTransform = Transformation.compose(transformation, this.transformation);

        ItemModel bakedBase = new CuboidItemModelWrapper(
                List.of(),
                baseQuads,
                baseProperties,
                composedTransform
        );

        return new FluidTankItemModel(bakedBase, baseProperties, this.capacity, this.isCreative);
    }
}