package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModLootTableProvider {
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ArchaeologyLoot::new, LootContextParamSets.ARCHAEOLOGY)
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
            this.dropSelf(ModBlocks.PUREMASH_GENERATOR.get());
            this.dropSelf(ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get());
            this.dropSelf(ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get());
            this.dropSelf(ModBlocks.PUREMASH_BATTERY.get());
            this.dropSelf(ModBlocks.CREATIVE_BATTERY.get());
            this.add(ModBlocks.FAKE_BEDROCK.get(), this.createSingleItemTable(Items.BEDROCK));
            // Suspicious End Stone drops NOTHING when broken/mined with pickaxe
            this.add(ModBlocks.SUSPICIOUS_END_STONE.get(), noDrop());
        }

        @Override
        protected @NonNull Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
        }
    }

    public record ArchaeologyLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
        public static final ResourceKey<LootTable> SUSPICIOUS_END_STONE_LOOT = ResourceKey.create(
                Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "archaeology/suspicious_end_stone")
        );

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            output.accept(SUSPICIOUS_END_STONE_LOOT, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            // Recompensas Raras & Principais
                            .add(LootItem.lootTableItem(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()).setWeight(10)) // 5.0%
                            .add(LootItem.lootTableItem(ModBlocks.MOLDELONIAN_BLOCK.asItem()).setWeight(4))          // 2.0%
                            .add(LootItem.lootTableItem(ModItems.MUSIC_DISC_NEW_HORIZONS.asItem()).setWeight(4))     // 2.0%

                            // Recompensas Úteis
                            .add(LootItem.lootTableItem(Items.SHULKER_SHELL).setWeight(14))                           // 7.0%
                            .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(14))                                 // 7.0%
                            .add(LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(20))                             // 10.0%
                            .add(LootItem.lootTableItem(Items.CHORUS_FRUIT).setWeight(20))                            // 10.0%

                            // Loots "Troll" e Despejo de Terreno
                            .add(LootItem.lootTableItem(Items.POPPED_CHORUS_FRUIT).setWeight(24))                     // 12.0%
                            .add(LootItem.lootTableItem(Blocks.END_STONE.asItem()).setWeight(24))                     // 12.0%
                            .add(LootItem.lootTableItem(Items.GLASS_BOTTLE).setWeight(18))                            // 9.0%
                            .add(LootItem.lootTableItem(Items.PHANTOM_MEMBRANE).setWeight(16))                        // 8.0%
                            .add(LootItem.lootTableItem(Blocks.OBSIDIAN.asItem()).setWeight(16))                      // 8.0%
                            .add(LootItem.lootTableItem(Items.BONE).setWeight(16))                                    // 8.0%
                    )
            );
        }
    }
}