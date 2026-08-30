package dev.davidklgames.puremashtweaks.block;

import dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity;
import dev.davidklgames.puremashtweaks.menu.CableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * Unified Cable Block transporting Items, Fluids, AND Energy simultaneously.
 */
public class UniversalCableBlock extends CableBlock {

    private final int tier; // 1 = Synthorium, 2 = Moldelonian

    public UniversalCableBlock(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return this.tier;
    }

    @Override
    public boolean canConnectTo(Level level, BlockPos pos, Direction side) {
        BlockPos targetPos = pos.relative(side);
        Direction opposite = side.getOpposite();

        return level.getCapability(Capabilities.Item.BLOCK, targetPos, opposite) != null ||
                level.getCapability(Capabilities.Fluid.BLOCK, targetPos, opposite) != null ||
                level.getCapability(Capabilities.Energy.BLOCK, targetPos, opposite) != null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new UniversalCableBlockEntity(pos, state, this.tier);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return (lvl, p, st, be) -> {
            if (be instanceof UniversalCableBlockEntity universalCable) {
                UniversalCableBlockEntity.tick(lvl, p, st, universalCable);
            }
        };
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof UniversalCableBlockEntity universalBe) {
            Direction targetedSide = getClickedExtractingSide(universalBe, hit, pos);

            if (targetedSide != null && universalBe.isExtracting(targetedSide)) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }

                player.openMenu(
                        new SimpleMenuProvider(
                                (id, playerInv, p) -> {
                                    CableMenu menu = new CableMenu(id, playerInv, universalBe);
                                    menu.setSide(targetedSide);
                                    return menu;
                                },
                                Component.empty()
                        ),
                        buf -> {
                            buf.writeBlockPos(pos);
                            buf.writeInt(targetedSide.get3DDataValue());
                        }
                );
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }
}