package dev.davidklgames.puremashtweaks.block;

import dev.davidklgames.puremashtweaks.block.entity.CableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

public abstract class CableBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final BooleanProperty NORTH_EXTRACT = BooleanProperty.create("north_extract");
    public static final BooleanProperty SOUTH_EXTRACT = BooleanProperty.create("south_extract");
    public static final BooleanProperty EAST_EXTRACT = BooleanProperty.create("east_extract");
    public static final BooleanProperty WEST_EXTRACT = BooleanProperty.create("west_extract");
    public static final BooleanProperty UP_EXTRACT = BooleanProperty.create("up_extract");
    public static final BooleanProperty DOWN_EXTRACT = BooleanProperty.create("down_extract");

    public static final Map<Direction, BooleanProperty> PROPERTY_MAP = new EnumMap<>(Map.of(
            Direction.NORTH, NORTH, Direction.SOUTH, SOUTH,
            Direction.EAST, EAST, Direction.WEST, WEST,
            Direction.UP, UP, Direction.DOWN, DOWN
    ));

    public static final Map<Direction, BooleanProperty> EXTRACT_PROPERTY_MAP = new EnumMap<>(Map.of(
            Direction.NORTH, NORTH_EXTRACT, Direction.SOUTH, SOUTH_EXTRACT,
            Direction.EAST, EAST_EXTRACT, Direction.WEST, WEST_EXTRACT,
            Direction.UP, UP_EXTRACT, Direction.DOWN, DOWN_EXTRACT
    ));

    protected static final VoxelShape CORE_SHAPE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    protected static final VoxelShape NORTH_SHAPE = Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    protected static final VoxelShape DOWN_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D);
    protected static final VoxelShape UP_SHAPE = Block.box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D);

    protected static final VoxelShape SHAPE_EXTRACT_NORTH = Shapes.or(NORTH_SHAPE, Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 1.0D));
    protected static final VoxelShape SHAPE_EXTRACT_SOUTH = Shapes.or(SOUTH_SHAPE, Block.box(4.0D, 4.0D, 15.0D, 12.0D, 12.0D, 16.0D));
    protected static final VoxelShape SHAPE_EXTRACT_EAST = Shapes.or(EAST_SHAPE, Block.box(15.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D));
    protected static final VoxelShape SHAPE_EXTRACT_WEST = Shapes.or(WEST_SHAPE, Block.box(0.0D, 4.0D, 4.0D, 1.0D, 12.0D, 12.0D));
    protected static final VoxelShape SHAPE_EXTRACT_UP = Shapes.or(UP_SHAPE, Block.box(4.0D, 15.0D, 4.0D, 12.0D, 16.0D, 12.0D));
    protected static final VoxelShape SHAPE_EXTRACT_DOWN = Shapes.or(DOWN_SHAPE, Block.box(4.0D, 0.0D, 4.0D, 12.0D, 1.0D, 12.0D));

    public CableBlock(Properties properties) {
        super(properties.strength(1.5F).sound(SoundType.METAL).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP, false).setValue(DOWN, false)
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(NORTH_EXTRACT, false).setValue(SOUTH_EXTRACT, false)
                .setValue(EAST_EXTRACT, false).setValue(WEST_EXTRACT, false)
                .setValue(UP_EXTRACT, false).setValue(DOWN_EXTRACT, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, WATERLOGGED,
                NORTH_EXTRACT, SOUTH_EXTRACT, EAST_EXTRACT, WEST_EXTRACT, UP_EXTRACT, DOWN_EXTRACT);
    }

    public abstract boolean canConnectTo(Level level, BlockPos pos, Direction side);

    public boolean isCable(LevelAccessor level, BlockPos pos, Direction side) {
        BlockState targetState = level.getBlockState(pos.relative(side));
        return targetState.getBlock().getClass() == this.getClass();
    }

    public boolean isConnected(Level level, BlockPos pos, Direction side) {
        CableBlockEntity cableBe = getCableEntity(level, pos);
        if (cableBe != null && cableBe.isDisconnected(side)) {
            return false;
        }

        if (isCable(level, pos, side)) {
            CableBlockEntity neighborBe = getCableEntity(level, pos.relative(side));
            return neighborBe == null || !neighborBe.isDisconnected(side.getOpposite());
        }

        return canConnectTo(level, pos, side);
    }

    @Nullable
    public CableBlockEntity getCableEntity(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof CableBlockEntity ? (CableBlockEntity) be : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = level.getFluidState(pos);

        BlockState state = this.defaultBlockState();
        for (Direction dir : Direction.values()) {
            boolean connected = isConnected(level, pos, dir);
            CableBlockEntity cableBe = getCableEntity(level, pos);
            boolean extracting = cableBe != null && cableBe.isExtracting(dir);

            state = state.setValue(PROPERTY_MAP.get(dir), connected)
                    .setValue(EXTRACT_PROPERTY_MAP.get(dir), extracting);
        }
        return state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected @NonNull BlockState updateShape(BlockState state, @NonNull LevelReader level, @NonNull ScheduledTickAccess tickAccess, @NonNull BlockPos pos, @NonNull Direction direction, @NonNull BlockPos neighborPos, @NonNull BlockState neighborState, @NonNull RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (level instanceof Level fullLevel) {
            CableBlockEntity cableBe = getCableEntity(fullLevel, pos);
            if (cableBe != null) {
                if (!canConnectTo(fullLevel, pos, direction) && !isCable(fullLevel, pos, direction)) {
                    cableBe.setExtracting(direction, false);
                    cableBe.setDisconnected(direction, false);
                }
            }

            boolean connected = isConnected(fullLevel, pos, direction);
            boolean extracting = cableBe != null && cableBe.isExtracting(direction);

            return state.setValue(PROPERTY_MAP.get(direction), connected)
                    .setValue(EXTRACT_PROPERTY_MAP.get(direction), extracting);
        }
        return state;
    }

    /**
     * Identifica com precisão se o jogador clicou no bico/face que está no modo de extração.
     */
    @Nullable
    public static Direction getClickedExtractingSide(CableBlockEntity cableBe, BlockHitResult hit, BlockPos pos) {
        if (cableBe == null) return null;

        double lx = hit.getLocation().x - pos.getX();
        double ly = hit.getLocation().y - pos.getY();
        double lz = hit.getLocation().z - pos.getZ();

        Direction face = hit.getDirection();

        if (lz <= 0.3125 && cableBe.isExtracting(Direction.NORTH)) return Direction.NORTH;
        if (lz >= 0.6875 && cableBe.isExtracting(Direction.SOUTH)) return Direction.SOUTH;
        if (lx <= 0.3125 && cableBe.isExtracting(Direction.WEST)) return Direction.WEST;
        if (lx >= 0.6875 && cableBe.isExtracting(Direction.EAST)) return Direction.EAST;
        if (ly <= 0.3125 && cableBe.isExtracting(Direction.DOWN)) return Direction.DOWN;
        if (ly >= 0.6875 && cableBe.isExtracting(Direction.UP)) return Direction.UP;

        if (cableBe.isExtracting(face)) {
            return face;
        }

        return null;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    public InteractionResult onWrenchClicked(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, Direction side) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            CableBlockEntity cableBe = getCableEntity(level, pos);
            if (cableBe != null) {
                boolean isNeighborCable = isCable(level, pos, side);

                if (!isNeighborCable) {
                    if (!canConnectTo(level, pos, side)) {
                        return InteractionResult.PASS;
                    }

                    boolean isExtracting = cableBe.isExtracting(side);
                    boolean isDisconnected = cableBe.isDisconnected(side);

                    if (!isExtracting && !isDisconnected) {
                        // Click 1: Insert -> Extract
                        cableBe.setExtracting(side, true);
                        cableBe.setDisconnected(side, false);
                    } else if (isExtracting && !isDisconnected) {
                        // Click 2: Extract -> Disconnect
                        cableBe.setExtracting(side, false);
                        cableBe.setDisconnected(side, true);
                    } else {
                        // Click 3: Disconnected -> Insert
                        cableBe.setExtracting(side, false);
                        cableBe.setDisconnected(side, false);
                    }
                } else {
                    boolean isDisconnected = cableBe.isDisconnected(side);
                    cableBe.setDisconnected(side, !isDisconnected);

                    CableBlockEntity neighborBe = getCableEntity(level, pos.relative(side));
                    if (neighborBe != null) {
                        neighborBe.setDisconnected(side.getOpposite(), !isDisconnected);
                    }
                }

                BlockState newState = state;
                for (Direction dir : Direction.values()) {
                    boolean connected = isConnected(level, pos, dir);
                    boolean extracting = cableBe.isExtracting(dir);

                    newState = newState.setValue(PROPERTY_MAP.get(dir), connected)
                            .setValue(EXTRACT_PROPERTY_MAP.get(dir), extracting);
                }
                level.setBlock(pos, newState, 3);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;
        for (Direction dir : Direction.values()) {
            if (state.getValue(PROPERTY_MAP.get(dir))) {
                boolean extracting = state.getValue(EXTRACT_PROPERTY_MAP.get(dir));
                if (extracting) {
                    shape = Shapes.or(shape, getExtractShape(dir));
                } else {
                    shape = Shapes.or(shape, getSideShape(dir));
                }
            }
        }
        return shape;
    }

    private VoxelShape getSideShape(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
        };
    }

    public static VoxelShape getExtractShape(Direction dir) {
        return switch (dir) {
            case NORTH -> SHAPE_EXTRACT_NORTH;
            case SOUTH -> SHAPE_EXTRACT_SOUTH;
            case WEST -> SHAPE_EXTRACT_WEST;
            case EAST -> SHAPE_EXTRACT_EAST;
            case UP -> SHAPE_EXTRACT_UP;
            case DOWN -> SHAPE_EXTRACT_DOWN;
        };
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }
}