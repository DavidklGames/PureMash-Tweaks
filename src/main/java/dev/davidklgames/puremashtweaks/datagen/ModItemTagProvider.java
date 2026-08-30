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

        // =========================================================================
        // OVERDRIVE ENCHANTABLE TAG (UNIVERSAL: ALL TOOLS, WEAPONS & ARMORS)
        // =========================================================================
        var overdriveEnchantableTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "enchantable/overdrive")
        );

        this.tag(overdriveEnchantableTag)
                // 1. Universal tool and weapon categories (Vanilla & all mods)
                .addTag(ItemTags.SWORDS)
                .addTag(ItemTags.AXES)
                .addTag(ItemTags.HOES)
                .addTag(ItemTags.PICKAXES)
                .addTag(ItemTags.SHOVELS)
                .addTag(ItemTags.MINING_ENCHANTABLE)
                .addTag(ItemTags.WEAPON_ENCHANTABLE)
                .addTag(ItemTags.MELEE_WEAPON_ENCHANTABLE)
                .addTag(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                .addTag(ItemTags.SWEEPING_ENCHANTABLE)
                .addTag(ItemTags.DURABILITY_ENCHANTABLE)

                // 2. Universal armor categories (Vanilla & all mods)
                .addTag(ItemTags.ARMOR_ENCHANTABLE)
                .addTag(ItemTags.HEAD_ARMOR_ENCHANTABLE)
                .addTag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .addTag(ItemTags.LEG_ARMOR_ENCHANTABLE)
                .addTag(ItemTags.FOOT_ARMOR_ENCHANTABLE)

                // 3. Explicit Paxels & PureMash tools
                .add(ModItems.SYNTHORIUM_PAXEL.get())
                .add(ModItems.MOLDELONIAN_PAXEL.get());

        this.tag(ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "synthorium_repair_items")))
                .add(ModItems.SYNTHORIUM_INGOT.get());

        this.tag(ItemTags.BEACON_PAYMENT_ITEMS)
                .add(ModItems.SYNTHORIUM_INGOT.get())
                .add(ModItems.MOLDELONIAN_INGOT.get());

        // =========================================================================
        // PLATES
        // =========================================================================
        this.tag(commonTag("plates"))
                .add(ModItems.SYNTHORIUM_PLATE.get())
                .add(ModItems.MOLDELONIAN_PLATE.get());

        this.tag(commonTag("plates/synthorium")).add(ModItems.SYNTHORIUM_PLATE.get());
        this.tag(commonTag("plates/moldelonian")).add(ModItems.MOLDELONIAN_PLATE.get());

        // =========================================================================
        // CABLES ITEM TAG
        // =========================================================================
        this.tag(commonTag("cables"))
                .add(ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get().asItem())
                .add(ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get().asItem());

        // =========================================================================
        // FOODS & FRUITS
        // =========================================================================
        this.tag(commonTag("foods"))
                .add(ModItems.SYNTHORIUM_APPLE.get())
                .add(ModItems.MOLDELONIAN_APPLE.get());

        this.tag(commonTag("foods/fruit"))
                .add(ModItems.SYNTHORIUM_APPLE.get())
                .add(ModItems.MOLDELONIAN_APPLE.get());

        this.tag(commonTag("foods/synthorium")).add(ModItems.SYNTHORIUM_APPLE.get());
        this.tag(commonTag("foods/moldelonian")).add(ModItems.MOLDELONIAN_APPLE.get());

        // =========================================================================
        // VANILLA TOOL AND ARMOR CATEGORIES
        // =========================================================================
        this.tag(ItemTags.PICKAXES).add(ModItems.SYNTHORIUM_PICKAXE.get(), ModItems.SYNTHORIUM_PAXEL.get());
        this.tag(ItemTags.AXES).add(ModItems.SYNTHORIUM_AXE.get(), ModItems.SYNTHORIUM_PAXEL.get());
        this.tag(ItemTags.SHOVELS).add(ModItems.SYNTHORIUM_SHOVEL.get(), ModItems.SYNTHORIUM_PAXEL.get());
        this.tag(ItemTags.SWORDS).add(ModItems.SYNTHORIUM_SWORD.get());
        this.tag(ItemTags.HOES).add(ModItems.SYNTHORIUM_HOE.get());
        this.tag(ItemTags.PICKAXES).add(ModItems.MOLDELONIAN_PICKAXE.get(), ModItems.MOLDELONIAN_PAXEL.get());
        this.tag(ItemTags.AXES).add(ModItems.MOLDELONIAN_AXE.get(), ModItems.MOLDELONIAN_PAXEL.get());
        this.tag(ItemTags.SHOVELS).add(ModItems.MOLDELONIAN_SHOVEL.get(), ModItems.MOLDELONIAN_PAXEL.get());
        this.tag(ItemTags.SWORDS).add(ModItems.MOLDELONIAN_SWORD.get());
        this.tag(ItemTags.HOES).add(ModItems.MOLDELONIAN_HOE.get());

        this.tag(ItemTags.HEAD_ARMOR).add(ModItems.MOLDELONIAN_HELMET.get());
        this.tag(ItemTags.CHEST_ARMOR).add(ModItems.MOLDELONIAN_CHESTPLATE.get());
        this.tag(ItemTags.LEG_ARMOR).add(ModItems.MOLDELONIAN_LEGGINGS.get());
        this.tag(ItemTags.FOOT_ARMOR).add(ModItems.MOLDELONIAN_BOOTS.get());

        this.tag(ItemTags.HEAD_ARMOR).add(ModItems.SYNTHORIUM_HELMET.get());
        this.tag(ItemTags.CHEST_ARMOR).add(ModItems.SYNTHORIUM_CHESTPLATE.get());
        this.tag(ItemTags.LEG_ARMOR).add(ModItems.SYNTHORIUM_LEGGINGS.get());
        this.tag(ItemTags.FOOT_ARMOR).add(ModItems.SYNTHORIUM_BOOTS.get());

        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.SYNTHORIUM_HELMET.get())
                .add(ModItems.SYNTHORIUM_CHESTPLATE.get())
                .add(ModItems.SYNTHORIUM_LEGGINGS.get())
                .add(ModItems.SYNTHORIUM_BOOTS.get());

        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.MOLDELONIAN_HELMET.get())
                .add(ModItems.MOLDELONIAN_CHESTPLATE.get())
                .add(ModItems.MOLDELONIAN_LEGGINGS.get())
                .add(ModItems.MOLDELONIAN_BOOTS.get());

        this.tag(ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "moldelonian_repair_items")))
                .add(ModItems.MOLDELONIAN_INGOT.get());

        this.tag(TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("music_discs"))).add(ModItems.MUSIC_DISC_BEYOND_THE_FINAL_STAGE.get());
        this.tag(TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("music_discs"))).add(ModItems.MUSIC_DISC_NEW_HORIZONS.get());

        // =========================================================================
        // SINGULARITIES TAGS (SAFE OPTIONAL REFERENCES)
        // =========================================================================
        TagKey<Item> puremashSingularityTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "puremash/singularity")
        );

        TagKey<Item> standardSingularitiesTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "singularities")
        );

        var puremashGroup = this.tag(puremashSingularityTag);
        var standardGroup = this.tag(standardSingularitiesTag);

        for (var itemHolder : ModSingularities.REGISTERED_SINGULARITIES) {
            if (itemHolder == null || itemHolder == ModSingularities.COSMIC_SINGULARITY) {
                continue;
            }

            Item item = itemHolder.get();
            puremashGroup.addOptional(item);
            standardGroup.addOptional(item);

            String pathName = itemHolder.getId().getPath();
            String cleanName = pathName.replace("_singularity", "");

            TagKey<Item> individualTag = TagKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("c", "singularities/" + cleanName)
            );
            this.tag(individualTag).addOptional(item);
        }

        // Generate empty tag files for optional modded singularities to prevent JSON deserialization failures
        String[] optionalSingularities = new String[] {
                "inferium", "prudentium", "tertium", "imperium", "supremium", "insanium",
                "deorum", "quantum_alloy", "certus_quartz",
                "netherite_iron", "netherite_gold", "netherite_emerald", "netherite_diamond"
        };
        for (String name : optionalSingularities) {
            TagKey<Item> tagKey = TagKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("c", "singularities/" + name)
            );
            this.tag(tagKey);
        }

        // =========================================================================
        // UNIFIED GLOBAL TAGS (c:)
        // =========================================================================
        this.tag(commonTag("ingots"))
                .add(ModItems.SYNTHORIUM_INGOT.get())
                .add(ModItems.MOLDELONIAN_INGOT.get());

        this.tag(commonTag("ingots/synthorium")).add(ModItems.SYNTHORIUM_INGOT.get());
        this.tag(commonTag("ingots/moldelonian")).add(ModItems.MOLDELONIAN_INGOT.get());

        this.tag(commonTag("nuggets"))
                .add(ModItems.SYNTHORIUM_NUGGET.get())
                .add(ModItems.MOLDELONIAN_NUGGET.get());

        this.tag(commonTag("nuggets/synthorium")).add(ModItems.SYNTHORIUM_NUGGET.get());
        this.tag(commonTag("nuggets/moldelonian")).add(ModItems.MOLDELONIAN_NUGGET.get());

        this.tag(commonTag("dusts")).add(ModItems.SYNTHORIUM_DUST.get(), ModItems.MOLDELONIAN_DUST.get());
        this.tag(commonTag("dusts/synthorium")).add(ModItems.SYNTHORIUM_DUST.get());
        this.tag(commonTag("dusts/moldelonian")).add(ModItems.MOLDELONIAN_DUST.get());

        this.tag(commonTag("scraps")).add(ModItems.SYNTHORIUM_SCRAP.get());
        this.tag(commonTag("scraps/synthorium")).add(ModItems.SYNTHORIUM_SCRAP.get());

        this.tag(commonTag("rods")).add(ModItems.SYNTHORIUM_ROD.get());
        this.tag(commonTag("rods/synthorium")).add(ModItems.SYNTHORIUM_ROD.get());

        this.tag(commonTag("ores")).add(ModBlocks.SYNTHORIUM_DEBRIS.get().asItem());
        this.tag(commonTag("ores/synthorium")).add(ModBlocks.SYNTHORIUM_DEBRIS.get().asItem());

        this.tag(commonTag("storage_blocks"))
                .add(ModBlocks.SYNTHORIUM_BLOCK.get().asItem())
                .add(ModBlocks.MOLDELONIAN_BLOCK.get().asItem())
                .add(ModBlocks.PUREMASH_CORE_BLOCK.get().asItem());

        this.tag(commonTag("storage_blocks/synthorium")).add(ModBlocks.SYNTHORIUM_BLOCK.get().asItem());
        this.tag(commonTag("storage_blocks/moldelonian")).add(ModBlocks.MOLDELONIAN_BLOCK.get().asItem());
        this.tag(commonTag("storage_blocks/puremash_core")).add(ModBlocks.PUREMASH_CORE_BLOCK.get().asItem());

        this.tag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools")))
                .add(ModItems.CONFIGURATION_WRENCH.get());

        this.tag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/wrench")))
                .add(ModItems.CONFIGURATION_WRENCH.get());

        this.tag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "wrenches")))
                .add(ModItems.CONFIGURATION_WRENCH.get());

        var overloadEnchantableTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "enchantable/overload")
        );

        this.tag(overloadEnchantableTag)
                .addTag(ItemTags.MINING_ENCHANTABLE)
                .addTag(ItemTags.WEAPON_ENCHANTABLE)
                .add(ModItems.SYNTHORIUM_HELMET.get())
                .add(ModItems.SYNTHORIUM_CHESTPLATE.get())
                .add(ModItems.SYNTHORIUM_LEGGINGS.get())
                .add(ModItems.SYNTHORIUM_BOOTS.get())
                .add(ModItems.MOLDELONIAN_HELMET.get())
                .add(ModItems.MOLDELONIAN_CHESTPLATE.get())
                .add(ModItems.MOLDELONIAN_LEGGINGS.get())
                .add(ModItems.MOLDELONIAN_BOOTS.get())
                .add(ModItems.PUREMASH_CORE_BLOCK_ITEM.get());

        String[] optionalIngots = new String[] {
                "steel", "bronze", "brass", "electrum", "invar", "constantan", "iridium", "titanium", "tungsten", "nickel", "platinum", "zinc", "silver", "thorium", "lead",
                "cobalt", "ardite", "manyullyn", "refined_glowstone", "refined_obsidian", "rose_gold", "aluminum", "tin", "osmium", "uranium",
                "enderium", "lumium", "signalum", "deorum", "quantum_alloy", "prosperity", "inferium", "prudentium", "tertium", "imperium", "supremium"
        };
        for (String metal : optionalIngots) {
            this.tag(commonTag("ingots/" + metal));
        }

        TagKey<Item> supremeSingularityTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("c", "puremash/supreme_singularity")
        );

        this.tag(supremeSingularityTag).add(ModSingularities.COSMIC_SINGULARITY.get());
    }
}