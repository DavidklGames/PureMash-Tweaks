package dev.davidklgames.puremashtweaks.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.*;
import java.util.List;

public class ModPlacedFeatures {
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);

        // --- 1. Standard (3 tentativas por chunk com pico em Y=-64) ---
        context.register(ModWorldGenKeys.SYNTHORIUM_STANDARD_PLACED, new PlacedFeature(
                lookup.getOrThrow(ModWorldGenKeys.SYNTHORIUM_STANDARD),
                List.of(
                        CountPlacement.of(3),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0)),
                        BiomeFilter.biome()
                )));

        // --- 2. Rare (1 a cada 4 chunks no fundo do mundo) ---
        context.register(ModWorldGenKeys.SYNTHORIUM_RARE_PLACED, new PlacedFeature(
                lookup.getOrThrow(ModWorldGenKeys.SYNTHORIUM_RARE),
                List.of(
                        RarityFilter.onAverageOnceEvery(4),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-16)),
                        BiomeFilter.biome()
                )));

        // --- 3. Epic "Chunk Bom" (1 a cada 12 chunks nas camadas mais profundas) ---
        context.register(ModWorldGenKeys.SYNTHORIUM_EPIC_PLACED, new PlacedFeature(
                lookup.getOrThrow(ModWorldGenKeys.SYNTHORIUM_EPIC),
                List.of(
                        RarityFilter.onAverageOnceEvery(12),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-32)),
                        BiomeFilter.biome()
                )));

        // --- SUSPICIOUS END STONE PLACED (3 Veios por Chunk na Superfície das Ilhas Y=55 a Y=75) ---
        context.register(ModWorldGenKeys.SUSPICIOUS_END_STONE_PLACED, new PlacedFeature(
                lookup.getOrThrow(ModWorldGenKeys.SUSPICIOUS_END_STONE),
                List.of(
                        CountPlacement.of(3),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(55), VerticalAnchor.absolute(75)),
                        BiomeFilter.biome()
                )));
    }
}