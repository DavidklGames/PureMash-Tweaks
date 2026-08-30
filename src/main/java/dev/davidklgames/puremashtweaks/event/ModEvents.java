package dev.davidklgames.puremashtweaks.event;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.PureMashDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = PureMashTweaks.MODID)
public class ModEvents {

    private static final Identifier OVERDRIVE_SPEED_ID = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overdrive_armor_speed");
    private static final Identifier OVERDRIVE_STEP_ID = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overdrive_armor_step");

    private static final ThreadLocal<Boolean> IS_AOE_MINING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> IS_AOE_ATTACKING = ThreadLocal.withInitial(() -> false);

    private static final Identifier OVERLOAD_REACH_BLOCK_ID = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload_reach_block");
    private static final Identifier OVERLOAD_REACH_ENTITY_ID = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload_reach_entity");

    /**
     * Context-aware tool filter: ensures Paxels and tools only break blocks
     * matching the category of the clicked target block (e.g. mining stone won't break dirt/wood).
     */
    private static boolean isMatchingBlockCategory(BlockState targetState, BlockState centerState) {
        if (centerState.is(BlockTags.LOGS) && (targetState.is(BlockTags.LOGS) || targetState.is(BlockTags.LEAVES))) {
            return true;
        }
        if (centerState.is(BlockTags.MINEABLE_WITH_PICKAXE) && targetState.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return true;
        }
        if (centerState.is(BlockTags.MINEABLE_WITH_SHOVEL) && targetState.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return true;
        }
        if (centerState.is(BlockTags.MINEABLE_WITH_AXE) && targetState.is(BlockTags.MINEABLE_WITH_AXE)) {
            return true;
        }
        if (centerState.is(BlockTags.MINEABLE_WITH_HOE) && targetState.is(BlockTags.MINEABLE_WITH_HOE)) {
            return true;
        }

        return targetState.getBlock() == centerState.getBlock();
    }

    /**
     * Treecapitator engine: fells connected tree logs and leaves upward without damaging the ground beneath.
     */
    private static void fellTree(
            net.minecraft.server.level.ServerLevel serverLevel,
            BlockPos startPos,
            Player player,
            ItemStack tool,
            int maxLogs,
            net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> boundHandler
    ) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> logsToBreak = new ArrayList<>();
        List<BlockPos> leavesToBreak = new ArrayList<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && logsToBreak.size() < maxLogs) {
            BlockPos current = queue.poll();
            BlockState state = serverLevel.getBlockState(current);

            if (state.is(BlockTags.LOGS)) {
                logsToBreak.add(current);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (visited.add(neighbor)) {
                                if (Math.abs(neighbor.getX() - startPos.getX()) <= 8 &&
                                        Math.abs(neighbor.getZ() - startPos.getZ()) <= 8 &&
                                        (neighbor.getY() - startPos.getY()) <= 35) {
                                    queue.add(neighbor);
                                }
                            }
                        }
                    }
                }
            } else if (state.is(BlockTags.LEAVES)) {
                leavesToBreak.add(current);
            }
        }

        List<BlockPos> allBlocks = new ArrayList<>(logsToBreak);
        allBlocks.addAll(leavesToBreak);

        boolean playedTeleportSound = false;
        int logsBrokenCount = 0;
        int uniqueTypesTeleported = 0;

        for (BlockPos pos : allBlocks) {
            BlockState state = serverLevel.getBlockState(pos);
            if (state.isAir()) continue;

            net.minecraft.world.level.block.entity.BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(state, serverLevel, pos, blockEntity, player, tool);

            serverLevel.destroyBlock(pos, false, player);
            if (state.is(BlockTags.LOGS)) {
                logsBrokenCount++;
            }

            for (ItemStack drop : drops) {
                if (drop.isEmpty()) continue;

                if (boundHandler != null && uniqueTypesTeleported < 64) {
                    var resource = ItemResource.of(drop);
                    int amountToTransfer = drop.getCount();

                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = boundHandler.insert(resource, amountToTransfer, tx);
                        if (inserted > 0) {
                            tx.commit();
                            drop.shrink(inserted);
                            uniqueTypesTeleported++;
                            serverLevel.sendParticles(
                                    net.minecraft.core.particles.ParticleTypes.PORTAL,
                                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                    3, 0.1, 0.1, 0.1, 0.0
                            );
                            if (!playedTeleportSound) {
                                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                        net.minecraft.sounds.SoundSource.PLAYERS, 0.2F, 1.0F);
                                playedTeleportSound = true;
                            }
                        }
                    }
                }

                if (!drop.isEmpty()) {
                    net.minecraft.world.level.block.Block.popResource(serverLevel, pos, drop);
                }
            }
        }

        if (logsBrokenCount > 0 && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            tool.hurtAndBreak(logsBrokenCount, serverLevel, serverPlayer, item -> {});
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(net.neoforged.neoforge.event.ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (isAllowedOverloadTool(stack)) {
            int overloadLvl = getOverloadLevel(stack);

            if (overloadLvl > 0) {
                double reachBonus = PureMashTweaksConfig.COMMON.overloadReachBonus.get() * overloadLvl;

                event.addModifier(
                        Attributes.BLOCK_INTERACTION_RANGE,
                        new AttributeModifier(OVERLOAD_REACH_BLOCK_ID, reachBonus, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                );

                event.addModifier(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(OVERLOAD_REACH_ENTITY_ID, reachBonus, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;

        handleArmorOverdriveAttributes(player);

        boolean flightDisabled = player.getPersistentData().getBooleanOr("OverloadFlightDisabled", false);

        if (flightDisabled) {
            disableFlight(player);
        } else {
            int level = getOverloadFlightLevel(player);

            if (level >= 3) {
                enableFlight(player);
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
                    new dev.davidklgames.puremashtweaks.network.SyncFlightPayload(currentTicks, flightDisabled)
            );

            boolean overdriveDisabled = player.getPersistentData().getBooleanOr("OverdriveDisabled", false);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new dev.davidklgames.puremashtweaks.network.SyncOverdrivePayload(overdriveDisabled)
            );
        }
    }

    private static void handleLimitedFlight(Player player, int level) {
        String TAG_KEY = "OverloadFlightTicks";
        int maxDuration = (level == 1) ?
                PureMashTweaksConfig.COMMON.overloadFlightTicksLvl1.get() :
                PureMashTweaksConfig.COMMON.overloadFlightTicksLvl2.get();

        int currentTicks = player.getPersistentData().getIntOr(TAG_KEY, 0);

        if (player.onGround()) {
            int rechargeRate = Math.max(1, maxDuration / 1200);
            currentTicks = Math.min(maxDuration, currentTicks + rechargeRate);
            player.getPersistentData().putInt(TAG_KEY, currentTicks);

        } else if (player.getAbilities().flying) {
            currentTicks = Math.max(0, currentTicks - 1);
            player.getPersistentData().putInt(TAG_KEY, currentTicks);
        }

        if (currentTicks > 0) {
            enableFlight(player);
        } else {
            disableFlight(player);
        }
    }

    private static int getArmorOverdriveLevel(Player player) {
        int maxLevel = 0;
        EquipmentSlot[] armorSlots = new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (EquipmentSlot slot : armorSlots) {
            ItemStack armorStack = player.getItemBySlot(slot);
            if (!armorStack.isEmpty()) {
                int level = getOverdriveLevel(armorStack, player.level().registryAccess());
                if (level > maxLevel) {
                    maxLevel = level;
                }
            }
        }
        return maxLevel;
    }

    private static boolean isFlightArmorPiece(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return false;
        return switch (slot) {
            case HEAD -> stack.is(ModItems.SYNTHORIUM_HELMET.get()) || stack.is(ModItems.MOLDELONIAN_HELMET.get());
            case CHEST -> stack.is(ModItems.SYNTHORIUM_CHESTPLATE.get()) || stack.is(ModItems.MOLDELONIAN_CHESTPLATE.get());
            case LEGS -> stack.is(ModItems.SYNTHORIUM_LEGGINGS.get()) || stack.is(ModItems.MOLDELONIAN_LEGGINGS.get());
            case FEET -> stack.is(ModItems.SYNTHORIUM_BOOTS.get()) || stack.is(ModItems.MOLDELONIAN_BOOTS.get());
            default -> false;
        };
    }

    private static int getOverloadFlightLevel(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        boolean wearsFullArmorSet =
                isFlightArmorPiece(helmet, EquipmentSlot.HEAD) &&
                        isFlightArmorPiece(chestplate, EquipmentSlot.CHEST) &&
                        isFlightArmorPiece(leggings, EquipmentSlot.LEGS) &&
                        isFlightArmorPiece(boots, EquipmentSlot.FEET);

        if (!wearsFullArmorSet) {
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
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.isCanceled()) return;

        net.minecraft.world.level.Level level = event.getLevel();
        if (level.isClientSide()) return;

        Player player = event.getEntity();
        if (player.isSpectator() || !player.mayBuild()) return;

        BlockPos pos = event.getPos();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);

        if (state.is(net.minecraft.world.level.block.Blocks.BEDROCK)) {
            ItemStack tool = player.getMainHandItem();

            boolean isOverloadTool = tool.is(ModItems.SYNTHORIUM_PICKAXE.get()) ||
                    tool.is(ModItems.SYNTHORIUM_PAXEL.get()) ||
                    tool.is(ModItems.MOLDELONIAN_PICKAXE.get()) ||
                    tool.is(ModItems.MOLDELONIAN_PAXEL.get());

            if (isOverloadTool) {
                boolean hasOverload = false;
                var reg = level.registryAccess().lookup(Registries.ENCHANTMENT);
                if (reg.isPresent()) {
                    var overloadOpt = reg.get().get(ModEnchantments.OVERLOAD);
                    if (overloadOpt.isPresent()) {
                        hasOverload = EnchantmentHelper.getItemEnchantmentLevel(overloadOpt.get(), tool) > 0;
                    }
                }

                if (hasOverload) {
                    level.setBlock(pos, ModBlocks.FAKE_BEDROCK.get().defaultBlockState(), 3);
                }
            }
        }
    }

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

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.isEmpty() || right.isEmpty()) return;

        int leftOverclock = getOverclockLevel(left);
        int rightOverclock = getOverclockLevel(right);
        int effectiveOverclock = Math.max(leftOverclock, rightOverclock);

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
            int allowedMax = ench.is(ModEnchantments.OVERCLOCK) ? baseMax : baseMax + effectiveOverclock;

            if (newLevel > allowedMax) {
                newLevel = allowedMax;
            }

            if (newLevel != leftLevel) {
                resultMap.put(ench, newLevel);
                changed = true;
                totalCost += newLevel;
            }
        }

        int updatedOverclock = 0;
        for (var entry : resultMap.entrySet()) {
            if (entry.getKey().is(ModEnchantments.OVERCLOCK)) {
                updatedOverclock = Math.min(2, entry.getValue());
                break;
            }
        }
        effectiveOverclock = Math.max(effectiveOverclock, updatedOverclock);

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

            if (event.getName() != null && !StringUtil.isBlank(event.getName())) {
                if (!event.getName().equals(left.getHoverName().getString())) {
                    output.set(DataComponents.CUSTOM_NAME, Component.literal(event.getName()));
                    totalCost += 1;
                }
            } else if (left.has(DataComponents.CUSTOM_NAME) && event.getName() != null && event.getName().isEmpty()) {
                output.remove(DataComponents.CUSTOM_NAME);
                totalCost += 1;
            }

            event.setOutput(output);
            event.setXpCost(Math.max(1, totalCost));
            event.setMaterialCost(1);
        }
    }

    public static int getOverdriveLevel(ItemStack stack, net.minecraft.core.HolderLookup.Provider registries) {
        if (stack == null || stack.isEmpty()) return 0;
        var reg = registries.lookup(Registries.ENCHANTMENT);
        if (reg.isPresent()) {
            var overdriveOpt = reg.get().get(ModEnchantments.OVERDRIVE);
            if (overdriveOpt.isPresent()) {
                return EnchantmentHelper.getItemEnchantmentLevel(overdriveOpt.get(), stack);
            }
        }
        return 0;
    }

    private static CompoundTag getBoundContainerTag(ItemStack stack) {
        if (stack.has(PureMashDataComponents.BOUND_CONTAINER.get())) {
            return stack.get(PureMashDataComponents.BOUND_CONTAINER.get());
        }
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("X") && tag.contains("Y") && tag.contains("Z")) {
                return tag;
            }
        }
        return null;
    }

    private static Direction determineHitFace(Player player, BlockPos centerPos) {
        var hitResult = player.pick(20.0D, 0.0F, false);
        if (hitResult instanceof BlockHitResult blockHit && blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            if (blockHit.getBlockPos().equals(centerPos)) {
                return blockHit.getDirection();
            }
        }

        Vec3 viewVec = player.getViewVector(1.0F);
        if (viewVec.lengthSqr() > 0.01D) {
            if (viewVec.y > 0.6D) return Direction.DOWN;
            if (viewVec.y < -0.6D) return Direction.UP;

            if (Math.abs(viewVec.y) < 0.3D) {
                Direction lookDir = Direction.getApproximateNearest(viewVec.x, 0.0D, viewVec.z);
                return lookDir.getOpposite();
            }
        }

        BlockPos playerPos = player.blockPosition();
        int dx = playerPos.getX() - centerPos.getX();
        int dy = playerPos.getY() - centerPos.getY();
        int dz = playerPos.getZ() - centerPos.getZ();

        if (dx != 0 || dy != 0 || dz != 0) {
            Direction nearest = Direction.getNearest(dx, dy, dz, null);
            if (nearest != null) {
                return nearest;
            }
        }

        return player.getDirection().getOpposite();
    }

    @SubscribeEvent
    public static void onBlockBreakOverdrive(BreakBlockEvent event) {
        if (IS_AOE_MINING.get()) return;

        Player player = event.getPlayer();
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;

        boolean overdriveDisabled = player.getPersistentData().getBooleanOr("OverdriveDisabled", false);
        if (overdriveDisabled) return;

        ItemStack tool = player.getMainHandItem();
        if (!isAllowedOverdriveTool(tool)) return;

        int overdriveLevel = getOverdriveLevel(tool, player.level().registryAccess());
        if (overdriveLevel <= 0) return;

        net.minecraft.world.level.Level level = (net.minecraft.world.level.Level) event.getLevel();
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        BlockPos centerPos = event.getPos();
        net.minecraft.world.level.block.state.BlockState centerState = level.getBlockState(centerPos);

        if (!tool.isCorrectToolForDrops(centerState)) return;

        CompoundTag boundTag = getBoundContainerTag(tool);
        net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> boundHandler = null;

        if (boundTag != null && boundTag.contains("X")) {
            String dim = boundTag.getStringOr("Dimension", "");
            if (level.dimension().identifier().toString().equals(dim)) {
                BlockPos boundPos = new BlockPos(
                        boundTag.getIntOr("X", 0),
                        boundTag.getIntOr("Y", 0),
                        boundTag.getIntOr("Z", 0)
                );
                Direction boundSide = Direction.from3DDataValue(boundTag.getIntOr("Side", 0));
                boundHandler = level.getCapability(Capabilities.Item.BLOCK, boundPos, boundSide);
            }
        }

        // 1. DERRUBADA COMPLETA DE ÁRVORES (AXE / PAXEL TREE FELLING)
        boolean isTreeTool = tool.is(ItemTags.AXES) || tool.is(ModItems.SYNTHORIUM_PAXEL.get()) || tool.is(ModItems.MOLDELONIAN_PAXEL.get());
        if (centerState.is(BlockTags.LOGS) && isTreeTool) {
            int maxTreeLogs = switch (overdriveLevel) {
                case 4 -> 256;
                case 3 -> 128;
                case 2 -> 64;
                default -> 32;
            };

            try {
                IS_AOE_MINING.set(true);
                fellTree(serverLevel, centerPos, player, tool, maxTreeLogs, boundHandler);
                event.setCanceled(true);
            } finally {
                IS_AOE_MINING.set(false);
            }
            return;
        }

        // 2. MINERAÇÃO EM ÁREA INTELIGENTE (3x3, 5x5, 7x7 COM FILTRO DE CATEGORIA)
        int radius = switch (overdriveLevel) {
            case 4 -> 3; // 7x7
            case 3 -> 2; // 5x5
            case 2 -> 1; // 3x3
            default -> 0;
        };

        if (radius == 0 && boundHandler == null) return;

        Direction hitFace = determineHitFace(player, centerPos);

        int minX = -radius, maxX = radius;
        int minY = -radius, maxY = radius;
        int minZ = -radius, maxZ = radius;

        switch (hitFace) {
            case UP, DOWN -> { minY = 0; maxY = 0; }
            case NORTH, SOUTH -> { minZ = 0; maxZ = 0; }
            case EAST, WEST -> { minX = 0; maxX = 0; }
        }

        try {
            IS_AOE_MINING.set(true);
            boolean playedTeleportSound = false;
            int blocksBrokenCount = 0;
            int uniqueTypesTeleported = 0;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = centerPos.offset(x, y, z);

                        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                        if (state.isAir() || state.getDestroySpeed(level, pos) < 0 || !tool.isCorrectToolForDrops(state)) continue;

                        // Filtro Contextual: Impede que o pacachado ou ferramentas destruam blocos de outras categorias
                        if (!isMatchingBlockCategory(state, centerState)) continue;

                        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
                        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(state, serverLevel, pos, blockEntity, player, tool);

                        level.destroyBlock(pos, false, player);
                        blocksBrokenCount++;

                        for (ItemStack drop : drops) {
                            if (drop.isEmpty()) continue;

                            if (boundHandler != null && uniqueTypesTeleported < 64) {
                                var resource = ItemResource.of(drop);
                                int amountToTransfer = Math.min(drop.getCount(), 64);

                                try (Transaction tx = Transaction.openRoot()) {
                                    int inserted = boundHandler.insert(resource, amountToTransfer, tx);
                                    if (inserted > 0) {
                                        tx.commit();
                                        drop.shrink(inserted);
                                        uniqueTypesTeleported++;
                                        serverLevel.sendParticles(
                                                net.minecraft.core.particles.ParticleTypes.PORTAL,
                                                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                                3, 0.1, 0.1, 0.1, 0.0
                                        );
                                        if (!playedTeleportSound) {
                                            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                                    net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                                    net.minecraft.sounds.SoundSource.PLAYERS, 0.2F, 1.0F);
                                            playedTeleportSound = true;
                                        }
                                    }
                                }
                            }

                            if (!drop.isEmpty()) {
                                net.minecraft.world.level.block.Block.popResource(serverLevel, pos, drop);
                            }
                        }
                    }
                }
            }

            if (blocksBrokenCount > 0 && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                tool.hurtAndBreak(blocksBrokenCount, serverLevel, serverPlayer, item -> {});
            }

            event.setCanceled(true);

        } finally {
            IS_AOE_MINING.set(false);
        }
    }

    // =========================================================================
    // OVERDRIVE SWORD COMBAT: ENLARGED AOE SWEEPING ATTACK ON MOBS
    // =========================================================================
    @SubscribeEvent
    public static void onLivingDamageOverdriveSword(LivingDamageEvent.Post event) {
        if (IS_AOE_ATTACKING.get()) return;

        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide()) return;
        if (player.isSpectator()) return;

        boolean overdriveDisabled = player.getPersistentData().getBooleanOr("OverdriveDisabled", false);
        if (overdriveDisabled) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty() || !weapon.is(ItemTags.SWORDS)) return;

        int overdriveLevel = getOverdriveLevel(weapon, player.level().registryAccess());
        if (overdriveLevel < 2) return;

        LivingEntity target = event.getEntity();
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        // Radius scaling: Level 2 = 2.5 blocks, Level 3 = 3.5 blocks, Level 4 = 5.0 blocks
        double radius = switch (overdriveLevel) {
            case 4 -> 5.0D;
            case 3 -> 3.5D;
            case 2 -> 2.5D;
            default -> 0.0D;
        };

        if (radius <= 0.0D) return;

        try {
            IS_AOE_ATTACKING.set(true);
            float sweepDamage = event.getOriginalDamage() * (0.6F + (0.1F * overdriveLevel));

            List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    target.getBoundingBox().inflate(radius, 1.5D, radius),
                    e -> e != player && e != target && !player.isAlliedTo(e) && e.isAlive() && !e.isSpectator()
            );

            for (LivingEntity nearby : nearbyEntities) {
                if (nearby instanceof TamableAnimal tamable && tamable.isOwnedBy(player)) {
                    continue;
                }
                if (nearby instanceof ArmorStand) {
                    continue;
                }

                nearby.hurtServer(serverLevel, player.damageSources().playerAttack(player), sweepDamage);

                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                        nearby.getX(), nearby.getY() + (nearby.getBbHeight() * 0.5D), nearby.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }

            serverLevel.playSound(
                    null, target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0F, 1.0F
            );
        } finally {
            IS_AOE_ATTACKING.set(false);
        }
    }

    private static void handleArmorOverdriveAttributes(Player player) {
        boolean overdriveDisabled = player.getPersistentData().getBooleanOr("OverdriveDisabled", false);
        int armorOverdrive = overdriveDisabled ? 0 : getArmorOverdriveLevel(player);

        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var stepAttr = player.getAttribute(Attributes.STEP_HEIGHT);

        if (armorOverdrive > 0) {
            if (speedAttr != null) {
                if (speedAttr.hasModifier(OVERDRIVE_SPEED_ID)) {
                    speedAttr.removeModifier(OVERDRIVE_SPEED_ID);
                }
                speedAttr.addTransientModifier(new AttributeModifier(
                        OVERDRIVE_SPEED_ID,
                        0.05 * armorOverdrive,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
            if (stepAttr != null && !stepAttr.hasModifier(OVERDRIVE_STEP_ID)) {
                stepAttr.addTransientModifier(new AttributeModifier(
                        OVERDRIVE_STEP_ID,
                        0.5,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        } else {
            if (speedAttr != null && speedAttr.hasModifier(OVERDRIVE_SPEED_ID)) {
                speedAttr.removeModifier(OVERDRIVE_SPEED_ID);
            }
            if (stepAttr != null && stepAttr.hasModifier(OVERDRIVE_STEP_ID)) {
                stepAttr.removeModifier(OVERDRIVE_STEP_ID);
            }
        }
    }

    private static boolean isAllowedOverdriveTool(ItemStack tool) {
        if (tool.isEmpty()) return false;
        return tool.is(ItemTags.PICKAXES) ||
                tool.is(ItemTags.SHOVELS) ||
                tool.is(ItemTags.AXES) ||
                tool.is(ItemTags.HOES) ||
                tool.is(ModItems.SYNTHORIUM_PAXEL.get()) ||
                tool.is(ModItems.MOLDELONIAN_PAXEL.get());
    }

    @SubscribeEvent
    public static void onLivingFallOverdrive(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        boolean overdriveDisabled = player.getPersistentData().getBooleanOr("OverdriveDisabled", false);
        if (overdriveDisabled) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int bootOverdrive = getOverdriveLevel(boots, player.level().registryAccess());

        if (bootOverdrive <= 0) return;

        if (event.getDistance() < 3.0f) {
            return;
        }

        event.setDistance(0.0f);
        event.setCanceled(true);
        player.resetFallDistance();

        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.GUST_EMITTER_LARGE,
                    px, py + 0.2, pz,
                    1, 0.0, 0.0, 0.0, 0.0
            );

            serverLevel.playSound(
                    null, px, py, pz,
                    net.minecraft.sounds.SoundEvents.WIND_CHARGE_BURST.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.5F, 1.0F
            );
        }
    }

    public static int getOverloadLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchants.entrySet()) {
            if (entry.getKey().is(ModEnchantments.OVERLOAD)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    public static boolean isAllowedOverloadTool(ItemStack tool) {
        if (tool.isEmpty()) return false;
        return tool.is(ItemTags.PICKAXES) ||
                tool.is(ItemTags.AXES) ||
                tool.is(ItemTags.SHOVELS) ||
                tool.is(ItemTags.HOES) ||
                tool.is(ItemTags.SWORDS) ||
                tool.is(ModItems.SYNTHORIUM_PAXEL.get()) ||
                tool.is(ModItems.MOLDELONIAN_PAXEL.get());
    }

    // =========================================================================
    // CONTAINER BINDING (TOOLS, PAXELS & WRENCHES ONLY - SWORDS EXCLUDED)
    // =========================================================================
    @SubscribeEvent
    public static void onRightClickContainerToBind(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || event.getLevel().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // Excludes swords: only allows wrenches and valid mining/harvesting tools with Overdrive
        boolean isBindable = dev.davidklgames.puremashtweaks.item.ConfigurationWrenchItem.isWrench(stack) ||
                (isAllowedOverdriveTool(stack) && getOverdriveLevel(stack, player.level().registryAccess()) > 0);

        if (!isBindable) return;

        net.minecraft.world.level.Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Direction face = event.getHitVec().getDirection();
        String currentDim = level.dimension().identifier().toString();

        var handler = level.getCapability(Capabilities.Item.BLOCK, pos, face);
        if (handler != null) {
            CompoundTag currentTag = stack.get(PureMashDataComponents.BOUND_CONTAINER.get());

            if (currentTag != null && currentTag.getIntOr("X", 0) == pos.getX() &&
                    currentTag.getIntOr("Y", 0) == pos.getY() &&
                    currentTag.getIntOr("Z", 0) == pos.getZ() &&
                    currentTag.getIntOr("Side", 0) == face.get3DDataValue() &&
                    currentTag.getStringOr("Dimension", "").equals(currentDim)) {

                stack.remove(PureMashDataComponents.BOUND_CONTAINER.get());

                Component prefix = Component.literal("[Overdrive]: ").withStyle(net.minecraft.ChatFormatting.AQUA);
                Component msg = Component.literal("Bound container link removed.").withStyle(net.minecraft.ChatFormatting.RED);
                player.sendSystemMessage(Component.empty().append(prefix).append(msg));

                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENDER_EYE_DEATH, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                CompoundTag newTag = new CompoundTag();
                newTag.putInt("X", pos.getX());
                newTag.putInt("Y", pos.getY());
                newTag.putInt("Z", pos.getZ());
                newTag.putInt("Side", face.get3DDataValue());
                newTag.putString("Dimension", currentDim);

                stack.set(PureMashDataComponents.BOUND_CONTAINER.get(), newTag);

                Component prefix = Component.literal("[Overdrive]: ").withStyle(net.minecraft.ChatFormatting.AQUA);
                net.minecraft.network.chat.MutableComponent coordsFormatted = Component.empty()
                        .append(Component.literal("X=").withStyle(net.minecraft.ChatFormatting.RED))
                        .append(Component.literal(String.valueOf(pos.getX())).withStyle(net.minecraft.ChatFormatting.GREEN))
                        .append(Component.literal(", ").withStyle(net.minecraft.ChatFormatting.GRAY))
                        .append(Component.literal("Y=").withStyle(net.minecraft.ChatFormatting.GOLD))
                        .append(Component.literal(String.valueOf(pos.getY())).withStyle(net.minecraft.ChatFormatting.GREEN))
                        .append(Component.literal(", ").withStyle(net.minecraft.ChatFormatting.GRAY))
                        .append(Component.literal("Z=").withStyle(net.minecraft.ChatFormatting.AQUA))
                        .append(Component.literal(String.valueOf(pos.getZ())).withStyle(net.minecraft.ChatFormatting.GREEN));

                net.minecraft.network.chat.MutableComponent msg = Component.literal("Bound target container at [")
                        .withStyle(net.minecraft.ChatFormatting.GREEN)
                        .append(coordsFormatted)
                        .append(Component.literal("] (").withStyle(net.minecraft.ChatFormatting.GREEN))
                        .append(Component.literal(face.getName().toUpperCase()).withStyle(net.minecraft.ChatFormatting.GRAY))
                        .append(Component.literal(") in ").withStyle(net.minecraft.ChatFormatting.GREEN))
                        .append(Component.literal(currentDim).withStyle(net.minecraft.ChatFormatting.DARK_AQUA))
                        .append(Component.literal(".").withStyle(net.minecraft.ChatFormatting.GREEN));

                player.sendSystemMessage(Component.empty().append(prefix).append(msg));
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.END_PORTAL_FRAME_FILL, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            event.setCanceled(true);
        }
    }
}