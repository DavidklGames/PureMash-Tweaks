package dev.davidklgames.puremashtweaks.api.compat.jei;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.compat.jei.transfer.AlchemicalSynthesizerTransferInfo;
import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;
import dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper;
import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper;
import dev.davidklgames.puremashtweaks.api.compat.jei.category.CompressionCategory;
import dev.davidklgames.puremashtweaks.api.compat.jei.category.SingularityCategory;
import dev.davidklgames.puremashtweaks.api.compat.jei.category.DustCategory;
import dev.davidklgames.puremashtweaks.api.compat.jei.category.SynthesisTableCategory;
import dev.davidklgames.puremashtweaks.api.compat.jei.category.AlchemicalSynthesizerCategory;
import dev.davidklgames.puremashtweaks.api.compat.jei.handler.JEIContainerHandler;
import dev.davidklgames.puremashtweaks.api.compat.jei.transfer.SynthesisTableCraftingTransferInfo;
import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModMenus;
import dev.davidklgames.puremashtweaks.registry.ModRecipes;
import dev.davidklgames.puremashtweaks.client.screen.BaseContainerScreen;
import dev.davidklgames.puremashtweaks.client.screen.SynthesisTableScreen;
import dev.davidklgames.puremashtweaks.client.screen.MultifunctionalCompressorScreen;
import dev.davidklgames.puremashtweaks.client.screen.AlchemicalSynthesizerScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("removal")
@JeiPlugin
public class PureMashJEIPlugin implements IModPlugin {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "jei_plugin_puremashtweaks");

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
                new AlchemicalSynthesizerCategory(helper) // Registration of the new machine category
        );
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(SynthesisTableMenu.class, ModMenus.SYNTHESIS_TABLE_MENU.get(), SynthesisTableCategory.RECIPE_TYPE, 0, 81, 83, 36);
        registration.addRecipeTransferHandler(new SynthesisTableCraftingTransferInfo());

        // =========================================================================
        // RECIPE TRANSFER REGISTRATION FOR THE ALCHEMICAL SYNTHESIZER
        // =========================================================================
        registration.addRecipeTransferHandler(new AlchemicalSynthesizerTransferInfo());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
            net.minecraft.world.item.crafting.RecipeManager manager = null;

            if (world.recipeAccess() instanceof net.minecraft.world.item.crafting.RecipeManager) {
                manager = (net.minecraft.world.item.crafting.RecipeManager) world.recipeAccess();
            } else if (net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
                manager = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getRecipeManager();
            }

            // 1. Registers recipes for the Compressor tabs
            registration.addRecipes(CompressionCategory.RECIPE_TYPE, CompressorRecipeHelper.getCompressionRecipes(manager));
            registration.addRecipes(SingularityCategory.RECIPE_TYPE, CompressorRecipeHelper.getSingularityRecipes());
            registration.addRecipes(DustCategory.RECIPE_TYPE, CompressorRecipeHelper.getDustRecipes());

            // 2. Triggers the automatic scanner so that JEI populates all in-game smelting recipes in the new machine's tab
            if (manager != null) {
                AlchemicalRecipeHelper.scanAutoSmeltingRecipes(manager);
            }
            registration.addRecipes(AlchemicalSynthesizerCategory.RECIPE_TYPE, AlchemicalRecipeHelper.getRecipes());

            // 3. Registers recipes for the Synthesis Table (9x9)
            if (manager != null) {
                List<RecipeHolder<Recipe<net.minecraft.world.item.crafting.CraftingInput>>> synthesisRecipes = new ArrayList<>();
                for (RecipeHolder<?> holder : manager.getRecipes()) {
                    if (holder.value().getType() == ModRecipes.SHAPED_SYNTHESIS_TYPE.get() ||
                            holder.value().getType() == ModRecipes.SHAPELESS_SYNTHESIS_TYPE.get()) {
                        synthesisRecipes.add((RecipeHolder<Recipe<net.minecraft.world.item.crafting.CraftingInput>>) holder);
                    }
                }
                registration.addRecipes(SynthesisTableCategory.RECIPE_TYPE, synthesisRecipes);
            }
            // Custom Description for Items in JEI.
            dev.davidklgames.puremashtweaks.api.compat.jei.JEIDescriptions.register(registration);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()), CompressionCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()), SingularityCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MULTIFUNCTIONAL_COMPRESSOR.get()), DustCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SYNTHESIS_TABLE.get()), SynthesisTableCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SYNTHESIS_TABLE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);

        // Registration of the Alchemical Synthesizer as the physical catalyst for its recipes in JEI
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ALCHEMICAL_SYNTHESIZER.get()), AlchemicalSynthesizerCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SynthesisTableScreen.class, 174, 90, 22, 12, SynthesisTableCategory.RECIPE_TYPE);

        registration.addRecipeClickArea(MultifunctionalCompressorScreen.class, 89, 35, 22, 16, CompressionCategory.RECIPE_TYPE, SingularityCategory.RECIPE_TYPE, DustCategory.RECIPE_TYPE);

        // Click area on the center arrow of the Alchemical Synthesizer to open its recipes in JEI
        registration.addRecipeClickArea(AlchemicalSynthesizerScreen.class, 51, 49, 24, 17, AlchemicalSynthesizerCategory.RECIPE_TYPE);

        registration.addGenericGuiContainerHandler(BaseContainerScreen.class, new JEIContainerHandler());
    }
}