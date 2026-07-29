package dev.davidklgames.puremashtweaks.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class ModToolMaterials {
    // Incorrect block tag for Synthorium (left empty so it can mine everything).
    public static final TagKey<Block> INCORRECT_FOR_SYNTHORIUM_TOOL = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "incorrect_for_synthorium_tool")
    );

    public static final ToolMaterial SYNTHORIUM = new ToolMaterial(
            INCORRECT_FOR_SYNTHORIUM_TOOL,        // 1. What it does NOT break (empty tag = breaks everything).
            2500,                                 // 2. Durability
            12.0f,                                // 3. Speed
            7.5f,                                 // 4. Damage
            22,                                   // 5. Enchanting
            ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "synthorium_repair_items")) // 6. Repair
    );
}