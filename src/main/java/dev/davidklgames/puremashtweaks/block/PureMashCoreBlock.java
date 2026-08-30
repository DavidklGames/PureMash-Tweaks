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
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PureMashCoreBlockEntity coreBe) {
            // Trava de Imersão: Se não estiver encantado com Overload, ignora totalmente
            if (coreBe.getOverloadLevel() <= 0) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (player.isCrouching()) {
                // SHIFT + CLICK: Alterna a visualização da área
                boolean newState = !coreBe.isShowArea();
                coreBe.setShowArea(newState);

                net.minecraft.ChatFormatting color = newState ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED;
                net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.literal("[PureMash Core Block]: ").withStyle(color);
                net.minecraft.network.chat.Component msg = net.minecraft.network.chat.Component.literal(
                        newState ? "Area visualization enabled!" : "Area visualization disabled."
                ).withStyle(color);

                player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(prefix).append(msg));
            } else {
                // NORMAL CLICK: Alterna a ativação da aceleração
                boolean newState = !coreBe.isActive();
                coreBe.setActive(newState);

                net.minecraft.ChatFormatting color = newState ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED;
                net.minecraft.network.chat.Component prefix = net.minecraft.network.chat.Component.literal("[PureMash Core Block]: ").withStyle(color);
                net.minecraft.network.chat.Component msg = net.minecraft.network.chat.Component.literal(
                        newState ? "Area acceleration enabled." : "Area acceleration disabled."
                ).withStyle(color);

                player.sendSystemMessage(net.minecraft.network.chat.Component.empty().append(prefix).append(msg));
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    // Retains the Overload enchantment level when the block is broken
    @Override
    protected @NonNull List<ItemStack> getDrops(@NonNull BlockState state, LootParams.@NonNull Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof PureMashCoreBlockEntity coreBe) {
            int overloadLvl = coreBe.getOverloadLevel();
            if (overloadLvl > 0) {
                assert be.getLevel() != null;
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