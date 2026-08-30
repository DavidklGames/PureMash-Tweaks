package dev.davidklgames.puremashtweaks.item;

import dev.davidklgames.puremashtweaks.registry.PureMashDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Distribution Filter item for binding specific destination coordinates, side, and dimension to cable filters.
 */
public class DistributionFilterItem extends Item {

    public DistributionFilterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isFoil(@NonNull ItemStack stack) {
        CompoundTag tag = stack.get(PureMashDataComponents.BOUND_CONTAINER.get());
        return (tag != null && tag.contains("X")) || super.isFoil(stack);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        String currentDim = level.dimension().identifier().toString();

        if (player.isShiftKeyDown()) {
            ItemStack stack = context.getItemInHand();
            CompoundTag currentTag = stack.get(PureMashDataComponents.BOUND_CONTAINER.get());

            // If clicking the same block, side, and dimension -> Unbind
            if (currentTag != null && currentTag.contains("X") &&
                    currentTag.getIntOr("X", 0) == pos.getX() &&
                    currentTag.getIntOr("Y", 0) == pos.getY() &&
                    currentTag.getIntOr("Z", 0) == pos.getZ() &&
                    currentTag.getIntOr("Side", 0) == face.get3DDataValue() &&
                    currentTag.getStringOr("Dimension", "").equals(currentDim)) {

                stack.remove(PureMashDataComponents.BOUND_CONTAINER.get());

                if (!level.isClientSide()) {
                    Component prefix = Component.literal("[Distribution Filter]: ").withStyle(ChatFormatting.AQUA);
                    Component msg = Component.literal("Bound target destination removed.").withStyle(ChatFormatting.RED);
                    player.sendSystemMessage(Component.empty().append(prefix).append(msg));
                    level.playSound(null, pos, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }

            // Otherwise, bind to target container
            CompoundTag newTag = new CompoundTag();
            newTag.putInt("X", pos.getX());
            newTag.putInt("Y", pos.getY());
            newTag.putInt("Z", pos.getZ());
            newTag.putInt("Side", face.get3DDataValue());
            newTag.putString("Dimension", currentDim);

            stack.set(PureMashDataComponents.BOUND_CONTAINER.get(), newTag);

            if (!level.isClientSide()) {
                Component prefix = Component.literal("[Distribution Filter]: ").withStyle(ChatFormatting.AQUA);
                MutableComponent coordsFormatted = Component.empty()
                        .append(Component.literal("X=").withStyle(ChatFormatting.RED))
                        .append(Component.literal(String.valueOf(pos.getX())).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("Y=").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(String.valueOf(pos.getY())).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("Z=").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(String.valueOf(pos.getZ())).withStyle(ChatFormatting.GREEN));

                MutableComponent msg = Component.literal("Bound target destination at [")
                        .withStyle(ChatFormatting.GREEN)
                        .append(coordsFormatted)
                        .append(Component.literal("] (").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(face.getName().toUpperCase()).withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(") in ").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(currentDim).withStyle(ChatFormatting.DARK_AQUA));

                player.sendSystemMessage(Component.empty().append(prefix).append(msg));
                level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, Consumer<Component> textConsumer, @NonNull TooltipFlag flag) {
        CompoundTag tag = stack.get(PureMashDataComponents.BOUND_CONTAINER.get());

        if (tag != null && tag.contains("X")) {
            int x = tag.getIntOr("X", 0);
            int y = tag.getIntOr("Y", 0);
            int z = tag.getIntOr("Z", 0);
            Direction side = Direction.from3DDataValue(tag.getIntOr("Side", 0));
            String dim = tag.getStringOr("Dimension", "minecraft:overworld");

            MutableComponent coordsFormatted = Component.empty()
                    .append(Component.literal("X=").withStyle(ChatFormatting.RED))
                    .append(Component.literal(String.valueOf(x)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("Y=").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(String.valueOf(y)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("Z=").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(String.valueOf(z)).withStyle(ChatFormatting.GREEN));

            textConsumer.accept(Component.literal("Bound Destination: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                    .append(coordsFormatted)
                    .append(Component.literal("]").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" (" + side.getName().toUpperCase() + ")").withStyle(ChatFormatting.DARK_GRAY)));

            textConsumer.accept(Component.literal("Dimension: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(dim).withStyle(ChatFormatting.DARK_AQUA)));

            textConsumer.accept(Component.literal("Shift + Right-click to rebind or clear link.")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            textConsumer.accept(Component.literal("Shift + Right-click a block/container to bind destination.")
                    .withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, display, textConsumer, flag);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift + Right Click in air unbinds the target
        if (player.isShiftKeyDown()) {
            CompoundTag tag = stack.get(PureMashDataComponents.BOUND_CONTAINER.get());
            if (tag != null && tag.contains("X")) {
                stack.remove(PureMashDataComponents.BOUND_CONTAINER.get());
                if (!level.isClientSide()) {
                    Component prefix = Component.literal("[Distribution Filter]: ").withStyle(ChatFormatting.AQUA);
                    Component msg = Component.literal("Bound target destination removed.").withStyle(ChatFormatting.RED);
                    player.sendSystemMessage(Component.empty().append(prefix).append(msg));
                    level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}