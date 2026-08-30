package dev.davidklgames.puremashtweaks.block;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class SuspiciousEndStoneBlock extends BrushableBlock {
    public static final ResourceKey<LootTable> LOOT_TABLE_KEY = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "archaeology/suspicious_end_stone")
    );

    public SuspiciousEndStoneBlock(Properties properties) {
        super(Blocks.END_STONE, SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, properties);
    }

    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        BrushableBlockEntity be = new BrushableBlockEntity(pos, state) {
            @Override
            public net.minecraft.world.level.block.entity.@NonNull BlockEntityType<?> getType() {
                return ModBlockEntities.SUSPICIOUS_END_STONE_BE.get();
            }
        };
        // Sets default archaeology loot table for natural generation
        be.setLootTable(LOOT_TABLE_KEY, 0L);
        return be;
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrushableBlockEntity brushableBe) {
                // If placed manually by a player, strip loot table and item (vanilla archaeology standard)
                CompoundTag tag = brushableBe.saveCustomOnly(level.registryAccess());
                tag.remove("LootTable");
                tag.remove("LootTableSeed");
                tag.remove("item");

                brushableBe.loadCustomOnly(net.minecraft.world.level.storage.TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        level.registryAccess(),
                        tag
                ));
            }
        }
    }
}