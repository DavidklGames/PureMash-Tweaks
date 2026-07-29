package dev.davidklgames.puremashtweaks.menu;

import dev.davidklgames.puremashtweaks.block.entity.AlchemicalSynthesizerBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public class AlchemicalSynthesizerMenu extends AbstractContainerMenu {
    private final AlchemicalSynthesizerBlockEntity blockEntity;
    private final ContainerData data;
    private final int[] localData = new int[11];

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
                        case 3 -> (int) blockEntity.fluidTank.getCapacityAsLong(0, net.neoforged.neoforge.transfer.fluid.FluidResource.EMPTY);
                        case 4 -> {
                            var f = blockEntity.fluidTank.getResource(0).getFluid();
                            if (f == Fluids.LAVA) yield 1;
                            if (f == Fluids.WATER) yield 2;
                            yield 0;
                        }
                        case 5 -> blockEntity.isArrowTopActive() ? 1 : 0;
                        case 6 -> blockEntity.isArrowMiddleActive() ? 1 : 0;
                        case 7 -> blockEntity.isArrowBottomActive() ? 1 : 0;
                        case 8 -> (int) blockEntity.energyTank.getAmountAsLong();
                        case 9 -> (int) blockEntity.energyTank.getCapacityAsLong();
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
            public int getCount() { return 10; }
        };

        this.addDataSlots(this.data);

        net.neoforged.neoforge.items.IItemHandler invHandler = (entity != null) ? entity.inventory : new net.neoforged.neoforge.items.ItemStackHandler(26);

        this.addSlot(new SlotItemHandler(invHandler, 0, 30, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(invHandler, 1, 30, 56));
        this.addSlot(new SlotItemHandler(invHandler, 2, 30, 77));

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                int index = 3 + col + (row * 5);
                this.addSlot(new SlotItemHandler(invHandler, index, 80 + col * 18, 24 + row * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) { return false; }
                });
            }
        }

        this.addSlot(new SlotItemHandler(invHandler, 23, 182, 18));
        this.addSlot(new SlotItemHandler(invHandler, 24, 182, 39));
        this.addSlot(new SlotItemHandler(invHandler, 25, 182, 60));

        addPlayerInventory(playerInv);
    }

    @Override
    public void clicked(int slotId, int button, @NonNull ContainerInput containerInput, @NonNull Player player) {
        if (slotId == 0) {
            ItemStack carried = this.getCarried();
            long fluidAmount = blockEntity.fluidTank.getAmountAsLong(0);
            Fluid fluidType = blockEntity.fluidTank.getResource(0).getFluid();

            if (carried.is(net.minecraft.world.item.Items.WATER_BUCKET) || carried.is(net.minecraft.world.item.Items.LAVA_BUCKET)) {
                Fluid bucketFluid = carried.is(net.minecraft.world.item.Items.WATER_BUCKET) ? Fluids.WATER : Fluids.LAVA;
                if ((fluidType == Fluids.EMPTY || fluidType == bucketFluid) && fluidAmount <= 7000) {
                    try (Transaction tx = Transaction.openRoot()) {
                        long inserted = blockEntity.fluidTank.insert(0, net.neoforged.neoforge.transfer.fluid.FluidResource.of(bucketFluid), 1000, tx);
                        if (inserted == 1000) {
                            tx.commit();

                            ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
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

            if (carried.is(net.minecraft.world.item.Items.BUCKET)) {
                if (fluidAmount >= 1000 && fluidType != Fluids.EMPTY) {
                    net.minecraft.world.item.Item fullBucket = (fluidType == Fluids.LAVA) ? net.minecraft.world.item.Items.LAVA_BUCKET : net.minecraft.world.item.Items.WATER_BUCKET;
                    try (Transaction tx = Transaction.openRoot()) {
                        long extracted = blockEntity.fluidTank.extract(0, net.neoforged.neoforge.transfer.fluid.FluidResource.of(fluidType), 1000, tx);
                        if (extracted == 1000) {
                            tx.commit();

                            ItemStack filledBucket = new ItemStack(fullBucket);
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
                return;
            }
            return;
        }
        super.clicked(slotId, button, containerInput, player);
    }

    public int getProgress() { return this.data.get(0); }
    public int getMaxProgress() { return this.data.get(1); }
    public int getFluidAmount() { return this.data.get(2); }
    public int getFluidCapacity() { return this.data.get(3); }
    public int getFluidType() { return this.data.get(4); }

    public boolean isArrowTopActive() { return this.data.get(5) == 1; }
    public boolean isArrowMiddleActive() { return this.data.get(6) == 1; }
    public boolean isArrowBottomActive() { return this.data.get(7) == 1; }

    public int getEnergyAmount() { return this.data.get(8); }
    public int getEnergyCapacity() { return this.data.get(9); }

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
                if (stackInSlot.is(ModItems.SPEED_UPGRADE_1.get()) ||
                        stackInSlot.is(ModItems.SPEED_UPGRADE_2.get()) ||
                        stackInSlot.is(ModItems.SPEED_UPGRADE_3.get())) {
                    if (!this.moveItemStackTo(stackInSlot, 23, 26, false)) {
                        if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) return ItemStack.EMPTY;
                    }
                } else if (stackInSlot.has(net.minecraft.core.component.DataComponents.TOOL) ||
                        stackInSlot.is(net.minecraft.tags.ItemTags.PICKAXES) ||
                        stackInSlot.is(net.minecraft.tags.ItemTags.SHOVELS) ||
                        stackInSlot.is(net.minecraft.tags.ItemTags.AXES)) {
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
        return AbstractContainerMenu.stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
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