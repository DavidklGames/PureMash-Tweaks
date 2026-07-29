package dev.davidklgames.puremashtweaks.block;

import com.mojang.serialization.MapCodec;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class SynthesisTableBlock extends BaseEntityBlock {
    // 1. Create the CODEC using the static helper 'simpleCodec'
    public static final MapCodec<SynthesisTableBlock> CODEC = simpleCodec(SynthesisTableBlock::new);

    public SynthesisTableBlock(Properties properties) {
        super(properties);
    }

    // 2. Mandatory implementation required by BaseEntityBlock
    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new SynthesisTableBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    // 3. Replace 'use' with the new 'useWithoutItem' from the Minecraft interaction cycle
    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SynthesisTableBlockEntity) {
            // Write the BlockPos to the network buffer (buf -> buf.writeBlockPos(pos))
            player.openMenu((SynthesisTableBlockEntity) blockEntity, buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    // 4. Implement getMenuProvider to ensure spectator mode support
    @Nullable
    @Override
    protected MenuProvider getMenuProvider(@NonNull BlockState state, Level level, @NonNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }
}