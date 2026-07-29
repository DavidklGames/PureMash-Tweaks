package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.client.PureMashTweaksClient;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ClientPayloadRegistrar {
    public static void register(PayloadRegistrar registrar) {
        // Registers the tick packet by passing the client receiver directly.
        registrar.playToClient(
                SyncFlightPayload.TYPE,
                SyncFlightPayload.STREAM_CODEC,
                PureMashTweaksClient::handleSyncFlightTicks
        );
    }
}