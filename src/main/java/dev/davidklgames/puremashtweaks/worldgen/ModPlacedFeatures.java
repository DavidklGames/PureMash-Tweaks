package dev.davidklgames.puremashtweaks.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.*;
import java.util.List;

public class ModPlacedFeatures {
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);

        // --- Standard ---:
        context.register(ModWorldGenKeys.SYNTHORIUM_STANDARD_PLACED, new PlacedFeature(lookup.getOrThrow(ModWorldGenKeys.SYNTHORIUM_STANDARD),
                List.of(CountPlacement.of(1), InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0)),
                        BiomeFilter.biome())));

        //--- Rare ---:
        context.register(ModWorldGenKeys.SYNTHORIUM_RARE_PLACED, new PlacedFeature(lookup.getOrThrow(ModWorldGenKeys.SYNTHORIUM_RARE),
                List.of(RarityFilter.onAverageOnceEvery(10000000), InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0)),
                        BiomeFilter.biome())));

        //--- Epic ---:
        context.register(ModWorldGenKeys.SYNTHORIUM_EPIC_PLACED, new PlacedFeature(lookup.getOrThrow(ModWorldGenKeys.SYNTHORIUM_EPIC),
                List.of(RarityFilter.onAverageOnceEvery(1000000000), InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0)),
                        BiomeFilter.biome())));
    }
}