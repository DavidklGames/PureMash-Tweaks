package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.api.client.renderer.book.EnchantmentBookModelsUnbaked;
import dev.davidklgames.puremashtweaks.component.ModDataComponents;
import dev.davidklgames.puremashtweaks.item.ColorSingularityItem;
import dev.davidklgames.puremashtweaks.util.ModArmorMaterials;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.client.data.models.model.TextureSlot;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, PureMashTweaks.MODID);
    }

    // Helper to easily construct modded Identifiers.
    private static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, path);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {

        // =========================================================
        // Non-emissive vanilla cubes
        // =========================================================
        TextureMapping bedrockMapping = new TextureMapping()
                .put(TextureSlot.ALL, new net.minecraft.client.resources.model.sprite.Material(
                        Identifier.withDefaultNamespace("block/bedrock"),
                        false
                ));

        Identifier bedrockModelId = ModelTemplates.CUBE_ALL.create(ModBlocks.FAKE_BEDROCK.get(), bedrockMapping, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                ModBlocks.FAKE_BEDROCK.get(),
                BlockModelGenerators.plainVariant(bedrockModelId)
        ));

        // =========================================================
        // Synthorium e Moldelonian Blocks (Block Bench Static Models)
        // =========================================================
        registerStaticBlockBenchBlock(blockModels, itemModels, ModBlocks.SYNTHORIUM_BLOCK.get(), "synthorium_block");
        registerStaticBlockBenchBlock(blockModels, itemModels, ModBlocks.MOLDELONIAN_BLOCK.get(), "moldelonian_block");

        // =========================================================
        // Synthorium Debris (Block Bench Static Model)
        // =========================================================
        Identifier debrisModelId = modLoc("block/synthorium_debris");
        MultiVariant debrisVariant = BlockModelGenerators.plainVariant(debrisModelId);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ModBlocks.SYNTHORIUM_DEBRIS.get())
                        .with(PropertyDispatch.initial(BlockStateProperties.AXIS)
                                .select(Direction.Axis.Y, debrisVariant)
                                .select(Direction.Axis.X, debrisVariant.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.Axis.Z, debrisVariant.with(BlockModelGenerators.X_ROT_90))
                        )
        );

        itemModels.itemModelOutput.register(
                ModBlocks.SYNTHORIUM_DEBRIS.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        ItemModelUtils.plainModel(debrisModelId),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // =========================================================
        // Synthesis Table (Block Bench Static Model)
        // =========================================================
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                ModBlocks.SYNTHESIS_TABLE.get(),
                BlockModelGenerators.plainVariant(modLoc("block/synthesis_table"))
        ));

        itemModels.itemModelOutput.register(
                ModBlocks.SYNTHESIS_TABLE.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        ItemModelUtils.plainModel(modLoc("block/synthesis_table")),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // =========================================================
        // Multifunctional Compressor (Block Bench Static Model)
        // =========================================================
        Identifier compressorModelId = modLoc("block/multifunctional_compressor");
        MultiVariant compressorVariant = BlockModelGenerators.plainVariant(compressorModelId);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get())
                        .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                                .select(Direction.NORTH, compressorVariant)
                                .select(Direction.EAST, compressorVariant.with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, compressorVariant.with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, compressorVariant.with(BlockModelGenerators.Y_ROT_270))
                        )
        );

        itemModels.itemModelOutput.register(
                ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        ItemModelUtils.plainModel(compressorModelId),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // =========================================================
        // Alchemical Synthesizer (Block Bench Static Model)
        // =========================================================
        TextureSlot FRONT_TEX_SLOT = TextureSlot.create("10");

        ModelTemplate ACTIVE_ALCHEMICAL_TEMPLATE = new ModelTemplate(
                Optional.of(modLoc("block/alchemical_synthesizer")),
                Optional.empty(),
                FRONT_TEX_SLOT
        );

        TextureMapping activeTextureMap = new TextureMapping()
                .put(FRONT_TEX_SLOT, new Material(modLoc("block/alchemical_synthesizer/alchemical_synthesizer_front_active")));

        Identifier alchemicalInactiveModelId = modLoc("block/alchemical_synthesizer");

        // DataGen automatically generates the 'alchemical_synthesizer_active.json' file!
        Identifier alchemicalActiveModelId = ACTIVE_ALCHEMICAL_TEMPLATE.create(
                modLoc("block/alchemical_synthesizer_active"),
                activeTextureMap,
                blockModels.modelOutput
        );

        MultiVariant alchemicalInactiveVariant = BlockModelGenerators.plainVariant(alchemicalInactiveModelId);
        MultiVariant alchemicalActiveVariant = BlockModelGenerators.plainVariant(alchemicalActiveModelId);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ModBlocks.ALCHEMICAL_SYNTHESIZER.get())
                        .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.LIT)
                                .select(Direction.NORTH, Boolean.FALSE, alchemicalInactiveVariant)
                                .select(Direction.EAST, Boolean.FALSE, alchemicalInactiveVariant.with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, Boolean.FALSE, alchemicalInactiveVariant.with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, Boolean.FALSE, alchemicalInactiveVariant.with(BlockModelGenerators.Y_ROT_270))
                                .select(Direction.NORTH, Boolean.TRUE, alchemicalActiveVariant)
                                .select(Direction.EAST, Boolean.TRUE, alchemicalActiveVariant.with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, Boolean.TRUE, alchemicalActiveVariant.with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, Boolean.TRUE, alchemicalActiveVariant.with(BlockModelGenerators.Y_ROT_270))
                        )
        );

        itemModels.itemModelOutput.register(
                ModBlocks.ALCHEMICAL_SYNTHESIZER.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        ItemModelUtils.plainModel(alchemicalInactiveModelId),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // =========================================================
        // Fluid Tank (Block Bench Static Model with 3D Fluid)
        // =========================================================
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                ModBlocks.FLUID_TANK.get(),
                BlockModelGenerators.plainVariant(modLoc("block/fluid_tank"))
        ));

        itemModels.itemModelOutput.register(
                ModBlocks.FLUID_TANK.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        new dev.davidklgames.puremashtweaks.api.client.renderer.tank.FluidTankItemModelUnbaked(
                                modLoc("block/fluid_tank"),
                                32000L,
                                false,
                                Optional.empty()
                        ),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // =========================================================
        // Creative Fluid Tank (Block Bench Static Model with 3D Fluid)
        // =========================================================
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                ModBlocks.CREATIVE_FLUID_TANK.get(),
                BlockModelGenerators.plainVariant(modLoc("block/creative_fluid_tank"))
        ));

        itemModels.itemModelOutput.register(
                ModBlocks.CREATIVE_FLUID_TANK.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        new dev.davidklgames.puremashtweaks.api.client.renderer.tank.FluidTankItemModelUnbaked(
                                modLoc("block/creative_fluid_tank"),
                                1000000L,
                                true,
                                Optional.empty()
                        ),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // =========================================================
        // PureMash Chunk Loader (Block Bench Static Model)
        // =========================================================
        Identifier chunkLoaderModelId = modLoc("block/chunk_loader");
        MultiVariant chunkLoaderVariant = BlockModelGenerators.plainVariant(chunkLoaderModelId);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ModBlocks.CHUNK_LOADER.get())
                        .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                                .select(Direction.NORTH, chunkLoaderVariant)
                                .select(Direction.EAST, chunkLoaderVariant.with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, chunkLoaderVariant.with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, chunkLoaderVariant.with(BlockModelGenerators.Y_ROT_270))
                        )
        );

        itemModels.itemModelOutput.register(
                ModBlocks.CHUNK_LOADER.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        ItemModelUtils.plainModel(chunkLoaderModelId),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // =========================================================
        // PureMash Core Block (Block Bench Static Model - Simple)
        // =========================================================
        Identifier puremashCoreBlockModelId = modLoc("block/puremash_core_block");

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                ModBlocks.PUREMASH_CORE_BLOCK.get(),
                BlockModelGenerators.plainVariant(puremashCoreBlockModelId)
        ));

        itemModels.itemModelOutput.register(
                ModBlocks.PUREMASH_CORE_BLOCK.get().asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        ItemModelUtils.plainModel(puremashCoreBlockModelId),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );

        // --- ALL ITEMS REGISTER ---
        registerAllItems(itemModels);
    }



    private void registerAllItems(ItemModelGenerators itemModels) {

        // --- PureMash Core ---
        itemModels.generateFlatItem(ModItems.PUREMASH_CORE.get(), ModelTemplates.FLAT_ITEM);

        // --- Synthorium ---
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_SCRAP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_DUST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_ROD.get(), ModelTemplates.FLAT_ITEM);

        // --- Moldelonian
        itemModels.generateFlatItem(ModItems.MOLDELONIAN_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.MOLDELONIAN_CORE.get(), ModelTemplates.FLAT_ITEM);

        // --- Recipe Card (with Data Components swap) ---
        itemModels.itemModelOutput.accept(ModItems.MEMORY_CARD.get(),
                ItemModelUtils.conditional(ItemModelUtils.hasComponent(ModDataComponents.RECIPE_CARD_DATA.get()),
                ItemModelUtils.plainModel(itemModels.createFlatItemModel(ModItems.MEMORY_CARD.get(), "_filled", ModelTemplates.FLAT_ITEM)),
                ItemModelUtils.plainModel(itemModels.createFlatItemModel
                (ModItems.MEMORY_CARD.get(), ModelTemplates.FLAT_ITEM))));

        // --- Nuggets ---
        itemModels.generateFlatItem(ModItems.MOLDELONIAN_NUGGET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_NUGGET.get(), ModelTemplates.FLAT_ITEM);

        // --- Music Discs ---
        itemModels.generateFlatItem(ModItems.MUSIC_DISC_BEYOND_THE_FINAL_STAGE.get(), ModelTemplates.FLAT_ITEM);

        // --- Enchantment Books ---
        itemModels.generateFlatItem(ModItems.OVERLOAD_BOOK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.OVERCLOCK_BOOK.get(), ModelTemplates.FLAT_ITEM);

        // --- Machine Upgrades ---
        itemModels.generateFlatItem(ModItems.SPEED_UPGRADE_1.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SPEED_UPGRADE_2.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SPEED_UPGRADE_3.get(), ModelTemplates.FLAT_ITEM);

        // --- Tools ---
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SYNTHORIUM_PAXEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);

        itemModels.generateTrimmableItem(
                ModItems.SYNTHORIUM_HELMET.get(),
                ModArmorMaterials.SYNTHORIUM_ASSET,
                ItemModelGenerators.TRIM_PREFIX_HELMET,
                false
        );

        itemModels.generateTrimmableItem(
                ModItems.SYNTHORIUM_CHESTPLATE.get(),
                ModArmorMaterials.SYNTHORIUM_ASSET,
                ItemModelGenerators.TRIM_PREFIX_CHESTPLATE,
                false
        );

        itemModels.generateTrimmableItem(
                ModItems.SYNTHORIUM_LEGGINGS.get(),
                ModArmorMaterials.SYNTHORIUM_ASSET,
                ItemModelGenerators.TRIM_PREFIX_LEGGINGS,
                false
        );

        itemModels.generateTrimmableItem(
                ModItems.SYNTHORIUM_BOOTS.get(),
                ModArmorMaterials.SYNTHORIUM_ASSET,
                ItemModelGenerators.TRIM_PREFIX_BOOTS,
                false
        );

        itemModels.itemModelOutput.register(
                net.minecraft.world.item.Items.ENCHANTED_BOOK,
                new net.minecraft.client.renderer.item.ClientItem(
                        new EnchantmentBookModelsUnbaked(
                                Identifier.withDefaultNamespace("item/enchanted_book"),
                                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/overload_book"),
                                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/overclock_book")
                        ),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(
                                true, // hand_animation_on_swap (DEFAULT).
                                false, // oversized_in_gui (not).
                                1.0F   // swap_animation_scale.
                        )
                )
        );

        // --- DYNAMIC REGISTRY OF ACTIVE SINGULARITY MODELS ---
        registerSingularityModels(itemModels);
    }

    // Unified helper method for static blocks exported from Block Bench.
    private void registerStaticBlockBenchBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String name) {
        Identifier modelId = modLoc("block/" + name);

        // 1. Registers a simple BlockState pointing to the Block Bench model.
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                block,
                BlockModelGenerators.plainVariant(modelId)
        ));

        // 2. Registers the block item in hand/inventory, pointing to the same model.
        itemModels.itemModelOutput.register(
                block.asItem(),
                new net.minecraft.client.renderer.item.ClientItem(
                        ItemModelUtils.plainModel(modelId),
                        new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.0F)
                )
        );
    }

    private void registerSingularityModels(ItemModelGenerators itemModels) {
        ModelTemplate twoLayersTemplate = new ModelTemplate(
                Optional.of(Identifier.withDefaultNamespace("item/generated")),
                Optional.empty(),
                TextureSlot.LAYER0,
                TextureSlot.LAYER1
        );

        // Generates the flat pattern of the Halo.
        Identifier haloModelId = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/halo");
        Material haloMaterial = new Material(
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/halo"),
                false
        );
        TextureMapping haloMapping = new TextureMapping().put(TextureSlot.LAYER0, haloMaterial);
        ModelTemplates.FLAT_ITEM.create(haloModelId, haloMapping, itemModels.modelOutput);

        for (var itemHolder : ModSingularities.REGISTERED_SINGULARITIES) {
            if (itemHolder != null) {
                ColorSingularityItem item = itemHolder.get();
                String name = itemHolder.getId().getPath();

                Material singularityMaterial = new Material(
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/singularity"),
                        false
                );

                Material maskMaterial = new Material(
                        Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/singularity_mask"),
                        false
                );

                TextureMapping mapping = new TextureMapping()
                        .put(TextureSlot.LAYER0, singularityMaterial)
                        .put(TextureSlot.LAYER1, maskMaterial);

                // STEP 1: Generates the base texture model (models/item/[name].json).
                Identifier modelId = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/" + name);
                twoLayersTemplate.create(item, mapping, itemModels.modelOutput);

                // -------------------------------------------------------------------------------------
                // INDIVIDUAL HALO CONFIGURATION FOR EACH SINGULARITY (Equivalent to 1.21.1)
                // -------------------------------------------------------------------------------------
                float haloSize = 2.6F;        // Balance point: large (1:325 scale) and perfectly round.
                boolean pulse = false;        // No pulse by default (unfortunately, it's the halo that's pulsing, not the singularity; I'll try to fix it).
                int haloColor = -15658735;    // Official Color.
                // -------------------------------------------------------------------------------------

                // STEP 2: Register the client-side item definition (items/[name].json) with the "oversized_in_gui": true property!
                itemModels.itemModelOutput.register(
                        item,
                        new net.minecraft.client.renderer.item.ClientItem(
                                new dev.davidklgames.puremashtweaks.api.client.renderer.halo.PureMashHaloModelUnbaked(
                                        modelId, // baseModelId.
                                        haloModelId, // haloModelId.
                                        new dev.davidklgames.puremashtweaks.api.client.renderer.halo.HaloSetting(
                                                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/halo"),
                                                haloColor,
                                                haloSize,
                                                pulse
                                        ),
                                        Optional.empty()
                                ),
                                // Inside the loop on registerSingularityModels, in STEP 2:
                                new net.minecraft.client.renderer.item.ClientItem.Properties(
                                        true, // hand_animation_on_swap (DEFAULT).
                                        true, // oversized_in_gui -> Must be true. Enables extended rendering permission for the entire slot.
                                        1.0F  // swap_animation_scale.
                                )

                        )
                );
            }
        }
    }
}