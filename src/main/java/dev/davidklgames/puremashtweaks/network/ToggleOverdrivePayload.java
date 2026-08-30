package dev.davidklgames.puremashtweaks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import org.jspecify.annotations.NonNull;

public record ToggleOverdrivePayload() implements CustomPacketPayload {
    public static final Type<ToggleOverdrivePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "toggle_overdrive"));

    public static final StreamCodec<FriendlyByteBuf, ToggleOverdrivePayload> STREAM_CODEC = StreamCodec.unit(new ToggleOverdrivePayload());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}