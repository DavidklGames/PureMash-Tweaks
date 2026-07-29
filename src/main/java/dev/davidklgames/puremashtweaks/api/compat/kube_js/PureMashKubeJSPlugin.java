package dev.davidklgames.puremashtweaks.api.compat.kube_js;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

public class PureMashKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        // Maps 9x9 Synthesis recipes using the native inherited methods of KubeJS 26.1.2.
        registry.namespace("puremashtweaks").shaped("shaped_synthesis");
        registry.namespace("puremashtweaks").shapeless("shapeless_synthesis");
    }
}