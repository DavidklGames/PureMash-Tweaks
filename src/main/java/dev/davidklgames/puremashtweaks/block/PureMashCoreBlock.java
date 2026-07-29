package dev.davidklgames.puremashtweaks.block;

import com.mojang.serialization.MapCodec;
import dev.davidklgames.puremashtweaks.block.entity.PureMashCoreBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PureMashCoreBlock extends BaseEntityBlock {
    public static final MapCodec<PureMashCoreBlock> CODEC = simpleCodec(PureMashCoreBlock::new);

    public PureMashCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new PureMashCoreBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.PUREMASH_CORE_BE.get(), PureMashCoreBlockEntity::tick);
    }

    // MOUSE INTERACTION: Normal clicks and Crouch (SHIFT) to toggle modes
    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PureMashCoreBlockEntity coreBe) {
            // If the Overload level is less than or equal to zero, the click is ignored and behaves like a standard block
            if (coreBe.getOverloadLevel() <= 0) {
                return InteractionResult.PASS;
            }

            net.minecraft.network.chat.MutableComponent prefix = net.minecraft.network.chat.Component.literal("[PureMash Core Block]:")
                    .withStyle(net.minecraft.ChatFormatting.AQUA);

            if (player.isCrouching()) {
                // SHIFT + CLICK: Toggles Area Visualization
                boolean newState = !coreBe.isShowArea();
                coreBe.setShowArea(newState);

                net.minecraft.network.chat.Component msg = newState ?
                        net.minecraft.network.chat.Component.literal(" Area visualization enabled!").withStyle(net.minecraft.ChatFormatting.GREEN) :
                        net.minecraft.network.chat.Component.literal(" Area visualization disabled.").withStyle(net.minecraft.ChatFormatting.RED);

                player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(prefix).append(msg));
            } else {
                // NORMAL CLICK: Toggles Block Activation
                boolean newState = !coreBe.isActive();
                coreBe.setActive(newState);

                net.minecraft.network.chat.Component msg = newState ?
                        net.minecraft.network.chat.Component.literal(" Area acceleration enabled.").withStyle(net.minecraft.ChatFormatting.GREEN) :
                        net.minecraft.network.chat.Component.literal(" Area acceleration disabled.").withStyle(net.minecraft.ChatFormatting.RED);

                player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(prefix).append(msg));
            }
            return net.minecraft.world.InteractionResult.CONSUME;
        }

        return net.minecraft.world.InteractionResult.PASS;
    }

    // Retains the Overload enchantment level when the block is broken
    @Override
    protected @NonNull List<ItemStack> getDrops(@NonNull BlockState state, LootParams.@NonNull Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof PureMashCoreBlockEntity coreBe) {
            int overloadLvl = coreBe.getOverloadLevel();
            if (overloadLvl > 0) {
                var reg = be.getLevel().registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
                if (reg.isPresent()) {
                    var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                    if (overloadOpt.isPresent()) {
                        for (ItemStack drop : drops) {
                            if (drop.is(this.asItem())) {
                                drop.enchant(overloadOpt.get(), overloadLvl);
                            }
                        }
                    }
                }
            }
        }
        return drops;
    }

    // Records the enchantment level that was on the item at the moment it was placed on the ground
    @SuppressWarnings("deprecation")
    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PureMashCoreBlockEntity coreBe) {
            int overloadLvl = 0;
            var reg = level.registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
            if (reg.isPresent()) {
                var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                if (overloadOpt.isPresent()) {
                    overloadLvl = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(overloadOpt.get(), stack);
                }
            }
            coreBe.setOverloadLevel(overloadLvl);
        }
    }

    // PROXIMITY PROHIBITION (10 blocks)
    @Nullable
    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        int range = 10;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (checkPos.equals(pos)) continue;

                    if (level.getBlockState(checkPos).is(this)) {
                        if (context.getPlayer() != null && !level.isClientSide()) {
                            context.getPlayer().sendSystemMessage(
                                    net.minecraft.network.chat.Component.literal("§c[PureMash Core Block] There is already a PureMash Core Block within 10 blocks!")
                            );
                        }
                        return null;
                    }
                }
            }
        }
        return super.getStateForPlacement(context);
    }
}