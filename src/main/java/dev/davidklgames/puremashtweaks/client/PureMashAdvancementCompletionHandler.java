package dev.davidklgames.puremashtweaks.client;

import dev.davidklgames.puremashtweaks.registry.ModSounds;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "puremashtweaks")
public class PureMashAdvancementCompletionHandler {

    // Safely manages the waiting time (150 ticks = 7.5 seconds) for multiple players
    private static final Map<UUID, Integer> finaleCountdown = new ConcurrentHashMap<>();

    // The main advancements of the 'root' tree identified in the original file
    private static final String[] MAIN_ADVANCEMENTS = {
            "puremashtweaks:main/root",
            "puremashtweaks:main/find_debris",
            "puremashtweaks:main/get_ingot",
            "puremashtweaks:main/get_tools",
            "puremashtweaks:main/get_paxel",
            "puremashtweaks:main/get_compressor",
            "puremashtweaks:main/get_armor",
            "puremashtweaks:main/get_moldelonian_template",
            "puremashtweaks:main/get_moldelonian_ingot",
            "puremashtweaks:main/get_moldelonian_core",
            "puremashtweaks:main/get_puremash_core",
            "puremashtweaks:main/get_core_block",
            "puremashtweaks:main/get_synthesis_table",
            "puremashtweaks:main/get_alchemical_synthesizer",
            "puremashtweaks:secret/overloaded",
            "puremashtweaks:secret/energy_for_all",
            "puremashtweaks:main/get_moldelonian_armor",
            "puremashtweaks:main/get_moldelonian_paxel",
            "puremashtweaks:secret/overclocked",
            "puremashtweaks:secret/overdriven",
            "puremashtweaks:main/get_generator",
            "puremashtweaks:main/get_universal_cable",
            "puremashtweaks:main/get_fluid_tank",
            "puremashtweaks:main/get_creative_fluid_tank",
            "puremashtweaks:main/get_battery",
            "puremashtweaks:main/get_creative_battery",
            "puremashtweaks:secret/one_unites_all",
            "puremashtweaks:secret/cosmic_singularity"
    };

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Identifier id = event.getAdvancement().id();

        // Only processes if the earned advancement belongs to PureMash Tweaks
        if (!id.getNamespace().equals("puremashtweaks")) {
            return;
        }

        boolean hasAll = true;
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            var playerAdvancements = server.getPlayerList().getPlayerAdvancements(player);

            for (String advId : MAIN_ADVANCEMENTS) {
                Identifier identifier = Identifier.tryParse(advId);
                if (identifier == null) {
                    hasAll = false;
                    break;
                }

                var advancementHolder = server.getAdvancements().get(identifier);

                if (advancementHolder == null || !playerAdvancements.getOrStartProgress(advancementHolder).isDone()) {
                    hasAll = false;
                    break;
                }
            }

            // If the player has completed all main advancements, starts the countdown for the Grand Finale (150 ticks = 7.5 seconds)
            if (hasAll) {
                if (!finaleCountdown.containsKey(player.getUUID())) {
                    finaleCountdown.put(player.getUUID(), 150);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        if (finaleCountdown.containsKey(uuid)) {
            int countdown = finaleCountdown.get(uuid);
            if (countdown > 0) {
                finaleCountdown.put(uuid, countdown - 1);
            } else {
                finaleCountdown.remove(uuid);
                executeGrandFinale(player);
            }
        }
    }

    private static void executeGrandFinale(ServerPlayer player) {
        // 1. Plays the mod's completion sound directly to the player
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModSounds.BEYOND_THE_FINAL_STAGE_ACHIEVEMENTS_COMPLETED.get(),
                SoundSource.RECORDS,
                1.0F,
                1.0F
        );
        // 2. Delivers the music disc as a reward
        ItemStack discStack = new ItemStack(ModItems.MUSIC_DISC_BEYOND_THE_FINAL_STAGE.get());

        // Tries to add to the player's inventory; if full, safely drops it on the ground
        if (!player.getInventory().add(discStack)) {
            player.drop(discStack, false);
        }
    }
}