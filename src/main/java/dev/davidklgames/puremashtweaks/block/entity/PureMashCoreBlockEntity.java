package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class PureMashCoreBlockEntity extends BlockEntity {

    private boolean active = true;
    private boolean showArea = false;
    private int overloadLevel = 0;

    public PureMashCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PUREMASH_CORE_BE.get(), pos, state);
    }

    public boolean isActive() { return this.active; }
    public void setActive(boolean active) {
        this.active = active;
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isShowArea() { return this.showArea; }
    public void setShowArea(boolean showArea) {
        this.showArea = showArea;
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getOverloadLevel() { return this.overloadLevel; }
    public void setOverloadLevel(int level) {
        this.overloadLevel = level;
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @SuppressWarnings("unchecked")
    public static void tick(Level level, BlockPos pos, BlockState state, PureMashCoreBlockEntity blockEntity) {
        if (level.isClientSide() || state == null) return;

        if (!blockEntity.active || blockEntity.overloadLevel <= 0) return;

        int lvl = blockEntity.overloadLevel;

        // Escala da área: 3x3x3 (Lvl 1), 5x5x5 (Lvl 2), 7x7x7 (Lvl 3)
        int radius = switch (lvl) {
            case 3 -> 3; // 7x7x7
            case 2 -> 2; // 5x5x5
            default -> 1; // 3x3x3 (Nível 1)
        };

        int multiplier = switch (lvl) {
            case 3 -> 4 + (PureMashTweaksConfig.COMMON.overloadSpeedMultiplier.get() * 2);
            case 2 -> 2 + PureMashTweaksConfig.COMMON.overloadSpeedLvl1_2.get();
            default -> PureMashTweaksConfig.COMMON.overloadSpeedLvl1_2.get();
        };

        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        ServerLevel serverLevel = (ServerLevel) level;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    targetPos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (!level.isLoaded(targetPos)) continue;

                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.isAir()) continue;

                    BlockPos immutableTargetPos = targetPos.immutable();

                    // 1. Aceleração de Random Tick (Plantações, Saplings, etc.)
                    if (targetState.isRandomlyTicking()) {
                        for (int i = 0; i < multiplier; i++) {
                            targetState.randomTick(serverLevel, immutableTargetPos, level.getRandom());
                        }
                    }

                    // 2. Aceleração de Máquinas e Block Entities adjacentes
                    BlockEntity targetBe = level.getBlockEntity(immutableTargetPos);
                    if (targetBe != null && !(targetBe instanceof PureMashCoreBlockEntity)) {
                        Block targetBlock = targetState.getBlock();
                        if (targetBlock instanceof EntityBlock entityBlock) {
                            BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) entityBlock.getTicker(level, targetState, targetBe.getType());
                            if (ticker != null) {
                                for (int i = 0; i < multiplier; i++) {
                                    if (targetBe.isRemoved()) break;
                                    try {
                                        ticker.tick(level, immutableTargetPos, targetState, targetBe);
                                    } catch (Exception e) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Active", this.active);
        output.putBoolean("ShowArea", this.showArea);
        output.putInt("OverloadLevel", this.overloadLevel);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.active = input.getBooleanOr("Active", true);
        this.showArea = input.getBooleanOr("ShowArea", false);
        this.overloadLevel = input.getIntOr("OverloadLevel", 0);
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