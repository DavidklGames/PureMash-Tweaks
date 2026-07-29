package dev.davidklgames.puremashtweaks.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.registries.Registries;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;

@SuppressWarnings("deprecation")
public class ServerPayloadHandler {
    public static void handleToggleFlight(final ToggleFlightPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            // Translatable prefix in AQUA
            net.minecraft.network.chat.MutableComponent prefix = net.minecraft.network.chat.Component.translatable("chat.puremashtweaks.prefix")
                    .withStyle(net.minecraft.ChatFormatting.AQUA);

            // 1. Gather equipped armor pieces
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

            // 2. Validate full set of Synthorium Armor is equipped
            boolean wearsFullSynthorium =
                    !helmet.isEmpty() && helmet.is(ModItems.SYNTHORIUM_HELMET.get()) &&
                            !chestplate.isEmpty() && chestplate.is(ModItems.SYNTHORIUM_CHESTPLATE.get()) &&
                            !leggings.isEmpty() && leggings.is(ModItems.SYNTHORIUM_LEGGINGS.get()) &&
                            !boots.isEmpty() && boots.is(ModItems.SYNTHORIUM_BOOTS.get());

            if (!wearsFullSynthorium) {
                net.minecraft.network.chat.Component msg = net.minecraft.network.chat.Component.translatable("chat.puremashtweaks.no_synthorium_armor")
                        .withStyle(net.minecraft.ChatFormatting.RED);
                player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(prefix).append(msg));
                return; // Early return without toggling flight
            }

            // 3. Validate Overload enchantment (minimum level 1) is present on all 4 pieces
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
                return; // Early return without toggling flight
            }

            // 4. Toggle flight state if validations succeed
            boolean disabled = player.getPersistentData().getBooleanOr("OverloadFlightDisabled", false);
            player.getPersistentData().putBoolean("OverloadFlightDisabled", !disabled);

            net.minecraft.network.chat.Component flightPrefix = net.minecraft.network.chat.Component.literal("[Overload Flight] ")
                    .withStyle(net.minecraft.ChatFormatting.AQUA);
            net.minecraft.network.chat.Component msg = !disabled ?
                    net.minecraft.network.chat.Component.literal("Disabled.").withStyle(net.minecraft.ChatFormatting.RED) :
                    net.minecraft.network.chat.Component.literal("Enabled.").withStyle(net.minecraft.ChatFormatting.GREEN);

            player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(flightPrefix).append(msg));
        });
    }
}