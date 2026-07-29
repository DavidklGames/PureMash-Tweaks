package dev.davidklgames.puremashtweaks.api;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class PMT {
    public static final String MOD_ID = "puremashtweaks";

    public PMT() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Identifier c(String path) {
        return Identifier.fromNamespaceAndPath("c", path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, id(path));
    }
}
