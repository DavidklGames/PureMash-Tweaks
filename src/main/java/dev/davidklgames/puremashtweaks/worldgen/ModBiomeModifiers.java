package dev.davidklgames.puremashtweaks.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class ModBiomeModifiers {
    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placed = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "add_synthorium_debris")),
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(net.minecraft.tags.BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(
                                placed.getOrThrow(ModWorldGenKeys.SYNTHORIUM_STANDARD_PLACED),
                                placed.getOrThrow(ModWorldGenKeys.SYNTHORIUM_RARE_PLACED),
                                placed.getOrThrow(ModWorldGenKeys.SYNTHORIUM_EPIC_PLACED)
                        ),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                ));

        // --- ADD SUSPICIOUS END STONE TO END HIGHLANDS (End Cities) & MIDLANDS ---
        context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "add_suspicious_end_stone")),
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.END_HIGHLANDS),
                                biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.END_MIDLANDS)
                        ),
                        HolderSet.direct(placed.getOrThrow(ModWorldGenKeys.SUSPICIOUS_END_STONE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                ));
    }
}