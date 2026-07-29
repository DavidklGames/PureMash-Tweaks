package dev.davidklgames.puremashtweaks.api.compat.jade;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.MultifunctionalCompressorBlock;
import dev.davidklgames.puremashtweaks.block.SynthesisTableBlock;
import dev.davidklgames.puremashtweaks.block.entity.PureMashCoreBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {

    // Common registry (Server) - Synchronizes Block Entity data to the Client
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PureMashCoreServerDataProvider.INSTANCE, PureMashCoreBlockEntity.class);
    }

    // Client registry - Renders Tooltips on the player's screen
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CompressorComponentProvider.INSTANCE, MultifunctionalCompressorBlock.class);
        registration.registerBlockComponent(CraftingComponentProvider.INSTANCE, SynthesisTableBlock.class);
        registration.registerBlockComponent(PureMashCoreComponentProvider.INSTANCE, dev.davidklgames.puremashtweaks.block.PureMashCoreBlock.class);
    }

    // --- PUREMASH CORE SERVER DATA PROVIDER ---
    public enum PureMashCoreServerDataProvider implements IServerDataProvider<BlockAccessor> { // Using BlockAccessor as Generic
        INSTANCE;

        @Override
        public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
            BlockEntity be = accessor.getBlockEntity();
            if (be instanceof PureMashCoreBlockEntity coreBe) {
                // Writes the actual server states to the Jade packet
                tag.putInt("OverloadLevel", coreBe.getOverloadLevel());
                tag.putBoolean("Active", coreBe.isActive());
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "puremash_core_server");
        }
    }

    // --- PUREMASH CORE TOOLTIP PROVIDER (CLIENT) ---
    public enum PureMashCoreComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();

            // Only processes and displays if Overload data exists and the level is greater than zero
            if (serverData.contains("OverloadLevel")) {
                int overloadLvl = serverData.getIntOr("OverloadLevel", 0);

                if (overloadLvl > 0) {
                    boolean active = serverData.getBooleanOr("Active", true);

                    // 1. Shows the State: Enabled or Disabled
                    Component stateComponent = active ?
                            Component.literal("State: Enabled").withStyle(net.minecraft.ChatFormatting.GREEN) :
                            Component.literal("State: Disabled").withStyle(net.minecraft.ChatFormatting.RED);
                    tooltip.add(stateComponent);

                    // 2. Calculates the speed based on the mod's configurations
                    int speed = (overloadLvl >= 3) ?
                            dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.OVERLOAD_SPEED_MULTIPLIER.get() + 1 :
                            dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.OVERLOAD_SPEED_LVL1_2.get();

                    // 3. Shows the acceleration level in percentage
                    Component speedComponent = Component.literal("Acceleration: +" + (speed * 100) + "%")
                            .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE);
                    tooltip.add(speedComponent);
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "puremash_core_client");
        }
    }

    public enum CompressorComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();

            if (serverData.contains("Progress")) {
                int progress = serverData.getIntOr("Progress", 0);
                int mode = serverData.getIntOr("Mode", 0);

                int maxProgress = (mode == 1) ?
                        dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.COMPRESSOR_SPEED_SINGULARITY.get() :
                        dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig.COMPRESSOR_SPEED_ITEMS.get();

                if (progress > 0) {
                    int percent = (int) (((float) progress / maxProgress) * 100);
                    tooltip.add(Component.literal("Processing: " + percent + "%"));
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "multifunctional_compressor");
        }
    }

    public enum CraftingComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();

            if (serverData.contains("Items")) {
                serverData.getList("Items").ifPresent(itemsList -> {
                    for (int i = 0; i < itemsList.size(); i++) {
                        CompoundTag itemTag = itemsList.getCompoundOrEmpty(i);
                        if (itemTag.getIntOr("Slot", -1) == 81) {
                            var context = accessor.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
                            ItemStack resultStack = ItemStack.CODEC.parse(context, itemTag)
                                    .result()
                                    .orElse(ItemStack.EMPTY);

                            if (!resultStack.isEmpty()) {
                                tooltip.add(Component.literal("Result: " + resultStack.getHoverName().getString())
                                        .withStyle(net.minecraft.ChatFormatting.YELLOW));
                            }
                            break;
                        }
                    }
                });
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "crafting_table");
        }
    }
}