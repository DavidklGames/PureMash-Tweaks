package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider {
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    public static class BlockLoot extends BlockLootSubProvider {
        protected BlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            // Drop themselves.
            this.dropSelf(ModBlocks.PUREMASH_CORE_BLOCK.get());
            this.dropSelf(ModBlocks.SYNTHORIUM_DEBRIS.get());
            this.dropSelf(ModBlocks.SYNTHORIUM_BLOCK.get());
            this.dropSelf(ModBlocks.MOLDELONIAN_BLOCK.get());
            this.dropSelf(ModBlocks.SYNTHESIS_TABLE.get());
            this.dropSelf(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get());
            this.dropSelf(ModBlocks.ALCHEMICAL_SYNTHESIZER.get());
            this.dropSelf(ModBlocks.CHUNK_LOADER.get());
            this.dropSelf(ModBlocks.FLUID_TANK.get());
            this.dropSelf(ModBlocks.CREATIVE_FLUID_TANK.get());
            // Fake Bedrock Drop (Ensures the drop is the official Vanilla Bedrock).
            this.add(ModBlocks.FAKE_BEDROCK.get(), this.createSingleItemTable(net.minecraft.world.item.Items.BEDROCK));
        }

        @Override
        protected @NonNull Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
        }
    }
}