package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.menu.AlchemicalSynthesizerMenu;
import dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;

import static dev.davidklgames.puremashtweaks.registry.ModRecipes.RECIPE_SERIALIZERS;

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
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<dev.davidklgames.puremashtweaks.menu.ChunkLoaderMenu>> CHUNK_LOADER_MENU =
            MENUS.register("chunk_loader_menu", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(dev.davidklgames.puremashtweaks.menu.ChunkLoaderMenu::new));

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}