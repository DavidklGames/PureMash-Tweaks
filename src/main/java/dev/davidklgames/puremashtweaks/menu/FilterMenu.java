package dev.davidklgames.puremashtweaks.menu;

import dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.cable.CableFilter;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import dev.davidklgames.puremashtweaks.registry.PureMashDataComponents;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@SuppressWarnings("removal")
public class FilterMenu extends AbstractContainerMenu {

    private final UniversalCableBlockEntity blockEntity;
    private Direction side = Direction.NORTH;
    private int filterIndex = -1;
    private final CableFilter filter;
    private final ItemStackHandler destinationToolHandler;
    private boolean submitted = false;

    public FilterMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(
                id,
                playerInv,
                (UniversalCableBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()),
                Direction.from3DDataValue(extraData.readInt()),
                extraData.readInt()
        );
    }

    public FilterMenu(int id, Inventory playerInv, UniversalCableBlockEntity entity, Direction side, int filterIndex) {
        super(ModMenus.FILTER_MENU.get(), id);
        this.blockEntity = entity;
        this.side = side != null ? side : Direction.NORTH;
        this.filterIndex = filterIndex;

        if (entity != null) {
            int currentTab = entity.getSelectedTab(this.side);
            List<CableFilter> filters = entity.getFilters(this.side, currentTab);
            if (this.filterIndex >= 0 && this.filterIndex < filters.size()) {
                this.filter = filters.get(this.filterIndex);
            } else {
                this.filter = new CableFilter();
            }
        } else {
            this.filter = new CableFilter();
        }

        this.destinationToolHandler = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isBoundFilter(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                syncDestinationState();
            }
        };

        // Carrega o Distribution Filter já salvo no filtro se existir
        if (this.filter != null) {
            if (!this.filter.getDestinationTool().isEmpty()) {
                this.destinationToolHandler.setStackInSlot(0, this.filter.getDestinationTool().copy());
            } else if (this.filter.getDestinationTag() != null && this.filter.getDestinationTag().contains("X")) {
                ItemStack tool = new ItemStack(ModItems.DISTRIBUTION_FILTER.get());
                tool.set(PureMashDataComponents.BOUND_CONTAINER.get(), this.filter.getDestinationTag().copy());
                this.destinationToolHandler.setStackInSlot(0, tool);
            }
        }

        this.addSlot(new SlotItemHandler(destinationToolHandler, 0, 81, 82) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return isBoundFilter(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) {
                return 1;
            }
        });

        addPlayerInventory(playerInv);
    }

    public FilterMenu(int id, Inventory playerInv, UniversalCableBlockEntity entity) {
        this(id, playerInv, entity, Direction.NORTH, -1);
    }

    public static boolean isBoundFilter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(ModItems.DISTRIBUTION_FILTER.get())) return false;
        CompoundTag tag = stack.get(PureMashDataComponents.BOUND_CONTAINER.get());
        return tag != null && tag.contains("X");
    }

    public void markSubmitted() {
        this.submitted = true;
    }

    public void syncDestinationState() {
        if (this.filter == null) return;

        ItemStack inSlot = this.destinationToolHandler.getStackInSlot(0);
        if (isBoundFilter(inSlot)) {
            this.filter.setDestinationTool(inSlot.copy());
            this.filter.setDestinationTag(inSlot.get(PureMashDataComponents.BOUND_CONTAINER.get()));

            if (this.filter.getPriority() <= 0) {
                this.filter.setPriority(1);
            }
            if (this.filter.getStockLimit() <= 0) {
                this.filter.setStockLimit(64);
            }
        } else {
            this.filter.setDestinationTool(ItemStack.EMPTY);
            this.filter.setDestinationTag(null);
            this.filter.setPriority(0);
            this.filter.setStockLimit(0);
        }
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        // Se o jogador estava criando um novo filtro (-1) e cancelou/fechou sem submeter, devolve o Distribution Filter colocado no slot
        if (!this.submitted && this.filterIndex == -1 && !player.level().isClientSide()) {
            ItemStack inSlot = this.destinationToolHandler.getStackInSlot(0);
            if (!inSlot.isEmpty() && isBoundFilter(inSlot)) {
                if (!player.getInventory().add(inSlot)) {
                    player.drop(inSlot, false);
                }
            }
        }
    }

    public UniversalCableBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public Direction getSide() {
        return this.side;
    }

    public int getFilterIndex() {
        return this.filterIndex;
    }

    public CableFilter getFilter() {
        return this.filter;
    }

    public boolean hasBoundDestination() {
        ItemStack tool = this.destinationToolHandler.getStackInSlot(0);
        return isBoundFilter(tool);
    }

    public CompoundTag getBoundDestinationTag() {
        ItemStack tool = this.destinationToolHandler.getStackInSlot(0);
        if (isBoundFilter(tool)) {
            return tool.get(PureMashDataComponents.BOUND_CONTAINER.get());
        }
        return this.filter != null ? this.filter.getDestinationTag() : null;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (isBoundFilter(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
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
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 198));
        }
    }
}