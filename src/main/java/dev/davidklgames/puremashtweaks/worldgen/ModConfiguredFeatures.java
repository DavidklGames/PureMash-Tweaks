package dev.davidklgames.puremashtweaks.worldgen;

import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        TagMatchTest deepslateReplace = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        List<OreConfiguration.TargetBlockState> target = List.of(
                OreConfiguration.target(deepslateReplace, ModBlocks.SYNTHORIUM_DEBRIS.get().defaultBlockState())
        );

        // 1. Veio Padrão (Tamanho 3)
        context.register(ModWorldGenKeys.SYNTHORIUM_STANDARD, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(target, 3)));

        // 2. Veio Raro (Tamanho 6)
        context.register(ModWorldGenKeys.SYNTHORIUM_RARE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(target, 6)));

        // 3. O "Chunk Bom" / Veio Épico (Tamanho 10)
        context.register(ModWorldGenKeys.SYNTHORIUM_EPIC, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(target, 10)));

        // --- SUSPICIOUS END STONE FEATURE ---
        BlockMatchTest endStoneReplace = new BlockMatchTest(Blocks.END_STONE);
        List<OreConfiguration.TargetBlockState> susEndStoneTarget = List.of(
                OreConfiguration.target(endStoneReplace, ModBlocks.SUSPICIOUS_END_STONE.get().defaultBlockState())
        );

        // Reduzido de 5 para 4 para um spawn ligeiramente mais contido
        context.register(ModWorldGenKeys.SUSPICIOUS_END_STONE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(susEndStoneTarget, 4)));
    }
}