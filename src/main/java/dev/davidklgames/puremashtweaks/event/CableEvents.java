package dev.davidklgames.puremashtweaks.event;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.CableBlock;
import dev.davidklgames.puremashtweaks.item.ConfigurationWrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = PureMashTweaks.MODID)
public class CableEvents {

    @SubscribeEvent
    public static void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null || !player.isShiftKeyDown()) return;

        ItemStack stack = event.getItemStack();
        if (!ConfigurationWrenchItem.isWrench(stack)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof CableBlock cableBlock) {
            Vec3 hit = event.getHitVec().getLocation();
            double lx = hit.x - pos.getX();
            double ly = hit.y - pos.getY();
            double lz = hit.z - pos.getZ();

            Direction side = getTargetSide(event.getHitVec().getDirection(), lx, ly, lz);

            InteractionResult result = cableBlock.onWrenchClicked(state, level, pos, player, event.getHand(), event.getHitVec(), side);
            if (result.consumesAction()) {
                event.setUseItem(TriState.TRUE);
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        }
    }

    private static Direction getTargetSide(Direction hitFace, double lx, double ly, double lz) {
        // Precise arm hit testing
        if (lz <= 0.25D) return Direction.NORTH;
        if (lz >= 0.75D) return Direction.SOUTH;
        if (lx <= 0.25D) return Direction.WEST;
        if (lx >= 0.75D) return Direction.EAST;
        if (ly <= 0.25D) return Direction.DOWN;
        if (ly >= 0.75D) return Direction.UP;

        return hitFace;
    }
}