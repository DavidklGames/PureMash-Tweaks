package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.item.ColorSingularityItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModSingularities {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PureMashTweaks.MODID);

    private static final Map<String, String> DYNAMIC_NAMES = new HashMap<>();
    public static final List<DeferredItem<ColorSingularityItem>> REGISTERED_SINGULARITIES = new ArrayList<>();

    private static boolean isDataGen() {
        String target = System.getProperty("launch.target");
        if (target != null && target.toLowerCase().contains("data")) return true;
        if ("true".equalsIgnoreCase(System.getProperty("fml.datagen"))) return true;
        if ("true".equalsIgnoreCase(System.getProperty("neoforge.data"))) return true;

        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.toLowerCase().contains("data")) return true;
        }
        return false;
    }

    private static boolean isModLoaded(String... modIds) {
        if (isDataGen()) return true;
        if (modIds == null || modIds.length == 0) return true;

        for (String modId : modIds) {
            if (modId != null && !modId.equalsIgnoreCase("none") && ModList.get().isLoaded(modId)) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable DeferredItem<ColorSingularityItem> registerSingularity(String name, int color0, int color1, String... requiredModIds) {
        if (requiredModIds.length > 0 && !isModLoaded(requiredModIds)) {
            return null;
        }

        String customDisplayName = DYNAMIC_NAMES.get(name);

        DeferredItem<ColorSingularityItem> item = ITEMS.registerItem(name, properties -> new ColorSingularityItem(
                properties.rarity(Rarity.UNCOMMON).stacksTo(64),
                color0,
                color1,
                customDisplayName
        ));
        REGISTERED_SINGULARITIES.add(item);
        return item;
    }

    // =========================================================================
    // 1. VANILLA & PUREMASH SINGULARITIES
    // =========================================================================
    public static final DeferredItem<ColorSingularityItem> COAL_SINGULARITY =
            registerSingularity("coal_singularity", 0xFF1C1C1C, 0xFF3C3C3C);

    public static final DeferredItem<ColorSingularityItem> COPPER_SINGULARITY =
            registerSingularity("copper_singularity", 0xFFC15A39, 0xFFE0734D);

    public static final DeferredItem<ColorSingularityItem> IRON_SINGULARITY =
            registerSingularity("iron_singularity", 0xFFAFAF9F, 0xFFD8D8D8);

    public static final DeferredItem<ColorSingularityItem> GOLD_SINGULARITY =
            registerSingularity("gold_singularity", 0xFFE6C41E, 0xFFFDF55F);

    public static final DeferredItem<ColorSingularityItem> DIAMOND_SINGULARITY =
            registerSingularity("diamond_singularity", 0xFF37C3C0, 0xFF5DF3EF);

    public static final DeferredItem<ColorSingularityItem> REDSTONE_SINGULARITY =
            registerSingularity("redstone_singularity", 0xFF9E0A0A, 0xFFFF2222);

    public static final DeferredItem<ColorSingularityItem> LAPIS_SINGULARITY =
            registerSingularity("lapis_singularity", 0xFF1D307A, 0xFF1044A5);

    public static final DeferredItem<ColorSingularityItem> EMERALD_SINGULARITY =
            registerSingularity("emerald_singularity", 0xFF0A9E41, 0xFF17DD62);

    public static final DeferredItem<ColorSingularityItem> AMETHYST_SINGULARITY =
            registerSingularity("amethyst_singularity", 0xFF8D58D0, 0xFFC890F0);

    public static final DeferredItem<ColorSingularityItem> QUARTZ_SINGULARITY =
            registerSingularity("quartz_singularity", 0xFFC3C3BE, 0xFFE6E6E1);

    public static final DeferredItem<ColorSingularityItem> GLOWSTONE_SINGULARITY =
            registerSingularity("glowstone_singularity", 0xFFBFA01F, 0xFFFEE64A);

    public static final DeferredItem<ColorSingularityItem> NETHERITE_SINGULARITY =
            registerSingularity("netherite_singularity", 0xFF1B1B1B, 0xFF3E3030);

    public static final DeferredItem<ColorSingularityItem> SYNTHORIUM_SINGULARITY =
            registerSingularity("synthorium_singularity", -16728126, -16712705);

    public static final DeferredItem<ColorSingularityItem> SCULK_SINGULARITY =
            registerSingularity("sculk_singularity", -16770247, -16768702);

    public static final DeferredItem<ColorSingularityItem> SLIME_SINGULARITY =
            registerSingularity("slime_singularity", 0xFF519448, 0xFF7BC96C);

    public static final DeferredItem<ColorSingularityItem> ENDER_PEARL_SINGULARITY =
            registerSingularity("ender_pearl_singularity", 0xFF0A3E39, 0xFF105E52);

    public static final DeferredItem<ColorSingularityItem> SUGAR_SINGULARITY =
            registerSingularity("sugar_singularity", 0xFFD6D6D6, 0xFFFFFFFF);

    public static final DeferredItem<ColorSingularityItem> GUNPOWDER_SINGULARITY =
            registerSingularity("gunpowder_singularity", 0xFF4A4D4D, 0xFF7D7D7D);

    public static final DeferredItem<ColorSingularityItem> CLAY_SINGULARITY =
            registerSingularity("clay_singularity", 0xFF929094, 0xFFA1AAB6);

    public static final DeferredItem<ColorSingularityItem> NETHER_STAR_SINGULARITY =
            registerSingularity("nether_star_singularity", -7944731, -4198401);

    // =========================================================================
    // 2. CONDITIONAL MODDED SINGULARITIES
    // =========================================================================
    public static final @Nullable DeferredItem<ColorSingularityItem> INFERIUM_SINGULARITY =
            registerSingularity("inferium_singularity", -11245056, -8347648, "mysticalagriculture");

    public static final @Nullable DeferredItem<ColorSingularityItem> PRUDENTIUM_SINGULARITY =
            registerSingularity("prudentium_singularity", -16751070, -16735434, "mysticalagriculture");

    public static final @Nullable DeferredItem<ColorSingularityItem> TERTIUM_SINGULARITY =
            registerSingularity("tertium_singularity", -5093632, -39424, "mysticalagriculture");

    public static final @Nullable DeferredItem<ColorSingularityItem> IMPERIUM_SINGULARITY =
            registerSingularity("imperium_singularity", -16755286, -16744965, "mysticalagriculture");

    public static final @Nullable DeferredItem<ColorSingularityItem> SUPREMIUM_SINGULARITY =
            registerSingularity("supremium_singularity", -7733248, -2949120, "mysticalagriculture");

    public static final @Nullable DeferredItem<ColorSingularityItem> INSANIUM_SINGULARITY =
            registerSingularity("insanium_singularity", -11927453, -9240423, "mysticalagradditions");

    public static final @Nullable DeferredItem<ColorSingularityItem> DEORUM_SINGULARITY =
            registerSingularity("deorum_singularity", -5081019, -1390989, "forbidden_arcanus");

    public static final @Nullable DeferredItem<ColorSingularityItem> QUANTUM_ALLOY_SINGULARITY =
            registerSingularity("quantum_alloy_singularity", -8704066, -6723892, "advancedae", "advanced_ae");

    public static final @Nullable DeferredItem<ColorSingularityItem> CERTUS_QUARTZ_SINGULARITY =
            registerSingularity("certus_quartz_singularity", -10774596, -7944731, "ae2", "appliedenergistics2");

    public static final @Nullable DeferredItem<ColorSingularityItem> NETHERITE_IRON_SINGULARITY =
            registerSingularity("netherite_iron_singularity", -9079435, -2039584, "advancednetherite");

    public static final @Nullable DeferredItem<ColorSingularityItem> NETHERITE_GOLD_SINGULARITY =
            registerSingularity("netherite_gold_singularity", -4684277, -7911, "advancednetherite");

    public static final @Nullable DeferredItem<ColorSingularityItem> NETHERITE_EMERALD_SINGULARITY =
            registerSingularity("netherite_emerald_singularity", -15237097, -11468976, "advancednetherite");

    public static final @Nullable DeferredItem<ColorSingularityItem> NETHERITE_DIAMOND_SINGULARITY =
            registerSingularity("netherite_diamond_singularity", -13789771, -10690049, "advancednetherite");

    // =========================================================================
    // 3. COSMIC SINGULARITY
    // =========================================================================
    public static final DeferredItem<ColorSingularityItem> COSMIC_SINGULARITY =
            ITEMS.registerItem("cosmic_singularity", properties -> new ColorSingularityItem(
                    properties.rarity(Rarity.EPIC).stacksTo(64),
                    0xFFFFFFFF,
                    0xFFFFFFFF
            ));

    private static void registerDynamicSingularities() {
        Path configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("PureMash Tweaks/compressor_recipes/singularity");
        if (!Files.exists(configDir)) {
            return;
        }

        try (var stream = Files.list(configDir)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                File file = path.toFile();
                try (FileReader reader = new FileReader(file)) {
                    com.google.gson.JsonArray array = new com.google.gson.Gson().fromJson(reader, com.google.gson.JsonArray.class);
                    if (array != null) {
                        for (com.google.gson.JsonElement element : array) {
                            com.google.gson.JsonObject obj = element.getAsJsonObject();

                            boolean enableItem = !obj.has("enable_item") || obj.get("enable_item").getAsBoolean();
                            if (!enableItem) continue;

                            String rawName = obj.get("name").getAsString();
                            String name = rawName.toLowerCase()
                                    .replace(" ", "_")
                                    .replace("'", "")
                                    .replace("-", "_");

                            boolean alreadyRegistered = REGISTERED_SINGULARITIES.stream()
                                    .anyMatch(holder -> holder.getId().getPath().equals(name));
                            if (alreadyRegistered) continue;

                            String color0Str = obj.has("color0") ? obj.get("color0").getAsString() : "#FFFFFF";
                            String color1Str = obj.has("color1") ? obj.get("color1").getAsString() : "#FFFFFF";

                            int color0 = parseColor(color0Str);
                            int color1 = parseColor(color1Str);

                            DYNAMIC_NAMES.put(name, rawName);
                            registerSingularity(name, color0, color1);
                        }
                    }
                } catch (Exception e) {
                    PureMashTweaks.LOGGER.error("[PureMash]: Failed to parse dynamic singularity file: {}", path, e);
                }
            });
        } catch (Exception e) {
            PureMashTweaks.LOGGER.error("[PureMash]: Failed to list dynamic singularities directory: {}", configDir, e);
        }
    }

    private static int parseColor(String colorStr) {
        try {
            if (colorStr.startsWith("#")) {
                return (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000;
            }
            return Integer.decode(colorStr);
        } catch (Exception e) {
            return 0xFFFFFFFF;
        }
    }

    public static void register(IEventBus eventBus) {
        registerDynamicSingularities();
        REGISTERED_SINGULARITIES.addFirst(COSMIC_SINGULARITY);
        ITEMS.register(eventBus);
    }
}