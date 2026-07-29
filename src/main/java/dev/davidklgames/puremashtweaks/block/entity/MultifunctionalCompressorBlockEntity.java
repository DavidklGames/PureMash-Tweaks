package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("removal")
public class MultifunctionalCompressorBlockEntity extends BlockEntity implements MenuProvider {

    private int mode = 0;
    private int progress = 0;
    private int maxProgress = 100;
    private int singularityCount = 0;
    private boolean locked = false;
    private net.minecraft.world.item.Item lockedItem = net.minecraft.world.item.Items.AIR;
    private net.minecraft.world.item.Item singularityItem = net.minecraft.world.item.Items.AIR;
    private final int[] sideConfig = new int[]{0, 0, 0, 0, 0, 0};

    public net.minecraft.world.item.Item getSingularityItem() {
        return this.singularityItem;
    }

    public class CompressorInventory extends ItemStackHandler {
        public CompressorInventory() { super(5); }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 1) return false;
            if (slot >= 2 && slot <= 4) {
                return stack.is(ModItems.SPEED_UPGRADE_1.get()) ||
                        stack.is(ModItems.SPEED_UPGRADE_2.get()) ||
                        stack.is(ModItems.SPEED_UPGRADE_3.get());
            }
            if (slot == 0) {
                if (locked && lockedItem != net.minecraft.world.item.Items.AIR) {
                    return stack.is(lockedItem);
                }
            }
            return true;
        }

        public void setStackSilent(int slot, ItemStack stack) {
            this.stacks.set(slot, stack);
        }

        public ItemStack[] createSnapshot() {
            ItemStack[] snap = new ItemStack[5];
            for (int i = 0; i < 5; i++) snap[i] = this.stacks.get(i).copy();
            return snap;
        }

        public void restoreSnapshot(ItemStack[] snap) {
            for (int i = 0; i < 5; i++) this.stacks.set(i, snap[i]);
        }
    }

    public final CompressorInventory inventory = new CompressorInventory();

    public MultifunctionalCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MULTIFUNCIONAL_COMPRESSOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MultifunctionalCompressorBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        ItemStack input = blockEntity.inventory.getStackInSlot(0);
        ItemStack output = blockEntity.inventory.getStackInSlot(1);

        if (blockEntity.mode == 1) {
            blockEntity.tickSingularityMode(input, output);
        } else {
            blockEntity.tickStandardMode(input, output);
        }
    }

    private void tickStandardMode(ItemStack input, ItemStack output) {
        var recipe = dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.level, input, this.mode);
        if (recipe != null) {
            this.maxProgress = recipe.time();
        } else {
            updateMaxProgress();
        }

        if (canProcess(input, output)) {
            this.progress += getSpeedLevel();

            if (this.progress >= this.maxProgress) {
                processItem(input, output);

                if (getSpeedLevel() >= this.maxProgress && canProcess(input, output)) {
                    this.progress = this.maxProgress / 2;
                } else {
                    this.progress = 0;
                }
            }
            setChanged();
        } else {
            if (this.progress > 0) {
                this.progress = Math.max(0, this.progress - 2);
                setChanged();
            }
        }
    }

    private void tickSingularityMode(ItemStack input, ItemStack output) {
        if (this.singularityCount == 0) {
            this.singularityItem = net.minecraft.world.item.Items.AIR;
        }

        if (this.singularityItem == net.minecraft.world.item.Items.AIR && !input.isEmpty()) {
            var recipe = dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.level, input, 1);
            if (recipe != null) {
                this.singularityItem = input.getItem();
            }
        }

        if (this.singularityItem != net.minecraft.world.item.Items.AIR) {
            ItemStack tempInput = new ItemStack(this.singularityItem);
            var recipe = dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.level, tempInput, 1);

            if (recipe == null) {
                this.singularityCount = 0;
                this.singularityItem = net.minecraft.world.item.Items.AIR;
                this.progress = 0;
                setChanged();
                return;
            }

            this.maxProgress = recipe.time();

            if (this.singularityCount < recipe.cost()) {
                this.progress = 0;
                if (!input.isEmpty() && input.is(this.singularityItem)) {
                    int needed = recipe.cost() - this.singularityCount;
                    int toConsume = Math.min(input.getCount(), needed);
                    input.shrink(toConsume);
                    this.singularityCount += toConsume;
                    setChanged();
                }
            }

            if (this.singularityCount >= recipe.cost()) {
                ItemStack result = recipe.result();
                boolean canOutput = output.isEmpty() ||
                        (ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize());

                if (canOutput) {
                    this.progress += getSpeedLevel();
                    if (this.progress >= this.maxProgress) {
                        if (output.isEmpty()) {
                            this.inventory.setStackInSlot(1, result.copy());
                        } else {
                            output.grow(result.getCount());
                        }
                        this.singularityCount = 0;

                        if (getSpeedLevel() >= this.maxProgress && !input.isEmpty() && input.is(this.singularityItem)) {
                            this.progress = this.maxProgress / 2;
                        } else {
                            this.progress = 0;
                            this.singularityItem = net.minecraft.world.item.Items.AIR;
                        }
                    }
                    setChanged();
                }
            }
        } else {
            this.singularityCount = 0;
            this.progress = 0;
            setChanged();
        }
    }

    private void updateMaxProgress() {
        if (this.mode == 1) {
            this.maxProgress = PureMashTweaksConfig.COMPRESSOR_SPEED_SINGULARITY.get();
        } else {
            this.maxProgress = PureMashTweaksConfig.COMPRESSOR_SPEED_ITEMS.get();
        }
    }

    private boolean canProcess(ItemStack input, ItemStack output) {
        if (input.isEmpty()) return false;
        dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.CustomRecipeData recipe = dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.level, input, this.mode);
        if (recipe == null) return false;
        if ((this.mode == 0 || this.mode == 2) && input.getCount() < recipe.cost()) return false;

        ItemStack result = recipe.result();
        return !result.isEmpty() && (output.isEmpty() ||
                (ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize()));
    }

    private void processItem(ItemStack input, ItemStack output) {
        dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.CustomRecipeData recipe =
                dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.level, input, this.mode);

        if (recipe == null) return;

        ItemStack result = recipe.result().copy();

        if (this.mode == 2) {
            double chance = getDuplicationChance();
            if (chance > 0.0 && this.level != null && this.level.getRandom().nextDouble() < chance) {
                int originalCount = result.getCount();
                int spaceLeft = output.isEmpty() ? result.getMaxStackSize() : (result.getMaxStackSize() - output.getCount());

                int extraToGive = Math.max(1, originalCount / 3);

                int extraAllowed = spaceLeft - originalCount;
                int toAdd = Math.min(extraToGive, extraAllowed);

                if (toAdd > 0) {
                    result.grow(toAdd);
                }
            }
        }

        switch (this.mode) {
            case 0, 2 -> {
                input.shrink(recipe.cost());
                if (output.isEmpty()) {
                    inventory.setStackInSlot(1, result);
                } else {
                    output.grow(result.getCount());
                }
            }
            case 1 -> {
                input.shrink(1);
                this.singularityCount++;

                if (this.singularityCount >= recipe.cost()) {
                    if (output.isEmpty()) {
                        inventory.setStackInSlot(1, result);
                    } else {
                        output.grow(result.getCount());
                    }
                    this.singularityCount = 0;
                }
            }
        }
    }

    public int getSpeedLevel() {
        int speed = 1;
        int t3Count = 0;

        for (int i = 2; i <= 4; i++) {
            ItemStack upgrade = this.inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.SPEED_UPGRADE_1.get())) {
                speed += PureMashTweaksConfig.MACHINE_SPEED_UPGRADE_1_POWER.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.SPEED_UPGRADE_2.get())) {
                speed += PureMashTweaksConfig.MACHINE_SPEED_UPGRADE_2_POWER.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.SPEED_UPGRADE_3.get())) {
                t3Count += upgrade.getCount();
            }
        }

        if (t3Count > 0) {
            int basePower = PureMashTweaksConfig.MACHINE_SPEED_UPGRADE_3_POWER.get();
            if (t3Count == 1) {
                speed += basePower;
            } else if (t3Count == 2) {
                speed += basePower * 4;
            } else {
                speed += basePower * 25;
            }
        }

        return speed;
    }

    private double getDuplicationChance() {
        if (!PureMashTweaksConfig.ENABLE_DUPLICATION.get()) {
            return 0.0;
        }

        double chance = 0.0;
        for (int i = 2; i <= 4; i++) {
            ItemStack upgrade = inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.SPEED_UPGRADE_2.get())) {
                chance += PureMashTweaksConfig.MACHINE_UPGRADE_2_DUPLICATION_CHANCE.get() * upgrade.getCount();
            }
            if (upgrade.is(ModItems.SPEED_UPGRADE_3.get())) {
                chance += PureMashTweaksConfig.MACHINE_UPGRADE_3_DUPLICATION_CHANCE.get() * upgrade.getCount();
            }
        }
        return chance;
    }

    public int getMode() { return this.mode; }

    public void setMode(int mode) {
        this.mode = mode;
        this.progress = 0;
        this.singularityCount = 0;
        this.singularityItem = net.minecraft.world.item.Items.AIR;
        setChanged();
    }

    public int getProgress() { return this.progress; }
    public int getMaxProgress() { return this.maxProgress; }
    public int getSingularityCount() { return this.singularityCount; }

    public boolean isLocked() { return this.locked; }

    public void setLocked(boolean locked) {
        this.locked = locked;
        if (locked) {
            ItemStack input = this.inventory.getStackInSlot(0);
            this.lockedItem = input.isEmpty() ? net.minecraft.world.item.Items.AIR : input.getItem();
        } else {
            this.lockedItem = net.minecraft.world.item.Items.AIR;
        }
        setChanged();
    }

    public int getSideConfig(Direction side) { return this.sideConfig[side.get3DDataValue()]; }
    public void setSideConfig(Direction side, int config) {
        this.sideConfig[side.get3DDataValue()] = config;
        setChanged();
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.inventory.serialize(output);
        output.putInt("Mode", this.mode);
        output.putInt("Progress", this.progress);
        output.putInt("SingularityCount", this.singularityCount);
        output.putBoolean("Locked", this.locked);
        output.putString("LockedItem", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this.lockedItem).toString());
        output.putString("SingularityItem", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this.singularityItem).toString());
        for (int i = 0; i < 6; i++) output.putInt("SideConfig_" + i, this.sideConfig[i]);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.inventory.deserialize(input);
        this.mode = input.getIntOr("Mode", 0);
        this.progress = input.getIntOr("Progress", 0);
        this.singularityCount = input.getIntOr("SingularityCount", 0);
        this.locked = input.getBooleanOr("Locked", false);
        String lockedItemStr = input.getStringOr("LockedItem", "minecraft:air");
        this.lockedItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(Objects.requireNonNull(Identifier.tryParse(lockedItemStr))).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
        String singItemStr = input.getStringOr("SingularityItem", "minecraft:air");
        this.singularityItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(Objects.requireNonNull(Identifier.tryParse(singItemStr))).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
        for (int i = 0; i < 6; i++) this.sideConfig[i] = input.getIntOr("SideConfig_" + i, 0);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.puremashtweaks.multifunctional_compressor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NonNull Inventory playerInv, @NonNull Player player) {
        return new MultifunctionalCompressorMenu(id, playerInv, this);
    }

    private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() { return inventory.createSnapshot(); }
        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) { inventory.restoreSnapshot(snapshot); }
        @Override
        protected void onRootCommit(ItemStack[] originalState) { setChanged(); }
    };

    public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> getAutomationHandler(@Nullable Direction side) {
        return new net.neoforged.neoforge.transfer.ResourceHandler<>() {
            @Override
            public int size() { return 2; }

            @Override
            public net.neoforged.neoforge.transfer.item.@NonNull ItemResource getResource(int slot) {
                return net.neoforged.neoforge.transfer.item.ItemResource.of(inventory.getStackInSlot(slot));
            }

            @Override
            public long getAmountAsLong(int slot) { return inventory.getStackInSlot(slot).getCount(); }

            @Override
            public long getCapacityAsLong(int slot, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource) {
                return inventory.getSlotLimit(slot);
            }

            @Override
            public boolean isValid(int slot, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource) {
                if (slot == 1) return false;
                return inventory.isItemValid(slot, resource.toStack(1));
            }

            @Override
            public int insert(int index, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (side != null && sideConfig[side.get3DDataValue()] != 1) return 0;
                if (index != 0 || amount <= 0 || resource.isEmpty()) return 0;

                ItemStack stack = resource.toStack(amount);
                ItemStack remainder = inventory.insertItem(0, stack, true);
                int inserted = amount - remainder.getCount();

                if (inserted > 0) {
                    journal.updateSnapshots(transaction);
                    ItemStack newStack = inventory.getStackInSlot(0).copy();
                    if (newStack.isEmpty()) newStack = stack.copyWithCount(inserted);
                    else newStack.grow(inserted);
                    inventory.setStackSilent(0, newStack);
                }
                return inserted;
            }

            @Override
            public int extract(int index, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (side != null && sideConfig[side.get3DDataValue()] != 2) return 0;
                if (index != 1 || amount <= 0 || resource.isEmpty()) return 0;

                ItemStack currentResult = inventory.getStackInSlot(1);
                if (currentResult.isEmpty() || !net.neoforged.neoforge.transfer.item.ItemResource.of(currentResult).equals(resource)) {
                    return 0;
                }

                int extracted = Math.min(amount, currentResult.getCount());
                if (extracted > 0) {
                    journal.updateSnapshots(transaction);
                    ItemStack newStack = currentResult.copy();
                    newStack.shrink(extracted);
                    inventory.setStackSilent(1, newStack);
                }
                return extracted;
            }

            @Override
            public int insert(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                return this.insert(0, resource, amount, transaction);
            }

            @Override
            public int extract(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                return this.extract(1, resource, amount, transaction);
            }
        };
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                net.minecraft.world.Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getStackInSlot(i));
            }
        }
    }
}