package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.api.SynthesisRecipeHelper;
import dev.davidklgames.puremashtweaks.registry.PureMashDataComponents;
import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"removal"})
public class SynthesisTableBlockEntity extends BlockEntity implements MenuProvider {

    private int activeMode = 0;
    private boolean automationActive = false;
    private int automationMode = 0; // 0 = Craft, 1 = Encoder
    private int expectedOutputCount = 0;
    private boolean bypassGridLock = false;

    // High-Speed Pre-Cached Resources & Maps for instant O(1) AE2 pattern delivery
    private final ItemResource[] cachedExpectedResources = new ItemResource[81];
    private final int[] cachedExpectedCounts = new int[81];
    private final Map<ItemResource, List<Integer>> resourceToSlotsMap = new Object2ObjectOpenHashMap<>();
    private final ItemStack[] memoryCardCache = new ItemStack[81];
    private ItemStack memoryCardOutputCache = ItemStack.EMPTY;
    private ItemResource cachedOutputResource = ItemResource.EMPTY;
    private boolean hasMemoryCardRecipe = false;

    {
        for (int i = 0; i < 81; i++) {
            cachedExpectedResources[i] = ItemResource.EMPTY;
            cachedExpectedCounts[i] = 0;
            memoryCardCache[i] = ItemStack.EMPTY;
        }
    }

    public class SynthesisInventory extends ItemStackHandler {
        public SynthesisInventory() { super(83); }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == 82) {
                updateMemoryCardCache();
            }
            if (slot < 81) {
                updateCraftingResult();
            }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot >= 81 || isGridLocked() || stack.isEmpty()) return stack;

            if (automationActive && automationMode == 0 && hasMemoryCardRecipe) {
                ItemResource expectedRes = cachedExpectedResources[slot];
                if (expectedRes.isEmpty() || !expectedRes.equals(ItemResource.of(stack))) {
                    return stack;
                }

                ItemStack currentStack = this.getStackInSlot(slot);
                int limit = Math.min(stack.getMaxStackSize(), this.getSlotLimit(slot));
                if (currentStack.getCount() >= limit) {
                    return stack;
                }

                int space = limit - currentStack.getCount();
                int toInsertAmount = Math.min(stack.getCount(), space);

                if (!simulate) {
                    this.stacks.set(slot, stack.copyWithCount(currentStack.getCount() + toInsertAmount));
                    onContentsChanged(slot);
                }

                ItemStack remainder = stack.copy();
                remainder.shrink(toInsertAmount);
                return remainder;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 81 && isGridLocked()) return ItemStack.EMPTY;

            ItemStack result = super.extractItem(slot, amount, simulate);
            if (slot == 81 && !simulate && !result.isEmpty()) {
                if (getStackInSlot(81).isEmpty()) {
                    consumeCraftingIngredients();
                }
            }
            return result;
        }

        public void setStackSilent(int slot, ItemStack stack) {
            this.stacks.set(slot, stack);
        }

        public ItemStack[] createSnapshot() {
            ItemStack[] snap = new ItemStack[83];
            for (int i = 0; i < 83; i++) {
                snap[i] = this.stacks.get(i).copy();
            }
            return snap;
        }

        public void restoreSnapshot(ItemStack[] snap) {
            for (int i = 0; i < 83; i++) {
                this.stacks.set(i, snap[i]);
            }
        }
    }

    public final SynthesisInventory inventory = new SynthesisInventory();

    public SynthesisTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SYNTHESIS_TABLE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateMemoryCardCache();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SynthesisTableBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.automationActive && be.automationMode == 0 && be.hasMemoryCardRecipe) {
            be.autoProcessAndEject(level, pos);
        }
    }

    private void autoProcessAndEject(Level level, BlockPos pos) {
        this.updateCraftingResult();

        ItemStack outputStack = this.inventory.getStackInSlot(81);
        if (outputStack.isEmpty()) return;

        // Auto-eject result directly to adjacent AE2 Pattern Providers or containers (0-tick response)
        for (Direction dir : Direction.values()) {
            BlockPos targetPos = pos.relative(dir);
            ResourceHandler<ItemResource> targetHandler = level.getCapability(Capabilities.Item.BLOCK, targetPos, dir.getOpposite());

            if (targetHandler != null) {
                ItemResource res = ItemResource.of(outputStack);
                int amountToPush = outputStack.getCount();

                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = targetHandler.insert(res, amountToPush, tx);
                    if (inserted > 0) {
                        tx.commit();
                        outputStack.shrink(inserted);
                        if (outputStack.isEmpty()) {
                            this.inventory.setStackSilent(81, ItemStack.EMPTY);
                            this.consumeCraftingIngredientsSilent();
                        }
                        this.setChanged();
                        this.updateCraftingResult();
                        break;
                    }
                }
            }
        }
    }

    public void updateMemoryCardCache() {
        if (this.level == null) {
            this.hasMemoryCardRecipe = false;
            this.memoryCardOutputCache = ItemStack.EMPTY;
            this.cachedOutputResource = ItemResource.EMPTY;
            this.resourceToSlotsMap.clear();
            return;
        }

        for (int i = 0; i < 81; i++) {
            memoryCardCache[i] = ItemStack.EMPTY;
            cachedExpectedResources[i] = ItemResource.EMPTY;
            cachedExpectedCounts[i] = 0;
        }
        this.resourceToSlotsMap.clear();
        this.hasMemoryCardRecipe = false;
        this.memoryCardOutputCache = ItemStack.EMPTY;
        this.cachedOutputResource = ItemResource.EMPTY;

        ItemStack card = inventory.getStackInSlot(82);
        if (automationActive && automationMode == 0 && !card.isEmpty() && card.is(ModItems.MEMORY_CARD.get()) && card.has(PureMashDataComponents.RECIPE_CARD_DATA.get())) {
            CompoundTag recipeData = card.get(PureMashDataComponents.RECIPE_CARD_DATA.get());
            if (recipeData == null) return;

            ListTag itemsList = recipeData.getListOrEmpty("GridItems");
            var context = this.level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);

            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag itemTag = itemsList.getCompoundOrEmpty(i);
                int slot = itemTag.getIntOr("Slot", -1);
                if (slot >= 0 && slot < 81) {
                    ItemStack stack = ItemStack.CODEC.parse(context, itemTag.getCompoundOrEmpty("Item")).result().orElse(ItemStack.EMPTY);
                    if (!stack.isEmpty()) {
                        memoryCardCache[slot] = stack;
                        ItemResource res = ItemResource.of(stack);
                        cachedExpectedResources[slot] = res;
                        cachedExpectedCounts[slot] = Math.max(1, stack.getCount());

                        this.resourceToSlotsMap.computeIfAbsent(res, k -> new ArrayList<>()).add(slot);
                    }
                }
            }

            if (recipeData.contains("OutputItem")) {
                this.memoryCardOutputCache = ItemStack.CODEC.parse(context, recipeData.getCompoundOrEmpty("OutputItem")).result().orElse(ItemStack.EMPTY);
                if (!this.memoryCardOutputCache.isEmpty()) {
                    this.cachedOutputResource = ItemResource.of(this.memoryCardOutputCache);
                }
            }

            this.hasMemoryCardRecipe = true;
        }
    }

    public boolean isGridLocked() {
        if (this.bypassGridLock) return false;
        ItemStack output = this.inventory.getStackInSlot(81);
        if (output.isEmpty()) return false;
        return this.expectedOutputCount > 1 && output.getCount() < this.expectedOutputCount;
    }

    public void updateCraftingResult() {
        if (this.level == null || this.level.isClientSide()) return;

        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // 1. FAST MEMORY CARD RECIPE CHECK
            if (hasMemoryCardRecipe) {
                int possibleCrafts = Integer.MAX_VALUE;
                boolean recipeMatches = true;

                for (int i = 0; i < 81; i++) {
                    ItemResource expectedRes = cachedExpectedResources[i];
                    ItemStack actual = this.inventory.getStackInSlot(i);

                    if (expectedRes.isEmpty()) {
                        if (!actual.isEmpty()) {
                            recipeMatches = false;
                            break;
                        }
                    } else {
                        if (actual.isEmpty() || !expectedRes.equals(ItemResource.of(actual)) || actual.getCount() < cachedExpectedCounts[i]) {
                            recipeMatches = false;
                            break;
                        }
                        int craftsForSlot = actual.getCount() / cachedExpectedCounts[i];
                        possibleCrafts = Math.min(possibleCrafts, craftsForSlot);
                    }
                }

                if (recipeMatches && !this.memoryCardOutputCache.isEmpty() && possibleCrafts > 0 && possibleCrafts != Integer.MAX_VALUE) {
                    this.expectedOutputCount = this.memoryCardOutputCache.getCount();
                    if (this.inventory.getStackInSlot(81).isEmpty()) {
                        this.inventory.setStackInSlot(81, this.memoryCardOutputCache.copy());
                    }
                    setChanged();
                    return;
                }

                this.expectedOutputCount = 0;
                this.inventory.setStackInSlot(81, ItemStack.EMPTY);
                setChanged();
                return;
            }

            // 2. CUSTOM JSON RECIPES
            ItemStack[] gridArray = new ItemStack[81];
            for (int i = 0; i < 81; i++) {
                gridArray[i] = this.inventory.getStackInSlot(i);
            }

            ItemStack customResult = SynthesisRecipeHelper.getResult(gridArray);
            if (customResult != null && !customResult.isEmpty()) {
                this.expectedOutputCount = customResult.getCount();
                this.inventory.setStackInSlot(81, customResult.copy());
                setChanged();
                return;
            }

            // 3. DATAPACK 9x9 RECIPES
            NonNullList<ItemStack> ingredients = NonNullList.withSize(81, ItemStack.EMPTY);
            for (int i = 0; i < 81; i++) {
                ingredients.set(i, gridArray[i]);
            }
            CraftingInput input = CraftingInput.of(9, 9, ingredients);

            Optional<RecipeHolder<dev.davidklgames.puremashtweaks.recipe.ShapedSynthesisRecipe>> shapedMatch = serverLevel.recipeAccess().getRecipeFor(
                    dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPED_SYNTHESIS_TYPE.get(),
                    input,
                    serverLevel
            );

            if (shapedMatch.isPresent()) {
                ItemStack result = shapedMatch.get().value().assemble(input);
                this.expectedOutputCount = result.getCount();
                this.inventory.setStackInSlot(81, result);
                setChanged();
                return;
            }

            Optional<RecipeHolder<dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe>> shapelessMatch = serverLevel.recipeAccess().getRecipeFor(
                    dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPELESS_SYNTHESIS_TYPE.get(),
                    input,
                    serverLevel
            );

            if (shapelessMatch.isPresent()) {
                ItemStack result = shapelessMatch.get().value().assemble(input);
                this.expectedOutputCount = result.getCount();
                this.inventory.setStackInSlot(81, result);
                setChanged();
                return;
            }

            this.expectedOutputCount = 0;
            this.inventory.setStackInSlot(81, ItemStack.EMPTY);
            setChanged();
        }
    }

    public void consumeCraftingIngredientsSilent() {
        this.bypassGridLock = true;
        try {
            for (int i = 0; i < 81; i++) {
                ItemStack stack = this.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    int toShrink = hasMemoryCardRecipe ? Math.max(1, cachedExpectedCounts[i]) : 1;
                    ItemStackTemplate remainderTemplate = stack.getCraftingRemainder();
                    ItemStack remaining = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;

                    stack.shrink(toShrink);
                    if (stack.isEmpty() && !remaining.isEmpty()) {
                        this.inventory.setStackSilent(i, remaining);
                    }
                }
            }
        } finally {
            this.bypassGridLock = false;
        }
    }

    public void consumeCraftingIngredients() {
        consumeCraftingIngredientsSilent();
        updateCraftingResult();
    }

    public void encodeCurrentRecipeToCard() {
        ItemStack cardStack = this.inventory.getStackInSlot(82);
        if (cardStack.isEmpty() || !cardStack.is(ModItems.MEMORY_CARD.get())) return;

        CompoundTag recipeTag = new CompoundTag();
        ListTag itemsList = new ListTag();

        for (int i = 0; i < 81; i++) {
            ItemStack gridStack = this.inventory.getStackInSlot(i);
            if (!gridStack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                assert this.level != null;
                ItemStack.CODEC.encodeStart(this.level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), gridStack)
                        .result().ifPresent(tag -> {
                            if (tag instanceof CompoundTag stackTag) itemTag.put("Item", stackTag);
                        });
                itemsList.add(itemTag);
            }
        }

        recipeTag.put("GridItems", itemsList);
        ItemStack outputStack = this.inventory.getStackInSlot(81);
        if (!outputStack.isEmpty()) {
            recipeTag.putString("OutputName", outputStack.getHoverName().getString());
            recipeTag.putInt("OutputCount", outputStack.getCount());

            assert this.level != null;
            ItemStack.CODEC.encodeStart(this.level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), outputStack)
                    .result().ifPresent(tag -> {
                        if (tag instanceof CompoundTag outputTag) recipeTag.put("OutputItem", outputTag);
                    });
        } else {
            recipeTag.putString("OutputName", "Empty Craft");
            recipeTag.putInt("OutputCount", 0);
        }

        cardStack.set(PureMashDataComponents.RECIPE_CARD_DATA.get(), recipeTag);
        this.inventory.setStackInSlot(82, cardStack);
        updateMemoryCardCache();
        setChanged();
    }

    public boolean isAutomationActive() { return this.automationActive; }
    public void setAutomationActive(boolean active) {
        this.automationActive = active;
        if (!active) this.automationMode = 0;
        updateMemoryCardCache();
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getAutomationMode() { return this.automationMode; }
    public void setAutomationMode(int mode) {
        this.automationMode = mode;
        updateMemoryCardCache();
        setChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.puremashtweaks.synthesis_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NonNull Inventory playerInv, @NonNull Player player) {
        return new SynthesisTableMenu(id, playerInv, this);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.inventory.serialize(output);
        output.putInt("Mode", this.activeMode);
        output.putBoolean("Automation", this.automationActive);
        output.putInt("AutomationMode", this.automationMode);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.inventory.deserialize(input);
        this.activeMode = input.getIntOr("Mode", 0);
        this.automationActive = input.getBooleanOr("Automation", false);
        this.automationMode = input.getIntOr("AutomationMode", 0);
    }

    // ----------------------------------------------------------------------------------------------------
    // HIGH-PERFORMANCE TRANSFER API HANDLER (BALANCED PATTERN RECONSTRUCTION)
    // ----------------------------------------------------------------------------------------------------

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
            updateCraftingResult();
        }
    };

    private final net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> automationHandler = new net.neoforged.neoforge.transfer.ResourceHandler<>() {
        @Override
        public int size() { return 82; }

        @Override
        public @NonNull ItemResource getResource(int slot) {
            if (slot == 81) {
                ItemStack currentResult = inventory.getStackInSlot(81);
                if (!currentResult.isEmpty()) {
                    return ItemResource.of(currentResult);
                }
                if (hasMemoryCardRecipe && !memoryCardOutputCache.isEmpty()) {
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
            if (slot == 81) {
                return cachedOutputResource.isEmpty() ? 64 : cachedOutputResource.toStack().getMaxStackSize();
            }
            if (automationActive && automationMode == 0 && hasMemoryCardRecipe) {
                ItemResource expected = cachedExpectedResources[slot];
                if (!expected.isEmpty() && expected.equals(resource)) {
                    return resource.toStack().getMaxStackSize();
                }
                return 0;
            }
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isValid(int slot, @NonNull ItemResource resource) {
            if (slot == 81) return false;
            if (automationActive && automationMode == 0 && hasMemoryCardRecipe) {
                return cachedExpectedResources[slot].equals(resource);
            }
            return inventory.isItemValid(slot, resource.toStack(1));
        }

        @Override
        public int insert(int index, @NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
            if (automationActive && automationMode == 1) return 0;
            if (index >= 81 || amount <= 0 || resource.isEmpty() || isGridLocked()) return 0;

            if (automationActive && automationMode == 0 && hasMemoryCardRecipe) {
                ItemResource expected = cachedExpectedResources[index];
                if (!expected.equals(resource)) return 0;

                int maxStack = resource.toStack().getMaxStackSize();
                ItemStack currentStack = inventory.getStackInSlot(index);
                if (currentStack.getCount() >= maxStack) return 0;

                int space = maxStack - currentStack.getCount();
                int toInsertAmount = Math.min(amount, space);

                journal.updateSnapshots(transaction);

                ItemStack newStack = resource.toStack(currentStack.getCount() + toInsertAmount);
                inventory.setStackSilent(index, newStack);

                return toInsertAmount;
            }

            ItemStack stack = resource.toStack(amount);
            ItemStack remainder = inventory.insertItem(index, stack, true);
            int inserted = amount - remainder.getCount();
            if (inserted > 0) {
                journal.updateSnapshots(transaction);
                ItemStack newStack = inventory.getStackInSlot(index).copy();
                if (newStack.isEmpty()) newStack = resource.toStack(inserted);
                else newStack.grow(inserted);
                inventory.setStackSilent(index, newStack);
            }
            return inserted;
        }

        @Override
        public int extract(int index, @NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
            if (automationActive && automationMode == 1) return 0;
            if (index != 81 || amount <= 0 || resource.isEmpty()) return 0;

            ItemStack currentResult = inventory.getStackInSlot(81);
            if (currentResult.isEmpty() || !ItemResource.of(currentResult).equals(resource)) {
                return 0;
            }

            int extracted = Math.min(amount, currentResult.getCount());
            if (extracted > 0) {
                journal.updateSnapshots(transaction);

                ItemStack newStack = currentResult.copy();
                newStack.shrink(extracted);
                inventory.setStackSilent(81, newStack);

                if (newStack.isEmpty()) {
                    consumeCraftingIngredientsSilent();

                    boolean canCraftNext = true;
                    for (int i = 0; i < 81; i++) {
                        if (!cachedExpectedResources[i].isEmpty()) {
                            if (inventory.getStackInSlot(i).getCount() < cachedExpectedCounts[i]) {
                                canCraftNext = false;
                                break;
                            }
                        }
                    }
                    if (canCraftNext && !memoryCardOutputCache.isEmpty()) {
                        inventory.setStackSilent(81, memoryCardOutputCache.copy());
                    }
                }
            }
            return extracted;
        }

        @Override
        public int insert(@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
            if (automationActive && automationMode == 1) return 0;
            if (amount <= 0 || resource.isEmpty()) return 0;

            if (automationActive && automationMode == 0 && hasMemoryCardRecipe) {
                List<Integer> validSlots = resourceToSlotsMap.get(resource);
                if (validSlots == null || validSlots.isEmpty()) return 0;

                int totalInserted = 0;
                int maxStack = resource.toStack().getMaxStackSize();

                while (amount > 0) {
                    int minRatio = Integer.MAX_VALUE;
                    for (int slot : validSlots) {
                        int neededPerCraft = cachedExpectedCounts[slot];
                        int currentCount = inventory.getStackInSlot(slot).getCount();
                        minRatio = Math.min(minRatio, currentCount / neededPerCraft);
                    }

                    boolean insertedInRound = false;

                    for (int slot : validSlots) {
                        if (amount <= 0) break;

                        int neededPerCraft = cachedExpectedCounts[slot];
                        int currentCount = inventory.getStackInSlot(slot).getCount();
                        int currentRatio = currentCount / neededPerCraft;

                        if (currentRatio == minRatio && currentCount < maxStack) {
                            int spaceInQuota = ((minRatio + 1) * neededPerCraft) - currentCount;
                            int toInsert = Math.min(amount, Math.min(spaceInQuota, maxStack - currentCount));

                            if (toInsert > 0) {
                                int insertedHere = this.insert(slot, resource, toInsert, transaction);
                                if (insertedHere > 0) {
                                    amount -= insertedHere;
                                    totalInserted += insertedHere;
                                    insertedInRound = true;
                                }
                            }
                        }
                    }

                    if (!insertedInRound) {
                        break;
                    }
                }

                return totalInserted;
            }

            int totalInserted = 0;
            for (int i = 0; i < 81; i++) {
                if (amount <= 0) break;
                int insertedHere = this.insert(i, resource, amount, transaction);
                amount -= insertedHere;
                totalInserted += insertedHere;
            }
            return totalInserted;
        }

        @Override
        public int extract(@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
            return this.extract(81, resource, amount, transaction);
        }
    };

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            for (int i = 0; i < 81; i++) {
                ItemStack stack = this.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(
                            this.level,
                            pos.getX(), pos.getY(), pos.getZ(),
                            stack
                    );
                }
            }
            ItemStack card = this.inventory.getStackInSlot(82);
            if (!card.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        pos.getX(), pos.getY(), pos.getZ(),
                        card
                );
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

    public net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> getAutomationHandler() {
        return this.automationHandler;
    }
}