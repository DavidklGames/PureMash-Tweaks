package dev.davidklgames.puremashtweaks.client;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientSynthesisRecipeCache {
    private static final List<RecipeHolder<Recipe<CraftingInput>>> RECIPES = new ArrayList<>();

    public static void setRecipes(List<RecipeHolder<Recipe<CraftingInput>>> recipes) {
        RECIPES.clear();
        RECIPES.addAll(recipes);
    }

    public static List<RecipeHolder<Recipe<CraftingInput>>> getRecipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static void clear() {
        RECIPES.clear();
    }
}