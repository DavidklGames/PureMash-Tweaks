package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.item.*;
import dev.davidklgames.puremashtweaks.item.PaxelItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.util.ModToolMaterials;
import dev.davidklgames.puremashtweaks.util.ModArmorMaterials;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PureMashTweaks.MODID);

    // --- END GAME OF PUREMASH TWEAKS ---
    public static final DeferredItem<Item> PUREMASH_CORE = ITEMS.registerItem("puremash_core", properties -> new dev.davidklgames.puremashtweaks.item.PureMashCoreItem(properties.rarity(Rarity.EPIC)));
    public static final DeferredItem<BlockItem> PUREMASH_CORE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("puremash_core_block", ModBlocks.PUREMASH_CORE_BLOCK, p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BlockItem> CHUNK_LOADER_ITEM = ITEMS.registerSimpleBlockItem("chunk_loader", ModBlocks.CHUNK_LOADER);
    public static final DeferredItem<BlockItem> PUREMASH_BATTERY_ITEM = ITEMS.registerSimpleBlockItem("puremash_battery", ModBlocks.PUREMASH_BATTERY);
    public static final DeferredItem<BlockItem> CREATIVE_BATTERY_ITEM = ITEMS.registerItem("creative_battery",
            properties -> new BlockItem(ModBlocks.CREATIVE_BATTERY.get(), properties.rarity(Rarity.EPIC)) {
                @Override
                public boolean isFoil(@NotNull ItemStack stack) {
                    return true; // Brilho holográfico contínuo no inventário e GUI
                }
            }
    );
    public static final DeferredItem<BlockItem> SUSPICIOUS_END_STONE_ITEM = ITEMS.registerSimpleBlockItem("suspicious_end_stone", ModBlocks.SUSPICIOUS_END_STONE);

    // --- FLUID TANKS (NON-STACKABLE) ---
    public static final DeferredItem<BlockItem> FLUID_TANK_ITEM = ITEMS.registerItem("fluid_tank", properties -> new BlockItem(ModBlocks.FLUID_TANK.get(), properties.stacksTo(1)));
    public static final DeferredItem<BlockItem> CREATIVE_FLUID_TANK_ITEM = ITEMS.registerItem("creative_fluid_tank", properties -> new BlockItem(ModBlocks.CREATIVE_FLUID_TANK.get(), properties.rarity(Rarity.EPIC).stacksTo(1)));

    // =========================================================================
    // PUREMASH APPLES (FOOD & CONSUMABLE COMPONENTS)
    // =========================================================================
    public static final net.minecraft.world.food.FoodProperties SYNTHORIUM_APPLE_FOOD = new net.minecraft.world.food.FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(1.2F)
            .alwaysEdible()
            .build();

    public static final net.minecraft.world.item.component.Consumable SYNTHORIUM_APPLE_CONSUMABLE = net.minecraft.world.item.component.Consumable.builder()
            .consumeSeconds(1.6F)
            .onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(List.of(
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 2400, 1),       // Absorption II (2 min)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 900, 1),        // Resistance II (45s)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 400, 2),      // Regeneration III (20s)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 3600, 0),   // Fire Resistance (3 min)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SPEED, 1200, 1)             // Speed II (1 min)
            )))
            .build();

    public static final net.minecraft.world.food.FoodProperties MOLDELONIAN_APPLE_FOOD = new net.minecraft.world.food.FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(1.8F)
            .alwaysEdible()
            .build();

    public static final net.minecraft.world.item.component.Consumable MOLDELONIAN_APPLE_CONSUMABLE = net.minecraft.world.item.component.Consumable.builder()
            .consumeSeconds(1.6F)
            .onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(List.of(
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.INSTANT_HEALTH, 1, 1),       // Instant Health II (Half Life Heal)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 3600, 4),        // Absorption V (3 min - 10 extra hearts)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 700, 3),         // Resistance IV (35s - 80% damage reduction)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 600, 4),       // Regeneration V (30s)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.STRENGTH, 1800, 2),          // Strength III (1:30 min)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 6000, 0),   // Fire Resistance (5 min)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 3600, 0),      // Night Vision (3 min)
                    new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SATURATION, 100, 0)          // Saturation (5s)
            )))
            .build();

    public static final DeferredItem<Item> SYNTHORIUM_APPLE = ITEMS.registerItem("synthorium_apple",
            properties -> new Item(properties
                    .rarity(Rarity.RARE)
                    .food(SYNTHORIUM_APPLE_FOOD, SYNTHORIUM_APPLE_CONSUMABLE)
            ));

    public static final DeferredItem<Item> MOLDELONIAN_APPLE = ITEMS.registerItem("moldelonian_apple",
            properties -> new Item(properties
                    .rarity(Rarity.EPIC)
                    .food(MOLDELONIAN_APPLE_FOOD, MOLDELONIAN_APPLE_CONSUMABLE)
                    .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
            ));

    // --- DISTRIBUTION FILTER ITEM ---
    public static final DeferredItem<DistributionFilterItem> DISTRIBUTION_FILTER = ITEMS.registerItem("distribution_filter", properties -> new DistributionFilterItem(properties.rarity(Rarity.RARE)));

    // --- UNIVERSAL CABLE ITEMS ---
    public static final DeferredItem<BlockItem> SYNTHORIUM_UNIVERSAL_CABLE_ITEM = ITEMS.registerSimpleBlockItem("synthorium_universal_cable", ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE);
    public static final DeferredItem<BlockItem> MOLDELONIAN_UNIVERSAL_CABLE_ITEM = ITEMS.registerSimpleBlockItem("moldelonian_universal_cable", ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE);

    // --- SYNTHORIUM SCRAP AND INGOT ---
    public static final DeferredItem<Item> SYNTHORIUM_SCRAP = ITEMS.registerSimpleItem("synthorium_scrap", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<Item> SYNTHORIUM_INGOT = ITEMS.registerSimpleItem("synthorium_ingot", p -> p.rarity(Rarity.COMMON));

    // --- SYNTHORIUM TOOLS (20+ Damage Tier) ---
    public static final DeferredItem<Item> SYNTHORIUM_SWORD = ITEMS.registerSimpleItem("synthorium_sword",
            p -> p.sword(ModToolMaterials.SYNTHORIUM, 9.0f, -2.4f)); // Total: 22.0 Damage | 1.6 Speed

    public static final DeferredItem<Item> SYNTHORIUM_PICKAXE = ITEMS.registerSimpleItem("synthorium_pickaxe",
            p -> p.pickaxe(ModToolMaterials.SYNTHORIUM, 5.0f, -2.8f)); // Total: 18.0 Damage | 1.2 Speed

    public static final DeferredItem<Item> SYNTHORIUM_AXE = ITEMS.registerSimpleItem("synthorium_axe",
            p -> p.axe(ModToolMaterials.SYNTHORIUM, 11.0f, -3.0f)); // Total: 24.0 Damage | 1.0 Speed

    public static final DeferredItem<Item> SYNTHORIUM_SHOVEL = ITEMS.registerSimpleItem("synthorium_shovel",
            p -> p.shovel(ModToolMaterials.SYNTHORIUM, 3.5f, -3.0f)); // Total: 16.5 Damage | 1.0 Speed

    public static final DeferredItem<Item> SYNTHORIUM_HOE = ITEMS.registerSimpleItem("synthorium_hoe",
            p -> p.hoe(ModToolMaterials.SYNTHORIUM, -2.0f, 0.0f)); // Total: 11.0 Damage | 4.0 Speed

    public static final DeferredItem<Item> SYNTHORIUM_PAXEL = ITEMS.registerItem("synthorium_paxel",
            p -> new PaxelItem(p.tool(ModToolMaterials.SYNTHORIUM,
                    TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "mineable/paxel")),
                    12.0f, -2.9f, 0.0f))); // Total: 25.0 Damage | 1.1 Speed

    // --- MOLDELONIAN TOOLS (60.0 Damage Target Tier) ---
    public static final DeferredItem<Item> MOLDELONIAN_SWORD = ITEMS.registerSimpleItem("moldelonian_sword",
            p -> p.sword(ModToolMaterials.MOLDELONIAN, 29.0f, -2.2f)); // Total: 60.0 Damage | 1.8 Speed

    public static final DeferredItem<Item> MOLDELONIAN_PICKAXE = ITEMS.registerSimpleItem("moldelonian_pickaxe",
            p -> p.pickaxe(ModToolMaterials.MOLDELONIAN, 21.0f, -2.6f)); // Total: 52.0 Damage | 1.4 Speed

    public static final DeferredItem<Item> MOLDELONIAN_AXE = ITEMS.registerSimpleItem("moldelonian_axe",
            p -> p.axe(ModToolMaterials.MOLDELONIAN, 31.0f, -2.8f)); // Total: 62.0 Damage | 1.2 Speed

    public static final DeferredItem<Item> MOLDELONIAN_SHOVEL = ITEMS.registerSimpleItem("moldelonian_shovel",
            p -> p.shovel(ModToolMaterials.MOLDELONIAN, 17.0f, -2.8f)); // Total: 48.0 Damage | 1.2 Speed

    public static final DeferredItem<Item> MOLDELONIAN_HOE = ITEMS.registerSimpleItem("moldelonian_hoe",
            p -> p.hoe(ModToolMaterials.MOLDELONIAN, 2.0f, 0.0f)); // Total: 33.0 Damage | 4.0 Speed

    public static final DeferredItem<Item> MOLDELONIAN_PAXEL = ITEMS.registerItem("moldelonian_paxel",
            p -> new PaxelItem(p.tool(ModToolMaterials.MOLDELONIAN,
                    TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "mineable/paxel")),
                    34.0f, -2.7f, 0.0f))); // Total: 65.0 Damage | 1.3 Speed

    // --- SYNTHORIUM ARMOR (Durability 3000) ---
    public static final DeferredItem<Item> SYNTHORIUM_HELMET = ITEMS.registerSimpleItem("synthorium_helmet",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.HELMET).durability(3000));

    public static final DeferredItem<Item> SYNTHORIUM_CHESTPLATE = ITEMS.registerSimpleItem("synthorium_chestplate",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.CHESTPLATE).durability(3000));

    public static final DeferredItem<Item> SYNTHORIUM_LEGGINGS = ITEMS.registerSimpleItem("synthorium_leggings",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.LEGGINGS).durability(3000));

    public static final DeferredItem<Item> SYNTHORIUM_BOOTS = ITEMS.registerSimpleItem("synthorium_boots",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.BOOTS).durability(3000));

    // --- MOLDELONIAN ARMOR (Durability: 6500) ---
    public static final DeferredItem<Item> MOLDELONIAN_HELMET = ITEMS.registerSimpleItem("moldelonian_helmet",
            p -> p.humanoidArmor(ModArmorMaterials.MOLDELONIAN, ArmorType.HELMET).durability(6500));

    public static final DeferredItem<Item> MOLDELONIAN_CHESTPLATE = ITEMS.registerSimpleItem("moldelonian_chestplate",
            p -> p.humanoidArmor(ModArmorMaterials.MOLDELONIAN, ArmorType.CHESTPLATE).durability(6500));

    public static final DeferredItem<Item> MOLDELONIAN_LEGGINGS = ITEMS.registerSimpleItem("moldelonian_leggings",
            p -> p.humanoidArmor(ModArmorMaterials.MOLDELONIAN, ArmorType.LEGGINGS).durability(6500));

    public static final DeferredItem<Item> MOLDELONIAN_BOOTS = ITEMS.registerSimpleItem("moldelonian_boots",
            p -> p.humanoidArmor(ModArmorMaterials.MOLDELONIAN, ArmorType.BOOTS).durability(6500));

    // --- MOLDELONIAN SMITHING TEMPLATE ---
    public static final DeferredItem<Item> MOLDELONIAN_SMITHING_TEMPLATE = ITEMS.registerItem("moldelonian_smithing_template",
            properties -> new SmithingTemplateItem(
                    Component.translatable("item.puremashtweaks.moldelonian_smithing_template.applies_to").withStyle(ChatFormatting.BLUE),
                    Component.translatable("item.puremashtweaks.moldelonian_smithing_template.ingredients").withStyle(ChatFormatting.BLUE),
                    Component.translatable("item.puremashtweaks.moldelonian_smithing_template.base_slot_description"),
                    Component.translatable("item.puremashtweaks.moldelonian_smithing_template.additions_slot_description"),
                    List.of(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/empty_slot_synthorium_sword")),
                    List.of(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "item/empty_slot_moldelonian_ingot")),
                    properties.rarity(Rarity.EPIC)
            )
    );

    // --- PLATES ---
    public static final DeferredItem<Item> SYNTHORIUM_PLATE = ITEMS.registerSimpleItem("synthorium_plate", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<Item> MOLDELONIAN_PLATE = ITEMS.registerSimpleItem("moldelonian_plate", p -> p.rarity(Rarity.COMMON));

    // --- MOLTEN FLUID BUCKETS ---
    public static final DeferredItem<BucketItem> MOLTEN_SYNTHORIUM_BUCKET = ITEMS.registerItem("molten_synthorium_bucket",
            properties -> new BucketItem(ModFluids.MOLTEN_SYNTHORIUM_SOURCE.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BucketItem> MOLTEN_MOLDELONIAN_BUCKET = ITEMS.registerItem("molten_moldelonian_bucket",
            properties -> new BucketItem(ModFluids.MOLTEN_MOLDELONIAN_SOURCE.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    // --- INGOTS, NUGGETS AND DUSTS ---
    public static final DeferredItem<Item> MOLDELONIAN_INGOT = ITEMS.registerSimpleItem("moldelonian_ingot", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SYNTHORIUM_DUST = ITEMS.registerSimpleItem("synthorium_dust", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<Item> MOLDELONIAN_DUST = ITEMS.registerSimpleItem("moldelonian_dust", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SYNTHORIUM_NUGGET = ITEMS.registerSimpleItem("synthorium_nugget", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<Item> MOLDELONIAN_NUGGET = ITEMS.registerSimpleItem("moldelonian_nugget", p -> p.rarity(Rarity.COMMON));

    // --- MUSIC DISCS ---
    public static final DeferredItem<Item> MUSIC_DISC_BEYOND_THE_FINAL_STAGE = ITEMS.registerSimpleItem("music_disc_beyond_the_final_stage", p -> p.rarity(Rarity.EPIC).stacksTo(1).jukeboxPlayable(ModJukeboxSongs.BEYOND_THE_FINAL_STAGE));
    public static final DeferredItem<Item> MUSIC_DISC_NEW_HORIZONS = ITEMS.registerSimpleItem("music_disc_new_horizons", p -> p.rarity(Rarity.EPIC).stacksTo(1).jukeboxPlayable(ModJukeboxSongs.NEW_HORIZONS));

    // --- CRAFTING COMPONENTS ---
    public static final DeferredItem<Item> SYNTHORIUM_ROD = ITEMS.registerSimpleItem("synthorium_rod", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<MemoryCardItem> MEMORY_CARD = ITEMS.registerItem("memory_card", properties -> new MemoryCardItem(properties.rarity(Rarity.UNCOMMON).stacksTo(64)));
    public static final DeferredItem<Item> MOLDELONIAN_CORE = ITEMS.registerItem("moldelonian_core", properties -> new dev.davidklgames.puremashtweaks.item.MoldelonianCoreItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> OVERLOAD_BOOK = ITEMS.registerSimpleItem("overload_book", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> OVERCLOCK_BOOK = ITEMS.registerSimpleItem("overclock_book", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> OVERDRIVE_BOOK = ITEMS.registerSimpleItem("overdrive_book", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<ConfigurationWrenchItem> CONFIGURATION_WRENCH = ITEMS.registerItem("configuration_wrench", properties -> new ConfigurationWrenchItem(properties.rarity(Rarity.UNCOMMON)));

    // --- BLOCK ITEMS ---
    public static final DeferredItem<BlockItem> SYNTHORIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("synthorium_block", ModBlocks.SYNTHORIUM_BLOCK);
    public static final DeferredItem<BlockItem> MOLDELONIAN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("moldelonian_block", ModBlocks.MOLDELONIAN_BLOCK);
    public static final DeferredItem<BlockItem> SYNTHESIS_TABLE_ITEM = ITEMS.registerSimpleBlockItem("synthesis_table", ModBlocks.SYNTHESIS_TABLE);
    public static final DeferredItem<BlockItem> MULTIFUNCTIONAL_COMPRESSOR_ITEM = ITEMS.registerSimpleBlockItem("multifunctional_compressor", ModBlocks.MULTIFUNCTIONAL_COMPRESSOR);
    public static final DeferredItem<BlockItem> SYNTHORIUM_DEBRIS_ITEM = ITEMS.registerSimpleBlockItem("synthorium_debris", ModBlocks.SYNTHORIUM_DEBRIS, p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> ALCHEMICAL_SYNTHESIZER_ITEM = ITEMS.registerSimpleBlockItem("alchemical_synthesizer", ModBlocks.ALCHEMICAL_SYNTHESIZER);
    public static final DeferredItem<BlockItem> PUREMASH_GENERATOR_ITEM = ITEMS.registerSimpleBlockItem("puremash_generator", ModBlocks.PUREMASH_GENERATOR);

    // --- MACHINE UPGRADES ---
    public static final DeferredItem<Item> SPEED_UPGRADE_1 = ITEMS.registerSimpleItem("speed_upgrade_1", p -> p.rarity(Rarity.RARE).stacksTo(64));
    public static final DeferredItem<Item> SPEED_UPGRADE_2 = ITEMS.registerSimpleItem("speed_upgrade_2", p -> p.rarity(Rarity.UNCOMMON).stacksTo(64));
    public static final DeferredItem<Item> SPEED_UPGRADE_3 = ITEMS.registerItem("speed_upgrade_3", properties -> new SpeedUpgradeTier3Item(properties.rarity(Rarity.EPIC).stacksTo(64)));

    public static final DeferredItem<Item> CAPACITY_UPGRADE_1 = ITEMS.registerSimpleItem("capacity_upgrade_1", p -> p.rarity(Rarity.RARE).stacksTo(64));
    public static final DeferredItem<Item> CAPACITY_UPGRADE_2 = ITEMS.registerItem("capacity_upgrade_2", properties -> new CapacityUpgradeTier2Item(properties.rarity(Rarity.EPIC).stacksTo(64)));
    public static final DeferredItem<Item> DUPLICATION_UPGRADE_1 = ITEMS.registerSimpleItem("duplication_upgrade_1", p -> p.rarity(Rarity.RARE).stacksTo(64));
    public static final DeferredItem<Item> DUPLICATION_UPGRADE_2 = ITEMS.registerItem("duplication_upgrade_2", properties -> new DuplicationUpgradeTier2Item(properties.rarity(Rarity.EPIC).stacksTo(64)));

    public static final DeferredItem<Item> STACK_PROCESSING_UPGRADE = ITEMS.registerItem("stack_processing_upgrade", properties -> new StackProcessingUpgradeItem(properties.rarity(Rarity.EPIC).stacksTo(64)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}