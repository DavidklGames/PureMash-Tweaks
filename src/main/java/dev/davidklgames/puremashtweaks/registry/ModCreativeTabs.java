package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PureMashTweaks.MODID);

    private static void acceptSafe(CreativeModeTab.Output output, ItemLike item) {
        if (item != null && item.asItem() != net.minecraft.world.item.Items.AIR) {
            ItemStack stack = new ItemStack(item.asItem());
            stack.setCount(1);
            output.accept(stack);
        }
    }

    private static void acceptSafe(CreativeModeTab.Output output, ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            output.accept(copy);
        }
    }

    public static ItemStack getGuideBook() {
        Item guideItem = BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("guideme", "guide"))
                .map(Holder::value)
                .orElse(Items.AIR);
        if (guideItem == Items.AIR) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(guideItem);
        var compType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(Identifier.fromNamespaceAndPath("guideme", "guide_id"));
        compType.ifPresent(holder -> stack.set((DataComponentType<Identifier>) holder.value(), Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "guide")));
        return stack;
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PUREMASH_TAB =
            CREATIVE_MODE_TABS.register("puremash_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.puremashtweaks.puremash_tab"))
                    .icon(() -> new ItemStack(ModItems.PUREMASH_CORE.get()))
                    .displayItems((parameters, output) -> {

                        // =========================================================================
                        // 1. MACHINES & ENERGY SECTOR
                        // =========================================================================
                        acceptSafe(output, ModBlocks.SYNTHESIS_TABLE.get());
                        acceptSafe(output, ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get());
                        acceptSafe(output, ModBlocks.ALCHEMICAL_SYNTHESIZER.get());
                        acceptSafe(output, ModBlocks.CHUNK_LOADER.get());
                        acceptSafe(output, ModBlocks.PUREMASH_GENERATOR.get());
                        acceptSafe(output, ModBlocks.FLUID_TANK.get());
                        acceptSafe(output, ModBlocks.CREATIVE_FLUID_TANK.get());
                        acceptSafe(output, ModBlocks.PUREMASH_BATTERY.get());
                        acceptSafe(output, ModBlocks.CREATIVE_BATTERY.get());

                        // =========================================================================
                        // 2. LOGISTICS, CABLES & CONFIGURATION TOOLS
                        // =========================================================================
                        acceptSafe(output, ModBlocks.SYNTHORIUM_UNIVERSAL_CABLE.get());
                        acceptSafe(output, ModBlocks.MOLDELONIAN_UNIVERSAL_CABLE.get());
                        acceptSafe(output, ModItems.CONFIGURATION_WRENCH.get());
                        acceptSafe(output, ModItems.DISTRIBUTION_FILTER.get());
                        acceptSafe(output, ModItems.MEMORY_CARD.get());

                        // =========================================================================
                        // 3. ENDGAME CORES & CORE BLOCKS
                        // =========================================================================
                        acceptSafe(output, ModItems.PUREMASH_CORE.get());
                        acceptSafe(output, ModBlocks.PUREMASH_CORE_BLOCK.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_CORE.get());

                        // =========================================================================
                        // 4. SYNTHORIUM SECTOR (Minerals, Metals, Tools & Armor)
                        // =========================================================================
                        acceptSafe(output, ModBlocks.SYNTHORIUM_DEBRIS.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_SCRAP.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_PLATE.get());
                        acceptSafe(output, ModItems.MOLTEN_SYNTHORIUM_BUCKET.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_INGOT.get());
                        acceptSafe(output, ModBlocks.SYNTHORIUM_BLOCK.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_APPLE.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_NUGGET.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_DUST.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_ROD.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_SWORD.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_PICKAXE.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_SHOVEL.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_AXE.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_HOE.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_PAXEL.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_HELMET.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_CHESTPLATE.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_LEGGINGS.get());
                        acceptSafe(output, ModItems.SYNTHORIUM_BOOTS.get());

                        // =========================================================================
                        // 5. MOLDELONIAN SECTOR (Archaeology, Template, Metals, Tools & Armor)
                        // =========================================================================
                        acceptSafe(output, ModBlocks.SUSPICIOUS_END_STONE.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_SMITHING_TEMPLATE.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_PLATE.get());
                        acceptSafe(output, ModItems.MOLTEN_MOLDELONIAN_BUCKET.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_INGOT.get());
                        acceptSafe(output, ModBlocks.MOLDELONIAN_BLOCK.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_APPLE.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_NUGGET.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_DUST.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_SWORD.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_PICKAXE.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_SHOVEL.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_AXE.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_HOE.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_PAXEL.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_HELMET.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_CHESTPLATE.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_LEGGINGS.get());
                        acceptSafe(output, ModItems.MOLDELONIAN_BOOTS.get());

                        // =========================================================================
                        // 6. MACHINE UPGRADES & MEDIA
                        // =========================================================================
                        acceptSafe(output, ModItems.SPEED_UPGRADE_1.get());
                        acceptSafe(output, ModItems.SPEED_UPGRADE_2.get());
                        acceptSafe(output, ModItems.SPEED_UPGRADE_3.get());
                        acceptSafe(output, ModItems.CAPACITY_UPGRADE_1.get());
                        acceptSafe(output, ModItems.CAPACITY_UPGRADE_2.get());
                        acceptSafe(output, ModItems.DUPLICATION_UPGRADE_1.get());
                        acceptSafe(output, ModItems.DUPLICATION_UPGRADE_2.get());
                        acceptSafe(output, ModItems.STACK_PROCESSING_UPGRADE.get());
                        acceptSafe(output, ModItems.MUSIC_DISC_BEYOND_THE_FINAL_STAGE.get());
                        acceptSafe(output, ModItems.MUSIC_DISC_NEW_HORIZONS.get());

                        // =========================================================================
                        // 7. ENCHANTED BOOKS (Ordered by Max Levels: 2 -> 3 -> 4)
                        // =========================================================================
                        var enchantmentLookup = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

                        // 1. Overclock (Max Level 2)
                        enchantmentLookup.get(ModEnchantments.OVERCLOCK).ifPresent(overclockEnchant -> {
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overclockEnchant, 1)));
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overclockEnchant, 2)));
                        });

                        // 2. Overload (Max Level 3)
                        enchantmentLookup.get(ModEnchantments.OVERLOAD).ifPresent(overloadEnchant -> {
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overloadEnchant, 1)));
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overloadEnchant, 2)));
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overloadEnchant, 3)));
                        });

                        // 3. Overdrive (Max Level 4)
                        enchantmentLookup.get(ModEnchantments.OVERDRIVE).ifPresent(overdriveEnchant -> {
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overdriveEnchant, 1)));
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overdriveEnchant, 2)));
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overdriveEnchant, 3)));
                            acceptSafe(output, EnchantmentHelper.createBook(new EnchantmentInstance(overdriveEnchant, 4)));
                        });
                        // =========================================================================
                        // 8. PUREMASH GUIDE BOOK
                        // =========================================================================
                        acceptSafe(output, getGuideBook());
                    })
                    .build());

    // Separate dedicated tab for all singularities
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PUREMASH_SINGULARITY_TAB =
            CREATIVE_MODE_TABS.register("puremash_singularity_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.puremashtweaks.puremash_singularity_tab"))
                    .withTabsBefore(PUREMASH_TAB.getKey())
                    .icon(() -> {
                        if (ModSingularities.REGISTERED_SINGULARITIES.isEmpty()) {
                            return new ItemStack(ModItems.PUREMASH_CORE.get());
                        }
                        long index = (System.currentTimeMillis() / 1500) % ModSingularities.REGISTERED_SINGULARITIES.size();
                        var selectedHolder = ModSingularities.REGISTERED_SINGULARITIES.get((int) index);
                        return (selectedHolder != null) ? new ItemStack(selectedHolder.get()) : new ItemStack(ModItems.PUREMASH_CORE.get());
                    })
                    .displayItems((_, output) -> {
                        for (var itemHolder : ModSingularities.REGISTERED_SINGULARITIES) {
                            if (itemHolder != null) {
                                itemHolder.get();
                                acceptSafe(output, itemHolder.get());
                            }
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}