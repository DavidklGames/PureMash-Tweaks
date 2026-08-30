package dev.davidklgames.puremashtweaks.fluid;

import dev.davidklgames.puremashtweaks.registry.ModBlocks;
import dev.davidklgames.puremashtweaks.registry.ModFluids;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class MoltenMoldelonianFluid extends LavaFluid {
    public MoltenMoldelonianFluid() {
    }

    @Override
    public FluidType getFluidType() {
        return ModFluids.MOLTEN_MOLDELONIAN_TYPE.get();
    }

    @Override
    public Flowing getFlowing() {
        return ModFluids.MOLTEN_MOLDELONIAN_FLOWING.get();
    }

    @Override
    public Source getSource() {
        return ModFluids.MOLTEN_MOLDELONIAN_SOURCE.get();
    }

    @Override
    public BucketItem getBucket() {
        return ModItems.MOLTEN_MOLDELONIAN_BUCKET.get();
    }

    @Override
    public BlockState createLegacyBlock(FluidState fluidState) {
        return ModBlocks.MOLTEN_MOLDELONIAN_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, FlowingFluid.getLegacyLevel(fluidState));
    }

    @Override
    public boolean isSame(Fluid other) {
        return other == ModFluids.MOLTEN_MOLDELONIAN_SOURCE.get() || other == ModFluids.MOLTEN_MOLDELONIAN_FLOWING.get();
    }

    public static final class Flowing extends MoltenMoldelonianFluid {
        public Flowing() {
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(new Property[]{FlowingFluid.LEVEL});
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(FlowingFluid.LEVEL);
        }
    }

    public static final class Source extends MoltenMoldelonianFluid {
        public Source() {
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }
}