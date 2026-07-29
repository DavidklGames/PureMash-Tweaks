package dev.davidklgames.puremashtweaks.component;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "puremashtweaks");

    public static final DeferredHolder<SoundEvent, SoundEvent> PUREMASH_ADVANCEMENT_SOUND =
            SOUNDS.register("puremash_goal", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("puremashtweaks", "puremash_goal")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BEYOND_THE_FINAL_STAGE =
            SOUNDS.register("beyond_the_final_stage", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("puremashtweaks", "beyond_the_final_stage")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BEYOND_THE_FINAL_STAGE_ACHIEVEMENTS_COMPLETED =
            SOUNDS.register("beyond_the_final_stage_achievements_completed", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("puremashtweaks", "beyond_the_final_stage_achievements_completed")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}