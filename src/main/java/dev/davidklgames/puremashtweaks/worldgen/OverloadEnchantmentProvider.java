package dev.davidklgames.puremashtweaks.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import org.jspecify.annotations.NonNull;

public record OverloadEnchantmentProvider(Holder<Enchantment> enchantment) implements EnchantmentProvider {
    public static final MapCodec<OverloadEnchantmentProvider> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Enchantment.CODEC.fieldOf("enchantment").forGetter(OverloadEnchantmentProvider::enchantment)
            ).apply(instance, OverloadEnchantmentProvider::new)
    );

    @Override
    public void enchant(@NonNull ItemStack item, ItemEnchantments.@NonNull Mutable itemEnchantments, RandomSource random, @NonNull DifficultyInstance difficulty) {
        boolean isSynthoriumItem = isOverloadItem(item);
        int overloadLevelToApply = 0;
        int roll = random.nextInt(100);

        if (isSynthoriumItem) {
            if (roll < 5) {          // 5% chance for level 3 (Looting 3).
                overloadLevelToApply = 3;
            } else if (roll < 50) {  // 45% chance for level 2 (Sharpness 4).
                overloadLevelToApply = 2;
            } else if (roll < 85) {  // 35% chance for level 1 (Unbreaking 3).
                overloadLevelToApply = 1;
            }
        } else {
            if (roll < 2) {          // 2% chance for level 3.
                overloadLevelToApply = 3;
            } else if (roll < 12) {  // 10% chance for level 2.
                overloadLevelToApply = 2;
            } else if (roll < 17) {  // 5% chance for level 1.
                overloadLevelToApply = 1;
            }
        }

        if (overloadLevelToApply > 0) {
            itemEnchantments.upgrade(this.enchantment, overloadLevelToApply);
        }
    }

    private boolean isOverloadItem(ItemStack item) {
        if (item.isEmpty()) return false;
        return item.is(ModItems.SYNTHORIUM_HELMET.get()) ||
                item.is(ModItems.SYNTHORIUM_CHESTPLATE.get()) ||
                item.is(ModItems.SYNTHORIUM_LEGGINGS.get()) ||
                item.is(ModItems.SYNTHORIUM_BOOTS.get()) ||
                item.is(ModItems.SYNTHORIUM_PICKAXE.get()) ||
                item.is(ModItems.SYNTHORIUM_AXE.get()) ||
                item.is(ModItems.SYNTHORIUM_SHOVEL.get()) ||
                item.is(ModItems.SYNTHORIUM_SWORD.get()) ||
                item.is(ModItems.SYNTHORIUM_HOE.get()) ||
                item.is(ModItems.SYNTHORIUM_PAXEL.get()) ||
                item.is(ModItems.MOLDELONIAN_HELMET.get()) ||
                item.is(ModItems.MOLDELONIAN_CHESTPLATE.get()) ||
                item.is(ModItems.MOLDELONIAN_LEGGINGS.get()) ||
                item.is(ModItems.MOLDELONIAN_BOOTS.get()) ||
                item.is(ModItems.MOLDELONIAN_PICKAXE.get()) ||
                item.is(ModItems.MOLDELONIAN_AXE.get()) ||
                item.is(ModItems.MOLDELONIAN_SHOVEL.get()) ||
                item.is(ModItems.MOLDELONIAN_SWORD.get()) ||
                item.is(ModItems.MOLDELONIAN_HOE.get()) ||
                item.is(ModItems.MOLDELONIAN_PAXEL.get());
    }

    @Override
    public @NonNull MapCodec<OverloadEnchantmentProvider> codec() {
        return CODEC;
    }
}