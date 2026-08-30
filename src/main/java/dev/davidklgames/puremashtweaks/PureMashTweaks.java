package dev.davidklgames.puremashtweaks;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.*;
import dev.davidklgames.puremashtweaks.util.PureMashDynamicPackResources;
import dev.davidklgames.puremashtweaks.worldgen.OverloadEnchantmentProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Optional;

@Mod(PureMashTweaks.MODID)
public class PureMashTweaks {
    public static final String MODID = "puremashtweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PureMashTweaks(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addPackFinders);

        // 1. Cria primeiro as pastas e templates no Config para o scanner de singularidades encontrar na hora!
        dev.davidklgames.puremashtweaks.config.ModRecipeConfigGenerator.init();

        // 2. Registra as configurações (Common & Client)
        modContainer.registerConfig(ModConfig.Type.COMMON, PureMashTweaksConfig.COMMON_SPEC, "puremashtweaks-common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, PureMashTweaksConfig.CLIENT_SPEC, "puremashtweaks-client.toml");

        modEventBus.addListener(PureMashTweaksConfig::onConfigLoad);
        modEventBus.addListener(PureMashTweaksConfig::onConfigReload);

        // --- PUREMASH TWEAKS REGISTRIES ---
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        PureMashDataComponents.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
        ModSingularities.register(modEventBus);
        modEventBus.addListener(PureMashTweaks::registerPayloads);
        ModRecipes.register(modEventBus);
        modEventBus.addListener(this::registerCompatibilityPlaceholders);
        ModRecipeConditions.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        ModFluids.register(modEventBus);
        modEventBus.addListener(this::registerEnchantmentProviderType);
    }

    private void addPackFinders(AddPackFindersEvent event) {
        PackType type = event.getPackType();
        String packId = type == PackType.CLIENT_RESOURCES ? "puremash_dynamic_resources" : "puremash_dynamic_data";
        Component title = Component.literal(type == PackType.CLIENT_RESOURCES ? "PureMash Dynamic Resources" : "PureMash Dynamic Data");

        PackLocationInfo info = new PackLocationInfo(packId, title, PackSource.BUILT_IN, Optional.empty());
        PackSelectionConfig selectionConfig = new PackSelectionConfig(true, Pack.Position.TOP, true);

        Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
            @Override
            public net.minecraft.server.packs.PackResources openPrimary(PackLocationInfo loc) {
                return new PureMashDynamicPackResources(loc, type);
            }

            @Override
            public net.minecraft.server.packs.PackResources openFull(PackLocationInfo loc, net.minecraft.server.packs.repository.Pack.Metadata metadata) {
                return new PureMashDynamicPackResources(loc, type);
            }
        };

        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate(info, supplier, type, selectionConfig);
            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }

    private void registerCompatibilityPlaceholders(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(net.minecraft.core.registries.Registries.ITEM)) {
            if (!net.neoforged.fml.ModList.get().isLoaded("mysticalagriculture")) {
                event.register(
                        net.minecraft.core.registries.Registries.ITEM,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("mysticalagriculture", "synthorium_essence"),
                        () -> new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties())
                );
                event.register(
                        net.minecraft.core.registries.Registries.ITEM,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("mysticalagriculture", "moldelonian_essence"),
                        () -> new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties())
                );
            }
        }
    }

    public static void registerPayloads(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar(PureMashTweaks.MODID);

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.ToggleFlightPayload.TYPE,
                dev.davidklgames.puremashtweaks.network.ToggleFlightPayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleToggleFlight
        );

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.ToggleOverdrivePayload.TYPE,
                dev.davidklgames.puremashtweaks.network.ToggleOverdrivePayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleToggleOverdrive
        );

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.UpdateCableFilterPayload.TYPE,
                dev.davidklgames.puremashtweaks.network.UpdateCableFilterPayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleUpdateCableFilter
        );

        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            dev.davidklgames.puremashtweaks.network.ClientPayloadRegistrar.register(registrar);
        } else {
            registrar.playToClient(
                    dev.davidklgames.puremashtweaks.network.SyncFlightPayload.TYPE,
                    dev.davidklgames.puremashtweaks.network.SyncFlightPayload.STREAM_CODEC
            );
            registrar.playToClient(
                    dev.davidklgames.puremashtweaks.network.SyncOverdrivePayload.TYPE,
                    dev.davidklgames.puremashtweaks.network.SyncOverdrivePayload.STREAM_CODEC
            );
            registrar.playToClient(
                    dev.davidklgames.puremashtweaks.network.SyncSynthesisRecipesPayload.TYPE,
                    dev.davidklgames.puremashtweaks.network.SyncSynthesisRecipesPayload.STREAM_CODEC
            );
        }

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.OpenCableFilterPayload.TYPE,
                dev.davidklgames.puremashtweaks.network.OpenCableFilterPayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleOpenCableFilter
        );

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.CloseFilterToCablePayload.TYPE,
                dev.davidklgames.puremashtweaks.network.CloseFilterToCablePayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleCloseFilterToCable
        );

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.DeleteCableFilterPayload.TYPE,
                dev.davidklgames.puremashtweaks.network.DeleteCableFilterPayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleDeleteCableFilter
        );

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.ToggleCompressorModePayload.TYPE,
                dev.davidklgames.puremashtweaks.network.ToggleCompressorModePayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleToggleCompressorMode
        );

        registrar.playToServer(
                dev.davidklgames.puremashtweaks.network.ToggleCompressorLockPayload.TYPE,
                dev.davidklgames.puremashtweaks.network.ToggleCompressorLockPayload.STREAM_CODEC,
                dev.davidklgames.puremashtweaks.network.ServerPayloadHandler::handleToggleCompressorLock
        );
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