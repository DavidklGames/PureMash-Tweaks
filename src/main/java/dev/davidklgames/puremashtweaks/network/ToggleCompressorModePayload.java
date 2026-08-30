package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ToggleCompressorModePayload(BlockPos pos, int mode) implements CustomPacketPayload {
    public static final Type<ToggleCompressorModePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "toggle_compressor_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCompressorModePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleCompressorModePayload::pos,
            ByteBufCodecs.VAR_INT, ToggleCompressorModePayload::mode,
            ToggleCompressorModePayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}