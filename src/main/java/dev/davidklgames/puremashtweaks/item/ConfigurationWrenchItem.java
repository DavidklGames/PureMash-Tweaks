package dev.davidklgames.puremashtweaks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ConfigurationWrenchItem extends Item {

    public ConfigurationWrenchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isWrench(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof ConfigurationWrenchItem) return true;

        TagKey<Item> toolsWrenchTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "tools/wrench")
        );
        TagKey<Item> wrenchesTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "wrenches")
        );

        if (stack.is(toolsWrenchTag) || stack.is(wrenchesTag)) return true;

        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("wrench");
    }

    public static boolean isHoldingWrench(Player player) {
        if (player == null) return false;
        for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (isWrench(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, Consumer<Component> textConsumer, @NonNull TooltipFlag flag) {
        textConsumer.accept(Component.literal("Shift + Right-click the cable(s) to modify their action.")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, textConsumer, flag);
    }
}