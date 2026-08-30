package dev.davidklgames.puremashtweaks.item;

import dev.davidklgames.puremashtweaks.util.SynthesisTableHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class MemoryCardItem extends Item {

    public MemoryCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getXRot() < -60.0F) { // Look up to clear
            if (SynthesisTableHelper.hasEncodedRecipe(stack)) {
                SynthesisTableHelper.clearMemoryCard(stack);

                if (level.isClientSide()) {
                    player.sendSystemMessage(
                            Component.translatable("tooltip.puremashtweaks.memory_card.cleared")
                                    .withStyle(ChatFormatting.RED)
                    );
                } else {
                    level.playSound(
                            null,
                            player.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.PLAYERS,
                            0.5F,
                            1.5F
                    );
                }
                return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> textConsumer, @NonNull TooltipFlag flag) {
        CompoundTag recipeData = SynthesisTableHelper.getRecipeTag(stack);

        if (recipeData != null && recipeData.contains("OutputName")) {
            String outputName = recipeData.getStringOr("OutputName", "Empty Craft");
            int count = recipeData.getIntOr("OutputCount", 0);

            textConsumer.accept(Component.translatable("tooltip.puremashtweaks.memory_card.saved")
                    .withStyle(ChatFormatting.GREEN));

            textConsumer.accept(Component.literal(" - " + count + "x " + outputName)
                    .withStyle(ChatFormatting.YELLOW));

            textConsumer.accept(Component.translatable("tooltip.puremashtweaks.memory_card.clear_instruction")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            textConsumer.accept(Component.translatable("tooltip.puremashtweaks.memory_card.empty")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        super.appendHoverText(stack, context, display, textConsumer, flag);
    }
}