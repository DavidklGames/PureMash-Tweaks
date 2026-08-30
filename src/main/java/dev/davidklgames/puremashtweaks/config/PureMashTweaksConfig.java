package dev.davidklgames.puremashtweaks.config;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class PureMashTweaksConfig {

    // =========================================================================
    // COMMON / SERVER CONFIG SPEC
    // =========================================================================
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    // =========================================================================
    // CLIENT CONFIG SPEC
    // =========================================================================
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        // Build Common
        final var commonPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();

        // Build Client
        final var clientPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

    public static class Common {

        // --- MACHINES ---
        public final ModConfigSpec.BooleanValue enableMachines;
        public final ModConfigSpec.IntValue compressorItemSpeed;
        public final ModConfigSpec.IntValue compressorSingularitySpeed;
        public final ModConfigSpec.IntValue compressorSingularityBaseCost;
        public final ModConfigSpec.LongValue alchemicalSynthesizerBaseEnergyCapacity;
        public final ModConfigSpec.LongValue puremashGeneratorBaseEnergyCapacity;

        // --- UPGRADES & MULTIPLIERS ---
        public final ModConfigSpec.IntValue speedUpgrade1Power;
        public final ModConfigSpec.IntValue speedUpgrade2Power;
        public final ModConfigSpec.IntValue speedUpgrade3Power;
        public final ModConfigSpec.IntValue capacityUpgrade1Multiplier;
        public final ModConfigSpec.IntValue capacityUpgrade2Multiplier;
        public final ModConfigSpec.DoubleValue duplicationUpgrade1Chance;
        public final ModConfigSpec.DoubleValue duplicationUpgrade2Chance;
        public final ModConfigSpec.BooleanValue enableDuplication;
        public final ModConfigSpec.BooleanValue enableStackProcessing;

        // --- LOGISTICS & CABLES ---
        public final ModConfigSpec.IntValue synthoriumCableTransferRate;
        public final ModConfigSpec.IntValue moldelonianCableTransferRate;
        public final ModConfigSpec.IntValue synthoriumCableFluidRate;
        public final ModConfigSpec.IntValue moldelonianCableFluidRate;
        public final ModConfigSpec.IntValue synthoriumCableItemRate;
        public final ModConfigSpec.IntValue moldelonianCableItemRate;

        // --- STORAGE & BATTERIES ---
        public final ModConfigSpec.IntValue fluidTankCapacity;
        public final ModConfigSpec.LongValue batteryBaseCapacity;
        public final ModConfigSpec.LongValue moldelonianCoreCapacity;
        public final ModConfigSpec.LongValue moldelonianCoreTransferRate;

        // --- ENCHANTMENTS & OVERLOAD ---
        public final ModConfigSpec.IntValue overloadFlightTicksLvl1;
        public final ModConfigSpec.IntValue overloadFlightTicksLvl2;
        public final ModConfigSpec.DoubleValue overloadReachBonus;
        public final ModConfigSpec.IntValue overloadBlockRange;
        public final ModConfigSpec.IntValue overloadSpeedMultiplier;
        public final ModConfigSpec.IntValue overloadSpeedLvl1_2;

        // --- RECIPES & ADVANCEMENTS ---
        public final ModConfigSpec.BooleanValue enableCreativeEssenceFallback;
        public final ModConfigSpec.BooleanValue enableAdvancements;

        public Common(ModConfigSpec.Builder builder) {

            // =========================================================================
            // MACHINES
            // =========================================================================
            builder.comment("Settings for automated machines and processing units").push("Machines");

            enableMachines = builder
                    .comment("Enable custom automated processing machines.")
                    .define("enableMachines", true);

            compressorItemSpeed = builder
                    .comment("Base ticks required for Compression & Dust crushing operations (Default: 20t = 1.0s).")
                    .defineInRange("compressorItemSpeed", 20, 1, 72000);

            compressorSingularitySpeed = builder
                    .comment("Base ticks required to condense a Singularity (Default: 40t = 2.0s).")
                    .defineInRange("compressorSingularitySpeed", 40, 1, 72000);

            compressorSingularityBaseCost = builder
                    .comment("Default item count required to form a single Singularity (Default: 1000).")
                    .defineInRange("compressorSingularityBaseCost", 1000, 1, 1000000);

            alchemicalSynthesizerBaseEnergyCapacity = builder
                    .comment("Base Forge Energy (FE) capacity for the Alchemical Synthesizer (Default: 5,000,000 FE).")
                    .defineInRange("alchemicalSynthesizerBaseEnergyCapacity", 5000000L, 10000L, Long.MAX_VALUE);

            puremashGeneratorBaseEnergyCapacity = builder
                    .comment("Base Forge Energy (FE) capacity for the PureMash Generator (Default: 400,000,000 FE).")
                    .defineInRange("puremashGeneratorBaseEnergyCapacity", 400000000L, 10000L, 2000000000L);

            builder.pop();

            // =========================================================================
            // UPGRADES
            // =========================================================================
            builder.comment("Modifiers and capabilities granted by machine upgrade cards").push("Upgrades");

            speedUpgrade1Power = builder
                    .comment("Speed power granted per Speed Upgrade Tier 1 (Default: +2).")
                    .defineInRange("speedUpgrade1Power", 2, 1, 1000);

            speedUpgrade2Power = builder
                    .comment("Speed power granted per Speed Upgrade Tier 2 (Default: +4).")
                    .defineInRange("speedUpgrade2Power", 4, 1, 1000);

            speedUpgrade3Power = builder
                    .comment("Base speed multiplier granted per Speed Upgrade Tier 3 (Default: 8).")
                    .defineInRange("speedUpgrade3Power", 8, 1, 1000);

            capacityUpgrade1Multiplier = builder
                    .comment("Energy capacity multiplier for Tier 1 Capacity Upgrade (Default: 2x).")
                    .defineInRange("capacityUpgrade1Multiplier", 2, 1, 100);

            capacityUpgrade2Multiplier = builder
                    .comment("Energy capacity multiplier for Tier 2 Capacity Upgrade (Default: 5x).")
                    .defineInRange("capacityUpgrade2Multiplier", 5, 1, 100);

            duplicationUpgrade1Chance = builder
                    .comment("Duplication chance granted by Tier 1 Duplication Upgrade (Default: 0.15 = 15%).")
                    .defineInRange("duplicationUpgrade1Chance", 0.15, 0.0, 1.0);

            duplicationUpgrade2Chance = builder
                    .comment("Duplication chance granted by Tier 2 Duplication Upgrade (Default: 0.50 = 50%).")
                    .defineInRange("duplicationUpgrade2Chance", 0.50, 0.0, 1.0);

            enableDuplication = builder
                    .comment("Enable output duplication mechanics across machines.")
                    .define("enableDuplication", true);

            enableStackProcessing = builder
                    .comment("Enable Stack Processing Upgrade (up to 64 items simultaneously per cycle).")
                    .define("enableStackProcessing", true);

            builder.pop();

            // =========================================================================
            // LOGISTICS & CABLES
            // =========================================================================
            builder.comment("Transfer rates and limits for Universal Cables").push("Logistics");

            synthoriumCableTransferRate = builder
                    .comment("Base energy transfer rate for Synthorium Universal Cable in FE/t (Default: 50,000 FE/t).")
                    .defineInRange("synthoriumCableTransferRate", 50000, 1, Integer.MAX_VALUE);

            moldelonianCableTransferRate = builder
                    .comment("Base energy transfer rate for Moldelonian Universal Cable in FE/t (Default: 100,000 FE/t).")
                    .defineInRange("moldelonianCableTransferRate", 100000, 1, Integer.MAX_VALUE);

            synthoriumCableFluidRate = builder
                    .comment("Base fluid transfer rate for Synthorium Universal Cable in mB/t (Default: 1,000 mB/t).")
                    .defineInRange("synthoriumCableFluidRate", 1000, 1, Integer.MAX_VALUE);

            moldelonianCableFluidRate = builder
                    .comment("Base fluid transfer rate for Moldelonian Universal Cable in mB/t (Default: 10,000 mB/t).")
                    .defineInRange("moldelonianCableFluidRate", 10000, 1, Integer.MAX_VALUE);

            synthoriumCableItemRate = builder
                    .comment("Base item transfer amount per extraction cycle for Synthorium Universal Cable (Default: 8 items).")
                    .defineInRange("synthoriumCableItemRate", 8, 1, 64);

            moldelonianCableItemRate = builder
                    .comment("Base item transfer amount per extraction cycle for Moldelonian Universal Cable (Default: 64 items).")
                    .defineInRange("moldelonianCableItemRate", 64, 1, 64);

            builder.pop();

            // =========================================================================
            // STORAGE & BATTERIES
            // =========================================================================
            builder.comment("Capacities for Fluid Tanks, Batteries, and Portable Cores").push("Storage");

            fluidTankCapacity = builder
                    .comment("Standard Fluid Tank internal capacity in mB (Default: 32,000 mB).")
                    .defineInRange("fluidTankCapacity", 32000, 1000, Integer.MAX_VALUE);

            batteryBaseCapacity = builder
                    .comment("Standard PureMash Energy Battery capacity in FE (Default: 50,000,000 FE).")
                    .defineInRange("batteryBaseCapacity", 50000000L, 10000L, Long.MAX_VALUE);

            moldelonianCoreCapacity = builder
                    .comment("Moldelonian Core internal stored energy capacity in FE (Default: 500,000,000 FE).")
                    .defineInRange("moldelonianCoreCapacity", 500000000L, 10000L, Long.MAX_VALUE);

            moldelonianCoreTransferRate = builder
                    .comment("Moldelonian Core passive item wireless charging transfer rate in FE/t (Default: 406,000 FE/t).")
                    .defineInRange("moldelonianCoreTransferRate", 406000L, 1L, Long.MAX_VALUE);

            builder.pop();

            // =========================================================================
            // ENCHANTMENTS & OVERLOAD
            // =========================================================================
            builder.comment("Flight durations, reach bonuses, and acceleration properties").push("Enchantments");

            overloadFlightTicksLvl1 = builder
                    .comment("Creative flight duration in ticks granted by Overload I armor set (Default: 2400t = 2.0 min).")
                    .defineInRange("overloadFlightTicksLvl1", 2400, 20, Integer.MAX_VALUE);

            overloadFlightTicksLvl2 = builder
                    .comment("Creative flight duration in ticks granted by Overload II armor set (Default: 4200t = 3.5 min).")
                    .defineInRange("overloadFlightTicksLvl2", 4200, 20, Integer.MAX_VALUE);

            overloadReachBonus = builder
                    .comment("Interaction and entity reach range added per level of the Overload enchantment (Default: 1.0).")
                    .defineInRange("overloadReachBonus", 1.0, 0.1, 10.0);

            overloadBlockRange = builder
                    .comment("Block radius accelerated by the PureMash Core Block with Overload III (Default: 2 blocks radius).")
                    .defineInRange("overloadBlockRange", 2, 1, 8);

            overloadSpeedMultiplier = builder
                    .comment("Extra processing tick rate multiplier for PureMash Core Block at Overload III (Default: 2).")
                    .defineInRange("overloadSpeedMultiplier", 2, 1, 20);

            overloadSpeedLvl1_2 = builder
                    .comment("Tick acceleration bonus for PureMash Core Block at Overload I and II (Default: 1).")
                    .defineInRange("overloadSpeedLvl1_2", 1, 1, 10);

            builder.pop();

            // =========================================================================
            // RECIPES & ADVANCEMENTS
            // =========================================================================
            builder.comment("Compatibility switches and custom advancements").push("General");

            enableCreativeEssenceFallback = builder
                    .comment("Register dynamic fallback recipe for Creative Essence (Mystical Agradditions) if no other source exists.")
                    .define("enableCreativeEssenceFallback", false);

            enableAdvancements = builder
                    .comment("Enable custom advancements and goal completion rewards.")
                    .define("enableAdvancements", true);

            builder.pop();
        }
    }

    public static class Client {

        // --- HUD SETTINGS ---
        public final ModConfigSpec.BooleanValue showOverloadFlightHud;
        public final ModConfigSpec.IntValue flightHudXOffset;
        public final ModConfigSpec.IntValue flightHudYOffset;

        // --- VISUAL HIGHLIGHTS & OUTLINES ---
        public final ModConfigSpec.BooleanValue showBoundContainerBox;
        public final ModConfigSpec.BooleanValue showOverdriveMiningPreview;
        public final ModConfigSpec.BooleanValue showFlightParticles;

        public Client(ModConfigSpec.Builder builder) {

            // =========================================================================
            // CLIENT HUD
            // =========================================================================
            builder.comment("Client-side Heads-Up Display (HUD) overlays").push("HUD");

            showOverloadFlightHud = builder
                    .comment("Render the Overload flight percentage and infinity gauge on screen.")
                    .define("showOverloadFlightHud", true);

            flightHudXOffset = builder
                    .comment("Horizontal X pixel offset for the Overload flight gauge on screen.")
                    .defineInRange("flightHudXOffset", 0, -1000, 1000);

            flightHudYOffset = builder
                    .comment("Vertical Y pixel offset for the Overload flight gauge on screen.")
                    .defineInRange("flightHudYOffset", 0, -1000, 1000);

            builder.pop();

            // =========================================================================
            // VISUAL OUTLINES & EFFECTS
            // =========================================================================
            builder.comment("Client in-world outlines, bounding boxes, and visual cues").push("Visuals");

            showBoundContainerBox = builder
                    .comment("Render the 3D lime green outline around containers bound by the Distribution Filter or Overdrive.")
                    .define("showBoundContainerBox", true);

            showOverdriveMiningPreview = builder
                    .comment("Render 3x3 and 5x5 block selection outlines when holding an Overdrive mining tool.")
                    .define("showOverdriveMiningPreview", true);

            showFlightParticles = builder
                    .comment("Spawn visual glowing particle trails beneath the player during Overload creative flight.")
                    .define("showFlightParticles", true);

            builder.pop();
        }
    }

    // =========================================================================
    // LIVE CONFIG RELOAD EVENT HANDLERS & LAZY CACHE INVALIDATION
    // =========================================================================
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.reset();
            dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper.reset();
            dev.davidklgames.puremashtweaks.api.SynthesisRecipeHelper.reset();
        }
        logConfigEvent(event.getConfig(), "loaded");
    }

    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.reset();
            dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper.reset();
            dev.davidklgames.puremashtweaks.api.SynthesisRecipeHelper.reset();
        }
        logConfigEvent(event.getConfig(), "reloaded in real-time");
    }

    private static void logConfigEvent(ModConfig config, String action) {
        if (config.getModId().equals(PureMashTweaks.MODID)) {
            PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Configuration '{}' {} successfully!", config.getFileName(), action);
        }
    }
}