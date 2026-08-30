package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

public class CreativeBatteryBlockEntity extends BlockEntity {

    public final EnergyHandler infiniteEnergyHandler = new EnergyHandler() {
        @Override
        public long getAmountAsLong() { return 1000000000L; }

        @Override
        public long getCapacityAsLong() { return 1000000000L; }

        @Override
        public int insert(int amount, @NonNull TransactionContext transaction) { return amount; }

        @Override
        public int extract(int amount, @NonNull TransactionContext transaction) { return amount; }
    };

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_BATTERY_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity be) {
        if (level.isClientSide()) return;

        for (Direction direction : Direction.values()) {
            BlockPos targetPos = pos.relative(direction);
            EnergyHandler targetEnergy = level.getCapability(Capabilities.Energy.BLOCK, targetPos, direction.getOpposite());

            if (targetEnergy != null) {
                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = targetEnergy.insert(10000000, tx);
                    if (inserted > 0) {
                        tx.commit();
                    }
                }
            }
        }
    }
}