package dev.davidklgames.puremashtweaks.util;

import dev.davidklgames.puremashtweaks.registry.PureMashDataComponents;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

/**
 * Static utility helper for Memory Card operations and pattern data handling
 * across the PureMash Tweaks Synthesis Table ecosystem (Minecraft 26.1.2).
 */
public class SynthesisTableHelper {

    /**
     * Checks if a given stack is a valid Memory Card.
     */
    public static boolean isMemoryCard(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ModItems.MEMORY_CARD.get());
    }

    /**
     * Checks if the card has an encoded 9x9 recipe data component.
     */
    public static boolean hasEncodedRecipe(ItemStack cardStack) {
        if (!isMemoryCard(cardStack)) return false;
        return cardStack.has(PureMashDataComponents.RECIPE_CARD_DATA.get());
    }

    /**
     * Extracts the recipe CompoundTag stored on a Memory Card.
     */
    public static CompoundTag getRecipeTag(ItemStack cardStack) {
        if (!hasEncodedRecipe(cardStack)) return null;
        return cardStack.get(PureMashDataComponents.RECIPE_CARD_DATA.get());
    }

    /**
     * Clears the encoded recipe data from a Memory Card.
     */
    public static void clearMemoryCard(ItemStack cardStack) {
        if (isMemoryCard(cardStack)) {
            cardStack.remove(PureMashDataComponents.RECIPE_CARD_DATA.get());
        }
    }

    /**
     * Reads an array of 81 ghost item stacks stored inside a Memory Card tag.
     */
    public static ItemStack[] readGridFromCard(ItemStack cardStack, HolderLookup.Provider registries) {
        ItemStack[] grid = new ItemStack[81];
        for (int i = 0; i < 81; i++) grid[i] = ItemStack.EMPTY;

        CompoundTag tag = getRecipeTag(cardStack);
        if (tag == null || registries == null) return grid;

        ListTag itemsList = tag.getListOrEmpty("GridItems");
        var context = registries.createSerializationContext(NbtOps.INSTANCE);

        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompoundOrEmpty(i);
            int slot = itemTag.getIntOr("Slot", -1);
            if (slot >= 0 && slot < 81) {
                grid[slot] = ItemStack.CODEC.parse(context, itemTag.getCompoundOrEmpty("Item"))
                        .result().orElse(ItemStack.EMPTY);
            }
        }
        return grid;
    }

    /**
     * Reads the output ItemStack stored inside an encoded Memory Card.
     */
    public static ItemStack readOutputFromCard(ItemStack cardStack, HolderLookup.Provider registries) {
        if (!hasEncodedRecipe(cardStack) || registries == null) return ItemStack.EMPTY;
        CompoundTag tag = getRecipeTag(cardStack);
        if (tag == null || !tag.contains("OutputItem")) return ItemStack.EMPTY;

        var context = registries.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.parse(context, tag.getCompoundOrEmpty("OutputItem"))
                .result().orElse(ItemStack.EMPTY);
    }

    /**
     * Encodes a 9x9 recipe grid and its result ItemStack into a Memory Card.
     */
    public static void encodeRecipe(ItemStack cardStack, ItemStack[] grid, ItemStack outputStack, HolderLookup.Provider registries) {
        if (!isMemoryCard(cardStack) || registries == null) return;

        CompoundTag recipeTag = new CompoundTag();
        ListTag itemsList = new ListTag();
        var context = registries.createSerializationContext(NbtOps.INSTANCE);

        for (int i = 0; i < 81 && i < grid.length; i++) {
            ItemStack stack = grid[i];
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                ItemStack.CODEC.encodeStart(context, stack).result().ifPresent(tag -> {
                    if (tag instanceof CompoundTag compound) {
                        itemTag.put("Item", compound);
                    }
                });
                itemsList.add(itemTag);
            }
        }

        recipeTag.put("GridItems", itemsList);

        if (!outputStack.isEmpty()) {
            recipeTag.putString("OutputName", outputStack.getHoverName().getString());
            recipeTag.putInt("OutputCount", outputStack.getCount());
            ItemStack.CODEC.encodeStart(context, outputStack).result().ifPresent(tag -> {
                if (tag instanceof CompoundTag compound) {
                    recipeTag.put("OutputItem", compound);
                }
            });
        } else {
            recipeTag.putString("OutputName", "Empty Craft");
            recipeTag.putInt("OutputCount", 0);
        }

        cardStack.set(PureMashDataComponents.RECIPE_CARD_DATA.get(), recipeTag);
    }
}