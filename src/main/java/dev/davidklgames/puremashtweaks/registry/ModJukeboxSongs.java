package dev.davidklgames.puremashtweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

public class ModJukeboxSongs {
    // Registries the song key in the Jukebox in Minecraft Java Edition 26.1.2
    public static final ResourceKey<JukeboxSong> BEYOND_THE_FINAL_STAGE = ResourceKey.create(
            Registries.JUKEBOX_SONG,
            Identifier.fromNamespaceAndPath("puremashtweaks", "beyond_the_final_stage")
    );
    public static final ResourceKey<JukeboxSong> NEW_HORIZONS = ResourceKey.create(
            Registries.JUKEBOX_SONG,
            Identifier.fromNamespaceAndPath("puremashtweaks", "new_horizons")
    );
}