package dev.davidklgames.puremashtweaks.config;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.neoforged.fml.loading.FMLPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("CallToPrintStackTrace")
public class ModRecipeConfigGenerator {

    public static void init() {
        Path mainConfigDir = FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks");

        Path compressorFolder = mainConfigDir.resolve("compressor_recipes/compressor");
        Path singularityFolder = mainConfigDir.resolve("compressor_recipes/singularity");
        Path dustFolder = mainConfigDir.resolve("compressor_recipes/dust");

        Path synthesisShapedFolder = mainConfigDir.resolve("synthesis_recipes/shaped");
        Path synthesisShapelessFolder = mainConfigDir.resolve("synthesis_recipes/shapeless");

        Path alchemicalFolder = mainConfigDir.resolve("alchemical_recipes");

        try {
            if (!Files.exists(compressorFolder)) Files.createDirectories(compressorFolder);
            if (!Files.exists(singularityFolder)) Files.createDirectories(singularityFolder);
            if (!Files.exists(dustFolder)) Files.createDirectories(dustFolder);
            if (!Files.exists(synthesisShapedFolder)) Files.createDirectories(synthesisShapedFolder);
            if (!Files.exists(synthesisShapelessFolder)) Files.createDirectories(synthesisShapelessFolder);
            if (!Files.exists(alchemicalFolder)) Files.createDirectories(alchemicalFolder);

            generateCompressorExample(compressorFolder);
            generateSingularityExample(singularityFolder);
            generateDustExample(dustFolder);

            generateSynthesisShapedExample(synthesisShapedFolder);
            generateSynthesisShapelessExample(synthesisShapelessFolder);

            generateAlchemicalExample(alchemicalFolder);

            dev.davidklgames.puremashtweaks.util.PureMashUtility.copyReadmes();

        } catch (IOException e) {
            e.printStackTrace();
        }
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Custom recipe configuration folders and default templates initialized.");
    }

    private static void writeTemplateIfChanged(Path file, String content) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(file, content);
            return;
        }

        String existing = Files.readString(file);
        if (!existing.equals(content)) {
            Files.writeString(file, content);
            PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Updated outdated recipe template: {}", file.getFileName());
        }
    }

    private static void generateCompressorExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_compress.json");
        String json = """
                [
                  {
                    "input": "puremashtweaks:synthorium_ingot",
                    "input_count": 9,
                    "output": "puremashtweaks:synthorium_block",
                    "time_cost": 20,
                    "enable_recipe": false
                  }
                ]""";
        writeTemplateIfChanged(exampleFile, json);
    }

    private static void generateSingularityExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_singularity.json");
        String json = """
                [
                  {
                    "name": "Synthorium Singularity",
                    "item": "puremashtweaks:synthorium_block",
                    "cost": 1000,
                    "color0": "#101010",
                    "color1": "#00FFFF",
                    "enable_recipe": false,
                    "enable_item": false
                  }
                ]""";
        writeTemplateIfChanged(exampleFile, json);
    }

    private static void generateDustExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_dust.json");
        String json = """
                [
                  {
                    "input": "puremashtweaks:synthorium_ingot",
                    "output": "puremashtweaks:synthorium_dust",
                    "time_cost": 20,
                    "enable_recipe": false
                  }
                ]""";
        writeTemplateIfChanged(exampleFile, json);
    }

    private static void generateSynthesisShapedExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_shaped.json");
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
        writeTemplateIfChanged(exampleFile, json);
    }

    private static void generateSynthesisShapelessExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_shapeless.json");
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
        writeTemplateIfChanged(exampleFile, json);
    }

    private static void generateAlchemicalExample(Path folder) throws IOException {
        Path exampleFile = folder.resolve("example_alchemical.json");
        String json = """
                [
                  {
                    "input": "minecraft:gravel",
                    "fluid": "minecraft:water",
                    "fluid_amount": 250,
                    "tool_type": "shovel",
                    "output": "minecraft:flint",
                    "output_count": 1,
                    "time": 20,
                    "energy": 100,
                    "double_output": false,
                    "enable_recipe": false
                  },
                  {
                    "input": "minecraft:raw_iron",
                    "fluid": "minecraft:lava",
                    "fluid_amount": 250,
                    "tool_type": "pickaxe",
                    "output": "minecraft:iron_ingot",
                    "output_count": 2,
                    "time": 30,
                    "energy": 150,
                    "double_output": true,
                    "enable_recipe": false
                  }
                ]
                """;
        writeTemplateIfChanged(exampleFile, json);
    }
}