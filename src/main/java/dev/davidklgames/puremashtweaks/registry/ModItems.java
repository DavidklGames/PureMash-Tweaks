package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.item.MachineSpeedUpgradeTier3Item;
import dev.davidklgames.puremashtweaks.item.MemoryCardItem;
import net.minecraft.core.registries.Registries;
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

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PureMashTweaks.MODID);

    // --- END GAME OF PUREMASH TWEAKS ---
    public static final DeferredItem<Item> PUREMASH_CORE = ITEMS.registerItem("puremash_core", properties -> new dev.davidklgames.puremashtweaks.item.PureMashCoreItem(properties.rarity(Rarity.EPIC)));
    public static final DeferredItem<BlockItem> PUREMASH_CORE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("puremash_core_block", ModBlocks.PUREMASH_CORE_BLOCK, p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BlockItem> CHUNK_LOADER_ITEM = ITEMS.registerSimpleBlockItem("chunk_loader", ModBlocks.CHUNK_LOADER);

    // --- FLUID TANKS (NON-STACKABLE) ---
    public static final DeferredItem<BlockItem> FLUID_TANK_ITEM = ITEMS.registerItem("fluid_tank", properties -> new BlockItem(ModBlocks.FLUID_TANK.get(), properties.stacksTo(1)));
    public static final DeferredItem<BlockItem> CREATIVE_FLUID_TANK_ITEM = ITEMS.registerItem("creative_fluid_tank", properties -> new BlockItem(ModBlocks.CREATIVE_FLUID_TANK.get(), properties.stacksTo(1)) {
        @Override
        public @NotNull net.minecraft.network.chat.Component getName(@NotNull ItemStack stack) {
            return net.minecraft.network.chat.Component.translatable(this.getDescriptionId()).withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x527A80)));
        }
    });

    // --- SYNTHORIUM SCRAP AND INGOT ---
    public static final DeferredItem<Item> SYNTHORIUM_SCRAP = ITEMS.registerSimpleItem("synthorium_scrap", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<Item> SYNTHORIUM_INGOT = ITEMS.registerSimpleItem("synthorium_ingot", p -> p.rarity(Rarity.COMMON));

    // --- SYNTHORIUM TOOLS ---
    public static final DeferredItem<Item> SYNTHORIUM_SWORD = ITEMS.registerSimpleItem("synthorium_sword",
            p -> p.sword(ModToolMaterials.SYNTHORIUM, 4, -2.4f));

    public static final DeferredItem<Item> SYNTHORIUM_PICKAXE = ITEMS.registerSimpleItem("synthorium_pickaxe",
            p -> p.pickaxe(ModToolMaterials.SYNTHORIUM, 1, -2.8f));

    public static final DeferredItem<Item> SYNTHORIUM_AXE = ITEMS.registerSimpleItem("synthorium_axe",
            p -> p.axe(ModToolMaterials.SYNTHORIUM, 6, -3.1f));

    public static final DeferredItem<Item> SYNTHORIUM_SHOVEL = ITEMS.registerSimpleItem("synthorium_shovel",
            p -> p.shovel(ModToolMaterials.SYNTHORIUM, 1.5f, -3.0f));

    public static final DeferredItem<Item> SYNTHORIUM_HOE = ITEMS.registerSimpleItem("synthorium_hoe",
            p -> p.hoe(ModToolMaterials.SYNTHORIUM, -3, 0.0f));

    public static final DeferredItem<Item> SYNTHORIUM_PAXEL = ITEMS.registerSimpleItem("synthorium_paxel",
            p -> p.tool(ModToolMaterials.SYNTHORIUM,
                    TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "mineable/paxel")),
                    8, -2.5f, 0.0f));

    // --- INGOTS, NUGGETS AND DUSTS ---
    public static final DeferredItem<Item> MOLDELONIAN_INGOT = ITEMS.registerSimpleItem("moldelonian_ingot", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SYNTHORIUM_DUST = ITEMS.registerSimpleItem("synthorium_dust", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<Item> SYNTHORIUM_NUGGET = ITEMS.registerSimpleItem("synthorium_nugget", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<Item> MOLDELONIAN_NUGGET = ITEMS.registerSimpleItem("moldelonian_nugget", p -> p.rarity(Rarity.COMMON));

    // --- MUSIC DISCS ---
    public static final DeferredItem<Item> MUSIC_DISC_BEYOND_THE_FINAL_STAGE = ITEMS.registerSimpleItem("music_disc_beyond_the_final_stage", p -> p.rarity(Rarity.EPIC).stacksTo(1).jukeboxPlayable(dev.davidklgames.puremashtweaks.registry.ModJukeboxSongs.BEYOND_THE_FINAL_STAGE));

    // --- CRAFTING COMPONENTS ---
    public static final DeferredItem<Item> SYNTHORIUM_ROD = ITEMS.registerSimpleItem("synthorium_rod", p -> p.rarity(Rarity.COMMON));
    public static final DeferredItem<MemoryCardItem> MEMORY_CARD = ITEMS.registerItem("memory_card", properties -> new MemoryCardItem(properties.rarity(net.minecraft.world.item.Rarity.UNCOMMON).stacksTo(1)));
    public static final DeferredItem<Item> MOLDELONIAN_CORE = ITEMS.registerItem("moldelonian_core", properties -> new dev.davidklgames.puremashtweaks.item.MoldelonianCoreItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> OVERLOAD_BOOK = ITEMS.registerSimpleItem("overload_book", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> OVERCLOCK_BOOK = ITEMS.registerSimpleItem("overclock_book", p -> p.rarity(Rarity.RARE));

    // --- BLOCK ITEMS ---
    public static final DeferredItem<BlockItem> SYNTHORIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("synthorium_block", ModBlocks.SYNTHORIUM_BLOCK);
    public static final DeferredItem<BlockItem> MOLDELONIAN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("moldelonian_block", ModBlocks.MOLDELONIAN_BLOCK);
    public static final DeferredItem<BlockItem> SYNTHESIS_TABLE_ITEM = ITEMS.registerSimpleBlockItem("synthesis_table", ModBlocks.SYNTHESIS_TABLE);
    public static final DeferredItem<BlockItem> MULTIFUNCTIONAL_COMPRESSOR_ITEM = ITEMS.registerSimpleBlockItem("multifunctional_compressor", ModBlocks.MULTIFUNCTIONAL_COMPRESSOR);
    public static final DeferredItem<BlockItem> SYNTHORIUM_DEBRIS_ITEM = ITEMS.registerSimpleBlockItem("synthorium_debris", ModBlocks.SYNTHORIUM_DEBRIS, p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> ALCHEMICAL_SYNTHESIZER_ITEM = ITEMS.registerSimpleBlockItem("alchemical_synthesizer", ModBlocks.ALCHEMICAL_SYNTHESIZER);

    // --- ARMORS ---
    public static final DeferredItem<Item> SYNTHORIUM_HELMET = ITEMS.registerSimpleItem("synthorium_helmet",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.HELMET).durability(2500));

    public static final DeferredItem<Item> SYNTHORIUM_CHESTPLATE = ITEMS.registerSimpleItem("synthorium_chestplate",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.CHESTPLATE).durability(2500));

    public static final DeferredItem<Item> SYNTHORIUM_LEGGINGS = ITEMS.registerSimpleItem("synthorium_leggings",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.LEGGINGS).durability(2500));

    public static final DeferredItem<Item> SYNTHORIUM_BOOTS = ITEMS.registerSimpleItem("synthorium_boots",
            p -> p.humanoidArmor(ModArmorMaterials.SYNTHORIUM, ArmorType.BOOTS).durability(2500));

    // --- MACHINE UPGRADES ---
    public static final DeferredItem<Item> SPEED_UPGRADE_1 = ITEMS.registerSimpleItem("speed_upgrade_1", p -> p.rarity(Rarity.RARE).stacksTo(1));
    public static final DeferredItem<Item> SPEED_UPGRADE_2 = ITEMS.registerSimpleItem("speed_upgrade_2", p -> p.rarity(Rarity.UNCOMMON).stacksTo(1));
    public static final DeferredItem<Item> SPEED_UPGRADE_3 = ITEMS.registerItem("speed_upgrade_3", properties -> new MachineSpeedUpgradeTier3Item(properties.rarity(Rarity.EPIC).stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}