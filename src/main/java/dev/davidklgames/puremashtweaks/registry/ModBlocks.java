package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.block.AlchemicalSynthesizerBlock;
import dev.davidklgames.puremashtweaks.block.MultifunctionalCompressorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import dev.davidklgames.puremashtweaks.block.SynthesisTableBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("deprecation")
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PureMashTweaks.MODID);

    public static final DeferredBlock<dev.davidklgames.puremashtweaks.block.PureMashCoreBlock> PUREMASH_CORE_BLOCK = BLOCKS.registerBlock("puremash_core_block",
            dev.davidklgames.puremashtweaks.block.PureMashCoreBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(_ -> 12)
    );

    public static final DeferredBlock<RotatedPillarBlock> SYNTHORIUM_DEBRIS = BLOCKS.registerBlock("synthorium_debris",
            RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(30.0f, 1200.0f)
                    .lightLevel(_ -> 4)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)
    );

    public static final DeferredBlock<Block> SYNTHORIUM_BLOCK = BLOCKS.registerSimpleBlock("synthorium_block",
            () -> BlockBehaviour.Properties.of()
                    .strength(5.0f, 6.0f)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> MOLDELONIAN_BLOCK = BLOCKS.registerSimpleBlock("moldelonian_block",
            () -> BlockBehaviour.Properties.of().strength(6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<SynthesisTableBlock> SYNTHESIS_TABLE = BLOCKS.registerBlock("synthesis_table",
            SynthesisTableBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(_ -> 6)
                    .noOcclusion());

    public static final net.neoforged.neoforge.registries.DeferredBlock<MultifunctionalCompressorBlock> MULTIFUNCTIONAL_COMPRESSOR = BLOCKS.registerBlock("multifunctional_compressor",
            MultifunctionalCompressorBlock::new,
            () -> net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .lightLevel(_ -> 6)
                    .requiresCorrectToolForDrops()
    );

    // Fake Bedrock (native protection built into the block to prevent dependencies on block-breaking events.)
    public static final DeferredBlock<Block> FAKE_BEDROCK = BLOCKS.registerBlock("fake_bedrock",
            properties -> new Block(properties) {
                @Override
                public float getDestroyProgress(net.minecraft.world.level.block.state.@NonNull BlockState state, net.minecraft.world.entity.player.@NonNull Player player, net.minecraft.world.level.@NonNull BlockGetter level, net.minecraft.core.@NonNull BlockPos pos) {
                    net.minecraft.world.item.ItemStack tool = player.getMainHandItem();

                    // Checks if the held tool is made of Synthorium.
                    boolean isSynthoriumTool = tool.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_PICKAXE.get() ||
                            tool.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_PAXEL.get();

                    boolean hasOverload = false;
                    if (isSynthoriumTool) {
                        // Safely using the record lookup from phase 26.1.2 based on the player level.
                        var reg = player.level().registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
                        if (reg.isPresent()) {
                            var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                            if (overloadOpt.isPresent()) {
                                hasOverload = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(overloadOpt.get(), tool) > 0;
                            }
                        }
                    }

                    // If you are not using the Synthorium tool with Overload, the mining speed is zero (indestructible block).
                    if (!isSynthoriumTool || !hasOverload) {
                        return 0.0f;
                    }

                    return super.getDestroyProgress(state, player, level, pos);
                }
            },
            () -> BlockBehaviour.Properties.of()
                    .strength(0.4f, 6.0f) // It exits early only if it passes the validation above.
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final net.neoforged.neoforge.registries.DeferredBlock<AlchemicalSynthesizerBlock> ALCHEMICAL_SYNTHESIZER = BLOCKS.registerBlock("alchemical_synthesizer",
            AlchemicalSynthesizerBlock::new,
            () -> net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(4.0f)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .lightLevel(state -> state.getValue(dev.davidklgames.puremashtweaks.block.AlchemicalSynthesizerBlock.LIT) ? 10 : 4)
                    .requiresCorrectToolForDrops()
    );

    public static final net.neoforged.neoforge.registries.DeferredBlock<dev.davidklgames.puremashtweaks.block.ChunkLoaderBlock> CHUNK_LOADER = BLOCKS.registerBlock("chunk_loader",
            dev.davidklgames.puremashtweaks.block.ChunkLoaderBlock::new,
            () -> net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(4.0f)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .lightLevel(_ -> 6)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

    public static final DeferredBlock<dev.davidklgames.puremashtweaks.block.FluidTankBlock> FLUID_TANK = BLOCKS.registerBlock("fluid_tank",
            dev.davidklgames.puremashtweaks.block.FluidTankBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

    public static final DeferredBlock<dev.davidklgames.puremashtweaks.block.CreativeFluidTankBlock> CREATIVE_FLUID_TANK = BLOCKS.registerBlock("creative_fluid_tank",
            dev.davidklgames.puremashtweaks.block.CreativeFluidTankBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

        public static void register(IEventBus eventBus) {
            BLOCKS.register(eventBus);
        }
    }