package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.client.ClientSynthesisRecipeCache;
import dev.davidklgames.puremashtweaks.client.PureMashTweaksClient;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ClientPayloadRegistrar {
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(
                SyncFlightPayload.TYPE,
                SyncFlightPayload.STREAM_CODEC,
                PureMashTweaksClient::handleSyncFlightTicks
        );

        registrar.playToClient(
                SyncOverdrivePayload.TYPE,
                SyncOverdrivePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (net.minecraft.client.Minecraft.getInstance().player != null) {
                        net.minecraft.client.Minecraft.getInstance().player.getPersistentData().putBoolean("OverdriveDisabled", payload.disabled());
                    }
                })
        );

        registrar.playToClient(
                SyncSynthesisRecipesPayload.TYPE,
                SyncSynthesisRecipesPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientSynthesisRecipeCache.setRecipes(payload.recipes()))
        );
    }
}