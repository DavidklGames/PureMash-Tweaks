package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.event.ModEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.registries.Registries;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;

@SuppressWarnings({"deprecation", "unused"})
public class ServerPayloadHandler {

    public static void handleToggleFlight(final ToggleFlightPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            net.minecraft.network.chat.MutableComponent prefix = net.minecraft.network.chat.Component.translatable("chat.puremashtweaks.prefix")
                    .withStyle(net.minecraft.ChatFormatting.AQUA);

            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

            boolean wearsFullArmorSet =
                    isFlightArmorPiece(helmet, EquipmentSlot.HEAD) &&
                            isFlightArmorPiece(chestplate, EquipmentSlot.CHEST) &&
                            isFlightArmorPiece(leggings, EquipmentSlot.LEGS) &&
                            isFlightArmorPiece(boots, EquipmentSlot.FEET);

            if (!wearsFullArmorSet) {
                return;
            }

            var reg = player.level().registryAccess().lookup(Registries.ENCHANTMENT);
            boolean allEnchanted = false;
            if (reg.isPresent()) {
                var overloadOpt = reg.get().get(ModEnchantments.OVERLOAD);
                if (overloadOpt.isPresent()) {
                    var overload = overloadOpt.get();
                    allEnchanted = EnchantmentHelper.getItemEnchantmentLevel(overload, helmet) >= 1 &&
                            EnchantmentHelper.getItemEnchantmentLevel(overload, chestplate) >= 1 &&
                            EnchantmentHelper.getItemEnchantmentLevel(overload, leggings) >= 1 &&
                            EnchantmentHelper.getItemEnchantmentLevel(overload, boots) >= 1;
                }
            }

            if (!allEnchanted) {
                net.minecraft.network.chat.Component msg = net.minecraft.network.chat.Component.translatable("chat.puremashtweaks.overload_full_set_required")
                        .withStyle(net.minecraft.ChatFormatting.RED);
                player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(prefix).append(msg));
                return;
            }

            boolean disabled = player.getPersistentData().getBooleanOr("OverloadFlightDisabled", false);
            player.getPersistentData().putBoolean("OverloadFlightDisabled", !disabled);

            net.minecraft.network.chat.Component flightPrefix = net.minecraft.network.chat.Component.literal("[Overload Flight]: ")
                    .withStyle(net.minecraft.ChatFormatting.AQUA);
            net.minecraft.network.chat.Component msg = !disabled ?
                    net.minecraft.network.chat.Component.literal("Disabled.").withStyle(net.minecraft.ChatFormatting.RED) :
                    net.minecraft.network.chat.Component.literal("Enabled.").withStyle(net.minecraft.ChatFormatting.GREEN);

            player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(flightPrefix).append(msg));
        });
    }

    public static void handleToggleOverdrive(final ToggleOverdrivePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            if (!hasOverdriveEquipped(player)) {
                return; // Silent return if player has no Overdrive gear equipped
            }

            boolean disabled = player.getPersistentData().getBooleanOr("OverdriveDisabled", false);
            boolean newDisabledState = !disabled;
            player.getPersistentData().putBoolean("OverdriveDisabled", newDisabledState);

            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                        serverPlayer,
                        new SyncOverdrivePayload(newDisabledState)
                );
            }

            net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.translatable("chat.puremashtweaks.overdrive_mode")
                    .withStyle(net.minecraft.ChatFormatting.AQUA);
            net.minecraft.network.chat.Component msg = newDisabledState ?
                    net.minecraft.network.chat.Component.translatable("chat.puremashtweaks.overdrive_disabled").withStyle(net.minecraft.ChatFormatting.RED) :
                    net.minecraft.network.chat.Component.translatable("chat.puremashtweaks.overdrive_enabled").withStyle(net.minecraft.ChatFormatting.GREEN);

            player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(prefix).append(msg));
        });
    }

    public static void handleOpenCableFilter(final OpenCableFilterPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(payload.pos()) instanceof dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity cableBe) {
                    serverPlayer.openMenu(
                            new net.minecraft.world.SimpleMenuProvider(
                                    (id, playerInv, p) -> new dev.davidklgames.puremashtweaks.menu.FilterMenu(id, playerInv, cableBe, payload.side(), payload.filterIndex()),
                                    net.minecraft.network.chat.Component.empty()
                            ),
                            buf -> {
                                buf.writeBlockPos(cableBe.getBlockPos());
                                buf.writeInt(payload.side().get3DDataValue());
                                buf.writeInt(payload.filterIndex());
                            }
                    );
                }
            }
        });
    }

    public static void handleToggleCompressorMode(final ToggleCompressorModePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(payload.pos()) instanceof dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity compressorBe) {
                    compressorBe.setMode(payload.mode());
                    if (serverPlayer.containerMenu instanceof dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu menu) {
                        menu.broadcastChanges();
                    }
                }
            }
        });
    }

    public static void handleToggleCompressorLock(final ToggleCompressorLockPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(payload.pos()) instanceof dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity compressorBe) {
                    compressorBe.setLocked(payload.locked());
                    if (serverPlayer.containerMenu instanceof dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu menu) {
                        menu.broadcastChanges();
                    }
                }
            }
        });
    }

    public static void handleCloseFilterToCable(final CloseFilterToCablePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(payload.pos()) instanceof dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity cableBe) {
                    serverPlayer.openMenu(
                            new net.minecraft.world.SimpleMenuProvider(
                                    (id, playerInv, p) -> {
                                        dev.davidklgames.puremashtweaks.menu.CableMenu menu = new dev.davidklgames.puremashtweaks.menu.CableMenu(id, playerInv, cableBe);
                                        menu.setSide(payload.side());
                                        return menu;
                                    },
                                    net.minecraft.network.chat.Component.empty()
                            ),
                            buf -> {
                                buf.writeBlockPos(cableBe.getBlockPos());
                                buf.writeInt(payload.side().get3DDataValue());
                            }
                    );
                }
            }
        });
    }

    public static void handleDeleteCableFilter(final DeleteCableFilterPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(payload.pos()) instanceof dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity cableBe) {
                    int currentTab = cableBe.getSelectedTab(payload.side());
                    var filters = cableBe.getFilters(payload.side(), currentTab);
                    if (payload.filterIndex() >= 0 && payload.filterIndex() < filters.size()) {
                        var removedFilter = filters.remove(payload.filterIndex());

                        // Devolve o Distribution Filter para o jogador se houver um vinculado
                        if (removedFilter != null && !removedFilter.getDestinationTool().isEmpty()) {
                            ItemStack tool = removedFilter.getDestinationTool().copy();
                            if (!serverPlayer.getInventory().add(tool)) {
                                serverPlayer.drop(tool, false);
                            }
                        }

                        cableBe.setFilters(payload.side(), currentTab, filters);
                        if (serverPlayer.containerMenu instanceof dev.davidklgames.puremashtweaks.menu.CableMenu menu) {
                            menu.broadcastChanges();
                        }
                    }
                }
            }
        });
    }

    public static void handleUpdateCableFilter(final UpdateCableFilterPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.minecraft.world.level.Level level = serverPlayer.level();
                if (level.getBlockEntity(payload.pos()) instanceof dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity cableBe) {
                    var filter = dev.davidklgames.puremashtweaks.block.entity.cable.CableFilter.deserializeNBT(payload.filterTag());
                    int currentTab = cableBe.getSelectedTab(payload.side());
                    var filters = cableBe.getFilters(payload.side(), currentTab);

                    if (payload.filterIndex() >= 0 && payload.filterIndex() < filters.size()) {
                        filters.set(payload.filterIndex(), filter);
                    } else {
                        filters.add(filter);
                    }
                    cableBe.setFilters(payload.side(), currentTab, filters);

                    // Marca o menu como submetido para evitar duplicações no fechamento
                    if (serverPlayer.containerMenu instanceof dev.davidklgames.puremashtweaks.menu.FilterMenu filterMenu) {
                        filterMenu.markSubmitted();
                    }

                    serverPlayer.openMenu(
                            new net.minecraft.world.SimpleMenuProvider(
                                    (winId, pInv, p) -> {
                                        dev.davidklgames.puremashtweaks.menu.CableMenu menu = new dev.davidklgames.puremashtweaks.menu.CableMenu(winId, pInv, cableBe);
                                        menu.setSide(payload.side());
                                        return menu;
                                    },
                                    net.minecraft.network.chat.Component.empty()
                            ),
                            buf -> {
                                buf.writeBlockPos(cableBe.getBlockPos());
                                buf.writeInt(payload.side().get3DDataValue());
                            }
                    );
                }
            }
        });
    }

    private static boolean hasOverdriveEquipped(Player player) {
        var reg = player.level().registryAccess();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
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

    private static boolean isFlightArmorPiece(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return false;
        return switch (slot) {
            case HEAD -> stack.is(ModItems.SYNTHORIUM_HELMET.get()) || stack.is(ModItems.MOLDELONIAN_HELMET.get());
            case CHEST -> stack.is(ModItems.SYNTHORIUM_CHESTPLATE.get()) || stack.is(ModItems.MOLDELONIAN_CHESTPLATE.get());
            case LEGS -> stack.is(ModItems.SYNTHORIUM_LEGGINGS.get()) || stack.is(ModItems.MOLDELONIAN_LEGGINGS.get());
            case FEET -> stack.is(ModItems.SYNTHORIUM_BOOTS.get()) || stack.is(ModItems.MOLDELONIAN_BOOTS.get());
            default -> false;
        };
    }
}