package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.menu.AlchemicalSynthesizerMenu;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("removal")
public class AlchemicalSynthesizerBlockEntity extends BlockEntity implements MenuProvider {

    private int progress = 0;
    private int maxProgress = 60;

    private int arrowTopActive = 0;
    private int arrowMiddleActive = 0;
    private int arrowBottomActive = 0;

    public final SimpleEnergyHandler energyTank = new SimpleEnergyHandler(5000000, 5000000, 5000000) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    public final ItemStackHandler inventory = new ItemStackHandler(26) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return stack.is(net.minecraft.world.item.Items.WATER_BUCKET) ||
                        stack.is(net.minecraft.world.item.Items.LAVA_BUCKET) ||
                        stack.is(net.minecraft.world.item.Items.BUCKET);
            }
            if (slot == 1) return true;
            if (slot == 2) {
                return stack.has(net.minecraft.core.component.DataComponents.TOOL) ||
                        stack.is(net.minecraft.tags.ItemTags.PICKAXES) ||
                        stack.is(net.minecraft.tags.ItemTags.SHOVELS) ||
                        stack.is(net.minecraft.tags.ItemTags.AXES);
            }
            if (slot >= 3 && slot <= 22) return false;
            if (slot >= 23 && slot <= 25) {
                return stack.is(ModItems.SPEED_UPGRADE_1.get()) ||
                        stack.is(ModItems.SPEED_UPGRADE_2.get()) ||
                        stack.is(ModItems.SPEED_UPGRADE_3.get());
            }
            return true;
        }
    };

    public final FluidStacksResourceHandler fluidTank = new FluidStacksResourceHandler(1, 8000) {
        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return resource.getFluid() == Fluids.WATER || resource.getFluid() == Fluids.LAVA;
        }

        @Override
        protected void onContentsChanged(int index, net.neoforged.neoforge.fluids.@NonNull FluidStack previousContents) {
            setChanged();
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

        ItemStack fluidSlot = be.inventory.getStackInSlot(0);
        if (!fluidSlot.isEmpty()) {
            Fluid bucketFluid = getFluidFromBucket(fluidSlot);
            if (bucketFluid != Fluids.EMPTY) {
                long currentAmount = be.fluidTank.getAmountAsLong(0);
                Fluid currentTankFluid = be.fluidTank.getResource(0).getFluid();
                if ((currentTankFluid == Fluids.EMPTY || currentTankFluid == bucketFluid) && currentAmount <= 7000) {
                    try (Transaction tx = Transaction.openRoot()) {
                        long inserted = be.fluidTank.insert(0, FluidResource.of(bucketFluid), 1000, tx);
                        if (inserted == 1000) {
                            tx.commit();

                            be.inventory.getStackInSlot(0).shrink(1);
                            ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);

                            boolean pushed = false;
                            for (int i = 3; i <= 22; i++) {
                                ItemStack slotStack = be.inventory.getStackInSlot(i);
                                if (slotStack.isEmpty()) {
                                    be.inventory.setStackInSlot(i, emptyBucket);
                                    pushed = true;
                                    break;
                                } else if (ItemStack.isSameItemSameComponents(slotStack, emptyBucket)) {
                                    int maxStack = emptyBucket.getMaxStackSize();
                                    if (slotStack.getCount() < maxStack) {
                                        slotStack.grow(1);
                                        pushed = true;
                                        break;
                                    }
                                }
                            }

                            if (!pushed) {
                                if (be.inventory.getStackInSlot(0).isEmpty()) {
                                    be.inventory.setStackInSlot(0, emptyBucket);
                                } else {
                                    net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), emptyBucket);
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
            be.updateLitState(level, pos, state, false);
            return;
        }

        be.updateMaxProgress();

        long fluidAmount = be.fluidTank.getAmountAsLong(0);
        Fluid fluidType = be.fluidTank.getResource(0).getFluid();

        RecipeManager recipeManager = null;
        if (level.recipeAccess() instanceof RecipeManager rm) {
            recipeManager = rm;
        } else if (ServerLifecycleHooks.getCurrentServer() != null) {
            recipeManager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        }

        AlchemicalRecipeHelper.ParsedRecipe recipe = AlchemicalRecipeHelper.getRecipe(fluidType, sample, tool, recipeManager);

        if (recipe == null && be.energyTank.getAmountAsLong() <= 4950000) {
            if (sample.is(net.minecraft.world.item.Items.REDSTONE)) {
                be.energyTank.set((int) (be.energyTank.getAmountAsLong() + 5000));
                sample.shrink(1);
                be.setChanged();
                be.resetArrowFlags();
                be.coolDown();
                be.updateLitState(level, pos, state, false);
                return;
            } else if (sample.is(net.minecraft.world.item.Items.REDSTONE_BLOCK)) {
                be.energyTank.set((int) (be.energyTank.getAmountAsLong() + 45000));
                sample.shrink(1);
                be.setChanged();
                be.resetArrowFlags();
                be.coolDown();
                be.updateLitState(level, pos, state, false);
                return;
            }
        }

        if (recipe != null) {
            be.arrowTopActive = (recipe.fluid() != null) ? 1 : 0;
            be.arrowMiddleActive = 1;
            be.arrowBottomActive = (!recipe.toolType().equals("none")) ? 1 : 0;

            boolean hasEnoughFluid = true;
            if (recipe.fluid() != null) {
                hasEnoughFluid = (fluidAmount >= recipe.fluidAmount() && fluidType == recipe.fluid());
            }

            int energyUsage = 100 * be.getSpeedLevel();

            if (hasEnoughFluid && be.energyTank.getAmountAsLong() >= energyUsage && be.canOutput(recipe.output())) {
                isWorking = true;

                be.energyTank.set((int) (be.energyTank.getAmountAsLong() - energyUsage));
                be.progress += be.getSpeedLevel();

                if (be.progress >= be.maxProgress) {
                    be.generateItem(recipe.output());

                    if (recipe.fluid() != null && recipe.fluidAmount() > 0) {
                        try (Transaction tx = Transaction.openRoot()) {
                            be.fluidTank.extract(0, be.fluidTank.getResource(0), recipe.fluidAmount(), tx);
                            tx.commit();
                        }
                    }

                    sample.shrink(1);

                    if (!tool.isEmpty() && tool.isDamageableItem() && !recipe.toolType().equals("none")) {
                        int damageAmount = 1;
                        tool.setDamageValue(tool.getDamageValue() + damageAmount);
                        if (tool.getDamageValue() >= tool.getMaxDamage()) {
                            be.inventory.setStackInSlot(2, ItemStack.EMPTY);
                        }
                    }

                    if (be.getSpeedLevel() >= be.maxProgress && be.canOutput(recipe.output()) && !sample.isEmpty()) {
                        be.progress = be.maxProgress / 2;
                    } else {
                        be.progress = 0;
                    }
                }
                be.setChanged();
            } else {
                be.coolDown();
            }
        } else {
            be.resetArrowFlags();
            be.coolDown();
        }

        be.updateLitState(level, pos, state, isWorking);
    }

    private void updateLitState(Level level, BlockPos pos, BlockState state, boolean isWorking) {
        if (state.hasProperty(dev.davidklgames.puremashtweaks.block.AlchemicalSynthesizerBlock.LIT) &&
                state.getValue(dev.davidklgames.puremashtweaks.block.AlchemicalSynthesizerBlock.LIT) != isWorking) {
            level.setBlock(pos, state.setValue(dev.davidklgames.puremashtweaks.block.AlchemicalSynthesizerBlock.LIT, isWorking), 3);
        }
    }

    private void resetArrowFlags() {
        this.arrowTopActive = 0;
        this.arrowMiddleActive = 0;
        this.arrowBottomActive = 0;
    }

    private static Fluid getFluidFromBucket(ItemStack bucket) {
        if (bucket.is(net.minecraft.world.item.Items.WATER_BUCKET)) return Fluids.WATER;
        if (bucket.is(net.minecraft.world.item.Items.LAVA_BUCKET)) return Fluids.LAVA;
        return Fluids.EMPTY;
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

    private void updateMaxProgress() {
        this.maxProgress = 60;
    }

    public int getSpeedLevel() {
        int speed = 1;
        int t3Count = 0;

        for (int i = 23; i <= 25; i++) {
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
        for (int i = 23; i <= 25; i++) {
            ItemStack upgrade = this.inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.SPEED_UPGRADE_2.get())) {
                chance += PureMashTweaksConfig.MACHINE_UPGRADE_2_DUPLICATION_CHANCE.get() * upgrade.getCount();
            }
            if (upgrade.is(ModItems.SPEED_UPGRADE_3.get())) {
                chance += PureMashTweaksConfig.MACHINE_UPGRADE_3_DUPLICATION_CHANCE.get() * upgrade.getCount();
            }
        }
        return chance;
    }

    public int getProgress() { return this.progress; }
    public int getMaxProgress() { return this.maxProgress; }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
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
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
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
                net.minecraft.world.Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getStackInSlot(i));
            }
        }
    }

    private final net.neoforged.neoforge.transfer.transaction.SnapshotJournal<ItemStack[]> journal = new net.neoforged.neoforge.transfer.transaction.SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] snap = new ItemStack[26];
            for (int i = 0; i < 26; i++) {
                snap[i] = inventory.getStackInSlot(i).copy();
            }
            return snap;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            for (int i = 0; i < 26; i++) {
                inventory.setStackInSlot(i, snapshot[i]);
            }
        }

        @Override
        protected void onRootCommit(ItemStack[] originalState) {
            setChanged();
        }
    };

    public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> getAutomationHandler() {
        return new net.neoforged.neoforge.transfer.ResourceHandler<>() {
            @Override
            public int size() { return 26; }

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
                return inventory.isItemValid(slot, resource.toStack(1));
            }

            @Override
            public int insert(int index, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
                if (index > 2 || amount <= 0 || resource.isEmpty()) return 0;

                ItemStack stack = resource.toStack(amount);
                ItemStack remainder = inventory.insertItem(index, stack, true);
                int inserted = amount - remainder.getCount();

                if (inserted > 0) {
                    journal.updateSnapshots(transaction);
                    ItemStack newStack = inventory.getStackInSlot(index).copy();
                    if (newStack.isEmpty()) newStack = stack.copyWithCount(inserted);
                    else newStack.grow(inserted);
                    inventory.setStackInSlot(index, newStack);
                }
                return inserted;
            }

            @Override
            public int extract(int index, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
                if (index != 0 && (index < 3 || index > 22)) return 0;
                if (amount <= 0 || resource.isEmpty()) return 0;

                ItemStack currentResult = inventory.getStackInSlot(index);
                if (currentResult.isEmpty() || !net.neoforged.neoforge.transfer.item.ItemResource.of(currentResult).equals(resource)) {
                    return 0;
                }

                int extracted = Math.min(amount, currentResult.getCount());
                if (extracted > 0) {
                    journal.updateSnapshots(transaction);
                    ItemStack newStack = currentResult.copy();
                    newStack.shrink(extracted);
                    inventory.setStackInSlot(index, newStack);
                }
                return extracted;
            }

            @Override
            public int insert(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
                int totalInserted = 0;
                for (int i = 0; i <= 2; i++) {
                    if (amount <= 0) break;
                    int insertedHere = this.insert(i, resource, amount, transaction);
                    amount -= insertedHere;
                    totalInserted += insertedHere;
                }
                return totalInserted;
            }

            @Override
            public int extract(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
                int totalExtracted = 0;
                for (int i = 3; i <= 22; i++) {
                    if (amount <= 0) break;
                    int extractedHere = this.extract(i, resource, amount, transaction);
                    amount -= extractedHere;
                    totalExtracted += extractedHere;
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
}