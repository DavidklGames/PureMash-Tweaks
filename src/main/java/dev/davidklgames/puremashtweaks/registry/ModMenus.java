package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.menu.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, PureMashTweaks.MODID);

    // --- SYNTHESIS TABLE MENU ---
    public static final DeferredHolder<MenuType<?>, MenuType<SynthesisTableMenu>> SYNTHESIS_TABLE_MENU =
            MENUS.register("synthesis_table_menu", () -> IMenuTypeExtension.create(SynthesisTableMenu::new));

    // --- COMPRESSOR MENU ---
    public static final DeferredHolder<MenuType<?>, MenuType<MultifunctionalCompressorMenu>> MULTIFUNCTIONAL_COMPRESSOR_MENU =
            MENUS.register("multifunctional_compressor_menu", () -> IMenuTypeExtension.create(MultifunctionalCompressorMenu::new));

    // --- ALCHEMICAL SYNTHESIZER MENU ---
    public static final DeferredHolder<MenuType<?>, MenuType<AlchemicalSynthesizerMenu>> ALCHEMICAL_SYNTHESIZER_MENU =
            MENUS.register("alchemical_synthesizer_menu", () -> IMenuTypeExtension.create(AlchemicalSynthesizerMenu::new));

    // --- CHUNK LOADER MENU ---
    public static final DeferredHolder<MenuType<?>, MenuType<ChunkLoaderMenu>> CHUNK_LOADER_MENU =
            MENUS.register("chunk_loader_menu", () -> IMenuTypeExtension.create(ChunkLoaderMenu::new));

    // --- PUREMASH GENERATOR ---
    public static final DeferredHolder<MenuType<?>, MenuType<PureMashGeneratorMenu>> PUREMASH_GENERATOR_MENU =
            MENUS.register("puremash_generator_menu", () -> IMenuTypeExtension.create(PureMashGeneratorMenu::new));

    // --- CABLES MENU ---
    public static final DeferredHolder<MenuType<?>, MenuType<CableMenu>> CABLE_MENU =
            MENUS.register("cable_menu", () -> IMenuTypeExtension.create(CableMenu::new));

    // --- CABLE FILTER MENU ---
    public static final DeferredHolder<MenuType<?>, MenuType<FilterMenu>> FILTER_MENU =
            MENUS.register("filter_menu", () -> IMenuTypeExtension.create(FilterMenu::new));

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}