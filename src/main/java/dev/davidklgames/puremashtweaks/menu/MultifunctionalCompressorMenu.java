package dev.davidklgames.puremashtweaks.menu;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public class MultifunctionalCompressorMenu extends AbstractContainerMenu {
    private final MultifunctionalCompressorBlockEntity blockEntity;
    private final ContainerData data;
    private final int[] localData = new int[12];

    public MultifunctionalCompressorMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(id, playerInv, (MultifunctionalCompressorBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MultifunctionalCompressorMenu(int id, Inventory playerInv, MultifunctionalCompressorBlockEntity entity) {
        super(ModMenus.MULTIFUNCTIONAL_COMPRESSOR_MENU.get(),id);
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
                        default -> {
                            if (index >= 5 && index <= 10) {
                                yield blockEntity.getSideConfig(Direction.values()[index - 5]);
                            }
                            if (index == 11) {
                                yield net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(blockEntity.getSingularityItem());
                            }
                            yield 0;
                        }
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
                        default -> {
                            if (index >= 5 && index <= 10) {
                                blockEntity.setSideConfig(Direction.values()[index - 5], value);
                            }
                        }
                    }
                }
            }

            @Override
            public int getCount() {
                return 12;
            }
        };

        this.addDataSlots(this.data);

        net.neoforged.neoforge.items.IItemHandler invHandler = (entity != null) ? entity.inventory : new net.neoforged.neoforge.items.ItemStackHandler(5);

        this.addSlot(new SlotItemHandler(invHandler, 0, 39, 35));

        this.addSlot(new SlotItemHandler(invHandler, 1, 120, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        this.addSlot(new SlotItemHandler(invHandler, 2, 182, 18));
        this.addSlot(new SlotItemHandler(invHandler, 3, 182, 39));
        this.addSlot(new SlotItemHandler(invHandler, 4, 182, 60));

        addPlayerInventory(playerInv);
    }

    public net.minecraft.world.item.Item getSingularityItem() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(this.data.get(11));
    }

    public int getMode() { return this.data.get(0); }
    public int getProgress() { return this.data.get(1); }
    public int getMaxProgress() { return this.data.get(2); }
    public int getSingularityCount() { return this.data.get(3); }
    public boolean isLocked() { return this.data.get(4) == 1; }
    public int getSideConfig(Direction side) { return this.data.get(5 + side.get3DDataValue()); }
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
        } else if (buttonId >= 2 && buttonId <= 7) {
            Direction side = Direction.values()[buttonId - 2];
            int nextConfig = (this.getSideConfig(side) + 1) % 3;
            this.blockEntity.setSideConfig(side, nextConfig);
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
                if (!this.moveItemStackTo(stackInSlot, 5, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.is(dev.davidklgames.puremashtweaks.registry.ModItems.SPEED_UPGRADE_1.get()) ||
                        stackInSlot.is(dev.davidklgames.puremashtweaks.registry.ModItems.SPEED_UPGRADE_2.get()) ||
                        stackInSlot.is(dev.davidklgames.puremashtweaks.registry.ModItems.SPEED_UPGRADE_3.get())) {
                    if (!this.moveItemStackTo(stackInSlot, 2, 5, false)) {
                        if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity == null) return true; // Client Fallback
        assert blockEntity.getLevel() != null;
        return AbstractContainerMenu.stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public MultifunctionalCompressorBlockEntity getBlockEntity() { return this.blockEntity; }
}