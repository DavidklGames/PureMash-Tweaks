package dev.davidklgames.puremashtweaks.menu;

import dev.davidklgames.puremashtweaks.block.entity.PureMashGeneratorBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class PureMashGeneratorMenu extends AbstractContainerMenu {
    private final PureMashGeneratorBlockEntity blockEntity;
    private final ContainerData data;
    private final int[] localData = new int[14];

    public PureMashGeneratorMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(id, playerInv, (PureMashGeneratorBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public PureMashGeneratorMenu(int id, Inventory playerInv, PureMashGeneratorBlockEntity entity) {
        super(ModMenus.PUREMASH_GENERATOR_MENU.get(), id);
        this.blockEntity = entity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (playerInv.player.level().isClientSide()) {
                    return localData[index];
                }
                if (blockEntity != null) {
                    return switch (index) {
                        case 0 -> blockEntity.getBurnTime();
                        case 1 -> blockEntity.getMaxBurnTime();
                        case 2 -> (int) (blockEntity.energyTank.getAmountAsLong() & 0xFFFFL);
                        case 3 -> (int) ((blockEntity.energyTank.getAmountAsLong() >> 16) & 0xFFFFL);
                        case 4 -> (int) (blockEntity.getEnergyCapacity() & 0xFFFFL);
                        case 5 -> (int) ((blockEntity.getEnergyCapacity() >> 16) & 0xFFFFL);

                        case 6 -> (int) (blockEntity.getEnergyCapacity() & 0xFFFF);
                        case 7 -> (int) ((blockEntity.getEnergyCapacity() >> 16) & 0xFFFF);
                        case 8 -> (int) ((blockEntity.getEnergyCapacity() >> 32) & 0xFFFF);
                        case 9 -> (int) ((blockEntity.getEnergyCapacity() >> 48) & 0xFFFF);

                        case 10 -> blockEntity.getCurrentGenerationRate();
                        case 11 -> blockEntity.getTemperatureCelsius();
                        case 12 -> blockEntity.getEffectiveWaterAmount();
                        case 13 -> (int) blockEntity.steamTank.getAmountAsLong(0);
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
            public int getCount() { return 14; }
        };

        this.addDataSlots(this.data);

        net.neoforged.neoforge.items.IItemHandler invHandler = (entity != null) ? entity.inventory : new net.neoforged.neoforge.items.ItemStackHandler(8);

        // Slot 0: Fuel Input
        this.addSlot(new SlotItemHandler(invHandler, 0, 30, 56) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return PureMashGeneratorBlockEntity.isFuelValid(stack);
            }
        });

        // Slots 1, 2, 3: Upgrade Slots (Limites por slot de upgrade)
        this.addSlot(new SlotItemHandler(invHandler, 1, 182, 18) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 2, 182, 39) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 3, 182, 60) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }
        });

        // Slots 4, 5, 6: Waste Output
        this.addSlot(new SlotItemHandler(invHandler, 4, 80, 56) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 5, 98, 56) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });
        this.addSlot(new SlotItemHandler(invHandler, 6, 116, 56) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        // Slot 7: Energy Charging Slot
        this.addSlot(new SlotItemHandler(invHandler, 7, 182, 88));

        addPlayerInventory(playerInv);
    }

    public int getBurnTime() { return this.data.get(0); }
    public int getMaxBurnTime() { return this.data.get(1); }

    public long getEnergyAmountLong() {
        long low = this.data.get(2) & 0xFFFFL;
        long high = this.data.get(3) & 0xFFFFL;
        return low | (high << 16);
    }

    public long getEnergyCapacityLong() {
        long low = this.data.get(4) & 0xFFFFL;
        long high = this.data.get(5) & 0xFFFFL;
        return low | (high << 16);
    }

    public int getGenerationRate() { return this.data.get(10); }
    public int getTemperature() { return this.data.get(11); }
    public int getWaterAmount() { return this.data.get(12); }
    public int getSteamAmount() { return this.data.get(13); }

    public boolean isBurning() { return this.getBurnTime() > 0; }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < 8) {
                if (!this.moveItemStackTo(stackInSlot, 8, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (PureMashGeneratorBlockEntity.isUpgradeValid(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 1, 4, false)) return ItemStack.EMPTY;
                } else if (PureMashGeneratorBlockEntity.isFuelValid(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.moveItemStackTo(stackInSlot, 7, 8, false)) return ItemStack.EMPTY;
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

    public PureMashGeneratorBlockEntity getBlockEntity() { return this.blockEntity; }
}