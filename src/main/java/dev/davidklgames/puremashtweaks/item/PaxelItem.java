package dev.davidklgames.puremashtweaks.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class PaxelItem extends Item {

    public PaxelItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        Optional<BlockState> updated = updateBlockState(level, pos, state, player, context);
        if (updated.isEmpty()) {
            return InteractionResult.PASS;
        } else {
            ItemStack stack = context.getItemInHand();
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }

            level.setBlock(pos, updated.get(), 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(player, updated.get()));
            if (player != null) {
                stack.hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
            }

            return InteractionResult.SUCCESS;
        }
    }

    private static Optional<BlockState> updateBlockState(Level level, BlockPos pos, BlockState state, @Nullable Player player, UseOnContext context) {
        // 1. Hoe Tilling (Grass/Dirt/Coarse Dirt -> Farmland/Dirt)
        Optional<BlockState> tilled = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.HOE_TILL, false));
        if (tilled.isPresent()) {
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return tilled;
        }

        // 2. Axe Stripping (Logs -> Stripped Logs)
        Optional<BlockState> stripped = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false));
        if (stripped.isPresent()) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return stripped;
        }

        // 3. Axe Copper Scraping (Oxidized Copper -> Weathered -> Exposed -> Block)
        Optional<BlockState> scraped = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false));
        if (scraped.isPresent()) {
            level.levelEvent(player, 3005, pos, 0);
            return scraped;
        }

        // 4. Axe Copper Wax-Off (Waxed Copper -> Unwaxed)
        Optional<BlockState> waxOff = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false));
        if (waxOff.isPresent()) {
            level.levelEvent(player, 3004, pos, 0);
            return waxOff;
        }

        // 5. Shovel Pathing (Grass Block -> Dirt Path)
        Optional<BlockState> flattened = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false));
        if (flattened.isPresent()) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            return flattened;
        }

        // 6. Shovel Extinguish Campfire (Campfire -> Unlit)
        Optional<BlockState> doused = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false));
        if (doused.isPresent()) {
            level.levelEvent(player, 1009, pos, 0);
            return doused;
        }

        return Optional.empty();
    }

    @Override
    public boolean canPerformAction(@NonNull ItemInstance stack, @NonNull ItemAbility ability) {
        return ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability) ||
                ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability) ||
                ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability);
    }
}