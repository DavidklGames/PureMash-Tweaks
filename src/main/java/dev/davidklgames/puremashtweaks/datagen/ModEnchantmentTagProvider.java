package dev.davidklgames.puremashtweaks.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import dev.davidklgames.puremashtweaks.registry.ModEnchantments;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class ModEnchantmentTagProvider extends EnchantmentTagsProvider {

    public ModEnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        this.tag(EnchantmentTags.IN_ENCHANTING_TABLE).add(ModEnchantments.OVERLOAD);
        this.tag(EnchantmentTags.TRADEABLE).add(ModEnchantments.OVERLOAD);
        this.tag(EnchantmentTags.IN_ENCHANTING_TABLE).add(ModEnchantments.OVERCLOCK);
        this.tag(EnchantmentTags.TRADEABLE).add(ModEnchantments.OVERCLOCK);
    }
}