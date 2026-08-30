package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper;
import dev.davidklgames.puremashtweaks.block.AlchemicalSynthesizerBlock;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.menu.AlchemicalSynthesizerMenu;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public class AlchemicalSynthesizerBlockEntity extends BlockEntity implements MenuProvider {

    private int progress = 0;
    private int maxProgress = 20;

    private int arrowTopActive = 0;
    private int arrowMiddleActive = 0;
    private int arrowBottomActive = 0;
    private ItemResource cachedOutputResource = ItemResource.EMPTY;

    public void updateRecipeCache() {
        if (this.level == null) {
            this.cachedOutputResource = ItemResource.EMPTY;
            this.maxProgress = 20;
            return;
        }

        ItemStack sample = this.inventory.getStackInSlot(1);
        ItemStack tool = this.inventory.getStackInSlot(2);
        Fluid fluidType = this.fluidTank.getResource(0).getFluid();

        RecipeManager recipeManager = null;
        if (this.level.recipeAccess() instanceof RecipeManager rm) {
            recipeManager = rm;
        } else if (ServerLifecycleHooks.getCurrentServer() != null) {
            recipeManager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        }

        AlchemicalRecipeHelper.ParsedRecipe recipe = AlchemicalRecipeHelper.getRecipe(fluidType, sample, tool, recipeManager);
        if (recipe != null && !recipe.output().isEmpty()) {
            this.cachedOutputResource = ItemResource.of(recipe.output());
            this.maxProgress = Math.max(1, recipe.time());
        } else {
            this.cachedOutputResource = ItemResource.EMPTY;
            this.maxProgress = 20;
        }
    }

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
        long baseCapacity = PureMashTweaksConfig.COMMON.alchemicalSynthesizerBaseEnergyCapacity.get();
        return baseCapacity * getCapacityMultiplier();
    }

    public int getCapacityMultiplier() {
        int mult = 1;
        for (int i = 23; i <= 25; i++) {
            ItemStack upgrade = this.inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.CAPACITY_UPGRADE_1.get())) {
                mult += (PureMashTweaksConfig.COMMON.capacityUpgrade1Multiplier.get() - 1) * upgrade.getCount();
            } else if (upgrade.is(ModItems.CAPACITY_UPGRADE_2.get())) {
                mult += (PureMashTweaksConfig.COMMON.capacityUpgrade2Multiplier.get() - 1) * upgrade.getCount();
            }
        }
        return Math.max(1, mult);
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

    public class AlchemicalInventory extends ItemStackHandler {
        public AlchemicalInventory() { super(26); }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == 1 || slot == 2 || slot == 0) {
                updateRecipeCache();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return stack.getItem() instanceof BucketItem || !FluidUtil.getFirstStackContained(stack).isEmpty();
            }
            if (slot == 1) return true;
            if (slot == 2) {
                return stack.has(DataComponents.TOOL) ||
                        stack.is(ItemTags.PICKAXES) ||
                        stack.is(ItemTags.SHOVELS) ||
                        stack.is(ItemTags.AXES);
            }
            if (slot >= 3 && slot <= 22) return false;
            if (slot >= 23 && slot <= 25) {
                return isUpgradeValid(stack);
            }
            return true;
        }

        public void setStackSilent(int slot, ItemStack stack) {
            this.stacks.set(slot, stack);
        }

        public ItemStack[] createSnapshot() {
            ItemStack[] snap = new ItemStack[26];
            for (int i = 0; i < 26; i++) snap[i] = this.stacks.get(i).copy();
            return snap;
        }

        public void restoreSnapshot(ItemStack[] snap) {
            for (int i = 0; i < 26; i++) this.stacks.set(i, snap[i]);
        }
    }

    public final AlchemicalInventory inventory = new AlchemicalInventory();

    // Universal 16,000 mB fluid tank accepting ANY liquid
    public final FluidStacksResourceHandler fluidTank = new FluidStacksResourceHandler(1, 16000) {
        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return !resource.isEmpty() && resource.getFluid() != Fluids.EMPTY;
        }

        @Override
        protected void onContentsChanged(int index, net.neoforged.neoforge.fluids.@NonNull FluidStack previousContents) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public AlchemicalSynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMICAL_SYNTHESIZER_BE.get(), pos, state);
    }

    public boolean isArrowTopActive() { return this.arrowTopActive == 1; }
    public boolean isArrowMiddleActive() { return this.arrowMiddleActive == 1; }
    public boolean isArrowBottomActive() { return this.arrowBottomActive == 1; }

    public static void tick(Level level, BlockPos pos, BlockState state, AlchemicalSynthesizerBlockEntity be) {
        if (level.isClientSide()) return;

        boolean isWorking = false;

        // Drain any fluid container inserted into slot 0
        ItemStack fluidSlot = be.inventory.getStackInSlot(0);
        if (!fluidSlot.isEmpty()) {
            FluidStack contained = getFluidFromStack(fluidSlot);
            if (!contained.isEmpty()) {
                long currentAmount = be.fluidTank.getAmountAsLong(0);
                Fluid currentTankFluid = be.fluidTank.getResource(0).getFluid();

                if ((currentTankFluid == Fluids.EMPTY || currentTankFluid == contained.getFluid()) && currentAmount + contained.getAmount() <= 16000) {
                    try (Transaction tx = Transaction.openRoot()) {
                        long inserted = be.fluidTank.insert(0, FluidResource.of(contained.getFluid()), contained.getAmount(), tx);
                        if (inserted == contained.getAmount()) {
                            tx.commit();
                            be.inventory.getStackInSlot(0).shrink(1);
                            ItemStack emptyContainer = new ItemStack(Items.BUCKET);

                            boolean pushed = false;
                            for (int i = 3; i <= 22; i++) {
                                ItemStack slotStack = be.inventory.getStackInSlot(i);
                                if (slotStack.isEmpty()) {
                                    be.inventory.setStackInSlot(i, emptyContainer);
                                    pushed = true;
                                    break;
                                } else if (ItemStack.isSameItemSameComponents(slotStack, emptyContainer)) {
                                    if (slotStack.getCount() < emptyContainer.getMaxStackSize()) {
                                        slotStack.grow(1);
                                        pushed = true;
                                        break;
                                    }
                                }
                            }

                            if (!pushed) {
                                if (be.inventory.getStackInSlot(0).isEmpty()) {
                                    be.inventory.setStackInSlot(0, emptyContainer);
                                } else {
                                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), emptyContainer);
                                }
                            }
                            be.setChanged();
                        }
                    }
                }
            }
        }

        ItemStack sample = be.inventory.getStackInSlot(1);
        ItemStack tool = be.inventory.getStackInSlot(2);

        if (sample.isEmpty()) {
            be.resetArrowFlags();
            be.coolDown();
            be.updateLitState(level, pos, false);
            be.autoPushOutputs(level, pos);
            return;
        }

        long fluidAmount = be.fluidTank.getAmountAsLong(0);
        Fluid fluidType = be.fluidTank.getResource(0).getFluid();

        RecipeManager recipeManager = null;
        if (level.recipeAccess() instanceof RecipeManager rm) {
            recipeManager = rm;
        } else if (ServerLifecycleHooks.getCurrentServer() != null) {
            recipeManager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        }

        AlchemicalRecipeHelper.ParsedRecipe recipe = AlchemicalRecipeHelper.getRecipe(fluidType, sample, tool, recipeManager);

        // Direct Redstone charging fallback
        if (recipe == null && be.energyTank.getAmountAsLong() <= (be.getEnergyCapacity() - 5000)) {
            if (sample.is(Items.REDSTONE)) {
                be.energyTank.set((int) (be.energyTank.getAmountAsLong() + 5000));
                sample.shrink(1);
                be.setChanged();
                be.resetArrowFlags();
                be.coolDown();
                be.updateLitState(level, pos, false);
                be.autoPushOutputs(level, pos);
                return;
            } else if (sample.is(Items.REDSTONE_BLOCK)) {
                be.energyTank.set((int) (be.energyTank.getAmountAsLong() + 45000));
                sample.shrink(1);
                be.setChanged();
                be.resetArrowFlags();
                be.coolDown();
                be.updateLitState(level, pos, false);
                be.autoPushOutputs(level, pos);
                return;
            }
        }

        if (recipe != null) {
            be.maxProgress = Math.max(1, recipe.time());
            be.arrowTopActive = (recipe.fluid() != null) ? 1 : 0;
            be.arrowMiddleActive = 1;
            be.arrowBottomActive = (!recipe.toolType().equals("none")) ? 1 : 0;

            boolean hasEnoughFluid = true;
            if (recipe.fluid() != null) {
                hasEnoughFluid = (fluidAmount >= recipe.fluidAmount() && fluidType == recipe.fluid());
            }

            int energyUsage = recipe.energyCost() * be.getSpeedLevel();

            if (hasEnoughFluid && be.energyTank.getAmountAsLong() >= energyUsage && be.canOutput(recipe.output())) {
                isWorking = true;

                int speed = be.getSpeedLevel();
                be.progress += speed;

                while (be.progress >= be.maxProgress && be.energyTank.getAmountAsLong() >= energyUsage && be.canOutput(recipe.output()) && !sample.isEmpty()) {
                    be.energyTank.set((int) (be.energyTank.getAmountAsLong() - energyUsage));

                    int batchMultiplier = 1;
                    if (be.hasStackProcessingUpgrade()) {
                        int maxBySample = sample.getCount();
                        int maxByFluid = (recipe.fluid() != null && recipe.fluidAmount() > 0) ?
                                (int) (be.fluidTank.getAmountAsLong(0) / recipe.fluidAmount()) : 64;
                        int maxByEnergy = (energyUsage > 0) ? (int) (be.energyTank.getAmountAsLong() / energyUsage) : 64;

                        batchMultiplier = Math.min(maxBySample, Math.min(maxByFluid, maxByEnergy));
                        batchMultiplier = Math.clamp(batchMultiplier, 1, 64);
                    }

                    for (int b = 0; b < batchMultiplier; b++) {
                        if (sample.isEmpty() || !be.canOutput(recipe.output())) break;

                        FluidResource currentFluid = be.fluidTank.getResource(0);
                        if (recipe.fluid() != null && recipe.fluidAmount() > 0) {
                            if (currentFluid.isEmpty() || be.fluidTank.getAmountAsLong(0) < recipe.fluidAmount()) {
                                break;
                            }
                            try (Transaction tx = Transaction.openRoot()) {
                                be.fluidTank.extract(0, currentFluid, recipe.fluidAmount(), tx);
                                tx.commit();
                            }
                        }

                        be.generateItem(recipe.output());
                        sample.shrink(1);

                        if (!tool.isEmpty() && tool.isDamageableItem() && !recipe.toolType().equals("none")) {
                            tool.setDamageValue(tool.getDamageValue() + 1);
                            if (tool.getDamageValue() >= tool.getMaxDamage()) {
                                be.inventory.setStackInSlot(2, ItemStack.EMPTY);
                                break;
                            }
                        }
                    }

                    be.progress -= be.maxProgress;

                    // Immediate active auto-push after completing each craft in the loop
                    be.autoPushOutputs(level, pos);

                    sample = be.inventory.getStackInSlot(1);
                    fluidAmount = be.fluidTank.getAmountAsLong(0);
                }

                if (sample.isEmpty() || !be.canOutput(recipe.output())) {
                    be.progress = 0;
                }
                be.setChanged();
            } else {
                be.coolDown();
            }
        } else {
            be.resetArrowFlags();
            be.coolDown();
        }

        be.updateLitState(level, pos, isWorking);
        be.autoPushOutputs(level, pos);
    }

    // Direct active auto-ejection to adjacent AE2 pattern providers / chests (0-tick push)
    private void autoPushOutputs(Level level, BlockPos pos) {
        for (int slot = 3; slot <= 22; slot++) {
            ItemStack outputStack = this.inventory.getStackInSlot(slot);
            if (outputStack.isEmpty()) continue;

            for (Direction dir : Direction.values()) {
                BlockPos targetPos = pos.relative(dir);
                ResourceHandler<ItemResource> targetHandler = level.getCapability(Capabilities.Item.BLOCK, targetPos, dir.getOpposite());

                if (targetHandler != null) {
                    ItemResource res = ItemResource.of(outputStack);
                    int toPush = outputStack.getCount();

                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = targetHandler.insert(res, toPush, tx);
                        if (inserted > 0) {
                            tx.commit();
                            outputStack.shrink(inserted);
                            if (outputStack.isEmpty()) {
                                this.inventory.setStackSilent(slot, ItemStack.EMPTY);
                            }
                            this.setChanged();
                        }
                    }
                    if (outputStack.isEmpty()) break;
                }
            }
        }
    }

    private void updateLitState(Level level, BlockPos pos, boolean isWorking) {
        BlockState currentState = level.getBlockState(pos);
        if (currentState.hasProperty(AlchemicalSynthesizerBlock.LIT) && currentState.getValue(AlchemicalSynthesizerBlock.LIT) != isWorking) {
            level.setBlock(pos, currentState.setValue(AlchemicalSynthesizerBlock.LIT, isWorking), 3);
        }
    }

    private void resetArrowFlags() {
        this.arrowTopActive = 0;
        this.arrowMiddleActive = 0;
        this.arrowBottomActive = 0;
    }

    private static FluidStack getFluidFromStack(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucket && bucket.content != Fluids.EMPTY) {
            return new FluidStack(bucket.content, 1000);
        }
        return FluidUtil.getFirstStackContained(stack);
    }

    private void coolDown() {
        if (this.progress > 0) {
            this.progress = Math.max(0, this.progress - 2);
            this.setChanged();
        }
    }

    private boolean canOutput(ItemStack result) {
        ItemStack copy = result.copy();
        for (int i = 3; i <= 22; i++) {
            ItemStack slotStack = this.inventory.getStackInSlot(i);
            if (slotStack.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(slotStack, copy)) {
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                if (space > 0) {
                    copy.shrink(space);
                    if (copy.isEmpty()) return true;
                }
            }
        }
        return false;
    }

    private void generateItem(ItemStack recipeResult) {
        if (this.level == null || this.level.isClientSide()) return;

        ItemStack resultStack = recipeResult.copy();

        double dupChance = getDuplicationChance();
        if (dupChance > 0.0 && this.level.getRandom().nextDouble() < dupChance) {
            int extraToGive = Math.max(1, recipeResult.getCount() / 3);
            resultStack.grow(extraToGive);
        }

        for (int i = 3; i <= 22; i++) {
            ItemStack slotStack = this.inventory.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                this.inventory.setStackInSlot(i, resultStack);
                break;
            } else if (ItemStack.isSameItemSameComponents(slotStack, resultStack)) {
                int canAdd = Math.min(resultStack.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
                slotStack.grow(canAdd);
                resultStack.shrink(canAdd);
                if (resultStack.isEmpty()) break;
            }
        }
    }

    public int getSpeedLevel() {
        int speed = 1;
        int t3Count = 0;

        for (int i = 23; i <= 25; i++) {
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
        for (int i = 23; i <= 25; i++) {
            ItemStack upgrade = this.inventory.getStackInSlot(i);
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
        for (int i = 23; i <= 25; i++) {
            if (this.inventory.getStackInSlot(i).is(ModItems.STACK_PROCESSING_UPGRADE.get())) {
                return true;
            }
        }
        return false;
    }

    public int getProgress() { return this.progress; }
    public int getMaxProgress() { return this.maxProgress; }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.inventory.serialize(output);
        this.fluidTank.serialize(output);
        output.putInt("Progress", this.progress);
        output.putInt("ArrowTop", this.arrowTopActive);
        output.putInt("ArrowMiddle", this.arrowMiddleActive);
        output.putInt("ArrowBottom", this.arrowBottomActive);
        output.putLong("Energy", this.energyTank.getAmountAsLong());
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.inventory.deserialize(input);
        this.fluidTank.deserialize(input);
        this.progress = input.getIntOr("Progress", 0);
        this.arrowTopActive = input.getIntOr("ArrowTop", 0);
        this.arrowMiddleActive = input.getIntOr("ArrowMiddle", 0);
        this.arrowBottomActive = input.getIntOr("ArrowBottom", 0);
        this.energyTank.set((int) input.getLongOr("Energy", 0L));
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getStackInSlot(i));
            }
        }
    }

    private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            return inventory.createSnapshot();
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            inventory.restoreSnapshot(snapshot);
        }

        @Override
        protected void onRootCommit(ItemStack[] originalState) {
            setChanged();
        }
    };

    public ResourceHandler<ItemResource> getAutomationHandler() {
        return new ResourceHandler<>() {
            @Override
            public int size() { return 26; }

            @Override
            public @NonNull ItemResource getResource(int slot) {
                if (slot >= 3 && slot <= 22) {
                    ItemStack currentResult = inventory.getStackInSlot(slot);
                    if (!currentResult.isEmpty()) {
                        return ItemResource.of(currentResult);
                    }
                    if (slot == 3 && !cachedOutputResource.isEmpty()) {
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
                if (slot >= 3 && slot <= 22) {
                    return 64;
                }
                if (isValid(slot, resource)) {
                    return inventory.getSlotLimit(slot);
                }
                return 0;
            }

            @Override
            public boolean isValid(int slot, @NonNull ItemResource resource) {
                return inventory.isItemValid(slot, resource.toStack(1));
            }

            @Override
            public int insert(int index, @NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (index > 2 || amount <= 0 || resource.isEmpty()) return 0;

                ItemStack stack = resource.toStack(amount);
                ItemStack remainder = inventory.insertItem(index, stack, true);
                int inserted = amount - remainder.getCount();

                if (inserted > 0) {
                    journal.updateSnapshots(transaction);
                    ItemStack newStack = inventory.getStackInSlot(index).copy();
                    if (newStack.isEmpty()) newStack = resource.toStack(inserted);
                    else newStack.grow(inserted);
                    inventory.setStackSilent(index, newStack);
                    updateRecipeCache();
                }
                return inserted;
            }

            @Override
            public int extract(int index, @NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (index != 0 && (index < 3 || index > 22)) return 0;
                if (amount <= 0 || resource.isEmpty()) return 0;

                ItemStack currentResult = inventory.getStackInSlot(index);
                if (currentResult.isEmpty() || !ItemResource.of(currentResult).equals(resource)) {
                    return 0;
                }

                int extracted = Math.min(amount, currentResult.getCount());
                if (extracted > 0) {
                    journal.updateSnapshots(transaction);
                    ItemStack newStack = currentResult.copy();
                    newStack.shrink(extracted);
                    inventory.setStackSilent(index, newStack);
                }
                return extracted;
            }

            @Override
            public int insert(@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (amount <= 0 || resource.isEmpty()) return 0;

                ItemStack testStack = resource.toStack(1);

                // 1. Fluid Containers / Buckets -> Slot 0
                if (testStack.getItem() instanceof BucketItem || !FluidUtil.getFirstStackContained(testStack).isEmpty()) {
                    return this.insert(0, resource, amount, transaction);
                }

                // 2. Tools -> Slot 2
                if (testStack.has(DataComponents.TOOL) || testStack.is(ItemTags.PICKAXES) || testStack.is(ItemTags.SHOVELS) || testStack.is(ItemTags.AXES)) {
                    return this.insert(2, resource, amount, transaction);
                }

                // 3. Materials / Samples -> Slot 1
                return this.insert(1, resource, amount, transaction);
            }

            @Override
            public int extract(@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                int totalExtracted = 0;
                for (int i = 3; i <= 22; i++) {
                    if (amount <= 0) break;
                    int extractedHere = this.extract(i, resource, amount, transaction);
                    amount -= extractedHere;
                    totalExtracted += extractedHere;
                }
                if (totalExtracted == 0) {
                    totalExtracted = this.extract(0, resource, amount, transaction);
                }
                return totalExtracted;
            }
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.puremashtweaks.alchemical_synthesizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new AlchemicalSynthesizerMenu(id, playerInv, this);
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