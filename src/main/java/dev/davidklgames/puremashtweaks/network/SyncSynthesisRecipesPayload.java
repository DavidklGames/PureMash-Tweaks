package dev.davidklgames.puremashtweaks.network;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.recipe.ShapedSynthesisRecipe;
import dev.davidklgames.puremashtweaks.recipe.ShapelessSynthesisRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record SyncSynthesisRecipesPayload(List<RecipeHolder<Recipe<CraftingInput>>> recipes) implements CustomPacketPayload {
    public static final Type<SyncSynthesisRecipesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "sync_synthesis_recipes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSynthesisRecipesPayload> STREAM_CODEC = StreamCodec.of(
            SyncSynthesisRecipesPayload::encode,
            SyncSynthesisRecipesPayload::decode
    );

    private static void encode(RegistryFriendlyByteBuf buf, SyncSynthesisRecipesPayload payload) {
        buf.writeVarInt(payload.recipes.size());
        for (RecipeHolder<Recipe<CraftingInput>> holder : payload.recipes) {
            buf.writeResourceKey(holder.id());
            boolean isShaped = holder.value() instanceof ShapedSynthesisRecipe;
            buf.writeBoolean(isShaped);
            if (isShaped) {
                ShapedSynthesisRecipe.STREAM_CODEC.encode(buf, (ShapedSynthesisRecipe) holder.value());
            } else {
                ShapelessSynthesisRecipe.STREAM_CODEC.encode(buf, (ShapelessSynthesisRecipe) holder.value());
            }
        }
    }

    private static SyncSynthesisRecipesPayload decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<RecipeHolder<Recipe<CraftingInput>>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceKey<Recipe<?>> id = buf.readResourceKey(Registries.RECIPE);
            boolean isShaped = buf.readBoolean();
            Recipe<CraftingInput> recipe;
            if (isShaped) {
                recipe = ShapedSynthesisRecipe.STREAM_CODEC.decode(buf);
            } else {
                recipe = ShapelessSynthesisRecipe.STREAM_CODEC.decode(buf);
            }
            list.add(new RecipeHolder<>(id, recipe));
        }
        return new SyncSynthesisRecipesPayload(list);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}