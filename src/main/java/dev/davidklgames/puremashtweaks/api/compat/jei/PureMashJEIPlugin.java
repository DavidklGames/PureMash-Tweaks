package dev.davidklgames.puremashtweaks.api.compat.jei;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper;
import dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper;
import dev.davidklgames.puremashtweaks.api.SynthesisRecipeHelper;
import dev.davidklgames.puremashtweaks.api.compat.jei.category.*;
import dev.davidklgames.puremashtweaks.api.compat.jei.handler.JEIContainerHandler;
import dev.davidklgames.puremashtweaks.api.compat.jei.recipe.GeneratorFuelRecipe;
import dev.davidklgames.puremashtweaks.api.compat.jei.transfer.AlchemicalSynthesizerTransferInfo;
import dev.davidklgames.puremashtweaks.client.screen.*;
import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;
import dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe;
import dev.davidklgames.puremashtweaks.registry.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"removal", "unchecked"})
@JeiPlugin
public class PureMashJEIPlugin implements IModPlugin {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "jei_plugin");

    @Override
    public @NotNull Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new CompressionCategory(helper),
                new SingularityCategory(helper),
                new DustCategory(helper),
                new SynthesisTableCategory(helper),
                new AlchemicalSynthesizerCategory(helper),
                new GeneratorFuelCategory(helper)
        );
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(SynthesisTableMenu.class, ModMenus.SYNTHESIS_TABLE_MENU.get(), SynthesisTableCategory.RECIPE_TYPE, 0, 81, 83, 36);
        registration.addRecipeTransferHandler(new AlchemicalSynthesizerTransferInfo());
        registration.addRecipeTransferHandler(dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu.class, ModMenus.MULTIFUNCTIONAL_COMPRESSOR_MENU.get(), CompressionCategory.RECIPE_TYPE, 0, 1, 5, 36);
        registration.addRecipeTransferHandler(dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu.class, ModMenus.MULTIFUNCTIONAL_COMPRESSOR_MENU.get(), DustCategory.RECIPE_TYPE, 0, 1, 5, 36);
        registration.addRecipeTransferHandler(dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu.class, ModMenus.MULTIFUNCTIONAL_COMPRESSOR_MENU.get(), SingularityCategory.RECIPE_TYPE, 0, 1, 5, 36);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        RecipeManager manager = null;

        if (ServerLifecycleHooks.getCurrentServer() != null) {
            manager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        } else if (Minecraft.getInstance().getSingleplayerServer() != null) {
            manager = Minecraft.getInstance().getSingleplayerServer().getRecipeManager();
        } else if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.recipeAccess() instanceof RecipeManager rm) {
            manager = rm;
        } else if (Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().getConnection().recipes() instanceof RecipeManager rm) {
            manager = rm;
        }

        if (manager != null) {
            CompressorRecipeHelper.scanDatapackAndKubeJSRecipes(manager);
            AlchemicalRecipeHelper.scanAutoSmeltingRecipes(manager);
        }

        registration.addRecipes(CompressionCategory.RECIPE_TYPE, CompressorRecipeHelper.getCompressionRecipes(manager));
        registration.addRecipes(SingularityCategory.RECIPE_TYPE, CompressorRecipeHelper.getSingularityRecipes());
        registration.addRecipes(DustCategory.RECIPE_TYPE, CompressorRecipeHelper.getDustRecipes());
        registration.addRecipes(AlchemicalSynthesizerCategory.RECIPE_TYPE, AlchemicalRecipeHelper.getRecipes());

        // Generator fuel recipes
        List<GeneratorFuelRecipe> fuels = new ArrayList<>();
        fuels.add(new GeneratorFuelRecipe(new ItemStack(ModBlocks.MOLDELONIAN_BLOCK.get()), 12000, 15000, 1500.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(ModItems.MOLDELONIAN_INGOT.get()), 1200, 5000, 1200.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(ModBlocks.SYNTHORIUM_BLOCK.get()), 6000, 2500, 800.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(ModItems.SYNTHORIUM_INGOT.get()), 600, 1000, 600.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(ModItems.SYNTHORIUM_SCRAP.get()), 200, 500, 400.0));

        TagKey<Item> uraniumIngotTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/uranium"));
        Optional<HolderSet.Named<Item>> uraniumHolders = BuiltInRegistries.ITEM.get(uraniumIngotTag);

        if (uraniumHolders.isPresent()) {
            for (Holder<Item> holder : uraniumHolders.get()) {
                fuels.add(new GeneratorFuelRecipe(new ItemStack(holder.value()), 1800, 3000, 1000.0));
            }
        }

        fuels.add(new GeneratorFuelRecipe(new ItemStack(Items.REDSTONE_BLOCK), 1200, 300, 300.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(Items.REDSTONE), 120, 200, 200.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(Items.COAL_BLOCK), 1600, 150, 250.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(Items.COAL), 200, 100, 150.0));
        fuels.add(new GeneratorFuelRecipe(new ItemStack(Items.CHARCOAL), 200, 100, 150.0));

        registration.addRecipes(GeneratorFuelCategory.RECIPE_TYPE, fuels);

        // 9x9 Synthesis Table Recipes
        List<RecipeHolder<Recipe<CraftingInput>>> synthesisRecipes = new ArrayList<>();
        if (manager != null) {
            for (RecipeHolder<?> holder : manager.getRecipes()) {
                if (holder.value().getType() == ModRecipes.SHAPED_SYNTHESIS_TYPE.get() ||
                        holder.value().getType() == ModRecipes.SHAPELESS_SYNTHESIS_TYPE.get()) {
                    synthesisRecipes.add((RecipeHolder<Recipe<CraftingInput>>) holder);
                }
            }
        } else {
            // Quando em servidor dedicado, obtém as receitas sincronizadas pelo pacote de rede!
            synthesisRecipes.addAll(dev.davidklgames.puremashtweaks.client.ClientSynthesisRecipeCache.getRecipes());
        }

        for (var customShapeless : SynthesisRecipeHelper.getShapelessRecipes()) {
            List<Ingredient> ingList = new ArrayList<>();
            for (Item item : customShapeless.ingredients()) {
                ingList.add(Ingredient.of(item));
            }

            ShapelessSynthesisRecipe dynRecipe = new ShapelessSynthesisRecipe(
                    "",
                    ingList,
                    new ItemStackTemplate(customShapeless.result().getItem(), customShapeless.result().getCount())
            );

            String itemPath = BuiltInRegistries.ITEM.getKey(customShapeless.result().getItem()).getPath();
            RecipeHolder<Recipe<CraftingInput>> dynHolder = new RecipeHolder<>(
                    ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis/" + itemPath)),
                    dynRecipe
            );
            synthesisRecipes.add(dynHolder);
        }

        registration.addRecipes(SynthesisTableCategory.RECIPE_TYPE, synthesisRecipes);
        JEIDescriptions.register(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()), CompressionCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()), SingularityCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()), DustCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SYNTHESIS_TABLE.get()), SynthesisTableCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ALCHEMICAL_SYNTHESIZER.get()), AlchemicalSynthesizerCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PUREMASH_GENERATOR.get()), GeneratorFuelCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SynthesisTableScreen.class, 174, 90, 22, 12, SynthesisTableCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(MultifunctionalCompressorScreen.class, 89, 35, 22, 16, CompressionCategory.RECIPE_TYPE, SingularityCategory.RECIPE_TYPE, DustCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(AlchemicalSynthesizerScreen.class, 51, 49, 24, 17, AlchemicalSynthesizerCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(PureMashGeneratorScreen.class, 53, 32, 20, 18, GeneratorFuelCategory.RECIPE_TYPE);
        registration.addGhostIngredientHandler(FilterScreen.class, new dev.davidklgames.puremashtweaks.api.compat.jei.handler.FilterGhostIngredientHandler());

        registration.addGenericGuiContainerHandler(BaseContainerScreen.class, new JEIContainerHandler());
    }

    @Override
    public void registerItemSubtypes(@NonNull ISubtypeRegistration registration) {
        var guideItem = BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("guideme", "guide"));
        var compType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(Identifier.fromNamespaceAndPath("guideme", "guide_id"));

        if (guideItem.isPresent() && compType.isPresent()) {
            registration.registerSubtypeInterpreter(guideItem.get().value(), (stack, context) -> {
                Identifier id = stack.get((DataComponentType<Identifier>) compType.get().value());
                return id != null ? id.toString() : "";
            });
        }
    }

    @Override
    public void registerExtraIngredients(@NonNull IExtraIngredientRegistration registration) {
        ItemStack guideBook = ModCreativeTabs.getGuideBook();
        if (!guideBook.isEmpty()) {
            registration.addExtraIngredients(VanillaTypes.ITEM_STACK, List.of(guideBook));
        }
    }
}