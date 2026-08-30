package dev.davidklgames.puremashtweaks.menu;

import dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public class MultifunctionalCompressorMenu extends AbstractContainerMenu {
    private final MultifunctionalCompressorBlockEntity blockEntity;
    private final ContainerData data;
    private final int[] localData = new int[10];

    public MultifunctionalCompressorMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(id, playerInv, (MultifunctionalCompressorBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MultifunctionalCompressorMenu(int id, Inventory playerInv, MultifunctionalCompressorBlockEntity entity) {
        super(ModMenus.MULTIFUNCTIONAL_COMPRESSOR_MENU.get(), id);
        this.blockEntity = entity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (playerInv.player.level().isClientSide()) {
                    return localData[index];
                }

                if (blockEntity != null) {
                    return switch (index) {
                        case 0 -> blockEntity.getMode();
                        case 1 -> blockEntity.getProgress();
                        case 2 -> blockEntity.getMaxProgress();
                        case 3 -> blockEntity.getSingularityCount();
                        case 4 -> blockEntity.isLocked() ? 1 : 0;
                        case 5 -> BuiltInRegistries.ITEM.getId(blockEntity.getSingularityItem());
                        case 6 -> (int) (blockEntity.energyTank.getAmountAsLong() & 0xFFFF);
                        case 7 -> (int) ((blockEntity.energyTank.getAmountAsLong() >> 16) & 0xFFFF);
                        case 8 -> (int) (blockEntity.getEnergyCapacity() & 0xFFFF);
                        case 9 -> (int) ((blockEntity.getEnergyCapacity() >> 16) & 0xFFFF);
                        default -> 0;
                    };
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                localData[index] = value;

                if (!playerInv.player.level().isClientSide() && blockEntity != null) {
                    switch (index) {
                        case 0 -> blockEntity.setMode(value);
                        case 4 -> blockEntity.setLocked(value == 1);
                    }
                }
            }

            @Override
            public int getCount() {
                return 10;
            }
        };

        this.addDataSlots(this.data);

        IItemHandler invHandler = (entity != null) ? entity.inventory : new ItemStackHandler(5);

        // Slot 0: Input (Moved down +9px -> X=39, Y=44)
        this.addSlot(new SlotItemHandler(invHandler, 0, 39, 44));

        // Slot 1: Output (Moved down +9px -> X=120, Y=44)
        this.addSlot(new SlotItemHandler(invHandler, 1, 120, 44) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        // Slots 2, 3, 4: Machine Upgrades (Standardized X=182, Y=18, 39, 60)
        this.addSlot(new SlotItemHandler(invHandler, 2, 182, 18) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 3, 182, 39) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 4, 182, 60) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });

        addPlayerInventory(playerInv);
    }

    public Item getSingularityItem() {
        return BuiltInRegistries.ITEM.byId(this.data.get(5));
    }

    public int getMode() { return this.data.get(0); }
    public int getProgress() { return this.data.get(1); }
    public int getMaxProgress() { return this.data.get(2); }
    public int getSingularityCount() { return this.data.get(3); }
    public boolean isLocked() { return this.data.get(4) == 1; }

    public long getEnergyAmountLong() {
        long low = this.data.get(6) & 0xFFFFL;
        long high = this.data.get(7) & 0xFFFFL;
        return low | (high << 16);
    }

    public long getEnergyCapacityLong() {
        long low = this.data.get(8) & 0xFFFFL;
        long high = this.data.get(9) & 0xFFFFL;
        return low | (high << 16);
    }

    public net.minecraft.core.BlockPos getBlockPos() {
        return this.blockEntity != null ? this.blockEntity.getBlockPos() : net.minecraft.core.BlockPos.ZERO;
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int buttonId) {
        if (blockEntity == null) return false;
        if (buttonId == 0) {
            int nextMode = (this.getMode() + 1) % 3;
            this.blockEntity.setMode(nextMode);
            return true;
        } else if (buttonId == 1) {
            this.blockEntity.setLocked(!this.isLocked());
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < 5) {
                if (!this.moveItemStackTo(stackInSlot, 5, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (MultifunctionalCompressorBlockEntity.isUpgradeValid(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 2, 5, false)) {
                        if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
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
        // Player inventory rows starting at Y=99 (border 7, 98 -> slot 8, 99)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 99 + i * 18));
            }
        }
        // Player hotbar at Y=157
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 157));
        }
    }

    public MultifunctionalCompressorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}