package dev.davidklgames.puremashtweaks.worldgen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import java.util.List;

public class ModConfiguredFeatures {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        TagMatchTest deepslateReplace = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        List<OreConfiguration.TargetBlockState> target = List.of(
                OreConfiguration.target(deepslateReplace, ModBlocks.SYNTHORIUM_DEBRIS.get().defaultBlockState())
        );

        context.register(ModWorldGenKeys.SYNTHORIUM_STANDARD, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(target, 4)));
        context.register(ModWorldGenKeys.SYNTHORIUM_RARE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(target, 8)));
        context.register(ModWorldGenKeys.SYNTHORIUM_EPIC, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(target, 16)));
    }
}