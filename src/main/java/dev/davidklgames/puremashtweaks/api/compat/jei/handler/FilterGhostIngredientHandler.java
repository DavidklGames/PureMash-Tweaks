package dev.davidklgames.puremashtweaks.api.compat.jei.handler;

import dev.davidklgames.puremashtweaks.client.screen.FilterScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FilterGhostIngredientHandler implements IGhostIngredientHandler<FilterScreen> {

    @Override
    public <I> @NotNull List<Target<I>> getTargetsTyped(@NotNull FilterScreen gui, @NotNull ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        int activeTab = 1; // Default to Item Mode
        if (gui.getMenu().getBlockEntity() != null) {
            activeTab = gui.getMenu().getBlockEntity().getSelectedTab(gui.getMenu().getSide());
        }

        // 1. FLUID MODE (Tab 2)
        if (activeTab == 2) {
            // A. Direct FluidStack dragged from JEI
            if (ingredient.getIngredient(NeoForgeTypes.FLUID_STACK).isPresent()) {
                targets.add(new Target<>() {
                    @Override
                    public @NotNull Rect2i getArea() {
                        return new Rect2i(gui.getLeftPos() + 8, gui.getTopPos() + 11, 16, 16);
                    }

                    @Override
                    public void accept(@NotNull I value) {
                        if (value instanceof FluidStack fluidStack && !fluidStack.isEmpty()) {
                            gui.onInsertFluid(fluidStack.copy());
                        }
                    }
                });
            }
            // B. Fluid Container (Bucket, Tank, etc.) dragged from JEI -> Extracts the fluid
            else if (ingredient.getItemStack().isPresent()) {
                ItemStack itemStack = ingredient.getItemStack().get();
                FluidStack containedFluid;

                if (itemStack.getItem() instanceof net.minecraft.world.item.BucketItem bucket && bucket.content != Fluids.EMPTY) {
                    containedFluid = new FluidStack(bucket.content, 1000);
                } else {
                    containedFluid = FluidUtil.getFirstStackContained(itemStack);
                }

                if (!containedFluid.isEmpty()) {
                    final FluidStack finalFluid = containedFluid.copy();
                    targets.add(new Target<>() {
                        @Override
                        public @NotNull Rect2i getArea() {
                            return new Rect2i(gui.getLeftPos() + 8, gui.getTopPos() + 11, 16, 16);
                        }

                        @Override
                        public void accept(@NotNull I value) {
                            gui.onInsertFluid(finalFluid);
                        }
                    });
                }
                // Non-fluid items are intentionally ignored when in Fluid Mode!
            }
        }
        // 2. ITEM MODE (Tab 1)
        else if (activeTab == 1) {
            // Only accept ItemStacks; pure FluidStacks from JEI are rejected
            if (ingredient.getItemStack().isPresent()) {
                targets.add(new Target<>() {
                    @Override
                    public @NotNull Rect2i getArea() {
                        return new Rect2i(gui.getLeftPos() + 8, gui.getTopPos() + 11, 16, 16);
                    }

                    @Override
                    public void accept(@NotNull I value) {
                        if (value instanceof ItemStack stack && !stack.isEmpty()) {
                            gui.onInsertStack(stack.copy());
                        }
                    }
                });
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {}
}