package dev.davidklgames.puremashtweaks.event;

import dev.davidklgames.puremashtweaks.registry.ModSounds;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EventBusSubscriber(modid = "puremashtweaks")
public class ServerSideEvents {

    @SubscribeEvent
    public static void onDatapackSync(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
        net.minecraft.server.MinecraftServer server = event.getPlayerList().getServer();
        net.minecraft.world.item.crafting.RecipeManager recipeManager = server.getRecipeManager();
        List<RecipeHolder<Recipe<CraftingInput>>> recipesToSync = new java.util.ArrayList<>();

        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value().getType() == dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPED_SYNTHESIS_TYPE.get() ||
                    holder.value().getType() == dev.davidklgames.puremashtweaks.registry.ModRecipes.SHAPELESS_SYNTHESIS_TYPE.get()) {
                recipesToSync.add((net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.CraftingInput>>) holder);
            }
        }

        dev.davidklgames.puremashtweaks.network.SyncSynthesisRecipesPayload payload =
                new dev.davidklgames.puremashtweaks.network.SyncSynthesisRecipesPayload(recipesToSync);

        if (event.getPlayer() != null) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(event.getPlayer(), payload);
        } else {
            net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(payload);
        }
    }

    @SubscribeEvent
    public static void onAdvancement(@NotNull AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        AdvancementHolder advancement = event.getAdvancement();
        Identifier id = advancement.id();

        if (!id.getNamespace().equals("puremashtweaks")) {
            return;
        }

        if (!PureMashTweaksConfig.COMMON.enableAdvancements.get()) {
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