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
            37,
            Map.of(
                    ArmorType.HELMET, 6,
                    ArmorType.CHESTPLATE, 12,
                    ArmorType.LEGGINGS, 10,
                    ArmorType.BOOTS, 6,
                    ArmorType.BODY, 16
            ),
            20,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            5.0f,
            0.2f,
            ItemTags.create(Identifier.fromNamespaceAndPath("puremashtweaks", "synthorium_repair_items")),
            SYNTHORIUM_ASSET
    );
}