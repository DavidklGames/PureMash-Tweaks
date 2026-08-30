package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record CloseFilterToCablePayload(
        BlockPos pos,
        Direction side
) implements CustomPacketPayload {
    public static final Type<CloseFilterToCablePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "close_filter_to_cable"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseFilterToCablePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CloseFilterToCablePayload::pos,
            Direction.STREAM_CODEC, CloseFilterToCablePayload::side,
            CloseFilterToCablePayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}