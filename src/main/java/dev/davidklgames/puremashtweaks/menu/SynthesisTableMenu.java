package dev.davidklgames.puremashtweaks.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class SynthesisTableMenu extends AbstractContainerMenu {
    private final SynthesisTableBlockEntity blockEntity;

    // Native Minecraft data synchronizer for syncing button states.
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> blockEntity.isAutomationActive() ? 1 : 0;
                case 1 -> blockEntity.getAutomationMode();
                case 2 -> blockEntity.getMatchingVanillaRecipesCount();
                case 3 -> blockEntity.getSelectedVanillaRecipeIndex();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> blockEntity.setAutomationActive(value == 1);
                case 1 -> blockEntity.setAutomationMode(value);
                case 2 -> blockEntity.setMatchingVanillaRecipesCount(value);
                case 3 -> blockEntity.setSelectedVanillaRecipeIndex(value);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public int getMatchingVanillaRecipesCount() {
        return this.data.get(2);
    }

    public int getSelectedVanillaRecipeIndex() {
        return this.data.get(3);
    }

    public SynthesisTableMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(id, playerInv, (SynthesisTableBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @SuppressWarnings("removal")
    public SynthesisTableMenu(int id, Inventory playerInv, SynthesisTableBlockEntity entity) {
        super(ModMenus.SYNTHESIS_TABLE_MENU.get(), id);
        this.blockEntity = entity;

        // Registers the data synchronizers.
        this.addDataSlots(this.data);

        // GRID 9x9 (Slots 0 to 80)
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new SlotItemHandler(blockEntity.inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }

        this.addSlot(new SlotItemHandler(blockEntity.inventory, 81, 206, 89) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) { return false; }

            @Override
            public boolean mayPickup(@NotNull Player player) {
                // Prevents the player from removing the result item if automation is enabled in "Add" mode.
                return !blockEntity.isAutomationActive() || blockEntity.getAutomationMode() != 1;
            }

            @Override
            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                super.onTake(player, stack);
                blockEntity.consumeCraftingIngredients();
            }
        });

        this.addSlot(new SlotItemHandler(blockEntity.inventory, 82, 206, 50) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return blockEntity.isAutomationActive() && stack.is(ModItems.MEMORY_CARD.get());
            }

            @Override
            public boolean isActive() {
                return blockEntity.isAutomationActive();
            }
        });

        addPlayerInventory(playerInv);
    }

    // Receiver for client button actions, without the need for complex packages.
    @Override
    public boolean clickMenuButton(@NonNull Player player, int buttonId) {
        if (buttonId == 0) {
            this.blockEntity.setAutomationActive(!this.blockEntity.isAutomationActive());
            return true;
        } else if (buttonId == 1) {
            this.blockEntity.setAutomationMode(0); // Actives Craft Mode
            return true;
        } else if (buttonId == 2) {
            this.blockEntity.setAutomationMode(1); // Active Encoder Mode
            return true;
        } else if (buttonId == 3) {
            if (this.blockEntity.isAutomationActive() && this.blockEntity.getAutomationMode() == 1) {
                this.blockEntity.encodeCurrentRecipeToCard();
            }
            return true;
        } else if (buttonId == 4) {
            this.blockEntity.cycleVanillaRecipe();
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem() && slot.isActive()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            if (index < 83) {
                if (!this.moveItemStackTo(stackInSlot, 83, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (blockEntity.isAutomationActive() && blockEntity.getAutomationMode() == 1 && stackInSlot.is(ModItems.MEMORY_CARD.get())) {
                    if (!this.moveItemStackTo(stackInSlot, 82, 83, false)) {
                        if (!this.moveItemStackTo(stackInSlot, 0, 81, false)) return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stackInSlot, 0, 81, false)) return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return AbstractContainerMenu.stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 39 + j * 18, 198 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 39 + k * 18, 256));
        }
    }

    public SynthesisTableBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}