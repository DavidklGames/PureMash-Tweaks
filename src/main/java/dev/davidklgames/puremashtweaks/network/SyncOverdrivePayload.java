package dev.davidklgames.puremashtweaks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import org.jspecify.annotations.NonNull;

public record SyncOverdrivePayload(boolean disabled) implements CustomPacketPayload {
    public static final Type<SyncOverdrivePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "sync_overdrive"));

    public static final StreamCodec<FriendlyByteBuf, SyncOverdrivePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncOverdrivePayload::disabled,
            SyncOverdrivePayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}