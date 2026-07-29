package dev.davidklgames.puremashtweaks.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class TankNbtHelper {

    /**
     * Safely retrieves the CompoundTag stored on the ItemStack (from CUSTOM_DATA or BLOCK_ENTITY_DATA).
     */
    public static @Nullable CompoundTag getTagFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && !customData.isEmpty()) {
            return customData.copyTag();
        }

        var blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            return blockEntityData.copyTagWithoutId();
        }

        return null;
    }

    /**
     * Reads and parses the FluidStack from the item tag, strictly ignoring block entity IDs.
     */
    public static @Nullable FluidStack readFluidFromTag(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag == null || tag.isEmpty()) return null;

        // Step 1: Attempt deserialization via NeoForge FluidStacksResourceHandler
        if (provider != null) {
            try {
                FluidStacksResourceHandler tempHandler = new FluidStacksResourceHandler(1, Integer.MAX_VALUE) {
                    @Override
                    protected void onContentsChanged(int index, FluidStack previousContents) {}
                };
                var input = TagValueInput.create(ProblemReporter.DISCARDING, provider, tag);
                tempHandler.deserialize(input);

                long amount = tempHandler.getAmountAsLong(0);
                var resource = tempHandler.getResource(0);

                if (amount > 0 && resource != null && !resource.isEmpty() && resource.getFluid() != Fluids.EMPTY) {
                    return new FluidStack(
                            resource.getFluid(),
                            (int) Math.min(amount, Integer.MAX_VALUE),
                            resource.getComponentsPatch()
                    );
                }
            } catch (Exception ignored) {}
        }

        // Step 2: Manual inspection strictly inside the "stacks" array
        if (tag.contains("stacks")) {
            Optional<ListTag> stacksListOpt = tag.getList("stacks");
            if (stacksListOpt.isPresent() && !stacksListOpt.get().isEmpty()) {
                Optional<CompoundTag> firstStackOpt = stacksListOpt.get().getCompound(0);
                if (firstStackOpt.isPresent()) {
                    CompoundTag stackTag = firstStackOpt.get();

                    int amount = stackTag.getIntOr("amount", stackTag.getIntOr("Amount", 0));
                    // Strict guard: if amount <= 0, the tank is empty!
                    if (amount <= 0) return null;

                    String fluidIdStr = null;

                    // NeoForge 26.1.2 structure: stackTag -> "resource" -> "fluid"
                    if (stackTag.contains("resource")) {
                        Optional<CompoundTag> resOpt = stackTag.getCompound("resource");
                        if (resOpt.isPresent()) {
                            CompoundTag resTag = resOpt.get();
                            fluidIdStr = resTag.getStringOr("fluid", resTag.getStringOr("id", ""));
                        }
                    }

                    if (fluidIdStr == null || fluidIdStr.isEmpty()) {
                        fluidIdStr = stackTag.getStringOr("fluid", stackTag.getStringOr("FluidName", ""));
                    }

                    if (fluidIdStr != null && !fluidIdStr.isEmpty()) {
                        Identifier id = Identifier.tryParse(fluidIdStr);
                        if (id != null) {
                            Optional<Holder.Reference<Fluid>> holder = BuiltInRegistries.FLUID.get(id);
                            if (holder.isPresent() && holder.get().value() != Fluids.EMPTY) {
                                return new FluidStack(holder.get().value(), amount);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public static CompoundTag serializeHandler(FluidStacksResourceHandler handler) {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        handler.serialize(output);
        return output.buildResult();
    }
}