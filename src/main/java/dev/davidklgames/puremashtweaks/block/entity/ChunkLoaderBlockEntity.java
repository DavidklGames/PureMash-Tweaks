package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.menu.ChunkLoaderMenu;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("removal")
public class ChunkLoaderBlockEntity extends BlockEntity implements MenuProvider {

    private int activeLevel = 0;
    private boolean isShowingBoundary = false;

    private final List<Long> forcedChunks = new ArrayList<>();

    @SuppressWarnings("removal")
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                if (activeLevel >= 3 && !hasCoreInstalled()) {
                    activeLevel = 2;
                    level.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(worldPosition).inflate(16))
                            .forEach(player -> player.sendSystemMessage(Component.translatable("chat.puremashtweaks.chunk_loader.core_removed")));
                }
                updateForcedChunks();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && stack.is(ModItems.MOLDELONIAN_CORE.get());
        }
    };

    public ChunkLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHUNK_LOADER_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide()) {
            this.updateForcedChunks();
        }
    }

    /**
     * Energy consumption per tick scaling with chunk loading radius.
     */
    public static int getEnergyConsumption(int levelIndex) {
        return switch (levelIndex) {
            case 3 -> 500;   // Level 4 (9x9 Chunks) = 500 FE/t
            case 4 -> 2500;  // Level 5 (15x15 Chunks) = 2,500 FE/t
            case 5 -> 5000;  // Level 6 (17x17 Chunks) = 5,000 FE/t
            default -> 0;    // Levels 1-3 (1x1 to 5x5) = Free
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChunkLoaderBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.activeLevel >= 3) {
            ItemStack coreStack = be.inventory.getStackInSlot(0);

            if (!coreStack.isEmpty() && coreStack.is(ModItems.MOLDELONIAN_CORE.get())) {
                int energyCost = getEnergyConsumption(be.activeLevel);

                if (energyCost > 0) {
                    var itemAccess = net.neoforged.neoforge.transfer.access.ItemAccess.forStack(coreStack);
                    var energyHandler = coreStack.getCapability(Capabilities.Energy.ITEM, itemAccess);

                    boolean energyDrained = false;

                    if (energyHandler != null && energyHandler.getAmountAsLong() >= energyCost) {
                        try (Transaction tx = Transaction.openRoot()) {
                            int extracted = energyHandler.extract(energyCost, tx);
                            if (extracted == energyCost) {
                                tx.commit();
                                energyDrained = true;
                                be.setChanged();
                            }
                        }
                    }

                    // If Core runs out of FE, automatically downgrade to Level 3 (5x5 chunks)
                    if (!energyDrained) {
                        be.activeLevel = 2;
                        be.updateForcedChunks();
                        be.setChanged();
                        level.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(be.worldPosition).inflate(16))
                                .forEach(player -> player.sendSystemMessage(Component.literal("§c[PureMash Chunk Loader]: Moldelonian Core is out of energy! Range downgraded to Level 3 (5x5).")));
                    }
                }
            } else {
                be.activeLevel = 2;
                be.updateForcedChunks();
                be.setChanged();
            }
        }
    }

    public void updateForcedChunks() {
        if (this.level == null || this.level.isClientSide() || !(this.level instanceof ServerLevel serverLevel)) return;

        for (long packedChunk : this.forcedChunks) {
            ChunkPos cp = ChunkPos.unpack(packedChunk);
            serverLevel.setChunkForced(cp.x(), cp.z(), false);
        }
        this.forcedChunks.clear();

        ChunkPos center = ChunkPos.containing(this.worldPosition);
        int radius = getRadiusByLevel(this.activeLevel);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int cx = center.x() + x;
                int cz = center.z() + z;

                serverLevel.setChunkForced(cx, cz, true);
                this.forcedChunks.add(ChunkPos.pack(cx, cz));
            }
        }

        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        setChanged();
    }

    public static int getRadiusByLevel(int levelIndex) {
        return switch (levelIndex) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            case 4 -> 7;
            case 5 -> 8;
            default -> 0;
        };
    }

    public int getActiveLevel() { return this.activeLevel; }
    public void setActiveLevel(int level) {
        this.activeLevel = level;
        this.updateForcedChunks();
        setChanged();
    }

    public boolean isShowingBoundary() { return this.isShowingBoundary; }
    public void setShowingBoundary(boolean show) {
        this.isShowingBoundary = show;
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean hasCoreInstalled() {
        return !this.inventory.getStackInSlot(0).isEmpty();
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        this.inventory.serialize(output);
        output.putInt("ActiveLevel", this.activeLevel);
        output.putBoolean("ShowingBoundary", this.isShowingBoundary);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.inventory.deserialize(input);
        this.activeLevel = input.getIntOr("ActiveLevel", 0);
        this.isShowingBoundary = input.getBooleanOr("ShowingBoundary", false);
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide() && this.level instanceof ServerLevel serverLevel) {
            for (long packedChunk : this.forcedChunks) {
                ChunkPos cp = ChunkPos.unpack(packedChunk);
                serverLevel.setChunkForced(cp.x(), cp.z(), false);
            }
            this.forcedChunks.clear();
            net.minecraft.world.Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getStackInSlot(0));
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

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.puremashtweaks.chunk_loader");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NonNull Inventory playerInv, @NonNull Player player) {
        return new ChunkLoaderMenu(id, playerInv, this);
    }
}