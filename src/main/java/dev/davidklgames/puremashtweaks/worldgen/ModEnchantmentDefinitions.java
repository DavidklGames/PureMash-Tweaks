package dev.davidklgames.puremashtweaks.worldgen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;

import java.util.List;

public class ModEnchantmentDefinitions {
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var items = context.lookup(Registries.ITEM);

        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> overloadEnchantableTag = net.minecraft.tags.TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "enchantable/overload")
        );

        // --- OVERLOAD ENCHHANTMENT REGISTRY ---

        context.register(ModEnchantments.OVERLOAD, Enchantment.enchantment(
                        Enchantment.definition(
                                items.getOrThrow(overloadEnchantableTag),
                                5, 3, // Weight 5, Max Level 3.
                                Enchantment.dynamicCost(15, 15),
                                Enchantment.dynamicCost(65, 15),
                                8,
                                EquipmentSlotGroup.ANY
                        ))
                .withSpecialEffect(EnchantmentEffectComponents.ATTRIBUTES, List.of(
                        new EnchantmentAttributeEffect(
                                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload_reach_block"),
                                Attributes.BLOCK_INTERACTION_RANGE,
                                LevelBasedValue.perLevel(1.0f, 1.0f),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        new EnchantmentAttributeEffect(
                                Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload_reach_entity"),
                                Attributes.ENTITY_INTERACTION_RANGE,
                                LevelBasedValue.perLevel(1.0f, 1.0f),
                                AttributeModifier.Operation.ADD_VALUE
                        )
                ))
                .build(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload")));

        // --- OVERCLOCK ENCHANTMENT REGISTRY ---

        context.register(ModEnchantments.OVERCLOCK, Enchantment.enchantment(
                        Enchantment.definition(

                                items.getOrThrow(net.minecraft.tags.ItemTags.VANISHING_ENCHANTABLE),
                                5, 2, // Weight 5, Max Level 2
                                Enchantment.dynamicCost(15, 15),
                                Enchantment.dynamicCost(65, 15),
                                8,
                                net.minecraft.world.entity.EquipmentSlotGroup.ANY
                        ))
                .build(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overclock")));
    }
}