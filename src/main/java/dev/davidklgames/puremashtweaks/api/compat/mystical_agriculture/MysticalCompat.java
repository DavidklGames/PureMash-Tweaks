package dev.davidklgames.puremashtweaks.api.compat.mystical_agriculture;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import net.minecraft.resources.Identifier;

@MysticalAgriculturePlugin
public class MysticalCompat implements IMysticalAgriculturePlugin {

    // =========================================================================
    // SYNTHORIUM CROP
    // =========================================================================

    public static final Crop SYNTHORIUM = new Crop(
            Identifier.fromNamespaceAndPath("puremashtweaks", "synthorium"),
            CropTier.FOUR,
            CropType.RESOURCE,
            LazyIngredient.item("puremashtweaks:synthorium_ingot")
    );

    @Override
    public void onRegisterCrops(ICropRegistry registry) {

        // =========================================================================
        // REGISTER
        // =========================================================================

        registry.register(SYNTHORIUM);
    }
}