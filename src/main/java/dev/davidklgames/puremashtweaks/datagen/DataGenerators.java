package dev.davidklgames.puremashtweaks.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

@EventBusSubscriber(modid = PureMashTweaks.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        // 1. Registers the Datapack objects first to populate the internal variable `registriesWithModdedEntries`.
        event.createDatapackRegistryObjects(ModWorldGenProvider.BUILDER);

        // 2. Now, when retrieving the provider, it will automatically return the record containing the Overload.
        var lookup = event.getLookupProvider();

        // 3. Models (Assets)
        event.createProvider(ModModelProvider::new);

        // 4. Recipes (Official 26.1.2)
        event.createProvider(dev.davidklgames.puremashtweaks.datagen.ModRecipeProvider.Runner::new);

        // 5. (Tags)
        var blockTags = event.createProvider(out -> new ModBlockTagProvider(out, lookup));
        event.createProvider(out -> new ModItemTagProvider(out, lookup, blockTags.contentsGetter()));
        event.createProvider(out -> new ModFluidTagProvider(out, lookup));

        // 6. (Loot Tables)
        event.createProvider(out -> ModLootTableProvider.create(out, lookup));

        // 7. (Enchantments)
        event.createProvider(out -> new ModEnchantmentTagProvider(out, lookup));

        // 8. (Armor Assets)
        event.createProvider(PMTEquipmentAssetsProvider::new);

        // 9. (Advancements)
        event.createProvider(out -> new ModAdvancementProvider(out, lookup));

        PureMashTweaks.LOGGER.info("DataGen Client, Recipes, Tags, Loot are configured!");
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Server event) {
        PureMashTweaks.LOGGER.info("[PureMash Tweaks]: Data Generation completed successfully.");
    }

}