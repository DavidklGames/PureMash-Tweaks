package dev.davidklgames.puremashtweaks.block;

import com.mojang.serialization.MapCodec;
import dev.davidklgames.puremashtweaks.block.entity.CreativeBatteryBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class CreativeBatteryBlock extends BaseEntityBlock {
    public static final MapCodec<CreativeBatteryBlock> CODEC = simpleCodec(CreativeBatteryBlock::new);

    public CreativeBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new CreativeBatteryBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        player.sendSystemMessage(Component.literal("§d[Creative Energy Battery]: §fInfinite Energy Supply (∞ / ∞ FE)"));
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CREATIVE_BATTERY_BE.get(), CreativeBatteryBlockEntity::tick);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            @NonNull BlockState state,
            @NonNull ServerLevel level,
            @NonNull BlockPos pos,
            boolean movedByPiston
    ) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
}