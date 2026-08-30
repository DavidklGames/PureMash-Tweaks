package dev.davidklgames.puremashtweaks.registry;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.fluid.MoltenMoldelonianFluid;
import dev.davidklgames.puremashtweaks.fluid.MoltenSynthoriumFluid;
import dev.davidklgames.puremashtweaks.fluid.SteamFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, PureMashTweaks.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, PureMashTweaks.MODID);

    // --- MOLTEN SYNTHORIUM ---
    public static final DeferredHolder<Fluid, MoltenSynthoriumFluid.Source> MOLTEN_SYNTHORIUM_SOURCE =
            FLUIDS.register("molten_synthorium", MoltenSynthoriumFluid.Source::new);

    public static final DeferredHolder<Fluid, MoltenSynthoriumFluid.Flowing> MOLTEN_SYNTHORIUM_FLOWING =
            FLUIDS.register("flowing_molten_synthorium", MoltenSynthoriumFluid.Flowing::new);

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_SYNTHORIUM_TYPE =
            FLUID_TYPES.register("molten_synthorium", () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.puremashtweaks.molten_synthorium")
                            .lightLevel(15)
                            .density(3000)
                            .viscosity(6000)
                            .temperature(3000)
                            .motionScale(0.0023)
                            .canExtinguish(false)
                            .supportsBoating(true)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                            .canHydrate(false)
            ));

    // --- MOLTEN MOLDELONIAN ---
    public static final DeferredHolder<Fluid, MoltenMoldelonianFluid.Source> MOLTEN_MOLDELONIAN_SOURCE =
            FLUIDS.register("molten_moldelonian", MoltenMoldelonianFluid.Source::new);

    public static final DeferredHolder<Fluid, MoltenMoldelonianFluid.Flowing> MOLTEN_MOLDELONIAN_FLOWING =
            FLUIDS.register("flowing_molten_moldelonian", MoltenMoldelonianFluid.Flowing::new);

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_MOLDELONIAN_TYPE =
            FLUID_TYPES.register("molten_moldelonian", () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.puremashtweaks.molten_moldelonian")
                            .lightLevel(15)
                            .density(4500)
                            .viscosity(8000)
                            .temperature(4000)
                            .motionScale(0.0023)
                            .canExtinguish(false)
                            .supportsBoating(true)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                            .canHydrate(false)
            ));

    // --- STEAM (VAPOR) ---
    public static final DeferredHolder<Fluid, SteamFluid.Source> STEAM_SOURCE =
            FLUIDS.register("steam", SteamFluid.Source::new);

    public static final DeferredHolder<Fluid, SteamFluid.Flowing> STEAM_FLOWING =
            FLUIDS.register("flowing_steam", SteamFluid.Flowing::new);

    public static final DeferredHolder<FluidType, FluidType> STEAM_TYPE =
            FLUID_TYPES.register("steam", () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.puremashtweaks.steam")
                            .density(-1000)
                            .viscosity(200)
                            .temperature(500)
            ));

    public static void register(IEventBus bus) {
        FLUIDS.register(bus);
        FLUID_TYPES.register(bus);
    }
}