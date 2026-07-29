package dev.davidklgames.puremashtweaks.registry;

import com.mojang.serialization.MapCodec;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.recipe.condition.FallbackConfigCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.bus.api.IEventBus;

public class ModRecipeConditions {
    // --- Conditions CODEC for Minecraft 26.1.2 NeoForge's Conditions. ---
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, PureMashTweaks.MODID);

    //--- Registries the condition ---:
    public static final java.util.function.Supplier<MapCodec<FallbackConfigCondition>> FALLBACK_CONFIG =
            CONDITIONS.register("creative_fallback_enabled", () -> FallbackConfigCondition.CODEC);

    public static void register(IEventBus eventBus) {
        CONDITIONS.register(eventBus);
    }
}