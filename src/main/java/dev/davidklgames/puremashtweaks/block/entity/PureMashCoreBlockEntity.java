package dev.davidklgames.puremashtweaks.block.entity;

import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
    public void setActive(boolean active) { this.active = active; setChanged(); }

    public boolean isShowArea() { return this.showArea; }
    public void setShowArea(boolean showArea) { this.showArea = showArea; setChanged(); }

    public int getOverloadLevel() { return this.overloadLevel; }
    public void setOverloadLevel(int level) { this.overloadLevel = level; setChanged(); }

    @SuppressWarnings("unchecked")
    public static void tick(Level level, BlockPos pos, BlockState state, PureMashCoreBlockEntity blockEntity) {
        if (level.isClientSide() || state == null) return;

        if (!blockEntity.active || blockEntity.overloadLevel <= 0) return;

        int lvl = blockEntity.overloadLevel;
        int radius;
        int multiplier;

        if (lvl >= 3) {
            radius = 2;
            multiplier = 4 + (PureMashTweaksConfig.OVERLOAD_SPEED_MULTIPLIER.get() * 2);
        } else {
            radius = 1;
            multiplier = PureMashTweaksConfig.OVERLOAD_SPEED_LVL1_2.get();
        }

        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        ServerLevel serverLevel = (ServerLevel) level;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    targetPos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);

                    if (x == 0 && y == 0 && z == 0) continue;

                    if (!level.isLoaded(targetPos)) continue;

                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.isRandomlyTicking()) {
                        for (int i = 0; i < multiplier; i++) {
                            targetState.randomTick(serverLevel, targetPos.immutable(), level.getRandom());
                        }
                    }

                    BlockEntity targetBe = level.getBlockEntity(targetPos);
                    if (targetBe != null && !(targetBe instanceof PureMashCoreBlockEntity)) {
                        Block targetBlock = targetState.getBlock();
                        if (targetBlock instanceof EntityBlock entityBlock) {
                            BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) entityBlock.getTicker(level, targetState, targetBe.getType());
                            if (ticker != null) {
                                for (int i = 0; i < multiplier; i++) {
                                    if (targetBe.isRemoved()) break;
                                    try {
                                        ticker.tick(level, targetPos.immutable(), targetState, targetBe);
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

        if (blockEntity.showArea && level.getGameTime() % 10 == 0) {
            spawnEnhancedAreaParticles(serverLevel, pos, radius, lvl);
        }
    }

    private static void spawnEnhancedAreaParticles(ServerLevel level, BlockPos pos, int radius, int lvl) {
        SimpleParticleType p = (lvl >= 3) ? ParticleTypes.GLOW : ParticleTypes.HAPPY_VILLAGER;

        for (int x : new int[]{-radius, radius}) {
            for (int y : new int[]{-radius, radius}) {
                for (int z : new int[]{-radius, radius}) {
                    double px = (double)pos.getX() + 0.5D + (double)x;
                    double py = (double)pos.getY() + 0.5D + (double)y;
                    double pz = (double)pos.getZ() + 0.5D + (double)z;

                    level.sendParticles(p, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
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
}