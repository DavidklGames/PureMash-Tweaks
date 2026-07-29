package dev.davidklgames.puremashtweaks.datagen;

import java.util.function.BiConsumer;
import dev.davidklgames.puremashtweaks.api.PMT;
import dev.davidklgames.puremashtweaks.util.ModArmorMaterials;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public class PMTEquipmentAssetsProvider extends EquipmentAssetProvider {
    public PMTEquipmentAssetsProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        output.accept(
                ModArmorMaterials.SYNTHORIUM_ASSET,
                EquipmentClientInfo.builder()
                        .addHumanoidLayers(PMT.id("synthorium"))
                        .build()
        );
    }
}