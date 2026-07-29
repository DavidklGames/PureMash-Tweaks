package dev.davidklgames.puremashtweaks.event;

import dev.davidklgames.puremashtweaks.item.ColorSingularityItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "puremashtweaks")
public class NoPureMashSingularityDrop {

    public record PendingSingularity(
            ResourceKey<Level> dimension,
            double x, double y, double z,
            ItemStack stack
    ) {
        public static final Codec<PendingSingularity> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(PendingSingularity::dimension),
                        Codec.DOUBLE.fieldOf("x").forGetter(PendingSingularity::x),
                        Codec.DOUBLE.fieldOf("y").forGetter(PendingSingularity::y),
                        Codec.DOUBLE.fieldOf("z").forGetter(PendingSingularity::z),
                        ItemStack.CODEC.fieldOf("item").forGetter(PendingSingularity::stack)
                ).apply(instance, PendingSingularity::new)
        );
    }

    public static class PendingSingularitySavedData extends SavedData {
        private final List<PendingSingularity> pending;

        public PendingSingularitySavedData() {
            this.pending = new ArrayList<>();
        }

        public PendingSingularitySavedData(List<PendingSingularity> pending) {
            this.pending = new ArrayList<>(pending);
        }

        public List<PendingSingularity> getPending() {
            return this.pending;
        }

        public void add(PendingSingularity sig) {
            this.pending.add(sig);
            this.setDirty();
        }

        public void remove(PendingSingularity sig) {
            if (this.pending.remove(sig)) {
                this.setDirty();
            }
        }

        public static final Codec<PendingSingularitySavedData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        PendingSingularity.CODEC.listOf().fieldOf("pending").forGetter(PendingSingularitySavedData::getPending)
                ).apply(instance, PendingSingularitySavedData::new)
        );

        public static final SavedDataType<PendingSingularitySavedData> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath("puremashtweaks", "pending_singularities"),
                PendingSingularitySavedData::new,
                CODEC,
                null
        );
    }

    public static PendingSingularitySavedData getSavedData(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(PendingSingularitySavedData.TYPE);
        }
        return null;
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItem();

        if (stack.getItem() instanceof ColorSingularityItem) {
            event.setCanceled(true);

            Player player = event.getPlayer();
            if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
                if (!player.getInventory().add(stack.copy())) {
                    PendingSingularitySavedData data = getSavedData(serverLevel);
                    if (data != null) {
                        data.add(new PendingSingularity(
                                player.level().dimension(),
                                player.getX(), player.getY(), player.getZ(),
                                stack.copy()
                        ));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            if (stack.getItem() instanceof ColorSingularityItem) {
                event.setCanceled(true);

                if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
                    double x = itemEntity.getX();
                    double y = itemEntity.getY();
                    double z = itemEntity.getZ();

                    Player nearestPlayer = serverLevel.getNearestPlayer(x, y, z, 16.0D, false);

                    if (nearestPlayer != null) {
                        ItemStack copyStack = stack.copy();
                        if (nearestPlayer.getInventory().add(copyStack)) {
                            nearestPlayer.level().playSound(
                                    null, nearestPlayer.getX(), nearestPlayer.getY(), nearestPlayer.getZ(),
                                    net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                    net.minecraft.sounds.SoundSource.PLAYERS,
                                    0.2F,
                                    (nearestPlayer.getRandom().nextFloat() - nearestPlayer.getRandom().nextFloat()) * 0.2F + 1.0F
                            );
                            return;
                        }
                    }

                    PendingSingularitySavedData data = getSavedData(serverLevel);
                    if (data != null) {
                        data.add(new PendingSingularity(
                                serverLevel.dimension(),
                                x, y, z,
                                stack.copy()
                        ));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.isSpectator() || !(player.level() instanceof ServerLevel serverLevel)) return;

        PendingSingularitySavedData data = getSavedData(serverLevel);
        if (data == null || data.getPending().isEmpty()) return;

        ResourceKey<Level> currentLevel = player.level().dimension();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        List<PendingSingularity> currentPending = new ArrayList<>(data.getPending());

        for (PendingSingularity pending : currentPending) {
            if (pending.dimension().equals(currentLevel)) {
                double dx = pending.x() - px;
                double dy = pending.y() - py;
                double dz = pending.z() - pz;
                double distanceSq = dx * dx + dy * dy + dz * dz;

                if (distanceSq <= 256.0D) {
                    ItemStack copyStack = pending.stack().copy();
                    if (player.getInventory().add(copyStack)) {
                        player.level().playSound(
                                null, px, py, pz,
                                net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                0.2F,
                                (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F
                        );
                        data.remove(pending);
                    }
                }
            }
        }
    }
}