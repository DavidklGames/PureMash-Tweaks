package dev.davidklgames.puremashtweaks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import org.jspecify.annotations.NonNull;

public record SyncFlightPayload(int ticks, boolean disabled) implements CustomPacketPayload {
    public static final Type<SyncFlightPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "sync_flight"));

    public static final StreamCodec<FriendlyByteBuf, SyncFlightPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, SyncFlightPayload::ticks,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, SyncFlightPayload::disabled,
            SyncFlightPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}