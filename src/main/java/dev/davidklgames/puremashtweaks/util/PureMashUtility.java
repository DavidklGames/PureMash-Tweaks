package dev.davidklgames.puremashtweaks.util;

import net.neoforged.fml.loading.FMLPaths;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class PureMashUtility {

    public static void copyReadmes() {
        Path mainConfigDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks");

        // Maps internal resources from the source JAR to the corresponding physical destination folders.
        copyResource("/assets/puremashtweaks/info/alchemical_craft/README.txt",
                mainConfigDir.resolve("alchemical_recipes/README.txt"));

        copyResource("/assets/puremashtweaks/info/compressor_craft/README.txt",
                mainConfigDir.resolve("compressor_recipes/README.txt"));

        copyResource("/assets/puremashtweaks/info/synthesis_craft/README.txt",
                mainConfigDir.resolve("synthesis_recipes/README.txt"));
    }

    private static void copyResource(String resourcePath, Path targetPath) {
        try {
            // Ensures the parent directory exists before attempting to copy the file.
            Path parentDir = targetPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (InputStream in = PureMashUtility.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    PureMashTweaks.LOGGER.error("[PureMash Utility]: Internal resource not found in JAR: {}", resourcePath);
                    return;
                }

                byte[] resourceBytes = in.readAllBytes();

                // If file exists and content is identical, do not overwrite to save disk I/O
                if (Files.exists(targetPath)) {
                    byte[] existingBytes = Files.readAllBytes(targetPath);
                    if (Arrays.equals(resourceBytes, existingBytes)) {
                        return;
                    }
                }

                // If file does not exist or has outdated content, update it with the new version
                Files.write(targetPath, resourceBytes);
                PureMashTweaks.LOGGER.info("[PureMash Utility]: Successfully updated/copied resource {} to {}", resourcePath, targetPath);
            }
        } catch (IOException e) {
            PureMashTweaks.LOGGER.error("[PureMash Utility]: Failed to copy resource {} to {}", resourcePath, targetPath, e);
        }
    }
}