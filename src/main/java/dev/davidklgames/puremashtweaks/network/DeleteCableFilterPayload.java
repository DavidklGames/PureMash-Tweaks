package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record DeleteCableFilterPayload(
        BlockPos pos,
        Direction side,
        int filterIndex
) implements CustomPacketPayload {
    public static final Type<DeleteCableFilterPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "delete_cable_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteCableFilterPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DeleteCableFilterPayload::pos,
            Direction.STREAM_CODEC, DeleteCableFilterPayload::side,
            ByteBufCodecs.VAR_INT, DeleteCableFilterPayload::filterIndex,
            DeleteCableFilterPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}