package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.block.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import org.jspecify.annotations.NonNull;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PureMashTweaks.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SynthesisTableBlockEntity>> SYNTHESIS_TABLE_BE =
            BLOCK_ENTITIES.register("synthesis_table_be", () ->
                    new BlockEntityType<>(SynthesisTableBlockEntity::new, ModBlocks.SYNTHESIS_TABLE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultifunctionalCompressorBlockEntity>> MULTIFUNCIONAL_COMPRESSOR_BE =
            BLOCK_ENTITIES.register("multifunctional_compressor_be", () ->
                    new BlockEntityType<>(MultifunctionalCompressorBlockEntity::new, ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PureMashCoreBlockEntity>> PUREMASH_CORE_BE =
            BLOCK_ENTITIES.register("puremash_core_be", () ->
                    new BlockEntityType<>(PureMashCoreBlockEntity::new, ModBlocks.PUREMASH_CORE_BLOCK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemicalSynthesizerBlockEntity>> ALCHEMICAL_SYNTHESIZER_BE =
            BLOCK_ENTITIES.register("alchemical_synthesizer_be", () ->
                    new BlockEntityType<>(AlchemicalSynthesizerBlockEntity::new, ModBlocks.ALCHEMICAL_SYNTHESIZER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkLoaderBlockEntity>> CHUNK_LOADER_BE =
            BLOCK_ENTITIES.register("chunk_loader_be", () ->
                    new BlockEntityType<>(ChunkLoaderBlockEntity::new, ModBlocks.CHUNK_LOADER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTankBlockEntity>> FLUID_TANK_BE =
            BLOCK_ENTITIES.register("fluid_tank_be", () ->
                    new BlockEntityType<>(FluidTankBlockEntity::new, ModBlocks.FLUID_TANK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeFluidTankBlockEntity>> CREATIVE_FLUID_TANK_BE =
            BLOCK_ENTITIES.register("creative_fluid_tank_be", () ->
                    new BlockEntityType<>(CreativeFluidTankBlockEntity::new, ModBlocks.CREATIVE_FLUID_TANK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PureMashGeneratorBlockEntity>> PUREMASH_GENERATOR_BE =
            BLOCK_ENTITIES.register("puremash_generator_be", () ->
                    new BlockEntityType<>(PureMashGeneratorBlockEntity::new, ModBlocks.PUREMASH_GENERATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PureMashBatteryBlockEntity>> PUREMASH_BATTERY_BE =
            BLOCK_ENTITIES.register("puremash_battery_be", () ->
                    new BlockEntityType<>(PureMashBatteryBlockEntity::new, ModBlocks.PUREMASH_BATTERY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeBatteryBlockEntity>> CREATIVE_BATTERY_BE =
            BLOCK_ENTITIES.register("creative_battery_be", () ->
                    new BlockEntityType<>(CreativeBatteryBlockEntity::new, ModBlocks.CREATIVE_BATTERY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrushableBlockEntity>> SUSPICIOUS_END_STONE_BE =
            BLOCK_ENTITIES.register("suspicious_end_stone_be", () ->
                    new BlockEntityType<>(
                            (pos, state) -> new BrushableBlockEntity(pos, state) {
                                @Override
                                public @NonNull BlockEntityType<?> getType() {
                                    return ModBlockEntities.SUSPICIOUS_END_STONE_BE.get();
                                }
                            },
                            ModBlocks.SUSPICIOUS_END_STONE.get()
                    )
            );

    // --- UNIVERSAL CABLE BLOCK ENTITIES ---
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UniversalCableBlockEntity>> SYNTHORIUM_UNIVERSAL_CABLE_BE =
            BLOCK_ENTITIES.register("synthorium_universal_cable_be", () ->
                    new BlockEntityType<>(
                            (pos, state) -> new UniversalCableBlockEntity(pos, state, 1),
                            ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UniversalCableBlockEntity>> MOLDELONIAN_UNIVERSAL_CABLE_BE =
            BLOCK_ENTITIES.register("moldelonian_universal_cable_be", () ->
                    new BlockEntityType<>(
                            (pos, state) -> new UniversalCableBlockEntity(pos, state, 2),
                            ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get()
                    ));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}