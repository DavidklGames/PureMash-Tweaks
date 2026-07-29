package dev.davidklgames.puremashtweaks.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
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
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.PUREMASH_CORE_BLOCK.get())
                .add(ModBlocks.SYNTHORIUM_BLOCK.get())
                .add(ModBlocks.MOLDELONIAN_BLOCK.get())
                .add(ModBlocks.SYNTHORIUM_DEBRIS.get())
                .add(ModBlocks.SYNTHESIS_TABLE.get())
                .add(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get())
                .add(ModBlocks.FAKE_BEDROCK.get())
                .add(ModBlocks.ALCHEMICAL_SYNTHESIZER.get())
                .add(ModBlocks.FLUID_TANK.get())
                .add(ModBlocks.CREATIVE_FLUID_TANK.get());

        this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.SYNTHORIUM_DEBRIS.get())
                .add(ModBlocks.FAKE_BEDROCK.get())
                .add(ModBlocks.SYNTHORIUM_BLOCK.get())
                .add(ModBlocks.PUREMASH_CORE_BLOCK.get())
                .add(ModBlocks.SYNTHESIS_TABLE.get())
                .add(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get())
                .add(ModBlocks.ALCHEMICAL_SYNTHESIZER.get());

        this.tag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .add(ModBlocks.MOLDELONIAN_BLOCK.get());

        this.tag(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "mineable/paxel")))
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL);

        this.tag(commonBlockTag("ores")).add(ModBlocks.SYNTHORIUM_DEBRIS.get());
        this.tag(commonBlockTag("ores/synthorium")).add(ModBlocks.SYNTHORIUM_DEBRIS.get());

        this.tag(commonBlockTag("storage_blocks"))
                .add(ModBlocks.SYNTHORIUM_BLOCK.get())
                .add(ModBlocks.MOLDELONIAN_BLOCK.get());

        this.tag(commonBlockTag("storage_blocks/synthorium")).add(ModBlocks.SYNTHORIUM_BLOCK.get());
        this.tag(commonBlockTag("storage_blocks/moldelonian")).add(ModBlocks.MOLDELONIAN_BLOCK.get());
    }
}