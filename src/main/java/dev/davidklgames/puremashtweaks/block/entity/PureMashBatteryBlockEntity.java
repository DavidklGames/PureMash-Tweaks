package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;

public class PureMashBatteryBlockEntity extends BlockEntity {

    // 1.5M FE/t (1,500,000 FE/t) Max Receive and Max Extract
    public final SimpleEnergyHandler energyTank = new SimpleEnergyHandler(50000000, 1500000, 1500000) {
        @Override
        public long getCapacityAsLong() {
            if (PureMashTweaksConfig.COMMON_SPEC != null && PureMashTweaksConfig.COMMON_SPEC.isLoaded()) {
                return PureMashTweaksConfig.COMMON.batteryBaseCapacity.get();
            }
            return 50000000L;
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    public PureMashBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PUREMASH_BATTERY_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PureMashBatteryBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.energyTank.getAmountAsLong() > 0) {
            for (Direction direction : Direction.values()) {
                BlockPos targetPos = pos.relative(direction);
                EnergyHandler targetEnergy = level.getCapability(Capabilities.Energy.BLOCK, targetPos, direction.getOpposite());

                if (targetEnergy != null) {
                    long currentStored = be.energyTank.getAmountAsLong();
                    if (currentStored <= 0) break;

                    long maxSend = Math.min(currentStored, 1500000L);

                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = targetEnergy.insert((int) maxSend, tx);
                        if (inserted > 0) {
                            be.energyTank.set((int) (currentStored - inserted));
                            tx.commit();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("Energy", this.energyTank.getAmountAsLong());
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.energyTank.set((int) input.getLongOr("Energy", 0L));
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