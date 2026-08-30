package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(new MyAdvancements()));
    }

    private static class MyAdvancements implements AdvancementSubProvider {

        @Override
        public void generate(HolderLookup.@NonNull Provider registries, @NonNull Consumer<AdvancementHolder> saver) {

            // --- 1. ROOT (Simple advancement upon entering the world) ---
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(
                            ModItems.PUREMASH_CORE.get(), // Mod icon to represent the root
                            Component.translatable("advancements.puremashtweaks.root.title"),
                            Component.translatable("advancements.puremashtweaks.root.description"),
                            Identifier.fromNamespaceAndPath("puremashtweaks", "block/backgrounds/deepslate"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("tick", net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.tick())
                    .save(saver, "puremashtweaks:main/root");

            // --- 2. FIND SYNTHORIUM DEBRIS (Start of the practical journey) ---
            AdvancementHolder findDebris = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModBlocks.SYNTHORIUM_DEBRIS.get(),
                            Component.translatable("advancements.puremashtweaks.find_debris.title"),
                            Component.translatable("advancements.puremashtweaks.find_debris.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_debris", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.SYNTHORIUM_DEBRIS.get()))
                    .save(saver, "puremashtweaks:main/find_debris");

            // --- 3. OBTAIN SYNTHORIUM INGOT (Child of findDebris) ---
            AdvancementHolder getIngot = Advancement.Builder.advancement()
                    .parent(findDebris)
                    .display(
                            ModItems.SYNTHORIUM_INGOT.get(),
                            Component.translatable("advancements.puremashtweaks.get_ingot.title"),
                            Component.translatable("advancements.puremashtweaks.get_ingot.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_INGOT.get()))
                    .save(saver, "puremashtweaks:main/get_ingot");

            // --- 4. CRAFT ALL TOOLS (Child of getIngot) ---
            AdvancementHolder getTools = Advancement.Builder.advancement()
                    .parent(getIngot)
                    .display(
                            ModItems.SYNTHORIUM_PICKAXE.get(),
                            Component.translatable("advancements.puremashtweaks.get_tools.title"),
                            Component.translatable("advancements.puremashtweaks.get_tools.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_PICKAXE.get()))
                    .addCriterion("has_axe", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_AXE.get()))
                    .addCriterion("has_shovel", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_SHOVEL.get()))
                    .addCriterion("has_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_HOE.get()))
                    .addCriterion("has_sword", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_SWORD.get()))
                    .save(saver, "puremashtweaks:main/get_tools");

            // --- 5. CRAFT SYNTHORIUM PAXEL (Child of getTools) ---
            AdvancementHolder getPaxel = Advancement.Builder.advancement()
                    .parent(getTools)
                    .display(
                            ModItems.SYNTHORIUM_PAXEL.get(),
                            Component.translatable("advancements.puremashtweaks.get_paxel.title"),
                            Component.translatable("advancements.puremashtweaks.get_paxel.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_paxel", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_PAXEL.get()))
                    .save(saver, "puremashtweaks:main/get_paxel");

            // --- 6. COMPRESSOR (Child of getPaxel) ---
            AdvancementHolder getCompressor = Advancement.Builder.advancement()
                    .parent(getPaxel)
                    .display(
                            ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get(),
                            Component.translatable("advancements.puremashtweaks.get_multifunctional_compressor.description"),
                            Component.translatable("advancements.puremashtweaks.get_multifunctional_compressor.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_compressor", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()))
                    .save(saver, "puremashtweaks:main/get_compressor");

            // --- 7. CRAFT ALL ARMORS (Child of getPaxel) ---
            AdvancementHolder getArmor = Advancement.Builder.advancement()
                    .parent(getPaxel)
                    .display(
                            ModItems.SYNTHORIUM_CHESTPLATE.get(),
                            Component.translatable("advancements.puremashtweaks.get_armor.title"),
                            Component.translatable("advancements.puremashtweaks.get_armor.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_HELMET.get()))
                    .addCriterion("has_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_CHESTPLATE.get()))
                    .addCriterion("has_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_LEGGINGS.get()))
                    .addCriterion("has_boots", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SYNTHORIUM_BOOTS.get()))
                    .save(saver, "puremashtweaks:main/get_armor");

            // --- 8. FORGE MOLDELONIAN INGOT (Child of getArmor) ---
            AdvancementHolder getMoldelonianIngot = Advancement.Builder.advancement()
                    .parent(getArmor)
                    .display(
                            ModItems.MOLDELONIAN_INGOT.get(),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_ingot.title"),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_ingot.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_moldelonian", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_INGOT.get()))
                    .save(saver, "puremashtweaks:main/get_moldelonian_ingot");

            // --- 10. CRAFT SYNTHESIS TABLE (Child of getMoldelonianIngot) ---
            AdvancementHolder getSynthesisTable = Advancement.Builder.advancement()
                    .parent(getMoldelonianIngot)
                    .display(
                            ModBlocks.SYNTHESIS_TABLE.get(),
                            Component.translatable("advancements.puremashtweaks.get_synthesis_table.title"),
                            Component.translatable("advancements.puremashtweaks.get_synthesis_table.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_synthesis_table", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.SYNTHESIS_TABLE.get()))
                    .save(saver, "puremashtweaks:main/get_synthesis_table");

            // --- 9. FORGE MOLDELONIAN CORE (Child of getMoldelonianIngot) ---
            AdvancementHolder getMoldelonianCore = Advancement.Builder.advancement()
                    .parent(getMoldelonianIngot)
                    .display(
                            ModItems.MOLDELONIAN_CORE.get(),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_core.title"),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_core.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_m_core", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_CORE.get()))
                    .save(saver, "puremashtweaks:main/get_moldelonian_core");

            // --- 11. CRAFT PUREMASH CORE (Child of getMoldelonianCore) ---
            AdvancementHolder getCreativeCore = Advancement.Builder.advancement()
                    .parent(getMoldelonianCore)
                    .display(
                            ModItems.PUREMASH_CORE.get(),
                            Component.translatable("advancements.puremashtweaks.get_puremash_core.title"),
                            Component.translatable("advancements.puremashtweaks.get_puremash_core.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_puremash_core", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PUREMASH_CORE.get()))
                    .save(saver, "puremashtweaks:main/get_puremash_core");

            // --- 12. CRAFT PUREMASH CORE BLOCK (Child of getCreativeCore) ---
            AdvancementHolder getCoreBlock = Advancement.Builder.advancement()
                    .parent(getCreativeCore)
                    .display(
                            ModBlocks.PUREMASH_CORE_BLOCK.get(),
                            Component.translatable("advancements.puremashtweaks.get_core_block.title"),
                            Component.translatable("advancements.puremashtweaks.get_core_block.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_core_block", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PUREMASH_CORE_BLOCK.get()))
                    .save(saver, "puremashtweaks:main/get_core_block");

            // --- 13. CRAFT ALCHEMICAL SYNTHESIZER (Child of getMoldelonianIngot) ---
            AdvancementHolder getAlchemicalSynthesizer = Advancement.Builder.advancement()
                    .parent(getMoldelonianIngot)
                    .display(
                            ModBlocks.ALCHEMICAL_SYNTHESIZER.get(),
                            Component.translatable("advancements.puremashtweaks.get_alchemical_synthesizer.title"),
                            Component.translatable("advancements.puremashtweaks.get_alchemical_synthesizer.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_alchemical_synthesizer", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ALCHEMICAL_SYNTHESIZER.get()))
                    .save(saver, "puremashtweaks:main/get_alchemical_synthesizer");

            // =========================================================================
            // SECRET ADVANCEMENTS (GOAL TYPE)
            // =========================================================================

            // Enchant with Overload
            AdvancementHolder overloaded = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModItems.OVERLOAD_BOOK.get(),
                            Component.translatable("advancements.puremashtweaks.overloaded.title"),
                            Component.translatable("advancements.puremashtweaks.overloaded.description"),
                            null,
                            AdvancementType.GOAL,
                            true, true, true
                    )
                    .addCriterion("has_overloaded", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item()
                                    .withComponents(
                                            DataComponentMatchers.Builder.components()
                                                    .partial(
                                                            DataComponentPredicates.ENCHANTMENTS,
                                                            EnchantmentsPredicate.Enchantments.enchantments(List.of(
                                                                    new EnchantmentPredicate(
                                                                            registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.OVERLOAD),
                                                                            MinMaxBounds.Ints.atLeast(1)
                                                                    )
                                                            ))
                                                    ).build()
                                    ).build()
                    ))
                    .save(saver, "puremashtweaks:secret/overloaded");

            // --- OBTAIN MOLDELONIAN SMITHING TEMPLATE (Child of getArmor) ---
            AdvancementHolder getMoldelonianTemplate = Advancement.Builder.advancement()
                    .parent(getArmor)
                    .display(
                            ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get(),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_template.title"),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_template.description"),
                            null,
                            AdvancementType.CHALLENGE, // Moldura de desafio com som épico
                            true, true, false
                    )
                    .addCriterion("has_moldelonian_template", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get()))
                    .save(saver, "puremashtweaks:main/get_moldelonian_template");

            // Synthesize Synthorium Block
            AdvancementHolder energyForAll = Advancement.Builder.advancement()
                    .parent(getIngot)
                    .display(
                            ModBlocks.SYNTHORIUM_BLOCK.get(),
                            Component.translatable("advancements.puremashtweaks.energy_for_all.title"),
                            Component.translatable("advancements.puremashtweaks.energy_for_all.description"),
                            null,
                            AdvancementType.GOAL, // Goal -> Activates the puremash_goal sound!
                            true, true, true
                    )
                    .addCriterion("has_synthorium_block", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.SYNTHORIUM_BLOCK.get()))
                    .save(saver, "puremashtweaks:secret/energy_for_all");

            // Synthesize Moldelonian Block
            AdvancementHolder oneUnitesAll = Advancement.Builder.advancement()
                    .parent(getMoldelonianIngot)
                    .display(
                            ModBlocks.MOLDELONIAN_BLOCK.get(),
                            Component.translatable("advancements.puremashtweaks.one_unites_all.title"),
                            Component.translatable("advancements.puremashtweaks.one_unites_all.description"),
                            null,
                            AdvancementType.GOAL, // Goal -> Activates the puremash_goal sound!
                            true, true, true
                    )
                    .addCriterion("has_moldelonian_block", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.MOLDELONIAN_BLOCK.get()))
                    .save(saver, "puremashtweaks:secret/one_unites_all");

            // --- 1. MOLDELONIAN ARMOR (Child of getMoldelonianTemplate) ---
            AdvancementHolder getMoldelonianArmor = Advancement.Builder.advancement()
                    .parent(getMoldelonianTemplate)
                    .display(
                            ModItems.MOLDELONIAN_CHESTPLATE.get(),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_armor.title"),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_armor.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_HELMET.get()))
                    .addCriterion("has_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_CHESTPLATE.get()))
                    .addCriterion("has_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_LEGGINGS.get()))
                    .addCriterion("has_boots", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_BOOTS.get()))
                    .save(saver, "puremashtweaks:main/get_moldelonian_armor");

            // --- 2. MOLDELONIAN PAXEL (Child of getMoldelonianTemplate) ---
            AdvancementHolder getMoldelonianPaxel = Advancement.Builder.advancement()
                    .parent(getMoldelonianTemplate)
                    .display(
                            ModItems.MOLDELONIAN_PAXEL.get(),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_paxel.title"),
                            Component.translatable("advancements.puremashtweaks.get_moldelonian_paxel.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_moldelonian_paxel", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOLDELONIAN_PAXEL.get()))
                    .save(saver, "puremashtweaks:main/get_moldelonian_paxel");

            // --- ENCHANT WITH OVERCLOCK (Secret Goal) ---
            AdvancementHolder overclocked = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModItems.OVERCLOCK_BOOK.get(),
                            Component.translatable("advancements.puremashtweaks.overclocked.title"),
                            Component.translatable("advancements.puremashtweaks.overclocked.description"),
                            null,
                            AdvancementType.GOAL,
                            true, true, true
                    )
                    .addCriterion("has_overclocked", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item()
                                    .withComponents(
                                            DataComponentMatchers.Builder.components()
                                                    .partial(
                                                            DataComponentPredicates.ENCHANTMENTS,
                                                            EnchantmentsPredicate.Enchantments.enchantments(List.of(
                                                                    new EnchantmentPredicate(
                                                                            registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.OVERCLOCK),
                                                                            MinMaxBounds.Ints.atLeast(1)
                                                                    )
                                                            ))
                                                    ).build()
                                    ).build()
                    ))
                    .save(saver, "puremashtweaks:secret/overclocked");

            // --- ENCHANT WITH OVERDRIVE (Secret Goal) ---
            AdvancementHolder overdriven = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModItems.OVERDRIVE_BOOK.get(),
                            Component.translatable("advancements.puremashtweaks.overdriven.title"),
                            Component.translatable("advancements.puremashtweaks.overdriven.description"),
                            null,
                            AdvancementType.GOAL,
                            true, true, true
                    )
                    .addCriterion("has_overdriven", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item()
                                    .withComponents(
                                            DataComponentMatchers.Builder.components()
                                                    .partial(
                                                            DataComponentPredicates.ENCHANTMENTS,
                                                            EnchantmentsPredicate.Enchantments.enchantments(List.of(
                                                                    new EnchantmentPredicate(
                                                                            registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.OVERDRIVE),
                                                                            MinMaxBounds.Ints.atLeast(1)
                                                                    )
                                                            ))
                                                    ).build()
                                    ).build()
                    ))
                    .save(saver, "puremashtweaks:secret/overdriven");

            // --- 1. UNIVERSAL CABLE (Task, Child of getIngot) ---
            AdvancementHolder getUniversalCable = Advancement.Builder.advancement()
                    .parent(getIngot)
                    .display(
                            ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get(),
                            Component.translatable("advancements.puremashtweaks.get_universal_cable.title"),
                            Component.translatable("advancements.puremashtweaks.get_universal_cable.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_synthorium_cable", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get()))
                    .addCriterion("has_moldelonian_cable", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get()))
                    .requirements(net.minecraft.advancements.AdvancementRequirements.Strategy.OR)
                    .save(saver, "puremashtweaks:main/get_universal_cable");

            // --- 2. FLUID TANK (Task, Child of getSynthesisTable) ---
            AdvancementHolder getFluidTank = Advancement.Builder.advancement()
                    .parent(getSynthesisTable)
                    .display(
                            ModBlocks.FLUID_TANK.get(),
                            Component.translatable("advancements.puremashtweaks.get_fluid_tank.title"),
                            Component.translatable("advancements.puremashtweaks.get_fluid_tank.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_fluid_tank", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.FLUID_TANK.get()))
                    .save(saver, "puremashtweaks:main/get_fluid_tank");

            // --- 3. CREATIVE FLUID TANK (Challenge, Child of getFluidTank) ---
            AdvancementHolder getCreativeFluidTank = Advancement.Builder.advancement()
                    .parent(getFluidTank)
                    .display(
                            ModBlocks.CREATIVE_FLUID_TANK.get(),
                            Component.translatable("advancements.puremashtweaks.get_creative_fluid_tank.title"),
                            Component.translatable("advancements.puremashtweaks.get_creative_fluid_tank.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_creative_fluid_tank", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.CREATIVE_FLUID_TANK.get()))
                    .save(saver, "puremashtweaks:main/get_creative_fluid_tank");

            // --- 4. PUREMASH BATTERY (Task, Child of getMoldelonianCore) ---
            AdvancementHolder getBattery = Advancement.Builder.advancement()
                    .parent(getMoldelonianCore)
                    .display(
                            ModBlocks.PUREMASH_BATTERY.get(),
                            Component.translatable("advancements.puremashtweaks.get_battery.title"),
                            Component.translatable("advancements.puremashtweaks.get_battery.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_battery", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PUREMASH_BATTERY.get()))
                    .save(saver, "puremashtweaks:main/get_battery");

            // --- 5. CREATIVE ENERGY BATTERY (Challenge, Child of getBattery) ---
            AdvancementHolder getCreativeBattery = Advancement.Builder.advancement()
                    .parent(getBattery)
                    .display(
                            ModBlocks.CREATIVE_BATTERY.get(),
                            Component.translatable("advancements.puremashtweaks.get_creative_battery.title"),
                            Component.translatable("advancements.puremashtweaks.get_creative_battery.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true, true, false
                    )
                    .addCriterion("has_creative_battery", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.CREATIVE_BATTERY.get()))
                    .save(saver, "puremashtweaks:main/get_creative_battery");

            // --- PUREMASH GENERATOR (Task, Child of getFluidTank) ---
            AdvancementHolder getGenerator = Advancement.Builder.advancement()
                    .parent(getFluidTank)
                    .display(
                            ModBlocks.PUREMASH_GENERATOR.get(),
                            Component.translatable("advancements.puremashtweaks.get_generator.title"),
                            Component.translatable("advancements.puremashtweaks.get_generator.description"),
                            null,
                            AdvancementType.TASK,
                            true, true, false
                    )
                    .addCriterion("has_generator", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PUREMASH_GENERATOR.get()))
                    .save(saver, "puremashtweaks:main/get_generator");

            // --- COSMIC SINGULARITY (Secret Goal, Child of getCompressor) ---
            AdvancementHolder cosmicSingularity = Advancement.Builder.advancement()
                    .parent(getCompressor)
                    .display(
                            dev.davidklgames.puremashtweaks.registry.ModSingularities.COSMIC_SINGULARITY.get(),
                            Component.translatable("advancements.puremashtweaks.cosmic_singularity.title"),
                            Component.translatable("advancements.puremashtweaks.cosmic_singularity.description"),
                            null,
                            AdvancementType.GOAL, // Activates the puremash_goal sound when obtained
                            true, // Shows the toast on screen
                            true, // Announces in the global chat
                            true  // Hidden = true (Makes the advancement secret and invisible until obtained!)
                    )
                    .addCriterion("has_cosmic_singularity", InventoryChangeTrigger.TriggerInstance.hasItems(dev.davidklgames.puremashtweaks.registry.ModSingularities.COSMIC_SINGULARITY.get()))
                    .save(saver, "puremashtweaks:secret/cosmic_singularity");
        }
    }
}