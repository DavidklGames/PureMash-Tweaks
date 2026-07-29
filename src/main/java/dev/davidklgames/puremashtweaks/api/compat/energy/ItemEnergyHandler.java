package dev.davidklgames.puremashtweaks.api.compat.energy;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

public class ItemEnergyHandler implements EnergyHandler {
    private final ItemAccess itemAccess;
    private final long capacity;
    private final long maxReceive;
    private final long maxExtract;

    public ItemEnergyHandler(ItemAccess itemAccess, long capacity, long maxReceive, long maxExtract) {
        this.itemAccess = itemAccess;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public long getAmountAsLong() {
        ItemStack stack = this.itemAccess.getResource().toStack();
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getLongOr("Energy", 0L);
        }
        return 0L;
    }

    @Override
    public long getCapacityAsLong() {
        return this.capacity;
    }

    public void setEnergy(long amount, TransactionContext transaction) {
        ItemStack stack = this.itemAccess.getResource().toStack();
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putLong("Energy", amount);

        ItemStack newStack = stack.copy();
        newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        int count = this.itemAccess.getAmount();
        this.itemAccess.exchange(ItemResource.of(newStack), count, transaction);
    }

    @Override
    public int insert(int amount, @NonNull TransactionContext transaction) {
        if (amount <= 0) return 0;
        long current = getAmountAsLong();
        long inserted = Math.min(amount, Math.min(this.maxReceive, this.capacity - current));
        if (inserted > 0) {
            setEnergy(current + inserted, transaction);
        }
        return (int) inserted;
    }

    @Override
    public int extract(int amount, @NonNull TransactionContext transaction) {
        if (amount <= 0) return 0;
        long current = getAmountAsLong();
        long extracted = Math.min(amount, Math.min(this.maxExtract, current));
        if (extracted > 0) {
            setEnergy(current - extracted, transaction);
        }
        return (int) extracted;
    }
}