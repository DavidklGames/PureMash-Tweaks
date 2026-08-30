package dev.davidklgames.puremashtweaks.menu;

import dev.davidklgames.puremashtweaks.block.entity.AlchemicalSynthesizerBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public class AlchemicalSynthesizerMenu extends AbstractContainerMenu {
    private final AlchemicalSynthesizerBlockEntity blockEntity;
    private final ContainerData data;
    private final int[] localData = new int[12];

    public AlchemicalSynthesizerMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(id, playerInv, (AlchemicalSynthesizerBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public AlchemicalSynthesizerMenu(int id, Inventory playerInv, AlchemicalSynthesizerBlockEntity entity) {
        super(ModMenus.ALCHEMICAL_SYNTHESIZER_MENU.get(), id);
        this.blockEntity = entity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (playerInv.player.level().isClientSide()) return localData[index];
                if (blockEntity != null) {
                    return switch (index) {
                        case 0 -> blockEntity.getProgress();
                        case 1 -> blockEntity.getMaxProgress();
                        case 2 -> (int) blockEntity.fluidTank.getAmountAsLong(0);
                        case 3 -> (int) blockEntity.fluidTank.getCapacityAsLong(0, FluidResource.EMPTY);
                        case 4 -> BuiltInRegistries.FLUID.getId(blockEntity.fluidTank.getResource(0).getFluid());
                        case 5 -> blockEntity.isArrowTopActive() ? 1 : 0;
                        case 6 -> blockEntity.isArrowMiddleActive() ? 1 : 0;
                        case 7 -> blockEntity.isArrowBottomActive() ? 1 : 0;
                        case 8 -> (int) (blockEntity.energyTank.getAmountAsLong() & 0xFFFF);
                        case 9 -> (int) ((blockEntity.energyTank.getAmountAsLong() >> 16) & 0xFFFF);
                        case 10 -> (int) (blockEntity.getEnergyCapacity() & 0xFFFF);
                        case 11 -> (int) ((blockEntity.getEnergyCapacity() >> 16) & 0xFFFF);
                        default -> 0;
                    };
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                localData[index] = value;
            }

            @Override
            public int getCount() { return 12; }
        };

        this.addDataSlots(this.data);

        net.neoforged.neoforge.items.IItemHandler invHandler = (entity != null) ? entity.inventory : new net.neoforged.neoforge.items.ItemStackHandler(26);

        // Slot 0: Fluid Catalyst Valve
        this.addSlot(new SlotItemHandler(invHandler, 0, 30, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        // Slot 1: Input Sample
        this.addSlot(new SlotItemHandler(invHandler, 1, 30, 56));

        // Slot 2: Tool Catalyst
        this.addSlot(new SlotItemHandler(invHandler, 2, 30, 77));

        // Slots 3 to 22: Output Grid (5x4 = 20 slots)
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                int index = 3 + col + (row * 5);
                this.addSlot(new SlotItemHandler(invHandler, index, 80 + col * 18, 24 + row * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) { return false; }
                });
            }
        }

        // Slots 23, 24, 25: Upgrades
        this.addSlot(new SlotItemHandler(invHandler, 23, 182, 18) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 24, 182, 39) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 25, 182, 60) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });

        addPlayerInventory(playerInv);
    }

    @Override
    public void clicked(int slotId, int button, @NonNull ContainerInput containerInput, @NonNull Player player) {
        // Universal Cursor Drain & Fill Valve (Slot 0)
        if (slotId == 0 && blockEntity != null) {
            ItemStack carried = this.getCarried();
            long fluidAmount = blockEntity.fluidTank.getAmountAsLong(0);
            Fluid currentTankFluid = blockEntity.fluidTank.getResource(0).getFluid();

            // 1. Drain carried fluid bucket / container into tank
            if (!carried.isEmpty()) {
                FluidStack contained = getContainedFluid(carried);

                if (!contained.isEmpty()) {
                    Fluid bucketFluid = contained.getFluid();

                    if ((currentTankFluid == Fluids.EMPTY || currentTankFluid == bucketFluid) && fluidAmount + contained.getAmount() <= 16000) {
                        try (Transaction tx = Transaction.openRoot()) {
                            long inserted = blockEntity.fluidTank.insert(0, FluidResource.of(bucketFluid), contained.getAmount(), tx);
                            if (inserted == contained.getAmount()) {
                                tx.commit();

                                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                                if (carried.getCount() == 1) {
                                    this.setCarried(emptyBucket);
                                } else {
                                    carried.shrink(1);
                                    if (!player.getInventory().add(emptyBucket)) {
                                        player.drop(emptyBucket, false);
                                    }
                                }
                                blockEntity.setChanged();
                            }
                        }
                    }
                    return;
                }

                // 2. Extract fluid from tank into carried empty bucket
                if (carried.is(Items.BUCKET)) {
                    if (fluidAmount >= 1000 && currentTankFluid != Fluids.EMPTY) {
                        Item fullBucketItem = currentTankFluid.getBucket();

                        if (fullBucketItem != Items.AIR) {
                            try (Transaction tx = Transaction.openRoot()) {
                                long extracted = blockEntity.fluidTank.extract(0, FluidResource.of(currentTankFluid), 1000, tx);
                                if (extracted == 1000) {
                                    tx.commit();

                                    ItemStack filledBucket = new ItemStack(fullBucketItem);
                                    if (carried.getCount() == 1) {
                                        this.setCarried(filledBucket);
                                    } else {
                                        carried.shrink(1);
                                        if (!player.getInventory().add(filledBucket)) {
                                            player.drop(filledBucket, false);
                                        }
                                    }
                                    blockEntity.setChanged();
                                }
                            }
                        }
                    }
                    return;
                }
            }
            return;
        }
        super.clicked(slotId, button, containerInput, player);
    }

    private static FluidStack getContainedFluid(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucket && bucket.content != Fluids.EMPTY) {
            return new FluidStack(bucket.content, 1000);
        }
        return FluidUtil.getFirstStackContained(stack);
    }

    public int getProgress() { return this.data.get(0); }
    public int getMaxProgress() { return this.data.get(1); }
    public int getFluidAmount() { return this.data.get(2); }
    public int getFluidCapacity() { return this.data.get(3); }

    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.byId(this.data.get(4));
    }

    public boolean isArrowTopActive() { return this.data.get(5) == 1; }
    public boolean isArrowMiddleActive() { return this.data.get(6) == 1; }
    public boolean isArrowBottomActive() { return this.data.get(7) == 1; }

    public long getEnergyAmountLong() {
        long low = this.data.get(8) & 0xFFFFL;
        long high = this.data.get(9) & 0xFFFFL;
        return low | (high << 16);
    }

    public long getEnergyCapacityLong() {
        long low = this.data.get(10) & 0xFFFFL;
        long high = this.data.get(11) & 0xFFFFL;
        return low | (high << 16);
    }

    public AlchemicalSynthesizerBlockEntity getBlockEntity() { return this.blockEntity; }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < 26) {
                if (!this.moveItemStackTo(stackInSlot, 26, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (AlchemicalSynthesizerBlockEntity.isUpgradeValid(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 23, 26, false)) {
                        if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) return ItemStack.EMPTY;
                    }
                } else if (stackInSlot.has(DataComponents.TOOL) ||
                        stackInSlot.is(ItemTags.PICKAXES) ||
                        stackInSlot.is(ItemTags.SHOVELS) ||
                        stackInSlot.is(ItemTags.AXES)) {
                    if (!this.moveItemStackTo(stackInSlot, 2, 3, false)) {
                        if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity == null) return true;
        assert blockEntity.getLevel() != null;
        return AbstractContainerMenu.stillValid(
                net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                blockEntity.getBlockState().getBlock()
        );
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 112 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 170));
        }
    }
}