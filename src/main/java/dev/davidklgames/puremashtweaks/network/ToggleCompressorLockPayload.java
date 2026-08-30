package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ToggleCompressorLockPayload(BlockPos pos, boolean locked) implements CustomPacketPayload {
    public static final Type<ToggleCompressorLockPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "toggle_compressor_lock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCompressorLockPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleCompressorLockPayload::pos,
            ByteBufCodecs.BOOL, ToggleCompressorLockPayload::locked,
            ToggleCompressorLockPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}