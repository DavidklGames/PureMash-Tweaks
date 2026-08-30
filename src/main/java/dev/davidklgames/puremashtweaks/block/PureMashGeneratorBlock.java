package dev.davidklgames.puremashtweaks.block;

import com.mojang.serialization.MapCodec;
import dev.davidklgames.puremashtweaks.block.entity.PureMashGeneratorBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class PureMashGeneratorBlock extends BaseEntityBlock {
    public static final MapCodec<PureMashGeneratorBlock> CODEC = simpleCodec(PureMashGeneratorBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public PureMashGeneratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new PureMashGeneratorBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull RandomSource random) {
        if (state.getValue(LIT)) {
            double x = (double) pos.getX() + 0.5D;
            double y = pos.getY();
            double z = (double) pos.getZ() + 0.5D;

            if (random.nextDouble() < 0.1D) {
                level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = state.getValue(FACING);
            Direction.Axis axis = direction.getAxis();
            double horizontalOffset = random.nextDouble() * 0.6D - 0.3D;
            double offsetX = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52D : horizontalOffset;
            double offsetY = random.nextDouble() * 6.0D / 16.0D + 0.15D;
            double offsetZ = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52D : horizontalOffset;

            level.addParticle(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + offsetX, y + offsetY, z + offsetZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PureMashGeneratorBlockEntity generatorBe) {
            player.openMenu(generatorBe, buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(@NonNull BlockState state, Level level, @NonNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.PUREMASH_GENERATOR_BE.get(), PureMashGeneratorBlockEntity::tick);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            @NonNull BlockState state,
            net.minecraft.server.level.@NonNull ServerLevel level,
            @NonNull BlockPos pos,
            boolean movedByPiston
    ) {
        net.minecraft.world.Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
}