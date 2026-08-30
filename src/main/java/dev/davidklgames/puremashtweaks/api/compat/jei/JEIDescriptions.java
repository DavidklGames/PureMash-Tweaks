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

        registration.addIngredientInfo(
                new ItemStack(ModItems.MUSIC_DISC_NEW_HORIZONS.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tooltip.puremashtweaks.music_disc_new_horizons_in_jei.desc")
        );

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.PUREMASH_GENERATOR.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tooltip.puremashtweaks.puremash_generator_in_jei.desc")
        );

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.SUSPICIOUS_END_STONE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tooltip.puremashtweaks.suspicious_end_stone_in_jei.desc")
        );
    }
}