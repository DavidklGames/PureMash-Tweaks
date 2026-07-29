package dev.davidklgames.puremashtweaks.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class ModWorldGenKeys {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SYNTHORIUM_STANDARD = createConfigKey("synthorium_standard");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SYNTHORIUM_RARE = createConfigKey("synthorium_rare");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SYNTHORIUM_EPIC = createConfigKey("synthorium_epic");

    public static final ResourceKey<PlacedFeature> SYNTHORIUM_STANDARD_PLACED = createPlacedKey("synthorium_standard_placed");
    public static final ResourceKey<PlacedFeature> SYNTHORIUM_RARE_PLACED = createPlacedKey("synthorium_rare_placed");
    public static final ResourceKey<PlacedFeature> SYNTHORIUM_EPIC_PLACED = createPlacedKey("synthorium_epic_placed");

    private static ResourceKey<ConfiguredFeature<?, ?>> createConfigKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, name));
    }

    private static ResourceKey<PlacedFeature> createPlacedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, name));
    }
}