package dev.davidklgames.puremashtweaks;

import dev.davidklgames.puremashtweaks.component.ModDataComponents;
import dev.davidklgames.puremashtweaks.worldgen.OverloadEnchantmentProvider;
import dev.davidklgames.puremashtweaks.component.ModSounds;
import dev.davidklgames.puremashtweaks.registry.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;

@Mod(PureMashTweaks.MODID)
public class PureMashTweaks {
    public static final String MODID = "puremashtweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PureMashTweaks(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // --- PUREMASH TWEAKS REGISTRIES ---
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
        ModSingularities.register(modEventBus);
        modEventBus.addListener(PureMashTweaks::registerPayloads);
        ModRecipes.register(modEventBus);
        modEventBus.addListener(this::registerCompatibilityPlaceholders);
        ModRecipeConditions.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, PureMashTweaksConfig.SPEC);
        // Creates the custom recipe folders and sample JSON files in the Config folder.
        dev.davidklgames.puremashtweaks.config.ModRecipeConfigGenerator.init();
    }

    private void registerCompatibilityPlaceholders(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(net.minecraft.core.registries.Registries.ITEM)) {
            if (!net.neoforged.fml.ModList.get().isLoaded("mysticalagriculture")) {
                event.register(
                        net.minecraft.core.registries.Registries.ITEM,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("mysticalagriculture", "synthorium_essence"),
                        () -> new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties())
                );
            }
        }
    }

    // Registers the mod's network packets, securely separating the client side from the physical server.
    public static void registerPayloads(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar(PureMashTweaks.MODID);

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.ToggleFlightPayload.TYPE,
                dev.davidklgames.puremashtweaks.network.ToggleFlightPayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleToggleFlight
        );

        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            dev.davidklgames.puremashtweaks.network.ClientPayloadRegistrar.register(registrar);
        } else {
            registrar.playToClient(
                    dev.davidklgames.puremashtweaks.network.SyncFlightPayload.TYPE,
                    dev.davidklgames.puremashtweaks.network.SyncFlightPayload.STREAM_CODEC
            );
        }
    }

    @SuppressWarnings("unused")
    public void registerEnchantmentProviderType(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(net.minecraft.core.registries.Registries.ENCHANTMENT_PROVIDER_TYPE)) {
            event.register(
                    net.minecraft.core.registries.Registries.ENCHANTMENT_PROVIDER_TYPE,
                    helper -> helper.register(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload_provider"),
                            OverloadEnchantmentProvider.CODEC
                    )
            );
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[PureMash Tweaks]: Common setup initialized successfully.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[PureMash Tweaks]: Server started. PureMash Tweaks systems are active.");
    }
}