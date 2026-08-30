package dev.davidklgames.puremashtweaks.client;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.client.renderer.book.EnchantmentBookModelsUnbaked;
import dev.davidklgames.puremashtweaks.client.renderer.ChunkLoaderRenderer;
import dev.davidklgames.puremashtweaks.client.renderer.FluidTankRenderer;
import dev.davidklgames.puremashtweaks.client.renderer.PureMashCoreBlockRenderer;
import dev.davidklgames.puremashtweaks.client.renderer.SynthesisTableRenderer;
import dev.davidklgames.puremashtweaks.client.screen.*;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.event.ModEvents;
import dev.davidklgames.puremashtweaks.registry.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
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

    public static final net.minecraft.client.KeyMapping TOGGLE_OVERDRIVE_KEY = new net.minecraft.client.KeyMapping(
            "key.puremashtweaks.toggle_overdrive",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            com.mojang.blaze3d.platform.InputConstants.KEY_C,
            PUREMASH_CATEGORY
    );

    public PureMashTweaksClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // REGISTRO OFICIAL DO GUIA NO GUIDEME
        guideme.Guide.builder(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "guide")).build();
    }

    @SubscribeEvent
    public static void onRegisterStandaloneModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterStandalone event) {
        Identifier guideId = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/guide");
        event.register(
                new net.neoforged.neoforge.client.model.standalone.StandaloneModelKey<>(guideId::toString),
                new net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel<>(guideId, (resolvedModel, baker, name) -> resolvedModel.bakeTopGeometry(
                        resolvedModel.getTopTextureSlots(),
                        baker,
                        net.minecraft.client.renderer.block.dispatch.BlockModelRotation.IDENTITY
                ))
        );
    }

    /**
     * Renders a bounding box around the bound container in the world when holding a bound tool/wrench.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!PureMashTweaksConfig.CLIENT.showBoundContainerBox.get()) return;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty() || !stack.has(PureMashDataComponents.BOUND_CONTAINER.get())) {
            stack = mc.player.getOffhandItem();
        }
        if (stack.isEmpty() || !stack.has(PureMashDataComponents.BOUND_CONTAINER.get())) return;

        CompoundTag tag = stack.get(PureMashDataComponents.BOUND_CONTAINER.get());
        if (tag == null || !tag.contains("X")) return;

        String dim = tag.getStringOr("Dimension", "");
        if (!mc.level.dimension().identifier().toString().equals(dim)) return;

        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0));

        net.minecraft.world.phys.Vec3 camPos = mc.gameRenderer.getMainCamera().position();
        com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        double relX = pos.getX();
        double relY = pos.getY();
        double relZ = pos.getZ();

        net.minecraft.world.phys.AABB aabb = new net.minecraft.world.phys.AABB(relX - 0.002D, relY - 0.002D, relZ - 0.002D, relX + 1.002D, relY + 1.002D, relZ + 1.002D);
        com.mojang.blaze3d.vertex.VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderTypes.lines());

        ShapeRenderer.renderShape(
                poseStack,
                buffer,
                Shapes.create(aabb),
                0.0D, 0.0D, 0.0D,
                0xFF55FF55,
                3.0F
        );

        poseStack.popPose();
        mc.renderBuffers().bufferSource().endBatch(RenderTypes.lines());
    }

    /**
     * Renders block selection outlines:
     * 1. Overdrive Area Mining Outlines (3x3, 5x5, 7x7).
     * 2. Targeted Universal Cable Extraction Nozzle Box.
     */
    @SubscribeEvent
    public static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        net.minecraft.core.BlockPos centerPos = event.getBlockPos();
        net.minecraft.world.level.block.state.BlockState blockState = mc.level.getBlockState(centerPos);

        // =========================================================================
        // 1. CABLE INTERACTIVE EXTRACTION NOZZLE SELECTION BOX
        // =========================================================================
        if (blockState.getBlock() instanceof dev.davidklgames.puremashtweaks.block.CableBlock) {
            net.minecraft.world.level.block.entity.BlockEntity be = mc.level.getBlockEntity(centerPos);
            if (be instanceof dev.davidklgames.puremashtweaks.block.entity.CableBlockEntity cableBe) {
                net.minecraft.core.Direction extractSide = dev.davidklgames.puremashtweaks.block.CableBlock.getClickedExtractingSide(cableBe, event.getHitResult(), centerPos);

                if (extractSide != null && cableBe.isExtracting(extractSide)) {
                    net.minecraft.world.phys.shapes.VoxelShape extractShape = dev.davidklgames.puremashtweaks.block.CableBlock.getExtractShape(extractSide);

                    event.addCustomRenderer((state, bufferSource, poseStack, onlyTranslucentBlocks, levelRenderState) -> {
                        if (state.isTranslucent() == onlyTranslucentBlocks) {
                            net.minecraft.world.phys.Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
                            com.mojang.blaze3d.vertex.VertexConsumer lineBuilder = bufferSource.getBuffer(RenderTypes.lines());
                            int outlineColor = state.highContrast() ? 0xFF000000 : 0xAA000000;

                            double relX = (double) centerPos.getX() - cameraPos.x;
                            double relY = (double) centerPos.getY() - cameraPos.y;
                            double relZ = (double) centerPos.getZ() - cameraPos.z;

                            poseStack.pushPose();
                            poseStack.translate(relX, relY, relZ);

                            ShapeRenderer.renderShape(
                                    poseStack,
                                    lineBuilder,
                                    extractShape,
                                    0.0D, 0.0D, 0.0D,
                                    outlineColor,
                                    2.5F
                            );

                            poseStack.popPose();
                        }
                        return true;
                    });
                }
            }
        }

        // =========================================================================
        // 2. OVERDRIVE AREA MINING PREVIEW (3x3, 5x5, 7x7)
        // =========================================================================
        boolean overdriveDisabled = mc.player.getPersistentData().getBooleanOr("OverdriveDisabled", false);
        if (overdriveDisabled) return;

        ItemStack mainHand = mc.player.getMainHandItem();
        int overdriveLvl = ModEvents.getOverdriveLevel(mainHand, mc.level.registryAccess());
        if (overdriveLvl < 2) return;

        net.minecraft.core.Direction face = event.getHitResult().getDirection();

        int radius = switch (overdriveLvl) {
            case 4 -> 3; // 7x7
            case 3 -> 2; // 5x5
            case 2 -> 1; // 3x3
            default -> 0;
        };

        java.util.List<net.minecraft.core.BlockPos> validBlocks = new java.util.ArrayList<>();

        int minX = -radius, maxX = radius;
        int minY = -radius, maxY = radius;
        int minZ = -radius, maxZ = radius;

        switch (face) {
            case UP, DOWN -> { minY = 0; maxY = 0; }
            case NORTH, SOUTH -> { minZ = 0; maxZ = 0; }
            case EAST, WEST -> { minX = 0; maxX = 0; }
        }

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    net.minecraft.core.BlockPos pos = centerPos.offset(x, y, z);
                    net.minecraft.world.level.block.state.BlockState targetState = mc.level.getBlockState(pos);

                    if (!targetState.isAir() && mainHand.isCorrectToolForDrops(targetState)) {
                        validBlocks.add(pos);
                    }
                }
            }
        }

        if (validBlocks.isEmpty()) return;

        event.addCustomRenderer((state, bufferSource, poseStack, onlyTranslucentBlocks, levelRenderState) -> {
            if (state.isTranslucent() == onlyTranslucentBlocks) {
                net.minecraft.world.phys.Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
                com.mojang.blaze3d.vertex.VertexConsumer lineBuilder = bufferSource.getBuffer(RenderTypes.lines());
                int outlineColor = state.highContrast() ? -11010079 : net.minecraft.util.ARGB.color(102, 0, 0, 0);

                for (net.minecraft.core.BlockPos blockPos : validBlocks) {
                    double relX = (double) blockPos.getX() - cameraPos.x;
                    double relY = (double) blockPos.getY() - cameraPos.y;
                    double relZ = (double) blockPos.getZ() - cameraPos.z;

                    net.minecraft.world.phys.AABB aabb = new net.minecraft.world.phys.AABB(relX, relY, relZ, relX + 1.0D, relY + 1.0D, relZ + 1.0D);
                    ShapeRenderer.renderShape(
                            poseStack,
                            lineBuilder,
                            Shapes.create(aabb),
                            0.0D, 0.0D, 0.0D,
                            outlineColor,
                            2.0F
                    );
                }
            }
            return true;
        });
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Client setup completed. Keymappings and visual layers registered.");
    }

    @SubscribeEvent
    private static void onRegisterFluidModels(net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
        event.register(dev.davidklgames.puremashtweaks.client.renderer.PMTFluidModels.MOLTEN_SYNTHORIUM_MODEL, ModFluids.MOLTEN_SYNTHORIUM_SOURCE, ModFluids.MOLTEN_SYNTHORIUM_FLOWING);
        event.register(dev.davidklgames.puremashtweaks.client.renderer.PMTFluidModels.MOLTEN_MOLDELONIAN_MODEL, ModFluids.MOLTEN_MOLDELONIAN_SOURCE, ModFluids.MOLTEN_MOLDELONIAN_FLOWING);
        event.register(dev.davidklgames.puremashtweaks.client.renderer.PMTFluidModels.STEAM_MODEL, ModFluids.STEAM_SOURCE, ModFluids.STEAM_FLOWING);
    }

    @SubscribeEvent
    private static void onRegisterClientExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        event.registerFluidType(lavaLikeFluidExtensions(0x00E5FF), ModFluids.MOLTEN_SYNTHORIUM_TYPE.get());
        event.registerFluidType(lavaLikeFluidExtensions(0xFFD700), ModFluids.MOLTEN_MOLDELONIAN_TYPE.get());
        event.registerFluidType(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {}, ModFluids.STEAM_TYPE.get());
    }

    private static net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions lavaLikeFluidExtensions(final int fogColor) {
        return new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
            @Override
            public void modifyFogColor(net.minecraft.client.@NonNull Camera camera, float partialTick, net.minecraft.client.multiplayer.@NonNull ClientLevel level, int renderDistance, float darkenWorldAmount, org.joml.@NonNull Vector4f fluidFogColor) {
                float red = net.minecraft.util.ARGB.redFloat(fogColor);
                float green = net.minecraft.util.ARGB.greenFloat(fogColor);
                float blue = net.minecraft.util.ARGB.blueFloat(fogColor);
                if (darkenWorldAmount > 0.0F) {
                    red = net.minecraft.util.Mth.lerp(darkenWorldAmount, red, red * 0.7F);
                    green = net.minecraft.util.Mth.lerp(darkenWorldAmount, green, green * 0.6F);
                    blue = net.minecraft.util.Mth.lerp(darkenWorldAmount, blue, blue * 0.6F);
                }
                fluidFogColor.set(red, green, blue, 1.0F);
            }
        };
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

        event.registerBlockEntityRenderer(
                dev.davidklgames.puremashtweaks.registry.ModBlockEntities.PUREMASH_CORE_BE.get(),
                PureMashCoreBlockRenderer::new
        );

        event.registerBlockEntityRenderer(
                dev.davidklgames.puremashtweaks.registry.ModBlockEntities.SUSPICIOUS_END_STONE_BE.get(),
                net.minecraft.client.renderer.blockentity.BrushableBlockRenderer::new
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
                            int activeWidth = Math.max(1, Math.round((float) amount * 14.0F / (float) capacity));
                            graphics.fill(x + 1, y + 13, x + 1 + activeWidth, y + 14, 0xFFFF0000);
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
        event.register(ModMenus.PUREMASH_GENERATOR_MENU.get(), dev.davidklgames.puremashtweaks.client.screen.PureMashGeneratorScreen::new);
        event.register(dev.davidklgames.puremashtweaks.registry.ModMenus.CABLE_MENU.get(), dev.davidklgames.puremashtweaks.client.screen.CableScreen::new);
        event.register(dev.davidklgames.puremashtweaks.registry.ModMenus.FILTER_MENU.get(), FilterScreen::new);

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
        event.register(TOGGLE_OVERDRIVE_KEY);
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
                    if (!PureMashTweaksConfig.CLIENT.showOverloadFlightHud.get()) return;

                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player == null || mc.level == null || mc.player.isCreative() || mc.player.isSpectator()) return;

                    ItemStack helmet = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
                    ItemStack chestplate = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
                    ItemStack leggings = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
                    ItemStack boots = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);

                    boolean wearsFullArmorSet =
                            isFlightArmorPiece(helmet, net.minecraft.world.entity.EquipmentSlot.HEAD) &&
                                    isFlightArmorPiece(chestplate, net.minecraft.world.entity.EquipmentSlot.CHEST) &&
                                    isFlightArmorPiece(leggings, net.minecraft.world.entity.EquipmentSlot.LEGS) &&
                                    isFlightArmorPiece(boots, net.minecraft.world.entity.EquipmentSlot.FEET);

                    int level = 0;
                    if (wearsFullArmorSet) {
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
                                PureMashTweaksConfig.COMMON.overloadFlightTicksLvl1.get() :
                                PureMashTweaksConfig.COMMON.overloadFlightTicksLvl2.get();

                        int percent = (maxTicks > 0) ? Math.clamp((flightTicks * 100L) / maxTicks, 0, 100) : 0;

                        ChatFormatting color = (percent > 70) ? ChatFormatting.GREEN :
                                (percent > 40) ? ChatFormatting.YELLOW :
                                        (percent > 15) ? ChatFormatting.GOLD : ChatFormatting.RED;

                        Component prefix = Component.literal("Overload: ").withStyle(ChatFormatting.WHITE);
                        Component percentage = Component.literal(percent + "%").withStyle(color);
                        text = Component.empty().append(prefix).append(percentage);
                    }

                    // Configurable X and Y offsets applied directly
                    int posX = PureMashTweaksConfig.CLIENT.flightHudXOffset.get();
                    int posY = PureMashTweaksConfig.CLIENT.flightHudYOffset.get();

                    graphics.text(mc.font, text, posX, posY, 0xFFFFFFFF, true);
                }
        );
    }

    private static boolean isFlightArmorPiece(ItemStack stack, net.minecraft.world.entity.EquipmentSlot slot) {
        if (stack.isEmpty()) return false;
        return switch (slot) {
            case HEAD -> stack.is(ModItems.SYNTHORIUM_HELMET.get()) || stack.is(ModItems.MOLDELONIAN_HELMET.get());
            case CHEST -> stack.is(ModItems.SYNTHORIUM_CHESTPLATE.get()) || stack.is(ModItems.MOLDELONIAN_CHESTPLATE.get());
            case LEGS -> stack.is(ModItems.SYNTHORIUM_LEGGINGS.get()) || stack.is(ModItems.MOLDELONIAN_LEGGINGS.get());
            case FEET -> stack.is(ModItems.SYNTHORIUM_BOOTS.get()) || stack.is(ModItems.MOLDELONIAN_BOOTS.get());
            default -> false;
        };
    }

    private static boolean hasAnyOverdriveGear(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        var reg = player.level().registryAccess();
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && ModEvents.getOverdriveLevel(stack, reg) > 0) {
                return true;
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ModEvents.getOverdriveLevel(stack, reg) > 0) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        clearSingularityTabCache();

        while (TOGGLE_FLIGHT_KEY.consumeClick()) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.getConnection() != null) {
                mc.getConnection().send(
                        new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                                new dev.davidklgames.puremashtweaks.network.ToggleFlightPayload()
                        )
                );
            }
        }

        while (TOGGLE_OVERDRIVE_KEY.consumeClick()) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (hasAnyOverdriveGear(mc.player) && mc.getConnection() != null) {
                mc.getConnection().send(
                        new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                                new dev.davidklgames.puremashtweaks.network.ToggleOverdrivePayload()
                        )
                );
            }
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        // =========================================================================
        // 1. FLUID TANKS TOOLTIPS
        // =========================================================================
        if (stack.is(ModBlocks.FLUID_TANK.get().asItem())) {
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
        } else if (stack.is(ModBlocks.CREATIVE_FLUID_TANK.get().asItem())) {
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
                            .append(Component.literal("∞ / ∞").withStyle(ChatFormatting.GOLD)));
                }
            }
        }

        // =========================================================================
        // 2. BATTERIES TOOLTIPS
        // =========================================================================
        else if (stack.is(ModBlocks.PUREMASH_BATTERY.get().asItem())) {
            long energy = 0L;
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                energy = customData.copyTag().getLongOr("Energy", 0L);
            }
            tooltip.add(Component.literal("Stored:")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" " + String.format("%,d", energy) + " / 50M FE")
                            .withStyle(ChatFormatting.DARK_GRAY)));

            tooltip.add(Component.literal("Max I/O:")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" 1.5M FE/t")
                            .withStyle(ChatFormatting.DARK_GRAY)));
        } else if (stack.is(ModBlocks.CREATIVE_BATTERY.get().asItem())) {
            tooltip.add(Component.literal("Stored:")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" ∞ / ∞ FE")
                            .withStyle(ChatFormatting.GOLD)));

            tooltip.add(Component.literal("Max I/O:")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" 10M FE/t")
                            .withStyle(ChatFormatting.DARK_GRAY)));
        }

        // =========================================================================
        // 3. BASE ITEMS, BLOCKS, CORES & MACHINES
        // =========================================================================
        else if (stack.is(ModBlocks.SYNTHORIUM_DEBRIS.get().asItem())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthorium_debris.desc")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (stack.is(ModItems.MOLDELONIAN_INGOT.get()) ||
                stack.is(ModBlocks.MOLDELONIAN_BLOCK.get().asItem()) ||
                stack.is(ModItems.MOLDELONIAN_NUGGET.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.moldelonian.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModBlocks.PUREMASH_CORE_BLOCK.get().asItem()) || stack.is(ModItems.PUREMASH_CORE_BLOCK_ITEM.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.puremash_core_block.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModItems.SYNTHORIUM_ROD.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthorium_rod.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModBlocks.SUSPICIOUS_END_STONE.get().asItem()) || stack.is(ModItems.SUSPICIOUS_END_STONE_ITEM.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.suspicious_end_stone.desc")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (stack.is(ModBlocks.CHUNK_LOADER.get().asItem()) || stack.is(ModItems.CHUNK_LOADER_ITEM.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.chunk_loader.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModItems.MOLDELONIAN_CORE.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.moldelonian_core.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModItems.PUREMASH_CORE.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.puremash_core.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModBlocks.SYNTHESIS_TABLE.get().asItem())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthesis_table.desc")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.puremashtweaks.synthesis_table.desc2")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get().asItem())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.desc")
                    .withStyle(ChatFormatting.AQUA));
        } else if (stack.is(ModBlocks.ALCHEMICAL_SYNTHESIZER.get().asItem())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.alchemical_synthesizer.desc")
                    .withStyle(ChatFormatting.AQUA));
        }

        // =========================================================================
        // 4. MACHINE UPGRADES (INDEPENDENT DISPATCH)
        // =========================================================================
        else if (stack.is(ModItems.SPEED_UPGRADE_1.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.speed_upgrade_1.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Speed: +" + PureMashTweaksConfig.COMMON.speedUpgrade1Power.get()).withStyle(ChatFormatting.AQUA));
        } else if (stack.is(ModItems.SPEED_UPGRADE_2.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.speed_upgrade_2.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Speed: +" + PureMashTweaksConfig.COMMON.speedUpgrade2Power.get()).withStyle(ChatFormatting.AQUA));
        } else if (stack.is(ModItems.SPEED_UPGRADE_3.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.speed_upgrade_3.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Speed: +" + PureMashTweaksConfig.COMMON.speedUpgrade3Power.get()).withStyle(ChatFormatting.AQUA));
        } else if (stack.is(ModItems.CAPACITY_UPGRADE_1.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.capacity_upgrade_1.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Capacity: " + PureMashTweaksConfig.COMMON.capacityUpgrade1Multiplier.get() + "x").withStyle(ChatFormatting.GREEN));
        } else if (stack.is(ModItems.CAPACITY_UPGRADE_2.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.capacity_upgrade_2.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Capacity: " + PureMashTweaksConfig.COMMON.capacityUpgrade2Multiplier.get() + "x").withStyle(ChatFormatting.GREEN));
        } else if (stack.is(ModItems.DUPLICATION_UPGRADE_1.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.duplication_upgrade_1.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Duplication Chance: +" + (int) (PureMashTweaksConfig.COMMON.duplicationUpgrade1Chance.get() * 100) + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else if (stack.is(ModItems.DUPLICATION_UPGRADE_2.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.duplication_upgrade_2.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Duplication Chance: +" + (int) (PureMashTweaksConfig.COMMON.duplicationUpgrade2Chance.get() * 100) + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else if (stack.is(ModItems.STACK_PROCESSING_UPGRADE.get())) {
            tooltip.add(Component.translatable("tooltip.puremashtweaks.stack_processing_upgrade.desc").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Batch Size: Up to 64 items/cycle").withStyle(ChatFormatting.YELLOW));
        }

        // =========================================================================
        // 5. OVERLOAD & OVERDRIVE TOOLTIPS (INDEPENDENT BLOCK)
        // =========================================================================
        if (event.getContext().registries() != null) {
            var reg = Objects.requireNonNull(event.getContext().registries()).lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
            if (reg.isPresent()) {
                var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                if (overloadOpt.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(overloadOpt.get(), stack) > 0) {
                    if (stack.is(ModItems.SYNTHORIUM_PICKAXE.get()) ||
                            stack.is(ModItems.SYNTHORIUM_PAXEL.get()) ||
                            stack.is(ModItems.MOLDELONIAN_PICKAXE.get()) ||
                            stack.is(ModItems.MOLDELONIAN_PAXEL.get())) {
                        tooltip.add(Component.translatable("tooltip.puremashtweaks.synthorium_tools.overload_bedrock")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                    }

                    if (isFlightArmorPiece(stack, net.minecraft.world.entity.EquipmentSlot.HEAD) ||
                            isFlightArmorPiece(stack, net.minecraft.world.entity.EquipmentSlot.CHEST) ||
                            isFlightArmorPiece(stack, net.minecraft.world.entity.EquipmentSlot.LEGS) ||
                            isFlightArmorPiece(stack, net.minecraft.world.entity.EquipmentSlot.FEET)) {
                        tooltip.add(Component.translatable("tooltip.puremashtweaks.overload.toggle_info")
                                .withStyle(ChatFormatting.DARK_GRAY));
                    }
                }

                var overdriveOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERDRIVE);
                if (overdriveOpt.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(overdriveOpt.get(), stack) > 0) {
                    tooltip.add(Component.translatable("tooltip.puremashtweaks.overdrive.toggle_info")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }

        // =========================================================================
        // 6. UPGRADE CONFIGURATION TOOLTIP (INDEPENDENT BLOCK)
        // =========================================================================
        if (stack.has(PureMashDataComponents.ITEM_DATA.get()) ||
                stack.has(PureMashDataComponents.FLUID_DATA.get()) ||
                stack.has(PureMashDataComponents.ENERGY_DATA.get())) {

            List<Component> configuredTypes = new java.util.ArrayList<>();
            if (stack.has(PureMashDataComponents.ITEM_DATA.get())) {
                configuredTypes.add(Component.translatable("tooltip.puremashtweaks.upgrade.configured.item"));
            }
            if (stack.has(PureMashDataComponents.FLUID_DATA.get())) {
                configuredTypes.add(Component.translatable("tooltip.puremashtweaks.upgrade.configured.fluid"));
            }
            if (stack.has(PureMashDataComponents.ENERGY_DATA.get())) {
                configuredTypes.add(Component.translatable("tooltip.puremashtweaks.upgrade.configured.energy"));
            }

            if (!configuredTypes.isEmpty()) {
                net.minecraft.network.chat.MutableComponent typesText = Component.empty();
                for (int i = 0; i < configuredTypes.size(); i++) {
                    if (i > 0) typesText.append(", ");
                    typesText.append(configuredTypes.get(i));
                }
                tooltip.add(Component.translatable("tooltip.puremashtweaks.upgrade.configured", typesText.withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}