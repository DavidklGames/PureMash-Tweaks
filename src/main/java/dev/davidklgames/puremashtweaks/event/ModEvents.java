package dev.davidklgames.puremashtweaks.event;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = PureMashTweaks.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;

        boolean disabledByPlayer = player.getPersistentData().getBooleanOr("OverloadFlightDisabled", false);

        if (disabledByPlayer) {
            disableFlight(player);
        } else {
            int level = getOverloadFlightLevel(player);

            if (level >= 3) {
                enableFlight(player);
                if (player.getAbilities().flying) {
                    spawnFlightParticles(player);
                }
            } else if (level > 0) {
                handleLimitedFlight(player, level);
            } else {
                disableFlight(player);
            }
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            int currentTicks = player.getPersistentData().getIntOr("OverloadFlightTicks", 0);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new dev.davidklgames.puremashtweaks.network.SyncFlightPayload(currentTicks, disabledByPlayer)
            );
        }
    }

    private static void spawnFlightParticles(Player player) {
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            double px = player.getX();
            double py = player.getY() + 0.05;
            double pz = player.getZ();

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.GLOW,
                    px, py, pz,
                    2,
                    0.15, 0.02, 0.15,
                    0.02
            );
        }
    }

    private static int getOverloadFlightLevel(Player player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        boolean wearsFullSynthorium =
                !helmet.isEmpty() && helmet.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_HELMET.get()) &&
                        !chestplate.isEmpty() && chestplate.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_CHESTPLATE.get()) &&
                        !leggings.isEmpty() && leggings.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_LEGGINGS.get()) &&
                        !boots.isEmpty() && boots.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_BOOTS.get());

        if (!wearsFullSynthorium) {
            return 0;
        }

        var reg = player.level().registryAccess().lookup(Registries.ENCHANTMENT);
        if (reg.isPresent()) {
            var overloadOpt = reg.get().get(ModEnchantments.OVERLOAD);
            if (overloadOpt.isPresent()) {
                var overload = overloadOpt.get();
                int hLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, helmet);
                int cLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, chestplate);
                int lLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, leggings);
                int bLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, boots);

                if (hLvl <= 0 || cLvl <= 0 || lLvl <= 0 || bLvl <= 0) {
                    return 0;
                }

                return Math.min(Math.min(hLvl, cLvl), Math.min(lLvl, bLvl));
            }
        }
        return 0;
    }

    private static void handleLimitedFlight(Player player, int level) {
        String TAG_KEY = "OverloadFlightTicks";
        int maxDuration = (level == 1) ?
                PureMashTweaksConfig.OVERLOAD_FLIGHT_TICKS_LVL1.get() :
                PureMashTweaksConfig.OVERLOAD_FLIGHT_TICKS_LVL2.get();

        int currentTicks = player.getPersistentData().getIntOr(TAG_KEY, 0);

        if (player.onGround()) {
            int rechargeRate = Math.max(1, maxDuration / 1200);
            currentTicks = Math.min(maxDuration, currentTicks + rechargeRate);
            player.getPersistentData().putInt(TAG_KEY, currentTicks);

        } else if (player.getAbilities().flying) {
            currentTicks = Math.max(0, currentTicks - 1);
            player.getPersistentData().putInt(TAG_KEY, currentTicks);
            spawnFlightParticles(player);
        }

        if (currentTicks > 0) {
            enableFlight(player);
        } else {
            disableFlight(player);
        }
    }

    private static void enableFlight(Player player) {
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    private static void disableFlight(Player player) {
        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (event.isCanceled()) return;

        net.minecraft.world.level.Level level = event.getLevel();
        if (level.isClientSide()) return;

        net.minecraft.world.entity.player.Player player = event.getEntity();
        if (player.isSpectator() || !player.mayBuild()) return;

        net.minecraft.core.BlockPos pos = event.getPos();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);

        if (state.is(net.minecraft.world.level.block.Blocks.BEDROCK)) {
            net.minecraft.world.item.ItemStack tool = player.getMainHandItem();

            boolean isSynthoriumTool = tool.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_PICKAXE.get() ||
                    tool.getItem() == dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_PAXEL.get();

            if (isSynthoriumTool) {
                boolean hasOverload = false;
                var reg = level.registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
                if (reg.isPresent()) {
                    var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                    if (overloadOpt.isPresent()) {
                        hasOverload = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(overloadOpt.get(), tool) > 0;
                    }
                }

                if (hasOverload) {
                    level.setBlock(pos, dev.davidklgames.puremashtweaks.registry.ModBlocks.FAKE_BEDROCK.get().defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * Safely reads the physical Overclock level directly from the stack's data components.
     */
    public static int getOverclockLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;

        ItemEnchantments active = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!active.isEmpty()) {
            for (var entry : active.entrySet()) {
                if (entry.getKey().is(ModEnchantments.OVERCLOCK)) {
                    return entry.getIntValue();
                }
            }
        }

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            for (var entry : stored.entrySet()) {
                if (entry.getKey().is(ModEnchantments.OVERCLOCK)) {
                    return entry.getIntValue();
                }
            }
        }

        return 0;
    }

    /**
     * Physical Anvil Combination Handler:
     * Instantly elevates all existing enchantments on the item when Overclock is present,
     * and expands maximum limits physically on the output stack.
     */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.isEmpty() || right.isEmpty()) return;

        int leftOverclock = getOverclockLevel(left);
        int rightOverclock = getOverclockLevel(right);
        int effectiveOverclock = Math.max(leftOverclock, rightOverclock);

        // If neither item nor book contains Overclock, let vanilla handling take over.
        if (effectiveOverclock <= 0) return;

        boolean leftIsBook = left.is(Items.ENCHANTED_BOOK);
        boolean rightIsBook = right.is(Items.ENCHANTED_BOOK);

        ItemEnchantments leftEnchants = leftIsBook ?
                left.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
                left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        ItemEnchantments rightEnchants = rightIsBook ?
                right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
                right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        Map<Holder<Enchantment>, Integer> resultMap = new HashMap<>();
        for (var entry : leftEnchants.entrySet()) {
            resultMap.put(entry.getKey(), entry.getIntValue());
        }

        boolean changed = false;
        int totalCost = 0;

        // 1. Merge enchantments from the right input onto the result map
        for (var entry : rightEnchants.entrySet()) {
            Holder<Enchantment> ench = entry.getKey();
            int rightLevel = entry.getIntValue();
            int leftLevel = resultMap.getOrDefault(ench, 0);

            int newLevel;
            if (leftLevel > 0) {
                newLevel = (leftLevel == rightLevel) ? leftLevel + 1 : Math.max(leftLevel, rightLevel);
            } else {
                newLevel = rightLevel;
            }

            int baseMax = ench.value().getMaxLevel();
            int allowedMax = baseMax + effectiveOverclock;

            if (newLevel > allowedMax) {
                newLevel = allowedMax;
            }

            if (newLevel != leftLevel) {
                resultMap.put(ench, newLevel);
                changed = true;
                totalCost += newLevel;
            }
        }

        // 2. Instantly boost ALL active enchantments on the item by the effective Overclock bonus
        for (Map.Entry<Holder<Enchantment>, Integer> entry : new HashMap<>(resultMap).entrySet()) {
            Holder<Enchantment> ench = entry.getKey();
            if (ench.is(ModEnchantments.OVERCLOCK)) continue;

            int currentLevel = entry.getValue();
            int baseMax = ench.value().getMaxLevel();
            int allowedMax = baseMax + effectiveOverclock;

            int boostedLevel = Math.min(allowedMax, currentLevel + effectiveOverclock);
            if (boostedLevel > currentLevel) {
                resultMap.put(ench, boostedLevel);
                changed = true;
                totalCost += (boostedLevel - currentLevel);
            }
        }

        if (changed) {
            ItemStack output = left.copy();
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            resultMap.forEach(mutable::set);

            if (output.is(Items.ENCHANTED_BOOK)) {
                output.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            } else {
                output.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
            }

            event.setOutput(output);
            event.setXpCost(Math.max(1, totalCost));
            event.setMaterialCost(1);
        }
    }
}