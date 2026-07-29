package dev.davidklgames.puremashtweaks.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleSyncFlight(final SyncFlightPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                // Synchronizes local client data for the HUD to render.
                mc.player.getPersistentData().putInt("OverloadFlightTicks", payload.ticks());
                mc.player.getPersistentData().putBoolean("OverloadFlightDisabled", payload.disabled());
            }
        });
    }
}