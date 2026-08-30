package dev.davidklgames.puremashtweaks.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class PureMashDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PureMashTweaks.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> RECIPE_CARD_DATA =
            COMPONENTS.register("recipe_card_data", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> BOUND_CONTAINER =
            COMPONENTS.register("bound_container", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> ITEM_DATA =
            COMPONENTS.register("item_data", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> FLUID_DATA =
            COMPONENTS.register("fluid_data", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> ENERGY_DATA =
            COMPONENTS.register("energy_data", () -> DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG)
                    .build());

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}