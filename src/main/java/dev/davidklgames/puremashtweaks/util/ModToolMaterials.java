package dev.davidklgames.puremashtweaks.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class ModToolMaterials {
    // Incorrect block tag for Synthorium (empty = breaks all blocks)
    public static final TagKey<Block> INCORRECT_FOR_SYNTHORIUM_TOOL = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "incorrect_for_synthorium_tool")
    );

    // Incorrect block tag for Moldelonian (empty = breaks all blocks)
    public static final TagKey<Block> INCORRECT_FOR_MOLDELONIAN_TOOL = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "incorrect_for_moldelonian_tool")
    );

    public static final ToolMaterial SYNTHORIUM = new ToolMaterial(
            INCORRECT_FOR_SYNTHORIUM_TOOL,        // 1. What it does NOT break (empty = breaks all)
            3000,                                 // 2. Durability
            15.0f,                                // 3. Mining Speed (Above Netherite 9.0)
            12.0f,                                // 4. Base Material Damage Bonus (20+ Total Damage range)
            22,                                   // 5. Enchantability
            ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "synthorium_repair_items")) // 6. Repair
    );

    public static final ToolMaterial MOLDELONIAN = new ToolMaterial(
            INCORRECT_FOR_MOLDELONIAN_TOOL,         // 1. What it does NOT break (empty = breaks all)
            6500,                                   // 2. Durability
            20.0f,                                  // 3. Mining Speed
            30.0f,                                  // 4. Base Material Damage Bonus (60.0 Damage Target)
            30,                                     // 5. Enchantability
            ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "moldelonian_repair_items"))
    );
}