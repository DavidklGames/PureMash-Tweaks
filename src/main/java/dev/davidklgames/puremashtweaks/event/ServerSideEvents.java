package dev.davidklgames.puremashtweaks.event;

import dev.davidklgames.puremashtweaks.component.ModSounds;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = "puremashtweaks")
public class ServerSideEvents {

    @SubscribeEvent
    public static void onAdvancement(@NotNull AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        AdvancementHolder advancement = event.getAdvancement();
        Identifier id = advancement.id();

        if (!id.getNamespace().equals("puremashtweaks")) {
            return;
        }

        if (!PureMashTweaksConfig.ENABLE_ADVANCEMENTS.get()) {
            if (player instanceof ServerPlayer serverPlayer) {
                net.minecraft.advancements.AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                for (String criterion : progress.getCompletedCriteria()) {
                    serverPlayer.getAdvancements().revoke(advancement, criterion);
                }
            }
            return;
        }

        if (advancement.value().display().isPresent() &&
                advancement.value().display().get().getType() == net.minecraft.advancements.AdvancementType.GOAL) {

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    ModSounds.PUREMASH_ADVANCEMENT_SOUND.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }
}