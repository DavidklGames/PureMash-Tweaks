package dev.davidklgames.puremashtweaks.menu;

import dev.davidklgames.puremashtweaks.block.entity.ChunkLoaderBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("all")
public class ChunkLoaderMenu extends AbstractContainerMenu {
    private final ChunkLoaderBlockEntity blockEntity;
    private final ContainerData data;

    public ChunkLoaderMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(id, playerInv, (ChunkLoaderBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ChunkLoaderMenu(int id, Inventory playerInv, ChunkLoaderBlockEntity entity) {
        super(ModMenus.CHUNK_LOADER_MENU.get(), id);
        this.blockEntity = entity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (blockEntity != null) {
                    return switch (index) {
                        case 0 -> blockEntity.getActiveLevel();
                        case 1 -> blockEntity.isShowingBoundary() ? 1 : 0;
                        case 2 -> blockEntity.hasCoreInstalled() ? 1 : 0;
                        default -> 0;
                    };
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (blockEntity != null) {
                    switch (index) {
                        case 0 -> blockEntity.setActiveLevel(value);
                        case 1 -> blockEntity.setShowingBoundary(value == 1);
                    }
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };

        this.addDataSlots(this.data);

        this.addSlot(new SlotItemHandler(blockEntity.inventory, 0, 80, 56));

        addPlayerInventory(playerInv);
    }

    public int getActiveLevel() { return this.data.get(0); }
    public boolean isShowingBoundary() { return this.data.get(1) == 1; }

    public boolean hasCoreInstalled() {
        if (this.slots.isEmpty()) return false;

        ItemStack stackInSlot = this.slots.getFirst().getItem();
        return !stackInSlot.isEmpty() && stackInSlot.is(ModItems.MOLDELONIAN_CORE.get());
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int buttonId) {
        if (blockEntity == null) return false;

        if (buttonId == 0) {
            int currentLevel = this.getActiveLevel();
            if (currentLevel > 0) {
                this.blockEntity.setActiveLevel(currentLevel - 1);
                return true;
            }
        } else if (buttonId == 1) {
            int currentLevel = this.getActiveLevel();
            if (currentLevel < 5) {
                int nextLevel = currentLevel + 1;

                if (nextLevel >= 3 && !hasCoreInstalled()) {
                    if (!player.level().isClientSide()) {
                        player.sendSystemMessage(Component.translatable("chat.puremashtweaks.chunk_loader.needs_core"));
                    }
                    return false;
                }

                this.blockEntity.setActiveLevel(nextLevel);
                return true;
            }
        } else if (buttonId == 2) {
            this.blockEntity.setShowingBoundary(!this.isShowingBoundary());
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

            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.is(ModItems.MOLDELONIAN_CORE.get())) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
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

    public ChunkLoaderBlockEntity getBlockEntity() { return this.blockEntity; }
}