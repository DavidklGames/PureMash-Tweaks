package dev.davidklgames.puremashtweaks.config;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.neoforged.fml.loading.FMLPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("CallToPrintStackTrace")
public class ModRecipeConfigGenerator {

    public static void init() {
        // My mod's main folder at the Config root.
        Path mainConfigDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks");

        // ----------------------------------------------------
        // MULTIFUNCTIONAL COMPRESSOR FOLDER (New Folders!)
        // ----------------------------------------------------
        Path compressorFolder = mainConfigDir.resolve("compressor_recipes/compressor");
        Path singularityFolder = mainConfigDir.resolve("compressor_recipes/singularity");
        Path dustFolder = mainConfigDir.resolve("compressor_recipes/dust");

        // ----------------------------------------------------
        // SYNTHESIS TABLE FOLDER (New folders!)
        // ----------------------------------------------------
        Path synthesisShapedFolder = mainConfigDir.resolve("synthesis_recipes/shaped");
        Path synthesisShapelessFolder = mainConfigDir.resolve("synthesis_recipes/shapeless");

        // ----------------------------------------------------
        // ALCHEMICAL SYNTHESIZER FOLDER (Added!)
        // ----------------------------------------------------
        Path alchemicalFolder = mainConfigDir.resolve("alchemical_recipes");

        try {
            // Create directories if they do not exist
            if (!Files.exists(compressorFolder)) Files.createDirectories(compressorFolder);
            if (!Files.exists(singularityFolder)) Files.createDirectories(singularityFolder);
            if (!Files.exists(dustFolder)) Files.createDirectories(dustFolder);
            if (!Files.exists(synthesisShapedFolder)) Files.createDirectories(synthesisShapedFolder);
            if (!Files.exists(synthesisShapelessFolder)) Files.createDirectories(synthesisShapelessFolder);
            if (!Files.exists(alchemicalFolder)) Files.createDirectories(alchemicalFolder);

            // Generates Multifunctional Compressor's JSON files.
            generateCompressorExample(compressorFolder);
            generateSingularityExample(singularityFolder);
            generateDustExample(dustFolder);

            // Generates Synthesis Table's JSON files.
            generateSynthesisShapedExample(synthesisShapedFolder);
            generateSynthesisShapelessExample(synthesisShapelessFolder);

            // Generates Alchemical Synthesizer's JSON files.
            generateAlchemicalExample(alchemicalFolder);

            // UTILITY EXECUTION: Automatically copies the README.txt files from the JAR to the actual configuration folders.
            dev.davidklgames.puremashtweaks.util.PureMashUtility.copyReadmes();

        } catch (IOException e) {
            e.printStackTrace();
        }
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Custom recipe configuration folders and default templates initialized.");
    }

    // ----------------------------------------------------------------------------------------------------
    // JSON Generation - Disabled Files
    // ----------------------------------------------------------------------------------------------------

    private static void generateCompressorExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_compress.json");
        if (!Files.exists(exampleFile)) {
            String json = """
                    [
                      {
                        "input": "puremashtweaks:synthorium_ingot",
                        "input_count": 9,
                        "output": "minecraft:dirt",
                        "time_cost": 100,
                        "enable_recipe": false
                      }
                    ]""";
            Files.writeString(exampleFile, json);
        }
    }

    private static void generateSingularityExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_singularity.json");
        if (!Files.exists(exampleFile)) {
            String json = """
                    [
                      {
                        "name": "Synthorium Singularity",
                        "item": "puremashtweaks:synthorium_block",
                        "cost": 1000,
                        "color0": "#5C3E29",
                        "color1": "#866043",
                        "add_to_puremashtweaks_singularity_tab": false,
                        "add_to_puremash_and_singularity_tag": false,
                        "enable_recipe": false,
                        "enable_item": false
                      }
                    ]""";
            Files.writeString(exampleFile, json);
        }
    }

    private static void generateDustExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_dust.json");
        if (!Files.exists(exampleFile)) {
            String json = """
                    [
                      {
                        "input": "puremashtweaks:synthorium_ingot",
                        "output": "minecraft:sand",
                        "time_cost": 100,
                        "enable_recipe": false
                      }
                    ]""";
            Files.writeString(exampleFile, json);
        }
    }

    private static void generateSynthesisShapedExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_shaped.json");
        if (!Files.exists(exampleFile)) {
            String json = """
                    [
                      {
                        "pattern": [
                          " MMSDSMM ",
                          "MSSDIDSSM",
                          "MSDIMIDSM",
                          "SDIDSDIDS",
                          "DIMSNSMID",
                          "SDIDSDIDS",
                          "MSDIMIDSM",
                          "MSSDIDSSM",
                          " MMSDSMM "
                        ],
                        "key": {
                          "M": "puremashtweaks:moldelonian_block",
                          "S": "puremashtweaks:synthorium_block",
                          "I": "puremashtweaks:synthorium_ingot",
                          "D": "minecraft:diamond_block",
                          "N": "minecraft:nether_star"
                        },
                        "result": "puremashtweaks:moldelonian_core",
                        "result_count": 1,
                        "enable_recipe": false
                      }
                    ]""";
            Files.writeString(exampleFile, json);
        }
    }

    private static void generateSynthesisShapelessExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_shapeless.json");
        if (!Files.exists(exampleFile)) {
            String json = """
                    [
                      {
                        "ingredients": [
                          "puremashtweaks:synthorium_ingot",
                          "puremashtweaks:synthorium_ingot",
                          "puremashtweaks:synthorium_ingot",
                          "puremashtweaks:synthorium_ingot",
                          "puremashtweaks:puremash_core"
                        ],
                        "result": "puremashtweaks:moldelonian_ingot",
                        "result_count": 1,
                        "enable_recipe": false
                      }
                    ]""";
            Files.writeString(exampleFile, json);
        }
    }

    private static void generateAlchemicalExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_alchemical.json");
        if (!Files.exists(exampleFile)) {
            String json = """
                    [
                      {
                        "input": "minecraft:gravel",
                        "fluid": "minecraft:water",
                        "fluid_amount": 250,
                        "tool_type": "shovel",
                        "output": "minecraft:flint",
                        "output_count": 1,
                        "double_output": false,
                        "enable_recipe": false
                      },
                      {
                        "input": "minecraft:raw_iron",
                        "fluid": "minecraft:lava",
                        "fluid_amount": 250,
                        "tool_type": "pickaxe",
                        "output": "minecraft:iron_ingot",
                        "output_count": 1,
                        "double_output": true,
                        "enable_recipe": false
                      }
                    ]
                    """;
            Files.writeString(exampleFile, json);
        }
    }
}