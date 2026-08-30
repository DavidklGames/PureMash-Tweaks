package dev.davidklgames.puremashtweaks.api.compat.guideme;

import dev.davidklgames.puremashtweaks.registry.ModRecipes;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.compiler.tags.RecipeTypeMappingSupplier.RecipeTypeMappings;

public class RecipeTypeContributions implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        // Mapeia o layout 9x9 para as receitas Shaped e Shapeless da Mesa de Síntese
        mappings.add(ModRecipes.SHAPED_SYNTHESIS_TYPE.get(), LytSynthesis9x9Recipe::create);
        mappings.add(ModRecipes.SHAPELESS_SYNTHESIS_TYPE.get(), LytSynthesis9x9Recipe::create);
    }
}