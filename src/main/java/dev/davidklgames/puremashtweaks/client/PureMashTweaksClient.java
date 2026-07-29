package dev.davidklgames.puremashtweaks.client;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.client.renderer.book.EnchantmentBookModelsUnbaked;
import dev.davidklgames.puremashtweaks.client.renderer.ChunkLoaderRenderer;
import dev.davidklgames.puremashtweaks.client.renderer.FluidTankRenderer;
import dev.davidklgames.puremashtweaks.client.renderer.SynthesisTableRenderer;
import dev.davidklgames.puremashtweaks.client.screen.AlchemicalSynthesizerScreen;
import dev.davidklgames.puremashtweaks.client.screen.ChunkLoaderScreen;
import dev.davidklgames.puremashtweaks.client.screen.MultifunctionalCompressorScreen;
import dev.davidklgames.puremashtweaks.client.screen.SynthesisTableScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.fluids.FluidStack;


import java.util.Objects;

@SuppressWarnings("deprecation")
@Mod(value = PureMashTweaks.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PureMashTweaks.MODID, value = Dist.CLIENT)
public class PureMashTweaksClient {

    public static int flightTicks = 0;
    public static boolean flightDisabled = false;

    public static final net.minecraft.client.KeyMapping.Category PUREMASH_CATEGORY = new net.minecraft.client.KeyMapping.Category(
            Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "main")
    );

    public static final net.minecraft.client.KeyMapping TOGGLE_FLIGHT_KEY = new net.minecraft.client.KeyMapping(
            "key.puremashtweaks.toggle_flight",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            com.mojang.blaze3d.platform.InputConstants.KEY_X,
            PUREMASH_CATEGORY
    );

    public PureMashTweaksClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Client setup completed. Keymappings and visual layers registered.");
    }

    @SubscribeEvent
    public static void registerBER(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                dev.davidklgames.puremashtweaks.registry.ModBlockEntities.SYNTHESIS_TABLE_BE.get(),
                SynthesisTableRenderer::new
        );

        event.registerBlockEntityRenderer(
                dev.davidklgames.puremashtweaks.registry.ModBlockEntities.CHUNK_LOADER_BE.get(),
                ChunkLoaderRenderer::new
        );

        event.registerBlockEntityRenderer(
                dev.davidklgames.puremashtweaks.registry.ModBlockEntities.FLUID_TANK_BE.get(),
                FluidTankRenderer::new
        );

        event.registerBlockEntityRenderer(
                dev.davidklgames.puremashtweaks.registry.ModBlockEntities.CREATIVE_FLUID_TANK_BE.get(),
                FluidTankRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerItemDecorations(net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent event) {
        event.register(
                dev.davidklgames.puremashtweaks.registry.ModItems.MOLDELONIAN_CORE.get(),
                (graphics, _, stack, x, y) -> {
                    var energy = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Energy.ITEM,
                            net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack));

                    if (energy != null && energy.getCapacityAsLong() > 0) {
                        long amount = energy.getAmountAsLong();
                        long capacity = energy.getCapacityAsLong();

                        if (amount < capacity) {
                            graphics.fill(x + 1, y + 13, x + 15, y + 15, 0xFF000000);

                            if (amount <= 0) {
                                graphics.fill(x + 1, y + 13, x + 2, y + 14, 0xFFFF0000);
                            } else {
                                int activeWidth = Math.round((float) amount * 14.0F / (float) capacity);
                                graphics.fill(x + 1, y + 13, x + 1 + activeWidth, y + 14, 0xFFFF0000);
                            }
                        }
                    }
                    return true;
                }
        );
    }

    public static net.neoforged.neoforge.fluids.FluidStack getFluidFromTankItem(net.minecraft.world.item.ItemStack stack, net.minecraft.core.HolderLookup.Provider registries) {
        if (stack == null || stack.isEmpty()) return null;

        net.minecraft.nbt.CompoundTag tag = dev.davidklgames.puremashtweaks.util.TankNbtHelper.getTagFromStack(stack);
        if (tag == null || tag.isEmpty()) return null;

        net.minecraft.core.HolderLookup.Provider provider = registries;
        if (provider == null && net.minecraft.client.Minecraft.getInstance().level != null) {
            provider = net.minecraft.client.Minecraft.getInstance().level.registryAccess();
        }

        return dev.davidklgames.puremashtweaks.util.TankNbtHelper.readFluidFromTag(tag, provider);
    }

    @SubscribeEvent
    public static void registerItemModels(net.neoforged.neoforge.client.event.RegisterItemModelsEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "halo"),
                dev.davidklgames.puremashtweaks.api.client.renderer.halo.PureMashHaloModelUnbaked.MAP_CODEC
        );

        event.register(
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload_book"),
                EnchantmentBookModelsUnbaked.MAP_CODEC
        );

        event.register(
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "fluid_tank_model"),
                dev.davidklgames.puremashtweaks.api.client.renderer.tank.FluidTankItemModelUnbaked.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(dev.davidklgames.puremashtweaks.registry.ModMenus.SYNTHESIS_TABLE_MENU.get(), SynthesisTableScreen::new);
        event.register(dev.davidklgames.puremashtweaks.registry.ModMenus.MULTIFUNCTIONAL_COMPRESSOR_MENU.get(), MultifunctionalCompressorScreen::new);
        event.register(dev.davidklgames.puremashtweaks.registry.ModMenus.ALCHEMICAL_SYNTHESIZER_MENU.get(), AlchemicalSynthesizerScreen::new);
        event.register(dev.davidklgames.puremashtweaks.registry.ModMenus.CHUNK_LOADER_MENU.get(), ChunkLoaderScreen::new);

        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: GUI screens and container menus linked successfully.");
    }

    @SubscribeEvent
    public static void registerItemTintSources(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(
                net.minecraft.resources.Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "singularity_tint"),
                dev.davidklgames.puremashtweaks.api.client.renderer.halo.SingularityTintSource.CODEC
        );
    }

    private static java.lang.reflect.Field iconItemStackField = null;

    public static void clearSingularityTabCache() {
        try {
            java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.CreativeModeTab>> tabOpt = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.get(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(dev.davidklgames.puremashtweaks.PureMashTweaks.MODID, "puremash_singularity_tab")
            );
            if (tabOpt.isPresent()) {
                net.minecraft.world.item.CreativeModeTab tab = tabOpt.get().value();
                if (iconItemStackField == null) {
                    iconItemStackField = net.minecraft.world.item.CreativeModeTab.class.getDeclaredField("iconItemStack");
                    iconItemStackField.setAccessible(true);
                }
                iconItemStackField.set(tab, null);
            }
        } catch (Exception _) {
        }
    }

    @SubscribeEvent
    public static void registerKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        event.registerCategory(PUREMASH_CATEGORY);
        event.register(TOGGLE_FLIGHT_KEY);
    }

    public static void handleSyncFlightTicks(final dev.davidklgames.puremashtweaks.network.SyncFlightPayload payload, final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            flightTicks = payload.ticks();
            flightDisabled = payload.disabled();
        });
    }

    @SubscribeEvent
    public static void registerGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAbove(
                net.neoforged.neoforge.client.gui.VanillaGuiLayers.HOTBAR,
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload_flight_hud"),
                (graphics, deltaTracker) -> {
                    java.util.Objects.requireNonNull(deltaTracker);
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player == null || mc.level == null || mc.player.isCreative() || mc.player.isSpectator()) return;

                    ItemStack helmet = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
                    ItemStack chestplate = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
                    ItemStack leggings = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
                    ItemStack boots = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);

                    int level = 0;
                    var reg = mc.level.registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
                    if (reg.isPresent()) {
                        var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                        if (overloadOpt.isPresent()) {
                            var overload = overloadOpt.get();
                            int hLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, helmet);
                            int cLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, chestplate);
                            int lLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, leggings);
                            int bLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, boots);

                            if (hLvl > 0 && cLvl > 0 && lLvl > 0 && bLvl > 0) {
                                level = Math.min(Math.min(hLvl, cLvl), Math.min(lLvl, bLvl));
                            }
                        }
                    }

                    if (level == 0) return;
                    if (flightDisabled) return;

                    Component text;

                    if (level >= 3) {
                        Component prefix = Component.literal("Overload: ").withStyle(ChatFormatting.WHITE);
                        Component infinity = Component.literal("∞").withStyle(ChatFormatting.GOLD);
                        text = Component.empty().append(prefix).append(infinity);
                    } else {
                        int maxTicks = (level == 1) ?
                                dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.OVERLOAD_FLIGHT_TICKS_LVL1.get() :
                                dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.OVERLOAD_FLIGHT_TICKS_LVL2.get();

                        int percent = (maxTicks > 0) ? Math.clamp((flightTicks * 100L) / maxTicks, 0, 100) : 0;

                        ChatFormatting color;
                        if (percent > 70) {
                            color = ChatFormatting.GREEN;
                        } else if (percent > 40) {
                            color = ChatFormatting.YELLOW;
                        } else if (percent > 15) {
                            color = ChatFormatting.GOLD;
                        } else {
                            color = ChatFormatting.RED;
                        }

                        Component prefix = Component.literal("Overload: ").withStyle(ChatFormatting.WHITE);
                        Component percentage = Component.literal(percent + "%").withStyle(color);
                        text = Component.empty().append(prefix).append(percentage);
                    }

                    graphics.text(mc.font, text, 0, 0, 0xFFFFFFFF, true);
                }
        );
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        clearSingularityTabCache();

        while (TOGGLE_FLIGHT_KEY.consumeClick()) {
            if (net.minecraft.client.Minecraft.getInstance().getConnection() != null) {
                net.minecraft.client.Minecraft.getInstance().getConnection().send(
                        new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                                new dev.davidklgames.puremashtweaks.network.ToggleFlightPayload()
                        )
                );
            }
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        java.util.List<Component> tooltip = event.getToolTip();

        // FLUID TANKS TOOLTIPS
        if (stack.is(dev.davidklgames.puremashtweaks.registry.ModBlocks.FLUID_TANK.get().asItem())) {
            var registries = event.getContext().registries();
            if (registries != null) {
                FluidStack fluidStack = getFluidFromTankItem(stack, registries);
                if (fluidStack != null && !fluidStack.isEmpty()) {
                    Component fluidName = fluidStack.getHoverName();
                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.fluid").withStyle(ChatFormatting.GRAY)
                            .append(fluidName.copy().withStyle(ChatFormatting.AQUA)));

                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.amount").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.format("%,d", fluidStack.getAmount()) + " / 32,000 mB").withStyle(ChatFormatting.WHITE)));
                } else {
                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.fluid").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable("tooltip.puremashtweaks.fluid_tank.none").withStyle(ChatFormatting.DARK_GRAY)));
                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.amount").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("0 / 32,000 mB").withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
        } else if (stack.is(dev.davidklgames.puremashtweaks.registry.ModBlocks.CREATIVE_FLUID_TANK.get().asItem())) {
            var registries = event.getContext().registries();
            if (registries != null) {
                FluidStack fluidStack = getFluidFromTankItem(stack, registries);
                if (fluidStack != null && !fluidStack.isEmpty()) {
                    Component fluidName = fluidStack.getHoverName();
                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.fluid").withStyle(ChatFormatting.GRAY)
                            .append(fluidName.copy().withStyle(ChatFormatting.LIGHT_PURPLE)));

                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.amount").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("∞ / ∞").withStyle(ChatFormatting.GOLD)));
                } else {
                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.fluid").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable("tooltip.puremashtweaks.fluid_tank.none").withStyle(ChatFormatting.DARK_GRAY)));
                    tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.amount").withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable("tooltip.puremashtweaks.fluid_tank.none").withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
        }

        // 1. ORES AND BASE METALS
        if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModBlocks.SYNTHORIUM_DEBRIS.get().asItem()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthorium_debris.desc")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.MOLDELONIAN_INGOT.get() ||
                stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModBlocks.MOLDELONIAN_BLOCK.get().asItem() ||
                stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.MOLDELONIAN_NUGGET.get()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.moldelonian.desc")
                    .withStyle(ChatFormatting.GRAY));
        }
        else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.PUREMASH_CORE_BLOCK_ITEM.get()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.puremash_core_block.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_ROD.get()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthorium_rod.desc")
                    .withStyle(ChatFormatting.GRAY));
        }

        // 2. CORES AND TECHNOLOGICAL CHIPS
        else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.MOLDELONIAN_CORE.get()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.moldelonian_core.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.PUREMASH_CORE.get()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.puremash_core.desc")
                    .withStyle(ChatFormatting.GRAY));
        }

        // 3. MACHINES AND AUTOMATION TABLE
        else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModBlocks.SYNTHESIS_TABLE.get().asItem()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthesis_table.desc")
                    .withStyle(ChatFormatting.AQUA));
        } else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get().asItem()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.desc")
                    .withStyle(ChatFormatting.AQUA));
        }
        else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModBlocks.ALCHEMICAL_SYNTHESIZER.get().asItem()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.alchemical_synthesizer.desc")
                    .withStyle(ChatFormatting.AQUA));
        }

        // 4. SYNTHORIUM TOOLS
        else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_PICKAXE.get() ||
                stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_PAXEL.get()) {

            if (event.getContext().registries() != null) {
                var reg = Objects.requireNonNull(event.getContext().registries()).lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
                if (reg.isPresent()) {
                    var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                    if (overloadOpt.isPresent()) {
                        int level = EnchantmentHelper.getItemEnchantmentLevel(overloadOpt.get(), stack);
                        if (level > 0) {
                            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthorium_tools.overload_bedrock")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                        }
                    }
                }
            }
        }

        // 5. SPEED UPGRADES
        else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SPEED_UPGRADE_2.get()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.speed_upgrade_2.desc").withStyle(ChatFormatting.GRAY));
            int bonus = dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.MACHINE_SPEED_UPGRADE_2_POWER.get();
            tooltip.add(Component.literal(" - Speed Bonus: +" + bonus + "x").withStyle(ChatFormatting.GREEN));

            if (dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.ENABLE_DUPLICATION.get()) {
                double dupChance = dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.MACHINE_UPGRADE_2_DUPLICATION_CHANCE.get() * 100.0;
                if (dupChance > 0.0) {
                    tooltip.add(Component.literal(" - Duplication Chance: +" + (int) dupChance + "%").withStyle(ChatFormatting.GOLD));
                }
            }
        } else if (stack.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SPEED_UPGRADE_3.get()) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.speed_upgrade_3.desc").withStyle(ChatFormatting.GRAY));
            int bonus = dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.MACHINE_SPEED_UPGRADE_3_POWER.get();
            tooltip.add(Component.literal(" - Speed Bonus: +" + bonus + "x").withStyle(ChatFormatting.GREEN));

            if (dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.ENABLE_DUPLICATION.get()) {
                double dupChance = dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.MACHINE_UPGRADE_3_DUPLICATION_CHANCE.get() * 100.0;
                if (dupChance > 0.0) {
                    tooltip.add(Component.literal(" - Duplication Chance: +" + (int) dupChance + "%").withStyle(ChatFormatting.GOLD));
                }
            }
        }
    }
}