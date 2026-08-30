package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.util.ModToolMaterials;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PureMashTweaks.MODID);
    }

    private TagKey<Block> commonBlockTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        // Mineable with Pickaxe (Incluído Chunk Loader e Cabos)
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.PUREMASH_CORE_BLOCK.get())
                .add(ModBlocks.SYNTHORIUM_BLOCK.get())
                .add(ModBlocks.MOLDELONIAN_BLOCK.get())
                .add(ModBlocks.SYNTHORIUM_DEBRIS.get())
                .add(ModBlocks.SYNTHESIS_TABLE.get())
                .add(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get())
                .add(ModBlocks.FAKE_BEDROCK.get())
                .add(ModBlocks.PUREMASH_GENERATOR.get())
                .add(ModBlocks.ALCHEMICAL_SYNTHESIZER.get())
                .add(ModBlocks.CHUNK_LOADER.get())
                .add(ModBlocks.FLUID_TANK.get())
                .add(ModBlocks.CREATIVE_FLUID_TANK.get())
                .add(ModBlocks.PUREMASH_BATTERY.get())
                .add(ModBlocks.CREATIVE_BATTERY.get())
                .add(ModBlocks.SUSPICIOUS_END_STONE.get())
                .add(ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get())
                .add(ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get());

        // Needs Diamond Tool
        this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.SYNTHORIUM_DEBRIS.get())
                .add(ModBlocks.SYNTHORIUM_BLOCK.get())
                .add(ModBlocks.SYNTHESIS_TABLE.get())
                .add(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get())
                .add(ModBlocks.PUREMASH_GENERATOR.get())
                .add(ModBlocks.ALCHEMICAL_SYNTHESIZER.get())
                .add(ModBlocks.CHUNK_LOADER.get());

        this.tag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .add(ModBlocks.MOLDELONIAN_BLOCK.get())
                .add(ModBlocks.PUREMASH_CORE_BLOCK.get())
                .add(ModBlocks.FAKE_BEDROCK.get());

        this.tag(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "mineable/paxel")))
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addTag(BlockTags.MINEABLE_WITH_HOE);

        // Custom Tool Tier Tags
        this.tag(ModToolMaterials.INCORRECT_FOR_SYNTHORIUM_TOOL);
        this.tag(ModToolMaterials.INCORRECT_FOR_MOLDELONIAN_TOOL);

        this.tag(commonBlockTag("ores")).add(ModBlocks.SYNTHORIUM_DEBRIS.get());
        this.tag(commonBlockTag("ores/synthorium")).add(ModBlocks.SYNTHORIUM_DEBRIS.get());

        this.tag(commonBlockTag("storage_blocks"))
                .add(ModBlocks.SYNTHORIUM_BLOCK.get())
                .add(ModBlocks.MOLDELONIAN_BLOCK.get());

        this.tag(commonBlockTag("storage_blocks/synthorium")).add(ModBlocks.SYNTHORIUM_BLOCK.get());
        this.tag(commonBlockTag("storage_blocks/moldelonian")).add(ModBlocks.MOLDELONIAN_BLOCK.get());

        // Cables Block Tag (c:cables)
        this.tag(commonBlockTag("cables"))
                .add(ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get())
                .add(ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get());
    }
}