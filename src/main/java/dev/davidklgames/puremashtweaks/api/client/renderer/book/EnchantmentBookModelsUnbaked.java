package dev.davidklgames.puremashtweaks.api.client.renderer.book;

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

public record EnchantmentBookModelsUnbaked(
        Identifier baseModelId,
        Identifier overloadModelId,
        Identifier overclockModelId,
        Identifier overdriveModelId
) implements ItemModel.Unbaked {

    public static final MapCodec<EnchantmentBookModelsUnbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("base").forGetter(EnchantmentBookModelsUnbaked::baseModelId),
                    Identifier.CODEC.fieldOf("overload").forGetter(EnchantmentBookModelsUnbaked::overloadModelId),
                    Identifier.CODEC.fieldOf("overclock").forGetter(EnchantmentBookModelsUnbaked::overclockModelId),
                    Identifier.CODEC.fieldOf("overdrive").forGetter(EnchantmentBookModelsUnbaked::overdriveModelId)
            ).apply(instance, EnchantmentBookModelsUnbaked::new)
    );

    @Override
    public @NonNull MapCodec<? extends ItemModel.Unbaked> type() {
        return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.baseModelId);
        resolver.markDependency(this.overloadModelId);
        resolver.markDependency(this.overclockModelId);
        resolver.markDependency(this.overdriveModelId);
    }

    @Override
    public @NonNull ItemModel bake(ItemModel.BakingContext context, @NonNull Matrix4fc transformation) {
        ModelBaker baker = context.blockModelBaker();

        ResolvedModel baseResolved = baker.getModel(this.baseModelId);
        TextureSlots baseSlots = baseResolved.getTopTextureSlots();
        QuadCollection baseQuads = baseResolved.bakeTopGeometry(baseSlots, baker, BlockModelRotation.IDENTITY);
        ModelRenderProperties baseProperties = ModelRenderProperties.fromResolvedModel(baker, baseResolved, baseSlots);
        ItemModel bakedBase = new CuboidItemModelWrapper(
                java.util.List.of(),
                baseQuads,
                baseProperties,
                transformation
        );

        ResolvedModel overloadResolved = baker.getModel(this.overloadModelId);
        TextureSlots overloadSlots = overloadResolved.getTopTextureSlots();
        QuadCollection overloadQuads = overloadResolved.bakeTopGeometry(overloadSlots, baker, BlockModelRotation.IDENTITY);
        ModelRenderProperties overloadProperties = ModelRenderProperties.fromResolvedModel(baker, overloadResolved, overloadSlots);
        ItemModel bakedOverload = new CuboidItemModelWrapper(
                java.util.List.of(),
                overloadQuads,
                overloadProperties,
                transformation
        );

        ResolvedModel overclockResolved = baker.getModel(this.overclockModelId);
        TextureSlots overclockSlots = overclockResolved.getTopTextureSlots();
        QuadCollection overclockQuads = overclockResolved.bakeTopGeometry(overclockSlots, baker, BlockModelRotation.IDENTITY);
        ModelRenderProperties overclockProperties = ModelRenderProperties.fromResolvedModel(baker, overclockResolved, overclockSlots);
        ItemModel bakedOverclock = new CuboidItemModelWrapper(
                java.util.List.of(),
                overclockQuads,
                overclockProperties,
                transformation
        );

        ResolvedModel overdriveResolved = baker.getModel(this.overdriveModelId);
        TextureSlots overdriveSlots = overdriveResolved.getTopTextureSlots();
        QuadCollection overdriveQuads = overdriveResolved.bakeTopGeometry(overdriveSlots, baker, BlockModelRotation.IDENTITY);
        ModelRenderProperties overdriveProperties = ModelRenderProperties.fromResolvedModel(baker, overdriveResolved, overdriveSlots);
        ItemModel bakedOverdrive = new CuboidItemModelWrapper(
                java.util.List.of(),
                overdriveQuads,
                overdriveProperties,
                transformation
        );

        return new EnchantmentBookModels(bakedBase, bakedOverload, bakedOverclock, bakedOverdrive);
    }
}