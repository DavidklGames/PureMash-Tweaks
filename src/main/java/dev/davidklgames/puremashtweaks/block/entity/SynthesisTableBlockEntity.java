package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.api.SynthesisRecipeHelper;
import dev.davidklgames.puremashtweaks.component.ModDataComponents;
import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"removal"})
public class SynthesisTableBlockEntity extends BlockEntity implements MenuProvider {

    private int activeMode = 0;
    private boolean automationActive = false;
    private int automationMode = 0; // 0 = Craft, 1 = Encoder
    private int expectedOutputCount = 0;
    private boolean bypassGridLock = false;

    // High-speed Memory Card Cache
    private final ItemStack[] memoryCardCache = new ItemStack[81];
    // Memory Card result item cache
    private ItemStack memoryCardOutputCache = ItemStack.EMPTY;
    private boolean hasMemoryCardRecipe = false;

    {
        for (int i = 0; i < 81; i++) memoryCardCache[i] = ItemStack.EMPTY;
    }

    // ----------------------------------------------------------------------------------------------------
    // CUSTOM INVENTORY: To resolve visibility conflicts and allow Transactions
    // ----------------------------------------------------------------------------------------------------
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
            if (slot >= 81 || isGridLocked()) return stack;
            if (stack.isEmpty()) return stack;

            // Handling for AE2 and Restricted Automation (1 Recipe at a time)
            if (automationActive && automationMode == 0 && hasMemoryCardRecipe) {
                ItemStack expected = memoryCardCache[slot];
                if (expected.isEmpty() || !ItemStack.isSameItemSameComponents(stack, expected)) {
                    return stack;
                }
                int required = expected.getCount();
                ItemStack currentStack = this.getStackInSlot(slot);
                if (currentStack.getCount() >= required) {
                    return stack;
                }
                int space = required - currentStack.getCount();
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

        // Special Functions for the Transaction API (SnapshotJournal)
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

    // ----------------------------------------------------------------------------------------------------
    // CONSTRUCTOR AND CRAFTING LOGIC
    // ----------------------------------------------------------------------------------------------------

    public SynthesisTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SYNTHESIS_TABLE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateMemoryCardCache();
    }

    private void updateMemoryCardCache() {
        if (this.level == null) {
            this.hasMemoryCardRecipe = false;
            this.memoryCardOutputCache = ItemStack.EMPTY;
            return;
        }
        for (int i = 0; i < 81; i++) memoryCardCache[i] = ItemStack.EMPTY;
        this.hasMemoryCardRecipe = false;
        this.memoryCardOutputCache = ItemStack.EMPTY;

        ItemStack card = inventory.getStackInSlot(82);
        if (automationActive && automationMode == 0 && !card.isEmpty() && card.is(ModItems.MEMORY_CARD.get()) && card.has(ModDataComponents.RECIPE_CARD_DATA.get())) {
            CompoundTag recipeData = card.get(ModDataComponents.RECIPE_CARD_DATA.get());
            if (recipeData == null) return;
            ListTag itemsList = recipeData.getListOrEmpty("GridItems");
            var context = this.level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);

            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag itemTag = itemsList.getCompoundOrEmpty(i);
                int slot = itemTag.getIntOr("Slot", -1);
                if (slot >= 0 && slot < 81) {
                    memoryCardCache[slot] = ItemStack.CODEC.parse(context, itemTag.getCompoundOrEmpty("Item")).result().orElse(ItemStack.EMPTY);
                }
            }

            // NEW: Loads and saves the result item in the cache as soon as the card is read
            if (recipeData.contains("OutputItem")) {
                this.memoryCardOutputCache = ItemStack.CODEC.parse(context, recipeData.getCompoundOrEmpty("OutputItem")).result().orElse(ItemStack.EMPTY);
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
            NonNullList<ItemStack> ingredients = NonNullList.withSize(81, ItemStack.EMPTY);
            for (int i = 0; i < 81; i++) {
                ingredients.set(i, this.inventory.getStackInSlot(i));
            }

            // =========================================================================
            // INTERCEPTION AND READING OF THE MEMORY CARD STRICT RECIPE (Phase 4)
            // =========================================================================
            if (hasMemoryCardRecipe) {
                boolean recipeMatches = true;

                for (int i = 0; i < 81; i++) {
                    ItemStack expected = memoryCardCache[i];
                    ItemStack actual = this.inventory.getStackInSlot(i);

                    if (expected.isEmpty()) {
                        if (!actual.isEmpty()) {
                            recipeMatches = false;
                            break;
                        }
                    } else {
                        if (actual.isEmpty() || !ItemStack.isSameItemSameComponents(actual, expected) || actual.getCount() < expected.getCount()) {
                            recipeMatches = false;
                            break;
                        }
                    }
                }

                if (recipeMatches && !this.memoryCardOutputCache.isEmpty()) {
                    // Directly uses the item preloaded in the cache (saving CPU)
                    this.expectedOutputCount = this.memoryCardOutputCache.getCount();
                    this.inventory.setStackInSlot(81, this.memoryCardOutputCache.copy());
                    setChanged();
                    return;
                }

                this.expectedOutputCount = 0;
                this.inventory.setStackInSlot(81, ItemStack.EMPTY);
                setChanged();
                return;
            }

            // =========================================================================
            // INTERCEPTION AND READING OF CUSTOM 9x9 RECIPES (Phase 2)
            // =========================================================================
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

            // =========================================================================
            // MOD DATAPACK 9x9 RECIPES (Phase 3 - NEW INTEGRATION)
            // =========================================================================
            net.minecraft.world.item.crafting.CraftingInput input = net.minecraft.world.item.crafting.CraftingInput.of(9, 9, ingredients);

            // Attempts to validate the Shaped (9x9) Datapack recipe generated by DataGen
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

            // Attempts to validate the Shapeless (9x9) Datapack recipe generated by DataGen
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

            // =========================================================================
            // VANILLA 3x3 FALLBACK (Phase 4 - RECIPE TOGGLING)
            // =========================================================================
            List<RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>> matches = serverLevel.recipeAccess().recipeMap().getRecipesFor(
                    RecipeType.CRAFTING,
                    input,
                    serverLevel
            ).toList();

            this.matchingVanillaRecipesCount = matches.size();

            if (!matches.isEmpty()) {
                if (this.selectedVanillaRecipeIndex >= this.matchingVanillaRecipesCount) {
                    this.selectedVanillaRecipeIndex = 0;
                }
                RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> activeMatch = matches.get(this.selectedVanillaRecipeIndex);
                ItemStack result = activeMatch.value().assemble(input);
                this.expectedOutputCount = result.getCount();
                this.inventory.setStackInSlot(81, result);
            } else {
                this.selectedVanillaRecipeIndex = 0;
                this.expectedOutputCount = 0;
                this.inventory.setStackInSlot(81, ItemStack.EMPTY);
            }
        }
    }

    public void consumeCraftingIngredientsSilent() {
        this.bypassGridLock = true;
        try {
            for (int i = 0; i < 81; i++) {
                ItemStack stack = this.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    ItemStackTemplate remainderTemplate = stack.getCraftingRemainder();
                    ItemStack remaining = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
                    stack.shrink(1);
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

            // NEW: Saves the complete result ItemStack securely in 26.1.2
            assert this.level != null;
            ItemStack.CODEC.encodeStart(this.level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), outputStack)
                    .result().ifPresent(tag -> {
                        if (tag instanceof CompoundTag outputTag) recipeTag.put("OutputItem", outputTag);
                    });
        } else {
            recipeTag.putString("OutputName", "Empty Craft");
            recipeTag.putInt("OutputCount", 0);
        }

        cardStack.set(ModDataComponents.RECIPE_CARD_DATA.get(), recipeTag);
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
        output.putInt("SelectedVanillaRecipeIndex", this.selectedVanillaRecipeIndex);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.inventory.deserialize(input);
        this.activeMode = input.getIntOr("Mode", 0);
        this.automationActive = input.getBooleanOr("Automation", false);
        this.automationMode = input.getIntOr("AutomationMode", 0);
        this.selectedVanillaRecipeIndex = input.getIntOr("SelectedVanillaRecipeIndex", 0);
    }

    // ----------------------------------------------------------------------------------------------------
    // SECURED EXTERNAL AUTOMATION SECTION (Transfer API + SnapshotJournal)
    // ----------------------------------------------------------------------------------------------------

    // The "Patchwork Diary" that records item snapshots before AE2 performs actions.
    private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            return inventory.createSnapshot(); // Takes a "snapshot" of the table
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            inventory.restoreSnapshot(snapshot); // Returns the old snapshot if AE2 cancels
        }

        @Override
        protected void onRootCommit(ItemStack[] originalState) {
            setChanged();
            updateCraftingResult(); // Commits the operation and recalculates the result!
        }
    };

    private final net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> automationHandler = new net.neoforged.neoforge.transfer.ResourceHandler<>() {
        @Override
        public int size() { return 82; }

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
            if (slot == 81) return false;
            return inventory.isItemValid(slot, resource.toStack(1));
        }

        @Override
        public int insert(int index, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
            if (automationActive && automationMode == 1) {
                return 0;
            }

            if (index >= 81 || amount <= 0 || resource.isEmpty() || isGridLocked()) return 0;

            ItemStack stack = resource.toStack(amount);

            if (automationActive && automationMode == 0 && hasMemoryCardRecipe) {
                ItemStack expected = memoryCardCache[index];
                if (expected.isEmpty() || !ItemStack.isSameItemSameComponents(stack, expected)) {
                    return 0;
                }
                int required = expected.getCount();
                ItemStack currentStack = inventory.getStackInSlot(index);
                if (currentStack.getCount() >= required) {
                    return 0;
                }

                int toInsertAmount = Math.min(amount, required - currentStack.getCount());

                // ACTIVATES THE JOURNAL BEFORE MODIFICATION
                journal.updateSnapshots(transaction);

                ItemStack newStack = stack.copyWithCount(currentStack.getCount() + toInsertAmount);
                inventory.setStackSilent(index, newStack);
                return toInsertAmount;
            }

            // Free Auto-Crafter Mode
            ItemStack remainder = inventory.insertItem(index, stack, true); // Simulates
            int inserted = amount - remainder.getCount();
            if (inserted > 0) {
                journal.updateSnapshots(transaction); // Prepares for rollback
                ItemStack newStack = inventory.getStackInSlot(index).copy();
                if (newStack.isEmpty()) newStack = stack.copyWithCount(inserted);
                else newStack.grow(inserted);
                inventory.setStackSilent(index, newStack);
            }
            return inserted;
        }

        @Override
        public int extract(int index, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {

            // If automation is active and in 'Add' (1) mode, summarily rejects any extraction!
            if (automationActive && automationMode == 1) return 0;

            if (index != 81 || amount <= 0 || resource.isEmpty()) return 0;

            ItemStack currentResult = inventory.getStackInSlot(81);
            if (currentResult.isEmpty() || !net.neoforged.neoforge.transfer.item.ItemResource.of(currentResult).equals(resource)) {
                return 0;
            }

            int extracted = Math.min(amount, currentResult.getCount());
            if (extracted > 0) {
                // ACTIVATES THE JOURNAL BEFORE CONSUMING THE CRAFT
                journal.updateSnapshots(transaction);

                ItemStack newStack = currentResult.copy();
                newStack.shrink(extracted);
                inventory.setStackSilent(81, newStack);

                if (newStack.isEmpty()) {
                    consumeCraftingIngredientsSilent();
                }
            }
            return extracted;
        }

        @Override
        public int insert(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
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
        public int extract(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.@NonNull TransactionContext transaction) {
            return this.extract(81, resource, amount, transaction);
        }
    };

    // Official 26.1.2 logic to drop the items stored on the table before the block disappears from the world
    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            // 1. Drops the items from the crafting grid (Slots 0 to 80)
            for (int i = 0; i < 81; i++) {
                ItemStack stack = this.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(
                            this.level,
                            pos.getX(), pos.getY(), pos.getZ(),
                            stack
                    );
                }
            }
            // 2. Drops the memory card if one is inserted (Slot 82)
            ItemStack card = this.inventory.getStackInSlot(82);
            if (!card.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(
                        this.level,
                        pos.getX(), pos.getY(), pos.getZ(),
                        card
                );
            }
            // NOTE: We ignore slot 81 (Result) to prevent item duplication exploits!
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.@NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return this.saveCustomOnly(registries);
    }



    private int matchingVanillaRecipesCount = 0;
    private int selectedVanillaRecipeIndex = 0;

    public int getMatchingVanillaRecipesCount() {
        return this.matchingVanillaRecipesCount;
    }

    public void setMatchingVanillaRecipesCount(int count) {
        this.matchingVanillaRecipesCount = count;
    }

    public int getSelectedVanillaRecipeIndex() {
        return this.selectedVanillaRecipeIndex;
    }

    public void setSelectedVanillaRecipeIndex(int index) {
        this.selectedVanillaRecipeIndex = index;
    }

    public void cycleVanillaRecipe() {
        if (this.matchingVanillaRecipesCount > 1) {
            this.selectedVanillaRecipeIndex = (this.selectedVanillaRecipeIndex + 1) % this.matchingVanillaRecipesCount;
            updateCraftingResult();
            setChanged();
        }
    }

    public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> getAutomationHandler() {
        return this.automationHandler;
    }
}