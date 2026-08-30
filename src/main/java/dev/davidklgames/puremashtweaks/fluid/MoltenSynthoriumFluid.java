package dev.davidklgames.puremashtweaks.fluid;

import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModFluids;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.NonNull;

public abstract class MoltenSynthoriumFluid extends LavaFluid {
    public MoltenSynthoriumFluid() {
    }

    @Override
    public @NonNull FluidType getFluidType() {
        return ModFluids.MOLTEN_SYNTHORIUM_TYPE.get();
    }

    @Override
    public @NonNull Flowing getFlowing() {
        return ModFluids.MOLTEN_SYNTHORIUM_FLOWING.get();
    }

    @Override
    public @NonNull Source getSource() {
        return ModFluids.MOLTEN_SYNTHORIUM_SOURCE.get();
    }

    @Override
    public @NonNull BucketItem getBucket() {
        return ModItems.MOLTEN_SYNTHORIUM_BUCKET.get();
    }

    @Override
    public @NonNull BlockState createLegacyBlock(@NonNull FluidState fluidState) {
        return ModBlocks.MOLTEN_SYNTHORIUM_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, FlowingFluid.getLegacyLevel(fluidState));
    }

    @Override
    public boolean isSame(@NonNull Fluid other) {
        return other == ModFluids.MOLTEN_SYNTHORIUM_SOURCE.get() || other == ModFluids.MOLTEN_SYNTHORIUM_FLOWING.get();
    }

    public static final class Flowing extends MoltenSynthoriumFluid {
        public Flowing() {
        }

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

    public static final class Source extends MoltenSynthoriumFluid {
        public Source() {
        }

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