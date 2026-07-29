package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;

public class CreativeFluidTankBlockEntity extends BlockEntity {

    private static final int FILL_SPEED_PER_TICK = 50000;

    private boolean selfFilling = false;

    public final FluidStacksResourceHandler fluidTank = new FluidStacksResourceHandler(1, 1000000) {
        @Override
        public int extract(int index, @NonNull FluidResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
            if (this.getResource(index).equals(resource)) {
                return amount;
            }
            return 0;
        }

        @Override
        public int insert(int index, @NonNull FluidResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
            if (selfFilling) {
                return super.insert(index, resource, amount, transaction);
            }

            FluidResource current = this.getResource(index);
            if (current.isEmpty()) {
                return super.insert(index, resource, amount, transaction);
            } else if (current.equals(resource)) {
                if (getAmountAsLong(index) < 1000000) {
                    return super.insert(index, resource, amount, transaction);
                }
                return amount;
            }
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, net.neoforged.neoforge.fluids.@NonNull FluidStack previousContents) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public CreativeFluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_FLUID_TANK_BE.get(), pos, state);
    }

    public void clearTank() {
        this.fluidTank.set(0, FluidResource.EMPTY, 0);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CreativeFluidTankBlockEntity be) {
        if (level.isClientSide()) return;

        long currentAmount = be.fluidTank.getAmountAsLong(0);

        if (currentAmount > 0 && currentAmount < 1000000) {
            be.selfFilling = true;
            try (Transaction tx = Transaction.openRoot()) {
                FluidResource res = be.fluidTank.getResource(0);
                long needed = 1000000 - currentAmount;
                int toInsert = (int) Math.min(FILL_SPEED_PER_TICK, needed);
                be.fluidTank.insert(0, res, toInsert, tx);
                tx.commit();
            } finally {
                be.selfFilling = false;
            }
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.fluidTank.serialize(output);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.fluidTank.deserialize(input);
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.@NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return this.saveCustomOnly(registries);
    }
}