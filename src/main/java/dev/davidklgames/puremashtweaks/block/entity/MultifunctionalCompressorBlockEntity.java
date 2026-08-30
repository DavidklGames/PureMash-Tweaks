package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper;
import dev.davidklgames.puremashtweaks.block.MultifunctionalCompressorBlock;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("removal")
public class MultifunctionalCompressorBlockEntity extends BlockEntity implements MenuProvider {

    private int mode = 0; // 0 = Compression, 1 = Singularity, 2 = Dust
    private int progress = 0;
    private int maxProgress = 20;
    private int singularityCount = 0;
    private boolean locked = false;
    private Item lockedItem = Items.AIR;
    private Item singularityItem = Items.AIR;
    private ItemResource cachedExpectedInput = ItemResource.EMPTY;
    private ItemResource cachedOutputResource = ItemResource.EMPTY;

    // Base 5,000,000 FE energy storage with Capacity Upgrades support
    public final SimpleEnergyHandler energyTank = new SimpleEnergyHandler(5000000, 5000000, 5000000) {
        @Override
        public long getCapacityAsLong() {
            return getEnergyCapacity();
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    public long getEnergyCapacity() {
        long baseCapacity = 5000000L;
        return baseCapacity * getCapacityMultiplier();
    }

    public int getCapacityMultiplier() {
        int mult = 1;
        for (int i = 2; i <= 4; i++) {
            ItemStack upgrade = this.inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.CAPACITY_UPGRADE_1.get())) {
                mult += (PureMashTweaksConfig.COMMON.capacityUpgrade1Multiplier.get() - 1) * upgrade.getCount();
            } else if (upgrade.is(ModItems.CAPACITY_UPGRADE_2.get())) {
                mult += (PureMashTweaksConfig.COMMON.capacityUpgrade2Multiplier.get() - 1) * upgrade.getCount();
            }
        }
        return Math.max(1, mult);
    }

    public int getBaseEnergyUsage() {
        return switch (this.mode) {
            case 0 -> 50;   // Compression: 50 FE/t base
            case 1 -> 250;  // Singularity: 250 FE/t base
            case 2 -> 100;  // Dust Crushing: 100 FE/t base
            default -> 50;
        };
    }

    public void updateRecipeCache() {
        if (this.level == null) {
            this.cachedExpectedInput = ItemResource.EMPTY;
            this.cachedOutputResource = ItemResource.EMPTY;
            this.maxProgress = PureMashTweaksConfig.COMMON.compressorItemSpeed.get();
            return;
        }

        ItemStack input = this.inventory.getStackInSlot(0);
        if (!input.isEmpty()) {
            var recipe = CompressorRecipeHelper.getRecipe(this.level, input, this.mode);
            if (recipe != null && !recipe.result().isEmpty()) {
                this.cachedExpectedInput = ItemResource.of(input);
                this.cachedOutputResource = ItemResource.of(recipe.result());
                this.maxProgress = Math.max(1, recipe.time());
                return;
            }
        } else if (this.locked && this.lockedItem != Items.AIR) {
            var recipe = CompressorRecipeHelper.getRecipe(this.level, new ItemStack(this.lockedItem), this.mode);
            if (recipe != null && !recipe.result().isEmpty()) {
                this.cachedExpectedInput = ItemResource.of(new ItemStack(this.lockedItem));
                this.cachedOutputResource = ItemResource.of(recipe.result());
                this.maxProgress = Math.max(1, recipe.time());
                return;
            }
        }

        this.cachedExpectedInput = ItemResource.EMPTY;
        this.cachedOutputResource = ItemResource.EMPTY;
        updateMaxProgress();
    }

    public Item getSingularityItem() {
        return this.singularityItem;
    }

    public class CompressorInventory extends ItemStackHandler {
        public CompressorInventory() { super(5); }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == 0) {
                updateRecipeCache();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 1) return false;
            if (slot >= 2 && slot <= 4) {
                return isUpgradeValid(stack);
            }
            if (slot == 0) {
                if (locked && lockedItem != Items.AIR) {
                    return stack.is(lockedItem);
                }
                if (level != null) {
                    return CompressorRecipeHelper.getRecipe(level, stack, mode) != null;
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

    public static boolean isUpgradeValid(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModItems.SPEED_UPGRADE_1.get()) ||
                stack.is(ModItems.SPEED_UPGRADE_2.get()) ||
                stack.is(ModItems.SPEED_UPGRADE_3.get()) ||
                stack.is(ModItems.CAPACITY_UPGRADE_1.get()) ||
                stack.is(ModItems.CAPACITY_UPGRADE_2.get()) ||
                stack.is(ModItems.DUPLICATION_UPGRADE_1.get()) ||
                stack.is(ModItems.DUPLICATION_UPGRADE_2.get()) ||
                stack.is(ModItems.STACK_PROCESSING_UPGRADE.get());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MultifunctionalCompressorBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        ItemStack input = blockEntity.inventory.getStackInSlot(0);
        ItemStack output = blockEntity.inventory.getStackInSlot(1);

        boolean isWorking;
        if (blockEntity.mode == 1) {
            isWorking = blockEntity.tickSingularityMode(input, output);
        } else {
            isWorking = blockEntity.tickStandardMode(input, output);
        }

        blockEntity.updateLitState(level, pos, isWorking);
        blockEntity.autoPushOutput(level, pos);
    }

    private void updateLitState(Level level, BlockPos pos, boolean isWorking) {
        BlockState currentState = level.getBlockState(pos);
        if (currentState.hasProperty(MultifunctionalCompressorBlock.LIT) && currentState.getValue(MultifunctionalCompressorBlock.LIT) != isWorking) {
            level.setBlock(pos, currentState.setValue(MultifunctionalCompressorBlock.LIT, isWorking), 3);
        }
    }

    // Direct active auto-ejection to adjacent AE2 pattern providers / chests (0-tick push)
    private void autoPushOutput(Level level, BlockPos pos) {
        ItemStack output = this.inventory.getStackInSlot(1);
        if (output.isEmpty()) return;

        for (Direction dir : Direction.values()) {
            BlockPos targetPos = pos.relative(dir);
            ResourceHandler<ItemResource> targetHandler = level.getCapability(Capabilities.Item.BLOCK, targetPos, dir.getOpposite());

            if (targetHandler != null) {
                ItemResource res = ItemResource.of(output);
                int toPush = output.getCount();

                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = targetHandler.insert(res, toPush, tx);
                    if (inserted > 0) {
                        tx.commit();
                        output.shrink(inserted);
                        if (output.isEmpty()) {
                            this.inventory.setStackSilent(1, ItemStack.EMPTY);
                        }
                        this.setChanged();
                        if (output.isEmpty()) break;
                    }
                }
            }
        }
    }

    // High-throughput multi-operation cycle per tick
    private boolean tickStandardMode(ItemStack input, ItemStack output) {
        var recipe = CompressorRecipeHelper.getRecipe(this.level, input, this.mode);
        if (recipe != null) {
            this.maxProgress = Math.max(1, recipe.time());
        } else {
            updateMaxProgress();
        }

        int energyRequired = this.getBaseEnergyUsage() * getSpeedLevel();

        if (canProcess(input, output) && this.energyTank.getAmountAsLong() >= energyRequired) {
            int speed = getSpeedLevel();
            this.progress += speed;

            while (this.progress >= this.maxProgress && canProcess(input, output) && !input.isEmpty() && this.energyTank.getAmountAsLong() >= energyRequired) {
                this.energyTank.set((int) (this.energyTank.getAmountAsLong() - energyRequired));
                processItem(input, output);
                this.progress -= this.maxProgress;

                // Immediate 0-tick active ejection after completing craft
                this.autoPushOutput(this.level, this.worldPosition);

                input = this.inventory.getStackInSlot(0);
                output = this.inventory.getStackInSlot(1);
            }

            if (input.isEmpty() || !canProcess(input, output)) {
                this.progress = 0;
            }
            setChanged();
            return true;
        } else {
            if (this.progress > 0) {
                this.progress = Math.max(0, this.progress - 2);
                setChanged();
            }
            return false;
        }
    }

    private boolean tickSingularityMode(ItemStack input, ItemStack output) {
        this.maxProgress = PureMashTweaksConfig.COMMON.compressorSingularitySpeed.get();

        if (this.singularityCount == 0 && input.isEmpty()) {
            this.singularityItem = Items.AIR;
        }

        if (this.singularityItem == Items.AIR && !input.isEmpty()) {
            var recipe = CompressorRecipeHelper.getRecipe(this.level, input, 1);
            if (recipe != null) {
                this.singularityItem = input.getItem();
            }
        }

        if (this.singularityItem != Items.AIR) {
            ItemStack tempInput = new ItemStack(this.singularityItem);
            var recipe = CompressorRecipeHelper.getRecipe(this.level, tempInput, 1);

            if (recipe == null) {
                refundSingularityBuffer();
                this.singularityCount = 0;
                this.singularityItem = Items.AIR;
                this.progress = 0;
                setChanged();
                return false;
            }

            if (recipe.time() > 0) {
                this.maxProgress = recipe.time();
            }

            int energyRequired = this.getBaseEnergyUsage() * getSpeedLevel();

            // Consumes items into the internal accumulator
            if (!input.isEmpty() && input.is(this.singularityItem)) {
                int needed = recipe.cost() - this.singularityCount;
                int toConsume = Math.min(input.getCount(), needed);
                input.shrink(toConsume);
                this.singularityCount += toConsume;
                setChanged();
            }

            if (this.singularityCount >= recipe.cost() && this.energyTank.getAmountAsLong() >= energyRequired) {
                ItemStack result = recipe.result();
                boolean canOutput = output.isEmpty() ||
                        (ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize());

                if (canOutput) {
                    int speed = getSpeedLevel();
                    this.progress += speed;

                    while (this.progress >= this.maxProgress && this.singularityCount >= recipe.cost() && canOutput && this.energyTank.getAmountAsLong() >= energyRequired) {
                        this.energyTank.set((int) (this.energyTank.getAmountAsLong() - energyRequired));

                        if (output.isEmpty()) {
                            this.inventory.setStackInSlot(1, result.copy());
                        } else {
                            output.grow(result.getCount());
                        }
                        this.singularityCount -= recipe.cost();
                        this.progress -= this.maxProgress;

                        // Immediate 0-tick active ejection after completing singularity
                        this.autoPushOutput(this.level, this.worldPosition);

                        output = this.inventory.getStackInSlot(1);
                        canOutput = output.isEmpty() ||
                                (ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize());
                    }

                    if (this.singularityCount <= 0 && input.isEmpty()) {
                        this.singularityItem = Items.AIR;
                        this.progress = 0;
                    }
                    setChanged();
                    return true;
                }
            } else {
                this.progress = 0;
            }
        } else {
            this.singularityCount = 0;
            this.progress = 0;
            setChanged();
        }
        return false;
    }

    private void updateMaxProgress() {
        if (this.mode == 1) {
            this.maxProgress = PureMashTweaksConfig.COMMON.compressorSingularitySpeed.get();
        } else {
            this.maxProgress = PureMashTweaksConfig.COMMON.compressorItemSpeed.get();
        }
    }

    private boolean canProcess(ItemStack input, ItemStack output) {
        if (input.isEmpty()) return false;
        var recipe = CompressorRecipeHelper.getRecipe(this.level, input, this.mode);
        if (recipe == null) return false;
        if ((this.mode == 0 || this.mode == 2) && input.getCount() < recipe.cost()) return false;

        ItemStack result = recipe.result();
        return !result.isEmpty() && (output.isEmpty() ||
                (ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize()));
    }

    private void processItem(ItemStack input, ItemStack output) {
        var recipe = CompressorRecipeHelper.getRecipe(this.level, input, this.mode);
        if (recipe == null) return;

        int batchMultiplier = 1;
        if (hasStackProcessingUpgrade() && (this.mode == 0 || this.mode == 2)) {
            int maxPossibleByInput = input.getCount() / recipe.cost();
            int freeOutputSpace = output.isEmpty() ? recipe.result().getMaxStackSize() : (output.getMaxStackSize() - output.getCount());
            int maxPossibleByOutput = freeOutputSpace / recipe.result().getCount();
            batchMultiplier = Math.max(1, Math.min(maxPossibleByInput, maxPossibleByOutput));
        }

        ItemStack result = recipe.result().copy();
        result.setCount(result.getCount() * batchMultiplier);

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
                input.shrink(recipe.cost() * batchMultiplier);
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
                speed += PureMashTweaksConfig.COMMON.speedUpgrade1Power.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.SPEED_UPGRADE_2.get())) {
                speed += PureMashTweaksConfig.COMMON.speedUpgrade2Power.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.SPEED_UPGRADE_3.get())) {
                t3Count += upgrade.getCount();
            }
        }

        if (t3Count > 0) {
            int basePower = PureMashTweaksConfig.COMMON.speedUpgrade3Power.get();
            if (t3Count == 1) {
                speed += basePower * 2;
            } else if (t3Count == 2) {
                speed += basePower * 8;
            } else {
                speed += basePower * 32;
            }
        }

        return speed;
    }

    public double getDuplicationChance() {
        if (!PureMashTweaksConfig.COMMON.enableDuplication.get()) {
            return 0.0;
        }

        double chance = 0.0;
        for (int i = 2; i <= 4; i++) {
            ItemStack upgrade = inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.DUPLICATION_UPGRADE_1.get())) {
                chance += PureMashTweaksConfig.COMMON.duplicationUpgrade1Chance.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.DUPLICATION_UPGRADE_2.get())) {
                chance += PureMashTweaksConfig.COMMON.duplicationUpgrade2Chance.get() * upgrade.getCount();
            }
        }
        return Math.min(1.0, chance);
    }

    public boolean hasStackProcessingUpgrade() {
        if (!PureMashTweaksConfig.COMMON.enableStackProcessing.get()) return false;
        for (int i = 2; i <= 4; i++) {
            if (this.inventory.getStackInSlot(i).is(ModItems.STACK_PROCESSING_UPGRADE.get())) {
                return true;
            }
        }
        return false;
    }

    public int getMode() { return this.mode; }

    private void refundSingularityBuffer() {
        if (this.singularityCount > 0 && this.singularityItem != Items.AIR && this.level != null && !this.level.isClientSide()) {
            ItemStack refundStack = new ItemStack(this.singularityItem, this.singularityCount);

            ItemStack remainder = this.inventory.insertItem(0, refundStack, false);
            if (!remainder.isEmpty()) {
                Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, remainder);
            }
        }
    }

    public void setMode(int mode) {
        refundSingularityBuffer();

        this.mode = mode;
        this.progress = 0;
        this.singularityCount = 0;
        this.singularityItem = Items.AIR;
        this.updateMaxProgress();
        updateRecipeCache();

        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getProgress() { return this.progress; }
    public int getMaxProgress() { return this.maxProgress; }
    public int getSingularityCount() { return this.singularityCount; }

    public boolean isLocked() { return this.locked; }

    public void setLocked(boolean locked) {
        this.locked = locked;
        if (locked) {
            ItemStack input = this.inventory.getStackInSlot(0);
            this.lockedItem = input.isEmpty() ? Items.AIR : input.getItem();
        } else {
            this.lockedItem = Items.AIR;
        }
        setChanged();
        updateRecipeCache();
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.inventory.serialize(output);
        output.putInt("Mode", this.mode);
        output.putInt("Progress", this.progress);
        output.putInt("SingularityCount", this.singularityCount);
        output.putBoolean("Locked", this.locked);
        output.putString("LockedItem", BuiltInRegistries.ITEM.getKey(this.lockedItem).toString());
        output.putString("SingularityItem", BuiltInRegistries.ITEM.getKey(this.singularityItem).toString());
        output.putLong("Energy", this.energyTank.getAmountAsLong());
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
        this.lockedItem = BuiltInRegistries.ITEM.get(Objects.requireNonNull(Identifier.tryParse(lockedItemStr))).map(Holder::value).orElse(Items.AIR);
        String singItemStr = input.getStringOr("SingularityItem", "minecraft:air");
        this.singularityItem = BuiltInRegistries.ITEM.get(Objects.requireNonNull(Identifier.tryParse(singItemStr))).map(Holder::value).orElse(Items.AIR);
        this.energyTank.set((int) input.getLongOr("Energy", 0L));
        this.updateMaxProgress();
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

    public ResourceHandler<ItemResource> getAutomationHandler(@Nullable Direction side) {
        return new ResourceHandler<>() {
            @Override
            public int size() { return 2; }

            @Override
            public @NonNull ItemResource getResource(int slot) {
                if (slot == 1) {
                    ItemStack currentResult = inventory.getStackInSlot(1);
                    if (!currentResult.isEmpty()) {
                        return ItemResource.of(currentResult);
                    }
                    if (!cachedOutputResource.isEmpty()) {
                        return cachedOutputResource;
                    }
                }
                return ItemResource.of(inventory.getStackInSlot(slot));
            }

            @Override
            public long getAmountAsLong(int slot) {
                return inventory.getStackInSlot(slot).getCount();
            }

            @Override
            public long getCapacityAsLong(int slot, @NonNull ItemResource resource) {
                if (slot == 1) {
                    return cachedOutputResource.isEmpty() ? 64 : cachedOutputResource.toStack().getMaxStackSize();
                }
                if (slot == 0) {
                    if (isValid(0, resource)) {
                        return resource.toStack().getMaxStackSize();
                    }
                    return 0;
                }
                return inventory.getSlotLimit(slot);
            }

            @Override
            public boolean isValid(int slot, @NonNull ItemResource resource) {
                if (slot == 1) return false;
                if (slot == 0) {
                    if (locked && lockedItem != Items.AIR) {
                        return resource.toStack().is(lockedItem);
                    }
                    if (level != null) {
                        return CompressorRecipeHelper.getRecipe(level, resource.toStack(), mode) != null;
                    }
                }
                return false;
            }

            @Override
            public int insert(int index, @NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (index != 0 || amount <= 0 || resource.isEmpty()) return 0;

                ItemStack stack = resource.toStack(amount);
                if (!isValid(0, resource)) return 0;

                ItemStack remainder = inventory.insertItem(0, stack, true);
                int inserted = amount - remainder.getCount();

                if (inserted > 0) {
                    journal.updateSnapshots(transaction);
                    ItemStack newStack = inventory.getStackInSlot(0).copy();
                    if (newStack.isEmpty()) newStack = resource.toStack(inserted);
                    else newStack.grow(inserted);
                    inventory.setStackSilent(0, newStack);
                    updateRecipeCache();
                }
                return inserted;
            }

            @Override
            public int extract(int index, @NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (index != 1 || amount <= 0 || resource.isEmpty()) return 0;

                ItemStack currentResult = inventory.getStackInSlot(1);
                if (currentResult.isEmpty() || !ItemResource.of(currentResult).equals(resource)) {
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
            public int insert(@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                return this.insert(0, resource, amount, transaction);
            }

            @Override
            public int extract(@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                return this.extract(1, resource, amount, transaction);
            }
        };
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            refundSingularityBuffer();
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getStackInSlot(i));
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return this.saveCustomOnly(registries);
    }
}