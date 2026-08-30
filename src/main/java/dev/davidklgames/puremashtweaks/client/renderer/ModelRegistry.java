package dev.davidklgames.puremashtweaks.client.renderer;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ModelRegistry {

    public static void onModelRegister(ModelEvent.RegisterStandalone event) {
        for (Model model : Model.values()) {
            event.register(model.getModelKey(), new SimpleUnbakedStandaloneModel(model.resource, (resolvedModel, baker, name) -> {
                ResolvedModel resolved = baker.getModel(model.getIdentifier());
                TextureSlots textureSlots = resolved.getTopTextureSlots();
                return resolvedModel.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
            }));
        }
    }

    public static void onModelBake(ModelEvent.BakingCompleted event) {
        for (Model model : Model.values()) {
            QuadCollection quads = event.getBakingResult().standaloneModels().get(model.getModelKey());
            if (quads != null) {
                model.getModel().set(quads);
            }
        }
    }

    public enum Model {
        SYNTHORIUM_EXTRACT(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "block/synthorium_universal_cable_extract")),
        MOLDELONIAN_EXTRACT(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "block/moldelonian_universal_cable_extract"));

        private final Identifier resource;
        private final StandaloneModelKey<QuadCollection> modelKey;
        private final AtomicReference<QuadCollection> model;

        Model(Identifier rl) {
            this.resource = rl;
            Objects.requireNonNull(rl);
            this.modelKey = new StandaloneModelKey<>(rl::toString);
            this.model = new AtomicReference<>();
        }

        public Identifier getIdentifier() {
            return this.resource;
        }

        public StandaloneModelKey<QuadCollection> getModelKey() {
            return this.modelKey;
        }

        public AtomicReference<QuadCollection> getModel() {
            return this.model;
        }
    }
}