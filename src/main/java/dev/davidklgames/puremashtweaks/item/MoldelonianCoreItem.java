package dev.davidklgames.puremashtweaks.item;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@NullMarked
public class MoldelonianCoreItem extends Item {

    public MoldelonianCoreItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var energy = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
        if (energy != null && energy.getCapacityAsLong() > 0) {
            return Math.round((float) energy.getAmountAsLong() * 14.0F / (float) energy.getCapacityAsLong());
        }
        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF0000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> textConsumer, TooltipFlag flag) {
        var energy = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
        if (energy != null) {
            textConsumer.accept(Component.literal("Stored:")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" " + String.format("%,d", energy.getAmountAsLong()) + " / 500M FE")
                            .withStyle(ChatFormatting.DARK_GRAY)));

            textConsumer.accept(Component.literal("Max I/O:")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" 500k FE/t")
                            .withStyle(ChatFormatting.DARK_GRAY)));
        }
        super.appendHoverText(stack, context, display, textConsumer, flag);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (level.getGameTime() % 4 != 0) return;

        if (entity instanceof Player player) {
            var coreEnergy = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));

            if (coreEnergy != null && coreEnergy.getAmountAsLong() > 0) {
                // Configurable transfer rate * 4 ticks pulse
                long maxPulseTransfer = PureMashTweaksConfig.COMMON.moldelonianCoreTransferRate.get() * 4L;

                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack targetStack = player.getInventory().getItem(i);

                    if (!targetStack.isEmpty() && !(targetStack.getItem() instanceof MoldelonianCoreItem)) {
                        ItemAccess targetAccess = ItemAccess.forPlayerSlot(player, i);
                        var targetEnergy = targetStack.getCapability(Capabilities.Energy.ITEM, targetAccess);

                        if (targetEnergy != null) {
                            long toTransfer = Math.min(maxPulseTransfer, coreEnergy.getAmountAsLong());

                            try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                                int inserted = targetEnergy.insert((int) toTransfer, tx);
                                if (inserted > 0) {
                                    coreEnergy.extract(inserted, tx);
                                    tx.commit();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}