package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record UpdateCableFilterPayload(
        BlockPos pos,
        Direction side,
        int filterIndex,
        CompoundTag filterTag
) implements CustomPacketPayload {
    public static final Type<UpdateCableFilterPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "update_cable_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCableFilterPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateCableFilterPayload::pos,
            Direction.STREAM_CODEC, UpdateCableFilterPayload::side,
            ByteBufCodecs.VAR_INT, UpdateCableFilterPayload::filterIndex,
            ByteBufCodecs.COMPOUND_TAG, UpdateCableFilterPayload::filterTag,
            UpdateCableFilterPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}