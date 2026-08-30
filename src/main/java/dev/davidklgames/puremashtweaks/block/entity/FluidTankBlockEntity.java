package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.jspecify.annotations.NonNull;

public class FluidTankBlockEntity extends BlockEntity {

    public final FluidStacksResourceHandler fluidTank = new FluidStacksResourceHandler(1, 32000) {
        @Override
        public long getCapacityAsLong(int slot, net.neoforged.neoforge.transfer.fluid.@NonNull FluidResource resource) {
            if (PureMashTweaksConfig.COMMON_SPEC != null && PureMashTweaksConfig.COMMON_SPEC.isLoaded()) {
                return PureMashTweaksConfig.COMMON.fluidTankCapacity.get();
            }
            return 32000L;
        }

        @Override
        protected void onContentsChanged(int index, net.neoforged.neoforge.fluids.@NonNull FluidStack previousContents) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_TANK_BE.get(), pos, state);
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