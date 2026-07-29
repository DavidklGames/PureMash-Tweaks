package dev.davidklgames.puremashtweaks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import org.jspecify.annotations.NonNull;

public record ToggleFlightPayload() implements CustomPacketPayload {
    public static final Type<ToggleFlightPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "toggle_flight"));

    public static final StreamCodec<FriendlyByteBuf, ToggleFlightPayload> STREAM_CODEC = StreamCodec.unit(new ToggleFlightPayload());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}