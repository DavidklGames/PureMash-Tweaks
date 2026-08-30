package dev.davidklgames.puremashtweaks.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.Identifier;
import java.util.Map;

public class ModArmorMaterials {
    public static final ResourceKey<EquipmentAsset> SYNTHORIUM_ASSET = EquipmentAssets.createId("synthorium");

    public static final ArmorMaterial SYNTHORIUM = new ArmorMaterial(
            188,
            Map.of(
                    ArmorType.HELMET, 6,
                    ArmorType.CHESTPLATE, 12,
                    ArmorType.LEGGINGS, 10,
                    ArmorType.BOOTS, 6,
                    ArmorType.BODY, 34
            ),
            20,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            5.0f,
            0.2f,
            ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "synthorium_repair_items")),
            SYNTHORIUM_ASSET
    );

    public static final ResourceKey<EquipmentAsset> MOLDELONIAN_ASSET = EquipmentAssets.createId("moldelonian");

    public static final ArmorMaterial MOLDELONIAN = new ArmorMaterial(
            418,
            Map.of(
                    ArmorType.HELMET, 18,
                    ArmorType.CHESTPLATE, 36,
                    ArmorType.LEGGINGS, 28,
                    ArmorType.BOOTS, 18,
                    ArmorType.BODY, 100
            ),
            30,                                   // Encantabilidade máxima (padrão ouro)
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            12.5f,                                // Toughness supremo: 12.5 por peça (50.0 total no full set)
            0.25f,                                // Knockback Resistance: 0.25 por peça (100% total)
            ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "moldelonian_repair_items")),
            MOLDELONIAN_ASSET
    );
}