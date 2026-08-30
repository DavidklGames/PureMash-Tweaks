package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.block.PureMashGeneratorBlock;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.menu.PureMashGeneratorMenu;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModFluids;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@SuppressWarnings({"removal"})
public class PureMashGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    // Common & Legacy Uranium Tags
    private static final TagKey<Item> URANIUM_INGOTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/uranium"));
    private static final TagKey<Item> URANIUM_BLOCKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/uranium"));
    private static final TagKey<Item> URANIUM_RAW = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "raw_materials/uranium"));
    private static final TagKey<Item> URANIUM_DUSTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/uranium"));
    private static final TagKey<Item> URANIUM_ORES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/uranium"));

    private static final TagKey<Item> FORGE_URANIUM_INGOTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "ingots/uranium"));
    private static final TagKey<Item> FORGE_URANIUM_BLOCKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "storage_blocks/uranium"));
    private static final TagKey<Item> FORGE_URANIUM_RAW = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "raw_materials/uranium"));

    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int currentFePerTick = 80;
    private double temperature = 20.0;
    private boolean isEnergyFull = false;
    private ItemStack activeFuelStack = ItemStack.EMPTY;
    private int internalCoolantBuffer = 0;

    public record FuelData(int burnTicks, int fePerTick, double maxTemp) {}

    // Base 500M FE Capacity, dynamically multiplied by Capacity Upgrades
    public final SimpleEnergyHandler energyTank = new SimpleEnergyHandler(500000000, 5000000, 5000000) {
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
        long baseCapacity = PureMashTweaksConfig.COMMON.puremashGeneratorBaseEnergyCapacity.get();
        long total = baseCapacity * getCapacityMultiplier();
        return Math.min(2000000000L, total);
    }

    public int getCapacityMultiplier() {
        int mult = 1;
        for (int i = 1; i <= 3; i++) {
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
                stack.is(ModItems.DUPLICATION_UPGRADE_2.get());
    }

    // Coolant / Water Tank (20,000 mB = 20k)
    public final FluidStacksResourceHandler waterTank = new FluidStacksResourceHandler(1, 20000) {
        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return isCoolantFluid(resource.getFluid());
        }

        @Override
        protected void onContentsChanged(int index, net.neoforged.neoforge.fluids.@NonNull FluidStack previousContents) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    // Steam Output Tank (100,000 mB = 100k)
    public final FluidStacksResourceHandler steamTank = new FluidStacksResourceHandler(1, 100000) {
        @Override
        protected void onContentsChanged(int index, net.neoforged.neoforge.fluids.@NonNull FluidStack previousContents) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    // 8 Slots: 0 = Fuel, 1-3 = Upgrades, 4-6 = Waste, 7 = Charge Slot
    public final ItemStackHandler inventory = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) return isFuelValid(stack);
            if (slot >= 1 && slot <= 3) {
                return isUpgradeValid(stack);
            }
            if (slot == 7) {
                return stack.getCapability(Capabilities.Energy.ITEM, net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack)) != null;
            }
            return false;
        }
    };

    public PureMashGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PUREMASH_GENERATOR_BE.get(), pos, state);
    }

    public static boolean isCoolantFluid(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) return true;
        return fluid.defaultFluidState().is(TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "coolants"))) ||
                fluid.defaultFluidState().is(TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "coolant")));
    }

    public static boolean isUranium(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(URANIUM_INGOTS) || stack.is(URANIUM_BLOCKS) || stack.is(URANIUM_RAW) || stack.is(URANIUM_DUSTS) || stack.is(URANIUM_ORES)) {
            return true;
        }
        if (stack.is(FORGE_URANIUM_INGOTS) || stack.is(FORGE_URANIUM_BLOCKS) || stack.is(FORGE_URANIUM_RAW)) {
            return true;
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("uranium");
    }

    public static boolean isFuelValid(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        if (stack.is(ModBlocks.MOLDELONIAN_BLOCK.get().asItem()) ||
                stack.is(ModItems.MOLDELONIAN_INGOT.get()) ||
                stack.is(ModBlocks.SYNTHORIUM_BLOCK.get().asItem()) ||
                stack.is(ModItems.SYNTHORIUM_INGOT.get()) ||
                stack.is(ModItems.SYNTHORIUM_SCRAP.get())) {
            return true;
        }

        if (isUranium(stack)) {
            return true;
        }

        if (stack.is(Items.REDSTONE_BLOCK) || stack.is(Items.REDSTONE)) {
            return true;
        }

        return stack.is(Items.COAL_BLOCK) || stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(net.minecraft.tags.ItemTags.COALS);
    }

    public static FuelData getFuelData(ItemStack stack) {
        if (!isFuelValid(stack)) return new FuelData(0, 0, 20.0);

        if (stack.is(ModBlocks.MOLDELONIAN_BLOCK.get().asItem())) {
            return new FuelData(12000, 15000, 1500.0);
        }
        if (stack.is(ModItems.MOLDELONIAN_INGOT.get())) {
            return new FuelData(1200, 5000, 1200.0);
        }

        if (stack.is(URANIUM_BLOCKS) || stack.is(FORGE_URANIUM_BLOCKS) || (isUranium(stack) && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("block"))) {
            return new FuelData(18000, 8000, 1400.0);
        }
        if (isUranium(stack)) {
            return new FuelData(1800, 3000, 1000.0);
        }

        if (stack.is(ModBlocks.SYNTHORIUM_BLOCK.get().asItem())) {
            return new FuelData(6000, 2500, 800.0);
        }
        if (stack.is(ModItems.SYNTHORIUM_INGOT.get())) {
            return new FuelData(600, 1000, 600.0);
        }
        if (stack.is(ModItems.SYNTHORIUM_SCRAP.get())) {
            return new FuelData(200, 500, 400.0);
        }
        if (stack.is(Items.REDSTONE_BLOCK)) {
            return new FuelData(1200, 300, 300.0);
        }
        if (stack.is(Items.REDSTONE)) {
            return new FuelData(120, 200, 200.0);
        }
        if (stack.is(Items.COAL_BLOCK)) {
            return new FuelData(1600, 150, 250.0);
        }
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(net.minecraft.tags.ItemTags.COALS)) {
            return new FuelData(200, 100, 150.0);
        }

        return new FuelData(0, 0, 20.0);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PureMashGeneratorBlockEntity be) {
        if (level.isClientSide()) return;

        long currentEnergy = be.energyTank.getAmountAsLong();
        long maxCap = be.getEnergyCapacity();

        if (currentEnergy >= maxCap) {
            be.isEnergyFull = true;
        } else if (currentEnergy <= (long)(maxCap * 0.75)) {
            be.isEnergyFull = false;
        }

        boolean stateChanged = false;
        int speedMultiplier = be.getSpeedLevel();

        // 1. Process active burning & heat management
        if (be.isBurning()) {
            if (!be.isEnergyFull) {
                int ticksToConsume = Math.min(be.burnTime, speedMultiplier);
                be.burnTime -= ticksToConsume;

                double efficiency = 1.0;
                if (be.getEffectiveWaterAmount() <= 0 && be.temperature > 200.0) {
                    efficiency = Math.max(0.25, 1.0 - ((be.temperature - 200.0) / 1000.0));
                }

                long generationRate = (long) (be.currentFePerTick * ticksToConsume * efficiency);
                long newEnergy = Math.min(maxCap, currentEnergy + generationRate);
                be.energyTank.set((int) newEnergy);

                FuelData fuelData = getFuelData(be.activeFuelStack);
                if (be.temperature < fuelData.maxTemp()) {
                    be.temperature = Math.min(fuelData.maxTemp(), be.temperature + (1.0 * ticksToConsume));
                }

                if (be.burnTime <= 0) {
                    be.burnTime = 0;
                    be.maxBurnTime = 0;
                    be.produceWasteAtEnd(be.activeFuelStack);
                    be.activeFuelStack = ItemStack.EMPTY;
                }
                stateChanged = true;
            }
        } else {
            if (be.temperature > 20.0) {
                be.temperature = Math.max(20.0, be.temperature - 1.0);
                stateChanged = true;
            }
        }

        // 2. Cooling & Steam Production
        if (be.temperature > 100.0 && (be.waterTank.getAmountAsLong(0) > 0 || be.internalCoolantBuffer > 0)) {
            int waterNeeded = (int) Math.clamp((be.temperature - 100.0) / 4.0, 10.0, 200.0);

            if (be.internalCoolantBuffer < waterNeeded && be.waterTank.getAmountAsLong(0) >= 1000) {
                try (Transaction tx = Transaction.openRoot()) {
                    be.waterTank.extract(0, be.waterTank.getResource(0), 1000, tx);
                    be.internalCoolantBuffer += 1000;
                    tx.commit();
                }
            }

            int waterToCool = Math.min(be.internalCoolantBuffer, waterNeeded);

            if (waterToCool > 0) {
                be.internalCoolantBuffer -= waterToCool;
                be.temperature = Math.max(100.0, be.temperature - (3.0 * waterToCool));

                if (be.steamTank.getAmountAsLong(0) < 100000) {
                    try (Transaction tx = Transaction.openRoot()) {
                        be.steamTank.insert(0, FluidResource.of(ModFluids.STEAM_SOURCE.get()), waterToCool * 10, tx);
                        tx.commit();
                    }
                }
                stateChanged = true;
            }
        }

        // 3. Charge Slot
        ItemStack chargeStack = be.inventory.getStackInSlot(7);
        if (!chargeStack.isEmpty() && be.energyTank.getAmountAsLong() > 0) {
            var itemAccess = net.neoforged.neoforge.transfer.access.ItemAccess.forStack(chargeStack);
            EnergyHandler itemEnergy = chargeStack.getCapability(Capabilities.Energy.ITEM, itemAccess);

            if (itemEnergy != null) {
                long maxTransfer = Math.min(50000L, be.energyTank.getAmountAsLong());
                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = itemEnergy.insert((int) maxTransfer, tx);
                    if (inserted > 0) {
                        be.energyTank.set((int) (be.energyTank.getAmountAsLong() - inserted));
                        tx.commit();
                        stateChanged = true;
                    }
                }
            }
        }

        // 4. Consume Fuel
        if (!be.isBurning() && !be.isEnergyFull) {
            ItemStack fuelStack = be.inventory.getStackInSlot(0);
            if (!fuelStack.isEmpty() && isFuelValid(fuelStack)) {
                FuelData fuelData = getFuelData(fuelStack);
                if (fuelData.burnTicks() > 0) {
                    be.burnTime = fuelData.burnTicks();
                    be.maxBurnTime = fuelData.burnTicks();
                    be.currentFePerTick = fuelData.fePerTick();
                    be.activeFuelStack = fuelStack.copy();

                    ItemStackTemplate remainderTemplate = fuelStack.getCraftingRemainder();
                    ItemStack remainder = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
                    fuelStack.shrink(1);

                    if (fuelStack.isEmpty() && !remainder.isEmpty()) {
                        be.inventory.setStackInSlot(0, remainder);
                    }

                    stateChanged = true;
                }
            }
        }

        // 5. Dynamic Blockstate LIT management (Lit ONLY when actively generating and not paused by full battery)
        boolean shouldBeLit = be.isBurning() && !be.isEnergyFull;
        BlockState currentState = level.getBlockState(pos);
        if (currentState.hasProperty(PureMashGeneratorBlock.LIT) && currentState.getValue(PureMashGeneratorBlock.LIT) != shouldBeLit) {
            level.setBlock(pos, currentState.setValue(PureMashGeneratorBlock.LIT, shouldBeLit), 3);
            stateChanged = true;
        }

        if (be.energyTank.getAmountAsLong() > 0) {
            be.pushEnergyToNeighbors(level, pos);
        }

        if (stateChanged) {
            be.setChanged();
        }
    }

    public int getEffectiveWaterAmount() {
        long total = this.waterTank.getAmountAsLong(0) + this.internalCoolantBuffer;
        return (int) Math.min(20000L, total);
    }

    private static final TagKey<Item> URANIUM_NUGGETS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "nuggets/uranium"));
    private static final TagKey<Item> FORGE_URANIUM_NUGGETS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "nuggets/uranium"));

    private static ItemStack getUraniumNugget(int count) {
        var cNuggets = BuiltInRegistries.ITEM.get(URANIUM_NUGGETS);
        if (cNuggets.isPresent()) {
            var firstHolder = cNuggets.get().stream().findFirst();
            if (firstHolder.isPresent()) {
                return new ItemStack(firstHolder.get().value(), count);
            }
        }

        var forgeNuggets = BuiltInRegistries.ITEM.get(FORGE_URANIUM_NUGGETS);
        if (forgeNuggets.isPresent()) {
            var firstHolder = forgeNuggets.get().stream().findFirst();
            if (firstHolder.isPresent()) {
                return new ItemStack(firstHolder.get().value(), count);
            }
        }

        return new ItemStack(Items.REDSTONE, count);
    }

    private void produceWasteAtEnd(ItemStack fuelStack) {
        if (fuelStack.isEmpty() || this.level == null) return;

        if (this.level.getRandom().nextFloat() > 0.25f) return;

        ItemStack waste = ItemStack.EMPTY;

        if (fuelStack.is(ModBlocks.MOLDELONIAN_BLOCK.get().asItem())) {
            waste = new ItemStack(ModItems.MOLDELONIAN_NUGGET.get(), 2);
        } else if (fuelStack.is(ModItems.MOLDELONIAN_INGOT.get())) {
            waste = new ItemStack(ModItems.MOLDELONIAN_NUGGET.get(), 1);
        } else if (fuelStack.is(ModBlocks.SYNTHORIUM_BLOCK.get().asItem())) {
            waste = new ItemStack(ModItems.SYNTHORIUM_NUGGET.get(), 2);
        } else if (fuelStack.is(ModItems.SYNTHORIUM_INGOT.get()) || fuelStack.is(ModItems.SYNTHORIUM_SCRAP.get())) {
            waste = new ItemStack(ModItems.SYNTHORIUM_NUGGET.get(), 1);
        } else if (isUranium(fuelStack)) {
            boolean isBlock = fuelStack.is(URANIUM_BLOCKS) || fuelStack.is(FORGE_URANIUM_BLOCKS) || BuiltInRegistries.ITEM.getKey(fuelStack.getItem()).getPath().contains("block");
            waste = getUraniumNugget(isBlock ? 2 : 1);
        }

        if (!waste.isEmpty()) {
            double dupChance = getDuplicationChance();
            if (dupChance > 0.0 && this.level.getRandom().nextDouble() < dupChance) {
                waste.grow(1);
            }

            for (int slot = 4; slot <= 6; slot++) {
                ItemStack current = this.inventory.getStackInSlot(slot);
                if (current.isEmpty()) {
                    this.inventory.setStackInSlot(slot, waste.copy());
                    break;
                } else if (ItemStack.isSameItemSameComponents(current, waste)) {
                    int space = current.getMaxStackSize() - current.getCount();
                    if (space >= waste.getCount()) {
                        current.grow(waste.getCount());
                        break;
                    }
                }
            }
        }
    }

    private double getDuplicationChance() {
        if (!PureMashTweaksConfig.COMMON.enableDuplication.get()) return 0.0;
        double chance = 0.0;
        for (int i = 1; i <= 3; i++) {
            ItemStack upgrade = this.inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.DUPLICATION_UPGRADE_1.get())) {
                chance += PureMashTweaksConfig.COMMON.duplicationUpgrade1Chance.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.DUPLICATION_UPGRADE_2.get())) {
                chance += PureMashTweaksConfig.COMMON.duplicationUpgrade2Chance.get() * upgrade.getCount();
            }
        }
        return Math.min(1.0, chance);
    }

    private void pushEnergyToNeighbors(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = pos.relative(direction);
            EnergyHandler targetEnergy = level.getCapability(Capabilities.Energy.BLOCK, targetPos, direction.getOpposite());

            if (targetEnergy != null) {
                long currentStored = this.energyTank.getAmountAsLong();
                if (currentStored <= 0) break;

                long maxSend = Math.min(currentStored, 500000L);

                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = targetEnergy.insert((int) maxSend, tx);
                    if (inserted > 0) {
                        this.energyTank.set((int) (currentStored - inserted));
                        tx.commit();
                    }
                }
            }
        }
    }

    public net.neoforged.neoforge.transfer.ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return new net.neoforged.neoforge.transfer.ResourceHandler<>() {
            @Override
            public int size() { return 2; }

            @Override
            public @NonNull FluidResource getResource(int slot) {
                if (slot == 0) {
                    FluidResource res = waterTank.getResource(0);
                    if (!res.isEmpty() && !isCoolantFluid(res.getFluid())) return FluidResource.EMPTY;
                    return res;
                }
                if (slot == 1) return steamTank.getResource(0);
                return FluidResource.EMPTY;
            }

            @Override
            public long getAmountAsLong(int slot) {
                if (slot == 0) return getEffectiveWaterAmount();
                if (slot == 1) return Math.min(steamTank.getAmountAsLong(0), 100000L);
                return 0;
            }

            @Override
            public long getCapacityAsLong(int slot, @NonNull FluidResource resource) {
                if (slot == 0) return 20000L;
                if (slot == 1) return 100000L;
                return 0;
            }

            @Override
            public boolean isValid(int slot, @NonNull FluidResource resource) {
                if (slot == 0) return isCoolantFluid(resource.getFluid());
                return false;
            }

            @Override
            public int insert(int slot, @NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
                if (slot == 0 && isCoolantFluid(resource.getFluid())) {
                    return waterTank.insert(0, resource, amount, transaction);
                }
                return 0;
            }

            @Override
            public int extract(int slot, @NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
                if (slot == 1) {
                    return steamTank.extract(0, resource, amount, transaction);
                }
                return 0;
            }

            @Override
            public int insert(@NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
                if (isCoolantFluid(resource.getFluid())) {
                    return waterTank.insert(0, resource, amount, transaction);
                }
                return 0;
            }

            @Override
            public int extract(@NonNull FluidResource resource, int amount, @NonNull TransactionContext transaction) {
                return steamTank.extract(0, resource, amount, transaction);
            }
        };
    }

    private final net.neoforged.neoforge.transfer.transaction.SnapshotJournal<ItemStack[]> journal = new net.neoforged.neoforge.transfer.transaction.SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] snap = new ItemStack[8];
            for (int i = 0; i < 8; i++) snap[i] = inventory.getStackInSlot(i).copy();
            return snap;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            for (int i = 0; i < 8; i++) inventory.setStackInSlot(i, snapshot[i]);
        }

        @Override
        protected void onRootCommit(ItemStack[] originalState) {
            setChanged();
        }
    };

    public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> getItemHandler(@Nullable Direction side) {
        return new net.neoforged.neoforge.transfer.ResourceHandler<>() {
            @Override
            public int size() { return 8; }

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
            public int insert(int slot, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if (slot == 0) {
                    if (!isFuelValid(resource.toStack(amount))) return 0;
                    ItemStack remainder = inventory.insertItem(0, resource.toStack(amount), true);
                    int inserted = amount - remainder.getCount();
                    if (inserted > 0) {
                        journal.updateSnapshots(transaction);
                        ItemStack current = inventory.getStackInSlot(0).copy();
                        if (current.isEmpty()) current = resource.toStack(inserted);
                        else current.grow(inserted);
                        inventory.setStackInSlot(0, current);
                    }
                    return inserted;
                }
                if (slot == 7) {
                    ItemStack remainder = inventory.insertItem(7, resource.toStack(amount), true);
                    int inserted = amount - remainder.getCount();
                    if (inserted > 0) {
                        journal.updateSnapshots(transaction);
                        ItemStack current = inventory.getStackInSlot(7).copy();
                        if (current.isEmpty()) current = resource.toStack(inserted);
                        else current.grow(inserted);
                        inventory.setStackInSlot(7, current);
                    }
                    return inserted;
                }
                return 0;
            }

            @Override
            public int extract(int slot, net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                if ((slot >= 4 && slot <= 6) || slot == 7) {
                    ItemStack current = inventory.getStackInSlot(slot);
                    if (current.isEmpty() || !net.neoforged.neoforge.transfer.item.ItemResource.of(current).equals(resource)) {
                        return 0;
                    }
                    int extracted = Math.min(amount, current.getCount());
                    if (extracted > 0) {
                        journal.updateSnapshots(transaction);
                        ItemStack newStack = current.copy();
                        newStack.shrink(extracted);
                        inventory.setStackInSlot(slot, newStack);
                    }
                    return extracted;
                }
                return 0;
            }

            @Override
            public int insert(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                return this.insert(0, resource, amount, transaction);
            }

            @Override
            public int extract(net.neoforged.neoforge.transfer.item.@NonNull ItemResource resource, int amount, @NonNull TransactionContext transaction) {
                for (int s = 4; s <= 7; s++) {
                    int extracted = this.extract(s, resource, amount, transaction);
                    if (extracted > 0) return extracted;
                }
                return 0;
            }
        };
    }

    public int getCurrentGenerationRate() {
        if (!isBurning() || isEnergyFull) return 0;
        return currentFePerTick * getSpeedLevel();
    }

    public int getTemperatureCelsius() {
        return (int) Math.round(this.temperature);
    }

    public int getSpeedLevel() {
        int speed = 1;
        int maxSlots = this.inventory.getSlots();

        for (int i = 1; i < maxSlots && i <= 3; i++) {
            ItemStack upgrade = this.inventory.getStackInSlot(i);
            if (upgrade.is(ModItems.SPEED_UPGRADE_1.get())) {
                speed += PureMashTweaksConfig.COMMON.speedUpgrade1Power.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.SPEED_UPGRADE_2.get())) {
                speed += PureMashTweaksConfig.COMMON.speedUpgrade2Power.get() * upgrade.getCount();
            } else if (upgrade.is(ModItems.SPEED_UPGRADE_3.get())) {
                speed += PureMashTweaksConfig.COMMON.speedUpgrade3Power.get() * upgrade.getCount();
            }
        }
        return speed;
    }

    public boolean isBurning() { return this.burnTime > 0; }
    public int getBurnTime() { return this.burnTime; }
    public int getMaxBurnTime() { return this.maxBurnTime; }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.inventory.serialize(output);
        this.waterTank.serialize(output);
        this.steamTank.serialize(output);
        output.putInt("BurnTime", this.burnTime);
        output.putInt("MaxBurnTime", this.maxBurnTime);
        output.putInt("FePerTick", this.currentFePerTick);
        output.putDouble("Temperature", this.temperature);
        output.putBoolean("IsEnergyFull", this.isEnergyFull);
        output.putInt("InternalCoolantBuffer", this.internalCoolantBuffer);
        output.putLong("Energy", this.energyTank.getAmountAsLong());
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.inventory.deserialize(input);
        this.waterTank.deserialize(input);
        this.steamTank.deserialize(input);

        if (this.inventory.getSlots() < 8) {
            this.inventory.setSize(8);
        }

        this.burnTime = input.getIntOr("BurnTime", 0);
        this.maxBurnTime = input.getIntOr("MaxBurnTime", 0);
        this.currentFePerTick = input.getIntOr("FePerTick", 80);
        this.temperature = input.getDoubleOr("Temperature", 20.0);
        this.isEnergyFull = input.getBooleanOr("IsEnergyFull", false);
        this.internalCoolantBuffer = input.getIntOr("InternalCoolantBuffer", 0);
        this.energyTank.set((int) input.getLongOr("Energy", 0L));

        if (!this.waterTank.getResource(0).isEmpty() && !isCoolantFluid(this.waterTank.getResource(0).getFluid())) {
            this.waterTank.set(0, FluidResource.EMPTY, 0);
        }
        if (this.waterTank.getAmountAsLong(0) > 20000) {
            this.waterTank.set(0, this.waterTank.getResource(0), 20000);
        }
        if (this.steamTank.getAmountAsLong(0) > 100000) {
            this.steamTank.set(0, this.steamTank.getResource(0), 100000);
        }
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

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.puremashtweaks.puremash_generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new PureMashGeneratorMenu(id, playerInv, this);
    }
}