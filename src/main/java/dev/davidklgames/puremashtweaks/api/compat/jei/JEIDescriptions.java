package dev.davidklgames.puremashtweaks.api.compat.jei;

import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class JEIDescriptions {
    public static void register(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                new ItemStack(ModBlocks.SYNTHORIUM_DEBRIS.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tooltip.puremashtweaks.synthorium_debris_in_jei.desc")
        );

        registration.addIngredientInfo(
                new ItemStack(ModItems.MUSIC_DISC_BEYOND_THE_FINAL_STAGE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tooltip.puremashtweaks.music_disc_beyond_the_final_stage.desc")
        );
    }
}