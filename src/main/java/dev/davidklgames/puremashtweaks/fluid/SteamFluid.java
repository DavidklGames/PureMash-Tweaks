package dev.davidklgames.puremashtweaks.fluid;

import dev.davidklgames.puremashtweaks.registry.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.NonNull;

public abstract class SteamFluid extends LavaFluid {
    public SteamFluid() {}

    @Override
    public @NonNull FluidType getFluidType() {
        return ModFluids.STEAM_TYPE.get();
    }

    @Override
    public @NonNull Flowing getFlowing() {
        return ModFluids.STEAM_FLOWING.get();
    }

    @Override
    public @NonNull Source getSource() {
        return ModFluids.STEAM_SOURCE.get();
    }

    @Override
    public @NonNull BucketItem getBucket() {
        return (BucketItem) Items.AIR;
    }

    @Override
    public @NonNull BlockState createLegacyBlock(@NonNull FluidState fluidState) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSame(@NonNull Fluid other) {
        return other == ModFluids.STEAM_SOURCE.get() || other == ModFluids.STEAM_FLOWING.get();
    }

    public static final class Flowing extends SteamFluid {
        public Flowing() {}

        @Override
        protected void createFluidStateDefinition(StateDefinition.@NonNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(FlowingFluid.LEVEL);
        }

        @Override
        public boolean isSource(@NonNull FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(FlowingFluid.LEVEL);
        }
    }

    public static final class Source extends SteamFluid {
        public Source() {}

        @Override
        public boolean isSource(@NonNull FluidState state) {
            return true;
        }

        @Override
        public int getAmount(@NonNull FluidState state) {
            return 8;
        }
    }
}