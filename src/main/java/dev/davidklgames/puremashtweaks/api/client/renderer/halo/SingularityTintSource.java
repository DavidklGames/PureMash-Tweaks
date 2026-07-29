package dev.davidklgames.puremashtweaks.api.client.renderer.halo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.davidklgames.puremashtweaks.item.ColorSingularityItem;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SingularityTintSource(int layer) implements ItemTintSource {
    public static final MapCodec<SingularityTintSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("layer").forGetter(SingularityTintSource::layer)
            ).apply(instance, SingularityTintSource::new)
    );

    @Override
    public int calculate(@NotNull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        // 1. EXCLUSIVE CHECK FOR COSMIC SINGULARITY
        if (stack.is(ModSingularities.COSMIC_SINGULARITY.get())) {
            long time = System.currentTimeMillis();
            // Generates a hue that sweeps across the entire RGB spectrum every 3000ms (3 seconds)
            float hue = (time % 3000L) / 3000.0F;
            int animatedColor = Mth.hsvToRgb(hue, 1.0F, 1.0F);

            // Returns the animated fully opaque color for BOTH layers (layer 0 and 1)
            return ARGB.opaque(animatedColor);
        }

        // 2. DEFAULT LOGIC FOR OTHER SINGULARITIES
        if (stack.getItem() instanceof ColorSingularityItem singularity) {
            return this.layer == 0 ? ARGB.opaque(singularity.getColor0()) : ARGB.opaque(singularity.getColor1());
        }

        return 0xFFFFFFFF;
    }

    @Override
    public @NotNull MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}