package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.menu.CableMenu;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public abstract class CableBlockEntity extends BlockEntity implements MenuProvider {

    protected final boolean[] extractingSides = new boolean[6];
    protected final boolean[] disconnectedSides = new boolean[6];

    protected final ItemStackHandler upgradeInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                ItemStack stack = getStackInSlot(slot);
                if (!stack.isEmpty() && CableBlockEntity.this instanceof UniversalCableBlockEntity universalCable) {
                    for (Direction side : Direction.values()) {
                        universalCable.loadConfigurationFromUpgrade(side, stack);
                    }
                }
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isUpgradeValid(stack);
        }
    };

    public CableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static boolean isUpgradeValid(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModItems.SPEED_UPGRADE_1.get()) ||
                stack.is(ModItems.SPEED_UPGRADE_2.get()) ||
                stack.is(ModItems.SPEED_UPGRADE_3.get());
    }

    public ItemStackHandler getUpgradeInventory() {
        return this.upgradeInventory;
    }

    public boolean isExtracting(Direction side) {
        return this.extractingSides[side.get3DDataValue()];
    }

    public void setExtracting(Direction side, boolean extracting) {
        this.extractingSides[side.get3DDataValue()] = extracting;
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isDisconnected(Direction side) {
        return this.disconnectedSides[side.get3DDataValue()];
    }

    public void setDisconnected(Direction side, boolean disconnected) {
        this.disconnectedSides[side.get3DDataValue()] = disconnected;
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.upgradeInventory.serialize(output);
        for (int i = 0; i < 6; i++) {
            output.putBoolean("Extracting_" + i, this.extractingSides[i]);
            output.putBoolean("Disconnected_" + i, this.disconnectedSides[i]);
        }
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.upgradeInventory.deserialize(input);
        for (int i = 0; i < 6; i++) {
            this.extractingSides[i] = input.getBooleanOr("Extracting_" + i, false);
            this.disconnectedSides[i] = input.getBooleanOr("Disconnected_" + i, false);
        }
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            net.minecraft.world.Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.upgradeInventory.getStackInSlot(0));
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NonNull Inventory playerInv, @NonNull Player player) {
        if (this instanceof UniversalCableBlockEntity universalCable) {
            return new CableMenu(id, playerInv, universalCable);
        }
        return null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public int getTransferRateMultiplier() {
        ItemStack upgrade = this.upgradeInventory.getStackInSlot(0);
        if (upgrade.isEmpty()) return 1;

        if (upgrade.is(ModItems.SPEED_UPGRADE_1.get())) {
            return PureMashTweaksConfig.COMMON.speedUpgrade1Power.get();
        } else if (upgrade.is(ModItems.SPEED_UPGRADE_2.get())) {
            return PureMashTweaksConfig.COMMON.speedUpgrade2Power.get();
        } else if (upgrade.is(ModItems.SPEED_UPGRADE_3.get())) {
            return PureMashTweaksConfig.COMMON.speedUpgrade3Power.get();
        }
        return 1;
    }
}