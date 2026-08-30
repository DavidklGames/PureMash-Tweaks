package dev.davidklgames.puremashtweaks.menu;

import dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.cable.CableRedstoneMode;
import dev.davidklgames.puremashtweaks.block.entity.cable.DistributionMode;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public class CableMenu extends AbstractContainerMenu {

    private final UniversalCableBlockEntity blockEntity;
    private Direction side = Direction.NORTH;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (blockEntity == null) return 0;
            return switch (index) {
                case 0 -> blockEntity.getSelectedTab(side);
                case 1 -> blockEntity.getDistributionMode(side).ordinal();
                case 2 -> blockEntity.getRedstoneMode(side).ordinal();
                case 3 -> blockEntity.getFilterMode(side) ? 1 : 0;
                case 4 -> blockEntity.hasUpgrade(side) ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (blockEntity == null) return;
            switch (index) {
                case 0 -> blockEntity.setSelectedTab(side, Math.clamp(value, 0, 2));
                case 1 -> blockEntity.setDistributionMode(side, DistributionMode.values()[Math.clamp(value, 0, 2)]);
                case 2 -> blockEntity.setRedstoneMode(side, CableRedstoneMode.values()[Math.clamp(value, 0, 2)]);
                case 3 -> blockEntity.setFilterMode(side, value == 1);
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public CableMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(id, playerInv, (UniversalCableBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
        if (extraData.readableBytes() > 0) {
            this.side = Direction.from3DDataValue(extraData.readInt());
        }
    }

    public CableMenu(int id, Inventory playerInv, UniversalCableBlockEntity entity) {
        super(ModMenus.CABLE_MENU.get(), id);
        this.blockEntity = entity;

        this.addDataSlots(this.data);

        if (entity != null) {
            this.addSlot(new SlotItemHandler(entity.getUpgradeInventory(), 0, 80, 82) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return UniversalCableBlockEntity.isUpgradeValid(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        addPlayerInventory(playerInv);
    }

    public UniversalCableBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public Direction getSide() {
        return this.side;
    }

    public void setSide(Direction side) {
        this.side = side;
    }

    public int getSelectedTab() {
        return this.data.get(0);
    }

    public DistributionMode getDistributionMode() {
        return DistributionMode.values()[this.data.get(1)];
    }

    public CableRedstoneMode getRedstoneMode() {
        return CableRedstoneMode.values()[this.data.get(2)];
    }

    public boolean isBlacklist() {
        return this.data.get(3) == 1;
    }

    public boolean hasUpgrade() {
        return this.data.get(4) == 1;
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int buttonId) {
        if (blockEntity == null) return false;

        if (buttonId == 0) {
            if (!hasUpgrade()) return false;
            DistributionMode nextMode = getDistributionMode().cycle();
            this.blockEntity.setDistributionMode(this.side, nextMode);
            return true;
        } else if (buttonId == 1) {
            if (!hasUpgrade()) return false;
            CableRedstoneMode nextRedstone = getRedstoneMode().cycle();
            this.blockEntity.setRedstoneMode(this.side, nextRedstone);
            return true;
        } else if (buttonId == 2) {
            if (!hasUpgrade() || getSelectedTab() == 0) return false;
            this.blockEntity.setFilterMode(this.side, !isBlacklist());
            return true;
        } else if (buttonId == 3) { // ADD FILTER
            if (!hasUpgrade() || getSelectedTab() == 0) return false;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, playerInv, p) -> new FilterMenu(id, playerInv, blockEntity, side, -1),
                                Component.empty()
                        ),
                        buf -> {
                            buf.writeBlockPos(blockEntity.getBlockPos());
                            buf.writeInt(side.get3DDataValue());
                            buf.writeInt(-1);
                        }
                );
            }
            return true;
        } else if (buttonId >= 100 && buttonId < 200) { // EDIT FILTER
            int targetIndex = buttonId - 100;
            if (!hasUpgrade() || getSelectedTab() == 0) return false;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, playerInv, p) -> new FilterMenu(id, playerInv, blockEntity, side, targetIndex),
                                Component.empty()
                        ),
                        buf -> {
                            buf.writeBlockPos(blockEntity.getBlockPos());
                            buf.writeInt(side.get3DDataValue());
                            buf.writeInt(targetIndex);
                        }
                );
            }
            return true;
        } else if (buttonId >= 200 && buttonId < 300) { // DELETE FILTER
            int targetIndex = buttonId - 200;
            if (!hasUpgrade() || getSelectedTab() == 0) return false;
            var filters = this.blockEntity.getFilters(this.side, this.getSelectedTab());
            if (targetIndex >= 0 && targetIndex < filters.size()) {
                var removedFilter = filters.remove(targetIndex);

                // Devolve o Distribution Filter para o jogador se houver um vinculado
                if (removedFilter != null && !removedFilter.getDestinationTool().isEmpty()) {
                    ItemStack tool = removedFilter.getDestinationTool().copy();
                    if (!player.getInventory().add(tool)) {
                        player.drop(tool, false);
                    }
                }

                this.blockEntity.setFilters(this.side, this.getSelectedTab(), filters);
                this.broadcastChanges();
            }
            return true;
        } else if (buttonId >= 10 && buttonId <= 12) {
            this.blockEntity.setSelectedTab(this.side, buttonId - 10);
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
                if (UniversalCableBlockEntity.isUpgradeValid(stackInSlot)) {
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
        return AbstractContainerMenu.stillValid(
                net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                blockEntity.getBlockState().getBlock()
        );
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 107 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 165));
        }
    }
}