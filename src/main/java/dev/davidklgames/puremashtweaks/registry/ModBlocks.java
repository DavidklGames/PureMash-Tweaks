package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PureMashTweaks.MODID);

    public static final DeferredBlock<PureMashCoreBlock> PUREMASH_CORE_BLOCK = BLOCKS.registerBlock("puremash_core_block",
            PureMashCoreBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.0f, 6.0f).requiresCorrectToolForDrops().lightLevel(_ -> 12)
    );

    public static final DeferredBlock<RotatedPillarBlock> SYNTHORIUM_DEBRIS = BLOCKS.registerBlock("synthorium_debris",
            RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.of().strength(30.0f, 1200.0f).lightLevel(_ -> 4).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)
    );

    public static final DeferredBlock<Block> SYNTHORIUM_BLOCK = BLOCKS.registerSimpleBlock("synthorium_block",
            () -> BlockBehaviour.Properties.of().strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> MOLDELONIAN_BLOCK = BLOCKS.registerSimpleBlock("moldelonian_block",
            () -> BlockBehaviour.Properties.of().strength(6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<SynthesisTableBlock> SYNTHESIS_TABLE = BLOCKS.registerBlock("synthesis_table",
            SynthesisTableBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(_ -> 6).noOcclusion());

    public static final DeferredBlock<MultifunctionalCompressorBlock> MULTIFUNCTIONAL_COMPRESSOR = BLOCKS.registerBlock("multifunctional_compressor",
            MultifunctionalCompressorBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL).lightLevel(_ -> 6).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> FAKE_BEDROCK = BLOCKS.registerBlock("fake_bedrock",
            Block::new,
            () -> BlockBehaviour.Properties.of().strength(0.4f, 6.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<AlchemicalSynthesizerBlock> ALCHEMICAL_SYNTHESIZER = BLOCKS.registerBlock("alchemical_synthesizer",
            AlchemicalSynthesizerBlock::new,
            () -> BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.METAL).lightLevel(state -> state.getValue(AlchemicalSynthesizerBlock.LIT) ? 10 : 4).requiresCorrectToolForDrops());

    public static final DeferredBlock<ChunkLoaderBlock> CHUNK_LOADER = BLOCKS.registerBlock("chunk_loader",
            ChunkLoaderBlock::new,
            () -> BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.METAL).lightLevel(_ -> 6).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<FluidTankBlock> FLUID_TANK = BLOCKS.registerBlock("fluid_tank",
            FluidTankBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<PureMashGeneratorBlock> PUREMASH_GENERATOR = BLOCKS.registerBlock("puremash_generator",
            PureMashGeneratorBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL).lightLevel(state -> state.getValue(PureMashGeneratorBlock.LIT) ? 12 : 0).requiresCorrectToolForDrops());

    public static final DeferredBlock<CreativeFluidTankBlock> CREATIVE_FLUID_TANK = BLOCKS.registerBlock("creative_fluid_tank",
            CreativeFluidTankBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<PureMashBatteryBlock> PUREMASH_BATTERY = BLOCKS.registerBlock("puremash_battery",
            PureMashBatteryBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<CreativeBatteryBlock> CREATIVE_BATTERY = BLOCKS.registerBlock("creative_battery",
            CreativeBatteryBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> SUSPICIOUS_END_STONE = BLOCKS.registerBlock("suspicious_end_stone",
            SuspiciousEndStoneBlock::new,
            () -> BlockBehaviour.Properties.of().strength(0.8f).sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<LiquidBlock> MOLTEN_SYNTHORIUM_BLOCK = BLOCKS.registerBlock("molten_synthorium",
            properties -> new LiquidBlock(ModFluids.MOLTEN_SYNTHORIUM_FLOWING.get(), properties),
            () -> BlockBehaviour.Properties.of().noCollision().replaceable().randomTicks().strength(100.0F).lightLevel(_ -> 15).noLootTable().liquid());

    public static final DeferredBlock<LiquidBlock> MOLTEN_MOLDELONIAN_BLOCK = BLOCKS.registerBlock("molten_moldelonian",
            properties -> new LiquidBlock(ModFluids.MOLTEN_MOLDELONIAN_FLOWING.get(), properties),
            () -> BlockBehaviour.Properties.of().noCollision().replaceable().randomTicks().strength(100.0F).lightLevel(_ -> 15).noLootTable().liquid());

    public static final DeferredBlock<UniversalCableBlock> SYNTHORIUM_UNIVERSAL_CABLE = BLOCKS.registerBlock("synthorium_universal_cable",
            properties -> new UniversalCableBlock(1, properties),
            () -> BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.METAL).noOcclusion()
    );

    public static final DeferredBlock<UniversalCableBlock> MOLDELONIAN_UNIVERSAL_CABLE = BLOCKS.registerBlock("moldelonian_universal_cable",
            properties -> new UniversalCableBlock(2, properties),
            () -> BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL).noOcclusion()
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}