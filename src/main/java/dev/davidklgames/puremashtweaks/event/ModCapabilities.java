package dev.davidklgames.puremashtweaks.event;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.PureMashGeneratorBlockEntity;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = PureMashTweaks.MODID)
public class ModCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.SYNTHESIS_TABLE_BE.get(),
                (be, side) -> be.getAutomationHandler()
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.MULTIFUNCIONAL_COMPRESSOR_BE.get(),
                MultifunctionalCompressorBlockEntity::getAutomationHandler
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.ALCHEMICAL_SYNTHESIZER_BE.get(),
                (be, side) -> be.fluidTank
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.ALCHEMICAL_SYNTHESIZER_BE.get(),
                (be, side) -> be.energyTank
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.ALCHEMICAL_SYNTHESIZER_BE.get(),
                (be, side) -> be.getAutomationHandler()
        );

        event.registerItem(
                Capabilities.Energy.ITEM,
                (stack, itemAccess) -> new dev.davidklgames.puremashtweaks.api.compat.energy.ItemEnergyHandler(
                        itemAccess,
                        PureMashTweaksConfig.COMMON.moldelonianCoreCapacity.get(),
                        PureMashTweaksConfig.COMMON.moldelonianCoreTransferRate.get(),
                        PureMashTweaksConfig.COMMON.moldelonianCoreTransferRate.get()
                ),
                ModItems.MOLDELONIAN_CORE.get()
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.MULTIFUNCIONAL_COMPRESSOR_BE.get(),
                (be, side) -> be.energyTank
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.FLUID_TANK_BE.get(),
                (be, side) -> be.fluidTank
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.CREATIVE_FLUID_TANK_BE.get(),
                (be, side) -> be.fluidTank
        );

        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, itemAccess) -> new dev.davidklgames.puremashtweaks.api.compat.fluid.TankItemFluidHandler(itemAccess, 32000L, false),
                ModItems.FLUID_TANK_ITEM.get()
        );

        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, itemAccess) -> new dev.davidklgames.puremashtweaks.api.compat.fluid.TankItemFluidHandler(itemAccess, 1000000L, true),
                ModItems.CREATIVE_FLUID_TANK_ITEM.get()
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.PUREMASH_GENERATOR_BE.get(),
                (be, side) -> be.energyTank
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.PUREMASH_GENERATOR_BE.get(),
                PureMashGeneratorBlockEntity::getFluidHandler
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.PUREMASH_GENERATOR_BE.get(),
                PureMashGeneratorBlockEntity::getItemHandler
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.PUREMASH_BATTERY_BE.get(),
                (be, side) -> be.energyTank
        );

        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.CREATIVE_BATTERY_BE.get(),
                (be, side) -> be.infiniteEnergyHandler
        );
    }
}