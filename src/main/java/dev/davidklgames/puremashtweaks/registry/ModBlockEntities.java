package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.block.entity.AlchemicalSynthesizerBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PureMashTweaks.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SynthesisTableBlockEntity>> SYNTHESIS_TABLE_BE =
            BLOCK_ENTITIES.register("synthesis_table_be", () ->
                    new BlockEntityType<>(SynthesisTableBlockEntity::new, ModBlocks.SYNTHESIS_TABLE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultifunctionalCompressorBlockEntity>> MULTIFUNCIONAL_COMPRESSOR_BE =
            BLOCK_ENTITIES.register("multifunctional_compressor_be", () ->
                    new BlockEntityType<>(MultifunctionalCompressorBlockEntity::new, ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<dev.davidklgames.puremashtweaks.block.entity.PureMashCoreBlockEntity>> PUREMASH_CORE_BE =
            BLOCK_ENTITIES.register("puremash_core_be", () ->
                    new BlockEntityType<>(dev.davidklgames.puremashtweaks.block.entity.PureMashCoreBlockEntity::new, ModBlocks.PUREMASH_CORE_BLOCK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemicalSynthesizerBlockEntity>> ALCHEMICAL_SYNTHESIZER_BE =
            BLOCK_ENTITIES.register("alchemical_synthesizer_be", () ->
                    new BlockEntityType<>(AlchemicalSynthesizerBlockEntity::new, ModBlocks.ALCHEMICAL_SYNTHESIZER.get()));

    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<dev.davidklgames.puremashtweaks.block.entity.ChunkLoaderBlockEntity>> CHUNK_LOADER_BE =
            BLOCK_ENTITIES.register("chunk_loader_be", () ->
                    new net.minecraft.world.level.block.entity.BlockEntityType<>(dev.davidklgames.puremashtweaks.block.entity.ChunkLoaderBlockEntity::new, ModBlocks.CHUNK_LOADER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<dev.davidklgames.puremashtweaks.block.entity.FluidTankBlockEntity>> FLUID_TANK_BE =
            BLOCK_ENTITIES.register("fluid_tank_be", () ->
                    new BlockEntityType<>(dev.davidklgames.puremashtweaks.block.entity.FluidTankBlockEntity::new, ModBlocks.FLUID_TANK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<dev.davidklgames.puremashtweaks.block.entity.CreativeFluidTankBlockEntity>> CREATIVE_FLUID_TANK_BE =
            BLOCK_ENTITIES.register("creative_fluid_tank_be", () ->
                    new BlockEntityType<>(dev.davidklgames.puremashtweaks.block.entity.CreativeFluidTankBlockEntity::new, ModBlocks.CREATIVE_FLUID_TANK.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}