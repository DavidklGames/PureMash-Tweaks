package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.block.CableBlock;
import dev.davidklgames.puremashtweaks.block.entity.cable.CableFilter;
import dev.davidklgames.puremashtweaks.block.entity.cable.CableRedstoneMode;
import dev.davidklgames.puremashtweaks.block.entity.cable.DistributionMode;
import dev.davidklgames.puremashtweaks.registry.PureMashDataComponents;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Unified Block Entity for Synthorium and Moldelonian Universal Cables.
 */
@SuppressWarnings("removal")
public class UniversalCableBlockEntity extends CableBlockEntity {

    private final int tier; // 1 = Synthorium, 2 = Moldelonian
    private final DistributionMode[] distributionModes = new DistributionMode[6];
    private final CableRedstoneMode[] redstoneModes = new CableRedstoneMode[6];
    private final boolean[] filterModes = new boolean[6]; // false = Whitelist, true = Blacklist
    private final int[] selectedTabs = new int[6]; // 0 = Energy, 1 = Item (Default), 2 = Fluid
    private final int[] roundRobinIndex = new int[6];

    public UniversalCableBlockEntity(BlockPos pos, BlockState state, int tier) {
        super(tier == 2 ? ModBlockEntities.MOLDELONIAN_UNIVERSAL_CABLE_BE.get() : ModBlockEntities.SYNTHORIUM_UNIVERSAL_CABLE_BE.get(), pos, state);
        this.tier = tier;
        for (int i = 0; i < 6; i++) {
            this.distributionModes[i] = DistributionMode.ROUND_ROBIN;
            this.redstoneModes[i] = CableRedstoneMode.IGNORED;
            this.filterModes[i] = false;
            this.selectedTabs[i] = 1; // Default to Item mode (Tab 1)
        }
    }

    public int getTier() {
        return this.tier;
    }

    public int getSelectedTab(Direction side) {
        return this.selectedTabs[side.get3DDataValue()];
    }

    public void setSelectedTab(Direction side, int tab) {
        this.selectedTabs[side.get3DDataValue()] = Math.clamp(tab, 0, 2);
        setChanged();
    }

    public DistributionMode getDistributionMode(Direction side) {
        return this.distributionModes[side.get3DDataValue()];
    }

    public void setDistributionMode(Direction side, DistributionMode mode) {
        this.distributionModes[side.get3DDataValue()] = mode;
        saveConfigurationToUpgrade(side, getSelectedTab(side));
    }

    public void setRedstoneMode(Direction side, CableRedstoneMode mode) {
        this.redstoneModes[side.get3DDataValue()] = mode;
        saveConfigurationToUpgrade(side, getSelectedTab(side));
    }

    public void setFilterMode(Direction side, boolean isBlacklist) {
        this.filterModes[side.get3DDataValue()] = isBlacklist;
        saveConfigurationToUpgrade(side, getSelectedTab(side));
    }

    public CableRedstoneMode getRedstoneMode(Direction side) {
        return this.redstoneModes[side.get3DDataValue()];
    }

    public boolean getFilterMode(Direction side) {
        return this.filterModes[side.get3DDataValue()];
    }

    public boolean hasUpgrade(Direction side) {
        ItemStack upgradeStack = this.getUpgradeInventory().getStackInSlot(0);
        return !upgradeStack.isEmpty() && isUpgradeValid(upgradeStack);
    }

    public List<CableFilter> getFilters(Direction side) {
        return getFilters(side, getSelectedTab(side));
    }

    public List<CableFilter> getFilters(Direction side, int tab) {
        ItemStack upgrade = this.getUpgradeInventory().getStackInSlot(0);
        if (upgrade.isEmpty()) return new ArrayList<>();

        CompoundTag tag = null;

        if (tab == 1 && upgrade.has(PureMashDataComponents.ITEM_DATA.get())) {
            tag = upgrade.get(PureMashDataComponents.ITEM_DATA.get());
        } else if (tab == 2 && upgrade.has(PureMashDataComponents.FLUID_DATA.get())) {
            tag = upgrade.get(PureMashDataComponents.FLUID_DATA.get());
        } else if (tab == 0 && upgrade.has(PureMashDataComponents.ENERGY_DATA.get())) {
            tag = upgrade.get(PureMashDataComponents.ENERGY_DATA.get());
        }

        List<CableFilter> list = new ArrayList<>();
        if (tag != null && tag.contains("Filters")) {
            ListTag listTag = tag.getListOrEmpty("Filters");
            for (int i = 0; i < listTag.size(); i++) {
                list.add(CableFilter.deserializeNBT(listTag.getCompoundOrEmpty(i)));
            }
        }
        return list;
    }

    public void setFilters(Direction side, int tabType, List<CableFilter> filters) {
        ItemStack upgradeStack = this.getUpgradeInventory().getStackInSlot(0);
        if (upgradeStack.isEmpty() || !isUpgradeValid(upgradeStack)) return;

        boolean isDefault = filters.isEmpty() &&
                this.getDistributionMode(side) == DistributionMode.ROUND_ROBIN &&
                this.getRedstoneMode(side) == CableRedstoneMode.IGNORED &&
                !this.getFilterMode(side);

        if (isDefault) {
            switch (tabType) {
                case 0 -> upgradeStack.remove(PureMashDataComponents.ENERGY_DATA.get());
                case 1 -> upgradeStack.remove(PureMashDataComponents.ITEM_DATA.get());
                case 2 -> upgradeStack.remove(PureMashDataComponents.FLUID_DATA.get());
            }
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putInt("DistributionMode", this.getDistributionMode(side).ordinal());
            tag.putInt("RedstoneMode", this.getRedstoneMode(side).ordinal());
            tag.putBoolean("FilterMode", this.getFilterMode(side));

            ListTag filterListTag = new ListTag();
            for (CableFilter filter : filters) {
                filterListTag.add(filter.serializeNBT());
            }
            tag.put("Filters", filterListTag);

            switch (tabType) {
                case 0 -> upgradeStack.set(PureMashDataComponents.ENERGY_DATA.get(), tag);
                case 1 -> upgradeStack.set(PureMashDataComponents.ITEM_DATA.get(), tag);
                case 2 -> upgradeStack.set(PureMashDataComponents.FLUID_DATA.get(), tag);
            }
        }

        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void saveConfigurationToUpgrade(Direction side, int tabType) {
        setFilters(side, tabType, getFilters(side));
    }

    public void loadConfigurationFromUpgrade(Direction side, ItemStack upgradeStack) {
        if (upgradeStack.isEmpty() || !isUpgradeValid(upgradeStack)) return;

        if (upgradeStack.has(PureMashDataComponents.ITEM_DATA.get())) {
            CompoundTag itemTag = upgradeStack.get(PureMashDataComponents.ITEM_DATA.get());
            if (itemTag != null) applyConfigTagToSide(side, itemTag);
        }

        if (upgradeStack.has(PureMashDataComponents.FLUID_DATA.get())) {
            CompoundTag fluidTag = upgradeStack.get(PureMashDataComponents.FLUID_DATA.get());
            if (fluidTag != null) applyConfigTagToSide(side, fluidTag);
        }

        if (upgradeStack.has(PureMashDataComponents.ENERGY_DATA.get())) {
            CompoundTag energyTag = upgradeStack.get(PureMashDataComponents.ENERGY_DATA.get());
            if (energyTag != null) applyConfigTagToSide(side, energyTag);
        }
    }

    private void applyConfigTagToSide(Direction side, CompoundTag tag) {
        if (tag.contains("DistributionMode")) {
            int distIdx = tag.getIntOr("DistributionMode", 0);
            this.distributionModes[side.get3DDataValue()] = DistributionMode.values()[Math.clamp(distIdx, 0, 2)];
        }
        if (tag.contains("RedstoneMode")) {
            int redIdx = tag.getIntOr("RedstoneMode", 0);
            this.redstoneModes[side.get3DDataValue()] = CableRedstoneMode.values()[Math.clamp(redIdx, 0, 2)];
        }
        if (tag.contains("FilterMode")) {
            this.filterModes[side.get3DDataValue()] = tag.getBooleanOr("FilterMode", false);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, UniversalCableBlockEntity be) {
        if (level.isClientSide()) return;

        for (Direction side : Direction.values()) {
            if (!be.isExtracting(side)) continue;

            if (!be.getRedstoneMode(side).isRedstoneRequirementMet(level, pos)) {
                continue;
            }

            BlockPos sourcePos = pos.relative(side);
            Direction opposite = side.getOpposite();

            int multiplier = be.getTransferRateMultiplier();
            int baseEnergy = (be.tier == 2) ?
                    PureMashTweaksConfig.COMMON.moldelonianCableTransferRate.get() :
                    PureMashTweaksConfig.COMMON.synthoriumCableTransferRate.get();

            int maxItems = (be.tier == 2 ? PureMashTweaksConfig.COMMON.moldelonianCableItemRate.get() : PureMashTweaksConfig.COMMON.synthoriumCableItemRate.get()) * multiplier;
            int maxFluid = (be.tier == 2 ? PureMashTweaksConfig.COMMON.moldelonianCableFluidRate.get() : PureMashTweaksConfig.COMMON.synthoriumCableFluidRate.get()) * multiplier;
            int maxEnergy = baseEnergy * multiplier;

            List<BlockPos> destinations = findDestinationNodes(level, pos, side);
            if (destinations.isEmpty()) continue;

            int activeMode = be.getSelectedTab(side);
            List<CableFilter> activeFilters = be.getFilters(side, activeMode);

            // Sort destinations by bound filter priority
            destinations.sort((p1, p2) -> {
                int prio1 = getPriorityForPos(p1, activeFilters);
                int prio2 = getPriorityForPos(p2, activeFilters);
                return Integer.compare(prio2, prio1);
            });

            DistributionMode distMode = be.getDistributionMode(side);

            if (distMode == DistributionMode.RANDOM) {
                Collections.shuffle(destinations);
            } else if (distMode == DistributionMode.ROUND_ROBIN) {
                int startIdx = be.roundRobinIndex[side.get3DDataValue()] % destinations.size();
                List<BlockPos> rotated = new ArrayList<>(destinations.subList(startIdx, destinations.size()));
                rotated.addAll(destinations.subList(0, startIdx));
                destinations = rotated;
            }

            // STRICT CHANNEL ISOLATION BASED ON THE ACTIVE EXTRACTING TAB
            if (activeMode == 1) { // 1 = ITEM CHANNEL ONLY
                ResourceHandler<ItemResource> itemSource = level.getCapability(Capabilities.Item.BLOCK, sourcePos, opposite);
                if (itemSource != null && !ResourceHandlerUtil.isEmpty(itemSource)) {
                    transferItems(level, itemSource, destinations, maxItems, distMode, be, side, activeFilters);
                }
            } else if (activeMode == 2) { // 2 = FLUID CHANNEL ONLY
                ResourceHandler<FluidResource> fluidSource = level.getCapability(Capabilities.Fluid.BLOCK, sourcePos, opposite);
                if (fluidSource != null) {
                    transferFluids(level, fluidSource, destinations, maxFluid, distMode, be, side, activeFilters);
                }
            } else if (activeMode == 0) { // 0 = ENERGY CHANNEL ONLY
                EnergyHandler energySource = level.getCapability(Capabilities.Energy.BLOCK, sourcePos, opposite);
                if (energySource != null && energySource.getAmountAsLong() > 0) {
                    transferEnergy(level, energySource, destinations, maxEnergy, distMode, be, side);
                }
            }
        }
    }

    private static int getPriorityForPos(BlockPos pos, List<CableFilter> filters) {
        int max = 0;
        for (CableFilter filter : filters) {
            CompoundTag dest = filter.getDestinationTag();
            if (dest != null && dest.getIntOr("X", 0) == pos.getX() &&
                    dest.getIntOr("Y", 0) == pos.getY() &&
                    dest.getIntOr("Z", 0) == pos.getZ()) {
                max = Math.max(max, filter.getPriority());
            }
        }
        return max;
    }

    private static void transferItems(Level level, ResourceHandler<ItemResource> source, List<BlockPos> destinations, int maxAmount, DistributionMode distMode, UniversalCableBlockEntity be, Direction side, List<CableFilter> filters) {
        int remaining = maxAmount;
        int targetCount = destinations.size();
        int perTargetLimit = (distMode == DistributionMode.DYNAMICALLY && targetCount > 0) ? Math.max(1, maxAmount / targetCount) : maxAmount;
        String currentDim = level.dimension().identifier().toString();

        for (BlockPos destPos : destinations) {
            if (remaining <= 0) break;
            int remainingForDest = Math.min(remaining, perTargetLimit);

            for (Direction destSide : Direction.values()) {
                if (remainingForDest <= 0) break;

                ResourceHandler<ItemResource> targetHandler = level.getCapability(Capabilities.Item.BLOCK, destPos, destSide);
                if (targetHandler == null || ResourceHandlerUtil.isFull(targetHandler)) continue;

                for (int srcSlot = 0; srcSlot < source.size(); srcSlot++) {
                    if (remainingForDest <= 0) break;

                    ItemResource res = source.getResource(srcSlot);
                    if (res.isEmpty()) continue;

                    ItemStack stack = res.toStack();
                    CableFilter matchingFilter = null;

                    if (!filters.isEmpty()) {
                        for (CableFilter filter : filters) {
                            CompoundTag dest = filter.getDestinationTag();
                            if (dest != null) {
                                String filterDim = dest.getStringOr("Dimension", "");
                                if (!filterDim.isEmpty() && !filterDim.equals(currentDim)) {
                                    continue; // Skips filter if bound to another dimension
                                }
                                if (dest.getIntOr("X", 0) != destPos.getX() ||
                                        dest.getIntOr("Y", 0) != destPos.getY() ||
                                        dest.getIntOr("Z", 0) != destPos.getZ()) {
                                    continue;
                                }
                            }

                            if (filter.matchesItem(stack)) {
                                matchingFilter = filter;
                                break;
                            }
                        }

                        if (matchingFilter == null && !be.getFilterMode(side)) {
                            continue; // Whitelist rejected
                        }
                        if (matchingFilter != null && be.getFilterMode(side)) {
                            continue; // Blacklist blocked
                        }
                    }

                    List<Integer> targetSlots = matchingFilter != null ? matchingFilter.getTargetSlotList() : List.of();
                    List<Integer> slotsToTry = new ArrayList<>();

                    if (targetSlots.isEmpty()) {
                        for (int s = 0; s < targetHandler.size(); s++) slotsToTry.add(s);
                    } else if (targetSlots.size() == 1) {
                        int start = targetSlots.getFirst();
                        for (int s = start; s < targetHandler.size(); s++) {
                            slotsToTry.add(s);
                        }
                    } else {
                        for (int s : targetSlots) {
                            if (s >= 0 && s < targetHandler.size()) {
                                slotsToTry.add(s);
                            }
                        }
                    }

                    for (int targetSlot : slotsToTry) {
                        if (remainingForDest <= 0) break;

                        long currentInSlot = targetHandler.getAmountAsLong(targetSlot);
                        long slotCapacity = targetHandler.getCapacityAsLong(targetSlot, res);
                        long effectiveLimit = slotCapacity;

                        if (matchingFilter != null && matchingFilter.getStockLimit() > 0) {
                            effectiveLimit = Math.min(slotCapacity, matchingFilter.getStockLimit());
                        }

                        if (currentInSlot >= effectiveLimit) {
                            continue;
                        }

                        long canInsertInSlot = effectiveLimit - currentInSlot;
                        int toTransfer = (int) Math.min(source.getAmountAsLong(srcSlot), Math.min(remainingForDest, canInsertInSlot));
                        if (toTransfer <= 0) continue;

                        try (Transaction tx = Transaction.openRoot()) {
                            int inserted = targetHandler.insert(targetSlot, res, toTransfer, tx);
                            if (inserted > 0) {
                                int extracted = source.extract(srcSlot, res, inserted, tx);
                                if (extracted == inserted) {
                                    tx.commit();
                                    remaining -= inserted;
                                    remainingForDest -= inserted;
                                    be.roundRobinIndex[side.get3DDataValue()]++;
                                    be.setChanged();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            // Dropa todos os Distribution Filters que estavam guardados nos filtros das 6 faces
            for (Direction dir : Direction.values()) {
                for (int tab = 0; tab < 3; tab++) {
                    for (CableFilter filter : this.getFilters(dir, tab)) {
                        if (!filter.getDestinationTool().isEmpty()) {
                            net.minecraft.world.Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), filter.getDestinationTool());
                        }
                    }
                }
            }
        }
    }

    private static void transferFluids(Level level, ResourceHandler<FluidResource> source, List<BlockPos> destinations, int maxAmount, DistributionMode distMode, UniversalCableBlockEntity be, Direction side, List<CableFilter> filters) {
        int remaining = maxAmount;
        int targetCount = destinations.size();
        int perTargetLimit = (distMode == DistributionMode.DYNAMICALLY && targetCount > 0) ? Math.max(1, maxAmount / targetCount) : maxAmount;
        String currentDim = level.dimension().identifier().toString();

        for (BlockPos destPos : destinations) {
            if (remaining <= 0) break;
            int transferForDest = Math.min(remaining, perTargetLimit);

            for (Direction destSide : Direction.values()) {

                ResourceHandler<FluidResource> targetHandler = level.getCapability(Capabilities.Fluid.BLOCK, destPos, destSide);
                if (targetHandler != null) {
                    int moved = ResourceHandlerUtil.move(source, targetHandler, res -> {
                        if (!filters.isEmpty()) {
                            for (CableFilter filter : filters) {
                                CompoundTag dest = filter.getDestinationTag();
                                if (dest != null) {
                                    String filterDim = dest.getStringOr("Dimension", "");
                                    if (!filterDim.isEmpty() && !filterDim.equals(currentDim)) {
                                        continue;
                                    }
                                    if (dest.getIntOr("X", 0) != destPos.getX() ||
                                            dest.getIntOr("Y", 0) != destPos.getY() ||
                                            dest.getIntOr("Z", 0) != destPos.getZ()) {
                                        continue;
                                    }
                                }

                                if (filter.matchesFluid(new net.neoforged.neoforge.fluids.FluidStack(res.getFluid(), 1000))) {
                                    return !be.getFilterMode(side);
                                }
                            }
                            return be.getFilterMode(side);
                        }
                        return true;
                    }, transferForDest, null);

                    if (moved > 0) {
                        remaining -= moved;
                        transferForDest -= moved;
                        be.roundRobinIndex[side.get3DDataValue()]++;
                        be.setChanged();
                        break;
                    }
                }
            }
        }
    }

    private static void transferEnergy(Level level, EnergyHandler source, List<BlockPos> destinations, int maxAmount, DistributionMode distMode, UniversalCableBlockEntity be, Direction side) {
        int remaining = maxAmount;
        int targetCount = destinations.size();
        int perTargetLimit = (distMode == DistributionMode.DYNAMICALLY && targetCount > 0) ? Math.max(1, maxAmount / targetCount) : maxAmount;

        for (BlockPos destPos : destinations) {
            if (remaining <= 0) break;
            int transferForDest = Math.min(remaining, perTargetLimit);

            for (Direction destSide : Direction.values()) {

                EnergyHandler targetHandler = level.getCapability(Capabilities.Energy.BLOCK, destPos, destSide);
                if (targetHandler != null && !EnergyHandlerUtil.isFull(targetHandler)) {
                    int moved = EnergyHandlerUtil.move(source, targetHandler, transferForDest, null);
                    if (moved > 0) {
                        remaining -= moved;
                        transferForDest -= moved;
                        be.roundRobinIndex[side.get3DDataValue()]++;
                        be.setChanged();
                        break;
                    }
                }
            }
        }
    }

    private static List<BlockPos> findDestinationNodes(Level level, BlockPos startPos, Direction extractSide) {
        List<BlockPos> destinations = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            BlockState currentState = level.getBlockState(currentPos);

            for (Direction dir : Direction.values()) {
                if (currentPos.equals(startPos) && dir == extractSide) continue;

                if (currentState.getBlock() instanceof CableBlock cableBlock) {
                    if (!cableBlock.isConnected(level, currentPos, dir)) continue;
                }

                BlockPos neighborPos = currentPos.relative(dir);
                if (!visited.add(neighborPos) || !level.isLoaded(neighborPos)) continue;

                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof CableBlock) {
                    queue.add(neighborPos);
                } else {
                    destinations.add(neighborPos);
                }
            }
        }
        return destinations;
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < 6; i++) {
            output.putInt("SelectedTab_" + i, this.selectedTabs[i]);
            output.putInt("DistributionMode_" + i, this.distributionModes[i].ordinal());
            output.putInt("RedstoneMode_" + i, this.redstoneModes[i].ordinal());
            output.putBoolean("FilterMode_" + i, this.filterModes[i]);
        }
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < 6; i++) {
            this.selectedTabs[i] = input.getIntOr("SelectedTab_" + i, 1);
            this.distributionModes[i] = DistributionMode.values()[Math.clamp(input.getIntOr("DistributionMode_" + i, 0), 0, 2)];
            this.redstoneModes[i] = CableRedstoneMode.values()[Math.clamp(input.getIntOr("RedstoneMode_" + i, 0), 0, 2)];
            this.filterModes[i] = input.getBooleanOr("FilterMode_" + i, false);
        }
    }
}