package dev.davidklgames.puremashtweaks.recipe.condition;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import org.jspecify.annotations.NonNull;

public record FallbackConfigCondition() implements ICondition {
    // Simple codec with no parameters, as it only reads the global configuration.
    public static final MapCodec<FallbackConfigCondition> CODEC = MapCodec.unit(new FallbackConfigCondition());

    @Override
    public boolean test(@NonNull IContext context) {
        // Returns TRUE if the fallback configuration is enabled, activating the recipe in the game!
        return PureMashTweaksConfig.ENABLE_CREATIVE_ESSENCE_FALLBACK.get();
    }

    @Override
    public @NonNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}