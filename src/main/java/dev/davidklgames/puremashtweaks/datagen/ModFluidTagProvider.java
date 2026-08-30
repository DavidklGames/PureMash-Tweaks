package dev.davidklgames.puremashtweaks.datagen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagProvider extends FluidTagsProvider {

    public ModFluidTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PureMashTweaks.MODID);
    }

    private TagKey<Fluid> commonFluidTag(String path) {
        return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        this.tag(commonFluidTag("molten_synthorium"))
                .add(ModFluids.MOLTEN_SYNTHORIUM_SOURCE.get())
                .add(ModFluids.MOLTEN_SYNTHORIUM_FLOWING.get());

        this.tag(commonFluidTag("molten_moldelonian"))
                .add(ModFluids.MOLTEN_MOLDELONIAN_SOURCE.get())
                .add(ModFluids.MOLTEN_MOLDELONIAN_FLOWING.get());
    }
}