package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PureMashTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PUREMASH_TAB =
            CREATIVE_MODE_TABS.register("puremash_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.puremashtweaks.puremash_tab"))
                    .icon(() -> new ItemStack(ModItems.PUREMASH_CORE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.SYNTHESIS_TABLE.get());
                        output.accept(ModItems.MEMORY_CARD.get());
                        output.accept(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get());
                        output.accept(ModBlocks.ALCHEMICAL_SYNTHESIZER.get());
                        output.accept(ModBlocks.CHUNK_LOADER.get());
                        output.accept(ModBlocks.FLUID_TANK.get());
                        output.accept(ModBlocks.CREATIVE_FLUID_TANK.get());
                        output.accept(ModItems.PUREMASH_CORE.get());
                        output.accept(ModBlocks.PUREMASH_CORE_BLOCK.get());
                        output.accept(ModBlocks.SYNTHORIUM_DEBRIS.get());
                        output.accept(ModItems.SYNTHORIUM_SCRAP.get());
                        output.accept(ModItems.SYNTHORIUM_INGOT.get());
                        output.accept(ModBlocks.SYNTHORIUM_BLOCK.get());
                        output.accept(ModItems.SYNTHORIUM_NUGGET.get());
                        output.accept(ModItems.SYNTHORIUM_DUST.get());
                        output.accept(ModItems.SYNTHORIUM_SWORD.get());
                        output.accept(ModItems.SYNTHORIUM_PICKAXE.get());
                        output.accept(ModItems.SYNTHORIUM_SHOVEL.get());
                        output.accept(ModItems.SYNTHORIUM_AXE.get());
                        output.accept(ModItems.SYNTHORIUM_HOE.get());
                        output.accept(ModItems.SYNTHORIUM_PAXEL.get());
                        output.accept(ModItems.SYNTHORIUM_ROD.get());
                        output.accept(ModItems.MUSIC_DISC_BEYOND_THE_FINAL_STAGE.get());
                        output.accept(ModItems.SYNTHORIUM_HELMET.get());
                        output.accept(ModItems.SYNTHORIUM_CHESTPLATE.get());
                        output.accept(ModItems.SYNTHORIUM_LEGGINGS.get());
                        output.accept(ModItems.SYNTHORIUM_BOOTS.get());
                        output.accept(ModItems.MOLDELONIAN_CORE.get());
                        output.accept(ModItems.MOLDELONIAN_INGOT.get());
                        output.accept(ModBlocks.MOLDELONIAN_BLOCK.get());
                        output.accept(ModItems.MOLDELONIAN_NUGGET.get());
                        output.accept(ModItems.SPEED_UPGRADE_1.get());
                        output.accept(ModItems.SPEED_UPGRADE_2.get());
                        output.accept(ModItems.SPEED_UPGRADE_3.get());

                        // --- ADDING THE OVERLOAD BOOKS (SAFE METHOD) ---
                        var enchantmentLookup = parameters.holders().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);

                        enchantmentLookup.get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD).ifPresent(overloadEnchant -> {
                            output.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                                    new net.minecraft.world.item.enchantment.EnchantmentInstance(overloadEnchant, 1)));
                            output.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                                    new net.minecraft.world.item.enchantment.EnchantmentInstance(overloadEnchant, 2)));
                            output.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                                    new net.minecraft.world.item.enchantment.EnchantmentInstance(overloadEnchant, 3)));
                        });

                        enchantmentLookup.get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERCLOCK).ifPresent(overclockEnchant -> {
                            output.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                                    new net.minecraft.world.item.enchantment.EnchantmentInstance(overclockEnchant, 1)));
                            output.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
                                    new net.minecraft.world.item.enchantment.EnchantmentInstance(overclockEnchant, 2)));
                        });
                    })
                    .build());

    // New tab just for singularities (with a dynamically cycling icon and side-by-side sorting!)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PUREMASH_SINGULARITY_TAB =
            CREATIVE_MODE_TABS.register("puremash_singularity_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.puremashtweaks.puremash_singularity_tab"))
                    // Ensures that this tab is positioned immediately to the right of your mod's main tab!
                    .withTabsBefore(PUREMASH_TAB.getKey())
                    .icon(() -> {
                        // If no singularity is loaded, PureMash Core is displayed as the default icon.
                        if (ModSingularities.REGISTERED_SINGULARITIES.isEmpty()) {
                            return new ItemStack(ModItems.PUREMASH_CORE.get());
                        }

                        // Selects the specific instance based on real time (changes every 1.5 seconds).
                        long index = (System.currentTimeMillis() / 1500) % ModSingularities.REGISTERED_SINGULARITIES.size();
                        var selectedHolder = ModSingularities.REGISTERED_SINGULARITIES.get((int) index);
                        return (selectedHolder != null) ? new ItemStack(selectedHolder.get()) : new ItemStack(ModItems.PUREMASH_CORE.get());
                    })
                    .displayItems((_, output) -> {
                        // Adds only the singularities that were actually successfully recorded during the session.
                        for (var itemHolder : ModSingularities.REGISTERED_SINGULARITIES) {
                            output.accept(itemHolder.get());
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}