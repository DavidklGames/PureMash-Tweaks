package dev.davidklgames.puremashtweaks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PureMashTweaksConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_MACHINES;
    public static final ModConfigSpec.IntValue OVERLOAD_BLOCK_RANGE;
    public static final ModConfigSpec.IntValue OVERLOAD_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OVERLOAD_REACH_BONUS;

    // MACHINE VARIABLES
    public static final ModConfigSpec.IntValue MACHINE_SPEED_UPGRADE_1_POWER;
    public static final ModConfigSpec.IntValue MACHINE_SPEED_UPGRADE_2_POWER;
    public static final ModConfigSpec.IntValue MACHINE_SPEED_UPGRADE_3_POWER;
    public static final ModConfigSpec.DoubleValue MACHINE_UPGRADE_2_DUPLICATION_CHANCE;
    public static final ModConfigSpec.DoubleValue MACHINE_UPGRADE_3_DUPLICATION_CHANCE;
    public static final ModConfigSpec.BooleanValue ENABLE_DUPLICATION;

    // FLIGHT SYSTEM CONFIGURATION
    public static final ModConfigSpec.IntValue OVERLOAD_FLIGHT_TICKS_LVL1;
    public static final ModConfigSpec.IntValue OVERLOAD_FLIGHT_TICKS_LVL2;

    // MULTIFUNCTIONAL COMPRESSOR CONFIGURATION
    public static final ModConfigSpec.IntValue COMPRESSOR_SPEED_ITEMS;
    public static final ModConfigSpec.IntValue COMPRESSOR_SPEED_SINGULARITY;
    public static final ModConfigSpec.IntValue COMPRESSOR_SINGULARITY_BASE_COST;

    public static final ModConfigSpec.IntValue OVERLOAD_SPEED_LVL1_2;
    public static final ModConfigSpec.BooleanValue ENABLE_ADVANCEMENTS;

    // RECIPES SYSTEM CONFIGURATION
    public static final ModConfigSpec.BooleanValue ENABLE_CREATIVE_ESSENCE_FALLBACK;

    static {

        // =========================================================================
        // CUSTOM RECIPES & FALLBACKS
        // =========================================================================
        BUILDER.push("Recipes");

        ENABLE_CREATIVE_ESSENCE_FALLBACK = BUILDER
                .comment("Whether the dynamic fallback recipe for Creative Essence (Mystical Agradditions) should be automatically registered and injected when no other recipes are active.")
                .define("enableCreativeEssenceFallback", false);

        BUILDER.pop();

        // =========================================================================
        // AUTOMATED MACHINES
        // =========================================================================
        BUILDER.push("Machines");

        ENABLE_MACHINES = BUILDER
                .comment("Whether the custom automation machines added by this mod (such as the Alchemical Synthesizer, Synthesis Table, and Multifunctional Compressor) are enabled and functional.")
                .define("enableMachines", true);

        OVERLOAD_BLOCK_RANGE = BUILDER
                .comment("The spherical block radius affected by the PureMash Core Block's acceleration effect when enchanted with the Overload enchantment.")
                .defineInRange("overloadBlockRange", 3, 1, 16);

        OVERLOAD_SPEED_MULTIPLIER = BUILDER
                .comment("How many extra processing ticks adjacent machines gain per level of the Overload enchantment applied to the PureMash Core Block (Level III).")
                .defineInRange("overloadSpeedMultiplier", 2, 1, 10);

        BUILDER.pop();

        // =========================================================================
        // UPGRADES AND SPEED MULTIPLIERS
        // =========================================================================
        BUILDER.push("Upgrades");

        MACHINE_SPEED_UPGRADE_1_POWER = BUILDER
                .comment("The speed multiplier bonus granted to automated machines by each installed Speed Upgrade Tier 1 (Default: 2).")
                .defineInRange("machineSpeedUpgrade1Power", 2, 1, 1000);

        MACHINE_SPEED_UPGRADE_2_POWER = BUILDER
                .comment("The speed multiplier bonus granted to automated machines by each installed Speed Upgrade Tier 2 (Default: 4).")
                .defineInRange("machineSpeedUpgrade2Power", 4, 1, 1000);

        MACHINE_SPEED_UPGRADE_3_POWER = BUILDER
                .comment("The speed multiplier bonus granted to automated machines by each installed Speed Upgrade Tier 3 (Default: 8).")
                .defineInRange("machineSpeedUpgrade3Power", 8, 1, 1000);

        MACHINE_UPGRADE_2_DUPLICATION_CHANCE = BUILDER
                .comment("The extra output duplication chance granted to machines by each installed Speed Upgrade Tier 2. Defined as a decimal percentage (Default: 0.10 = 10% chance).")
                .defineInRange("machineUpgrade2DuplicationChance", 0.10, 0.0, 1.0);

        MACHINE_UPGRADE_3_DUPLICATION_CHANCE = BUILDER
                .comment("The extra output duplication chance granted to machines by each installed Speed Upgrade Tier 3. Defined as a decimal percentage (Default: 0.35 = 35% chance).")
                .defineInRange("machineUpgrade3DuplicationChance", 0.35, 0.0, 1.0);

        // Upgrade system general configurations
        ENABLE_DUPLICATION = BUILDER
                .comment("Whether the item duplication mechanic granted by Tier 2 and Tier 3 speed upgrades is enabled in machines.")
                .define("enableDuplication", true);

        BUILDER.pop();

        // =========================================================================
        // MULTIFUNCTIONAL COMPRESSOR & REVEALING ACCELERATION
        // =========================================================================
        BUILDER.push("Multifunctional Compressor");

        COMPRESSOR_SPEED_ITEMS = BUILDER
                .comment("The base processing time (in ticks) required by the Multifunctional Compressor to process standard items in Compression or Dust modes (Default: 100 ticks = 5 seconds).")
                .defineInRange("compressorSpeedItems", 100, 1, 10000);

        COMPRESSOR_SPEED_SINGULARITY = BUILDER
                .comment("The base processing time (in ticks) required by the Multifunctional Compressor to condense a Singularity (Default: 400 ticks = 20 seconds).")
                .defineInRange("compressorSpeedSingularity", 400, 1, 10000);

        COMPRESSOR_SINGULARITY_BASE_COST = BUILDER
                .comment("The base number of items required to condense a single Singularity (Default: 1000 items).")
                .defineInRange("compressorSingularityBaseCost", 1000, 1, 100000);

        OVERLOAD_SPEED_LVL1_2 = BUILDER
                .comment("The additional tick acceleration bonus applied to surrounding blocks for Overload enchantment levels I and II (Default: 1).")
                .defineInRange("overloadSpeedLvl1_2", 1, 1, 100);

        BUILDER.pop();

        // =========================================================================
        // CUSTOM ADVANCEMENTS
        // =========================================================================
        BUILDER.push("Advancements");

        ENABLE_ADVANCEMENTS = BUILDER
                .comment("Whether custom advancements and goal completions added by PureMash Tweaks are enabled and earnable in the game.")
                .define("enableAdvancements", true);

        BUILDER.pop();

        // =========================================================================
        // PHYSICAL TOOLS AND RANGE
        // =========================================================================
        BUILDER.push("Tools");

        OVERLOAD_REACH_BONUS = BUILDER
                .comment("The additional reach distance (for block interaction and entity hit ranges) granted to tools per level of the Overload enchantment (Default: 1.5 blocks/level).")
                .defineInRange("overloadReachBonus", 1.5, 0.5, 5.0);

        BUILDER.pop();

        // =========================================================================
        // ENCHANTMENTS AND TEMPORARY FLIGHT
        // =========================================================================
        BUILDER.push("Enchantments");

        OVERLOAD_FLIGHT_TICKS_LVL1 = BUILDER
                .comment("The maximum creative flight duration (in ticks) granted by the Overload I enchantment on Synthorium Armor (Default: 2400 ticks = 2 minutes).")
                .defineInRange("overloadFlightLvl1", 2400, 1, Integer.MAX_VALUE);

        OVERLOAD_FLIGHT_TICKS_LVL2 = BUILDER
                .comment("The maximum creative flight duration (in ticks) granted by the Overload II enchantment on Synthorium Armor (Default: 4200 ticks = 3.5 minutes).")
                .defineInRange("overloadFlightLvl2", 4200, 1, Integer.MAX_VALUE);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}