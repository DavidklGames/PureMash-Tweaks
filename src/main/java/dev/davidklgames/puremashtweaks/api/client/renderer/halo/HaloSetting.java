package dev.davidklgames.puremashtweaks.api.client.renderer.halo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record HaloSetting(Identifier texture, int color, float size, boolean pulse) {
    public static final Codec<HaloSetting> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(HaloSetting::texture),
                    Codec.INT.fieldOf("color").forGetter(HaloSetting::color),
                    Codec.FLOAT.fieldOf("size").forGetter(HaloSetting::size),
                    Codec.BOOL.fieldOf("pulse").forGetter(HaloSetting::pulse)
            ).apply(instance, HaloSetting::new)
    );
}