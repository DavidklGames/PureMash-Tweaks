package dev.davidklgames.puremashtweaks.api.compat.fluid;

import dev.davidklgames.puremashtweaks.util.TankNbtHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

public class TankItemFluidHandler implements ResourceHandler<FluidResource> {
    private final ItemAccess itemAccess;
    private final long capacity;
    private final boolean isCreative;

    public TankItemFluidHandler(ItemAccess itemAccess, long capacity, boolean isCreative) {
        this.itemAccess = itemAccess;
        this.capacity = capacity;
        this.isCreative = isCreative;
    }

    @Override
    public int size() {
        return 1;
    }

    private FluidStacksResourceHandler getInternalHandler() {
        FluidStacksResourceHandler handler = new FluidStacksResourceHandler(1, (int) Math.min(this.capacity, Integer.MAX_VALUE)) {
            @Override
            protected void onContentsChanged(int index, @NonNull FluidStack previousContents) {}
        };
        ItemStack stack = this.itemAccess.getResource().toStack();
        CompoundTag tag = TankNbtHelper.getTagFromStack(stack);

        if (tag != null && !tag.isEmpty()) {
            net.minecraft.core.HolderLookup.Provider provider = null;
            if (net.minecraft.client.Minecraft.getInstance().level != null) {
                provider = net.minecraft.client.Minecraft.getInstance().level.registryAccess();
            } else if (net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
                provider = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess();
            }
            if (provider != null) {
                try {
                    var input = net.minecraft.world.level.storage.TagValueInput.create(
                            net.minecraft.util.ProblemReporter.DISCARDING,
                            provider,
                            tag
                    );
                    handler.deserialize(input);
                } catch (Exception ignored) {}
            }
        }
        return handler;
    }

    @Override
    public @NonNull FluidResource getResource(int index) {
        return getInternalHandler().getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return getInternalHandler().getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, @NonNull FluidResource resource) {
        return this.capacity;
    }

    @Override
    public boolean isValid(int index, @NonNull FluidResource resource) {
        return true;
    }

    @Override
    public int insert(int index, @NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
        if (amount <= 0 || resource.isEmpty()) return 0;

        FluidStacksResourceHandler handler = getInternalHandler();
        int inserted = handler.insert(index, resource, amount, transaction);
        if (inserted > 0 && !this.isCreative) {
            saveHandler(handler, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(int index, @NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
        if (amount <= 0 || resource.isEmpty()) return 0;

        FluidStacksResourceHandler handler = getInternalHandler();
        int extracted = handler.extract(index, resource, amount, transaction);
        if (extracted > 0 && !this.isCreative) {
            saveHandler(handler, transaction);
        }
        return extracted;
    }

    private void saveHandler(FluidStacksResourceHandler handler, TransactionContext transaction) {
        ItemStack stack = this.itemAccess.getResource().toStack();
        CompoundTag tag = TankNbtHelper.serializeHandler(handler);

        ItemStack newStack = stack.copy();
        if (tag.isEmpty() || handler.getAmountAsLong(0) <= 0) {
            newStack.remove(DataComponents.CUSTOM_DATA);
            newStack.remove(DataComponents.BLOCK_ENTITY_DATA);
        } else {
            newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        this.itemAccess.exchange(ItemResource.of(newStack), this.itemAccess.getAmount(), transaction);
    }
}