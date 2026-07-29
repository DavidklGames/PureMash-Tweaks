package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class ModItemTagProvider extends IntrinsicHolderTagsProvider<Item> {

    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, Registries.ITEM, lookupProvider, i -> i.builtInRegistryHolder().key(), PureMashTweaks.MODID);
    }

    private TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        // Repair Tag
        this.tag(ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "synthorium_repair_items")))
                .add(ModItems.SYNTHORIUM_INGOT.get());

        // =========================================================================
        // VANILLA MINECRAFT TOOL AND ARMOR CATEGORIES
        // =========================================================================
        this.tag(ItemTags.PICKAXES).add(ModItems.SYNTHORIUM_PICKAXE.get(), ModItems.SYNTHORIUM_PAXEL.get());
        this.tag(ItemTags.AXES).add(ModItems.SYNTHORIUM_AXE.get(), ModItems.SYNTHORIUM_PAXEL.get());
        this.tag(ItemTags.SHOVELS).add(ModItems.SYNTHORIUM_SHOVEL.get(), ModItems.SYNTHORIUM_PAXEL.get());
        this.tag(ItemTags.SWORDS).add(ModItems.SYNTHORIUM_SWORD.get());
        this.tag(ItemTags.HOES).add(ModItems.SYNTHORIUM_HOE.get());

        this.tag(ItemTags.HEAD_ARMOR).add(ModItems.SYNTHORIUM_HELMET.get());
        this.tag(ItemTags.CHEST_ARMOR).add(ModItems.SYNTHORIUM_CHESTPLATE.get());
        this.tag(ItemTags.LEG_ARMOR).add(ModItems.SYNTHORIUM_LEGGINGS.get());
        this.tag(ItemTags.FOOT_ARMOR).add(ModItems.SYNTHORIUM_BOOTS.get());

        // Allows Synthorium Armor to receive Armor Trims at the Smithing Table.
        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.SYNTHORIUM_HELMET.get())
                .add(ModItems.SYNTHORIUM_CHESTPLATE.get())
                .add(ModItems.SYNTHORIUM_LEGGINGS.get())
                .add(ModItems.SYNTHORIUM_BOOTS.get());

        this.tag(TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("music_discs"))).add(ModItems.MUSIC_DISC_BEYOND_THE_FINAL_STAGE.get());

        // =========================================================================
        // Singularities Tags (NATIVE AND DYNAMIC)
        // =========================================================================

        // Unified Tag: c:puremash/singularity
        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> puremashSingularityTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "puremash/singularity")
        );

        // Default Unified Tag c:singularities.
        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> standardSingularitiesTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "singularities")
        );

        var puremashGroup = this.tag(puremashSingularityTag);
        var standardGroup = this.tag(standardSingularitiesTag);

        // Scans and adds all registered singularities (both static and dynamic via JSON).
        for (var itemHolder : dev.davidklgames.puremashtweaks.registry.ModSingularities.REGISTERED_SINGULARITIES) {
            if (itemHolder == dev.davidklgames.puremashtweaks.registry.ModSingularities.COSMIC_SINGULARITY) {
                continue;
            }

            puremashGroup.add(itemHolder.get());
            standardGroup.add(itemHolder.get());

            // --- Dynamic generation of individual tags for the Cosmic Singularity recipe ---
            String pathName = itemHolder.getId().getPath();
            // Removes the "_singularity" suffix to maintain the standard (e.g., "coal_singularity" -> "coal")
            String cleanName = pathName.replace("_singularity", "");

            net.minecraft.tags.TagKey<Item> individualTag = net.minecraft.tags.TagKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("c", "singularities/" + cleanName)
            );
            this.tag(individualTag).add(itemHolder.get());
        }

        // --- EMPTY COMPATIBLE TAGS FOR CONDITIONAL SINGULARITIES TO PREVENT RECIPE LOADING FAILURES ---
        String[] optionalSingularities = new String[] {
                "inferium", "prudentium", "tertium", "imperium", "supremium", "insanium",
                "deorum", "quantum_alloy", "certus_quartz",
                "netherite_iron", "netherite_gold", "netherite_emerald", "netherite_diamond"
        };
        for (String name : optionalSingularities) {
            net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.TagKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("c", "singularities/" + name)
            );
            this.tag(tagKey); // Declare the tag so that the JSON file is physically generated, even if it is empty!
        }

        // =========================================================================
        // Unified global tags in the new "c:" standard of version 26.1.2
        // =========================================================================

        // 1. Ingots (c:ingots e c:ingots/metal_name)
        this.tag(commonTag("ingots"))
                .add(ModItems.SYNTHORIUM_INGOT.get())
                .add(ModItems.MOLDELONIAN_INGOT.get());

        this.tag(commonTag("ingots/synthorium")).add(ModItems.SYNTHORIUM_INGOT.get());
        this.tag(commonTag("ingots/moldelonian")).add(ModItems.MOLDELONIAN_INGOT.get());

        // 2. Nuggets (c:nuggets e c:nuggets/metal_name)
        this.tag(commonTag("nuggets"))
                .add(ModItems.SYNTHORIUM_NUGGET.get())
                .add(ModItems.MOLDELONIAN_NUGGET.get());

        this.tag(commonTag("nuggets/synthorium")).add(ModItems.SYNTHORIUM_NUGGET.get());
        this.tag(commonTag("nuggets/moldelonian")).add(ModItems.MOLDELONIAN_NUGGET.get());

        // 3. Dusts (c:dusts e c:dusts/metal_name)
        this.tag(commonTag("dusts")).add(ModItems.SYNTHORIUM_DUST.get());
        this.tag(commonTag("dusts/synthorium")).add(ModItems.SYNTHORIUM_DUST.get());

        // 4. Scraps (c:scraps e c:scraps/metal_name)
        this.tag(commonTag("scraps")).add(ModItems.SYNTHORIUM_SCRAP.get());
        this.tag(commonTag("scraps/synthorium")).add(ModItems.SYNTHORIUM_SCRAP.get());

        // 5. Rods (c:rods e c:rods/metal_name)
        this.tag(commonTag("rods")).add(ModItems.SYNTHORIUM_ROD.get());
        this.tag(commonTag("rods/synthorium")).add(ModItems.SYNTHORIUM_ROD.get());

        // 6. Ores (c:ores e c:ores/metal_name)
        this.tag(commonTag("ores")).add(ModBlocks.SYNTHORIUM_DEBRIS.get().asItem());
        this.tag(commonTag("ores/synthorium")).add(ModBlocks.SYNTHORIUM_DEBRIS.get().asItem());

        // 7. Storage Blocks (c:storage_blocks)
        this.tag(commonTag("storage_blocks"))
                .add(ModBlocks.SYNTHORIUM_BLOCK.get().asItem())
                .add(ModBlocks.MOLDELONIAN_BLOCK.get().asItem())
                .add(ModBlocks.PUREMASH_CORE_BLOCK.get().asItem());

        this.tag(commonTag("storage_blocks/synthorium")).add(ModBlocks.SYNTHORIUM_BLOCK.get().asItem());
        this.tag(commonTag("storage_blocks/moldelonian")).add(ModBlocks.MOLDELONIAN_BLOCK.get().asItem());
        this.tag(commonTag("storage_blocks/puremash_core")).add(ModBlocks.PUREMASH_CORE_BLOCK.get().asItem());

        // Creating the custom tag and adding only the allowed items in Datagen!
        var overloadEnchantableTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "enchantable/overload")
        );

        this.tag(overloadEnchantableTag)
                // 1. UNIVERSAL ACCEPTANCE: Includes all mining tools and weapons (Vanilla and Mods).
                .addTag(net.minecraft.tags.ItemTags.MINING_ENCHANTABLE)
                .addTag(net.minecraft.tags.ItemTags.WEAPON_ENCHANTABLE)

                // 2. EXCLUSIVE: Only PureMash Tweaks armor can receive this (blocks vanilla and other mods).
                .add(ModItems.SYNTHORIUM_HELMET.get())
                .add(ModItems.SYNTHORIUM_CHESTPLATE.get())
                .add(ModItems.SYNTHORIUM_LEGGINGS.get())
                .add(ModItems.SYNTHORIUM_BOOTS.get())

                // 3. EXCLUSIVE: Only the PureMash Core Block can receive the enchantment.
                .add(ModItems.PUREMASH_CORE_BLOCK_ITEM.get());

        // --- EXCLUSIVE: Only the PureMash Core Block can receive the enchantment ---
        String[] optionalIngots = new String[] {
                "steel", "bronze", "brass", "electrum", "invar", "constantan", "iridium", "titanium", "tungsten", "nickel", "platinum", "zync", "silver", "thorium", "lead",
                "cobalt", "ardite", "manyullyn", "refined_glowstone", "refined_obsidian", "rose_gold", "aluminum", "tin", "osmium", "electrum", "enderium", "invar", "uranium",
                "enderium", "lumium", "signalum", "deorum", "quantum_alloy", "prosperity", "inferium", "prudentium", "tertium", "imperium", "supremium"
        };
        for (String metal : optionalIngots) {
            this.tag(commonTag("ingots/" + metal));
        }

        // =========================================================================
        // COSMIC SINGULARITY TAG
        // =========================================================================
        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> supremeSingularityTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "puremash/supreme_singularity")
        );

        this.tag(supremeSingularityTag).add(ModSingularities.COSMIC_SINGULARITY.get());
    }
}