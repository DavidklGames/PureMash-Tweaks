package dev.davidklgames.puremashtweaks.block;

import com.mojang.serialization.MapCodec;
import dev.davidklgames.puremashtweaks.block.entity.PureMashBatteryBlockEntity;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PureMashBatteryBlock extends BaseEntityBlock {
    public static final MapCodec<PureMashBatteryBlock> CODEC = simpleCodec(PureMashBatteryBlock::new);

    public PureMashBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new PureMashBatteryBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PureMashBatteryBlockEntity batteryBe) {
            long stored = batteryBe.energyTank.getAmountAsLong();
            long max = batteryBe.energyTank.getCapacityAsLong();

            player.sendSystemMessage(Component.literal("§b[PureMash Battery]: §f" + String.format("%,d", stored) + " / " + String.format("%,d", max) + " FE"));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PureMashBatteryBlockEntity batteryBe) {
                batteryBe.loadCustomOnly(TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        level.registryAccess(),
                        customData.copyTag()
                ));
                if (!level.isClientSide()) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }

    @Override
    protected @NonNull ItemStack getCloneItemStack(@NonNull LevelReader level, @NonNull BlockPos pos, @NonNull BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PureMashBatteryBlockEntity batteryBe) {
            CompoundTag tag = batteryBe.saveCustomOnly(level.registryAccess());
            if (!tag.isEmpty()) {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
        return stack;
    }

    @Override
    protected @NonNull List<ItemStack> getDrops(@NonNull BlockState state, LootParams.@NonNull Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof PureMashBatteryBlockEntity batteryBe) {
            CompoundTag tag = batteryBe.saveCustomOnly(builder.getLevel().registryAccess());
            if (!tag.isEmpty()) {
                for (ItemStack drop : drops) {
                    if (drop.is(this.asItem())) {
                        drop.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    }
                }
            }
        }
        return drops;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.PUREMASH_BATTERY_BE.get(), PureMashBatteryBlockEntity::tick);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            @NonNull BlockState state,
            net.minecraft.server.level.@NonNull ServerLevel level,
            @NonNull BlockPos pos,
            boolean movedByPiston
    ) {
        net.minecraft.world.Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
}