package dev.davidklgames.puremashtweaks.api.compat.jade;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.AlchemicalRecipeHelper;
import dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper;
import dev.davidklgames.puremashtweaks.block.AlchemicalSynthesizerBlock;
import dev.davidklgames.puremashtweaks.block.MultifunctionalCompressorBlock;
import dev.davidklgames.puremashtweaks.block.PureMashCoreBlock;
import dev.davidklgames.puremashtweaks.block.SynthesisTableBlock;
import dev.davidklgames.puremashtweaks.block.UniversalCableBlock;
import dev.davidklgames.puremashtweaks.block.entity.AlchemicalSynthesizerBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.PureMashCoreBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;
import dev.davidklgames.puremashtweaks.block.entity.UniversalCableBlockEntity;
import dev.davidklgames.puremashtweaks.config.PureMashTweaksConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PureMashCoreServerDataProvider.INSTANCE, PureMashCoreBlockEntity.class);
        registration.registerBlockDataProvider(CompressorServerDataProvider.INSTANCE, MultifunctionalCompressorBlockEntity.class);
        registration.registerBlockDataProvider(AlchemicalSynthesizerServerDataProvider.INSTANCE, AlchemicalSynthesizerBlockEntity.class);
        registration.registerBlockDataProvider(SynthesisTableServerDataProvider.INSTANCE, SynthesisTableBlockEntity.class);
        registration.registerBlockDataProvider(UniversalCableServerDataProvider.INSTANCE, UniversalCableBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PureMashCoreComponentProvider.INSTANCE, PureMashCoreBlock.class);
        registration.registerBlockComponent(CompressorComponentProvider.INSTANCE, MultifunctionalCompressorBlock.class);
        registration.registerBlockComponent(AlchemicalSynthesizerComponentProvider.INSTANCE, AlchemicalSynthesizerBlock.class);
        registration.registerBlockComponent(CraftingComponentProvider.INSTANCE, SynthesisTableBlock.class);
        registration.registerBlockComponent(UniversalCableComponentProvider.INSTANCE, UniversalCableBlock.class);
    }

    // =========================================================================
    // 1. PUREMASH CORE BLOCK PROVIDERS
    // =========================================================================
    public enum PureMashCoreServerDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
            BlockEntity be = accessor.getBlockEntity();
            if (be instanceof PureMashCoreBlockEntity coreBe) {
                tag.putInt("OverloadLevel", coreBe.getOverloadLevel());
                tag.putBoolean("Active", coreBe.isActive());
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "puremash_core_server");
        }
    }

    public enum PureMashCoreComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();

            if (serverData.contains("OverloadLevel")) {
                int overloadLvl = serverData.getIntOr("OverloadLevel", 0);

                if (overloadLvl > 0) {
                    boolean active = serverData.getBooleanOr("Active", true);

                    Component stateComponent = active ?
                            Component.literal("State: Enabled").withStyle(ChatFormatting.GREEN) :
                            Component.literal("State: Disabled").withStyle(ChatFormatting.RED);
                    tooltip.add(stateComponent);

                    int speed = (overloadLvl >= 3) ?
                            PureMashTweaksConfig.COMMON.overloadSpeedMultiplier.get() + 1 :
                            PureMashTweaksConfig.COMMON.overloadSpeedLvl1_2.get();

                    Component speedComponent = Component.literal("Acceleration: +" + (speed * 100) + "%")
                            .withStyle(ChatFormatting.LIGHT_PURPLE);
                    tooltip.add(speedComponent);
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "puremash_core_client");
        }
    }

    // =========================================================================
    // 2. MULTIFUNCTIONAL COMPRESSOR (Input -> Native Progress Arrow -> Output)
    // =========================================================================
    @SuppressWarnings("removal")
    public enum CompressorServerDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
            BlockEntity be = accessor.getBlockEntity();
            if (be instanceof MultifunctionalCompressorBlockEntity compressorBe) {
                tag.putInt("Progress", compressorBe.getProgress());
                tag.putInt("MaxProgress", compressorBe.getMaxProgress());
                tag.putInt("Mode", compressorBe.getMode());

                int mode = compressorBe.getMode();
                ItemStack input = compressorBe.inventory.getStackInSlot(0);
                ItemStack output = compressorBe.inventory.getStackInSlot(1);

                ItemStack sampleInput = input;
                if (sampleInput.isEmpty() && mode == 1 && compressorBe.getSingularityItem() != Items.AIR) {
                    sampleInput = new ItemStack(compressorBe.getSingularityItem());
                }

                var recipe = CompressorRecipeHelper.getRecipe(accessor.getLevel(), sampleInput, mode);
                var context = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);

                if (!sampleInput.isEmpty()) {
                    ItemStack.CODEC.encodeStart(context, sampleInput).result().ifPresent(t -> {
                        if (t instanceof CompoundTag compound) tag.put("InputItem", compound);
                    });
                }

                ItemStack resultStack = recipe != null ? recipe.result() : output;
                if (!resultStack.isEmpty()) {
                    ItemStack.CODEC.encodeStart(context, resultStack).result().ifPresent(t -> {
                        if (t instanceof CompoundTag compound) tag.put("OutputItem", compound);
                    });
                }

                if (mode == 1 && recipe != null) {
                    tag.putInt("SingularityCount", compressorBe.getSingularityCount());
                    tag.putInt("SingularityCost", recipe.cost());
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "compressor_server");
        }
    }

    public enum CompressorComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();

            if (serverData.contains("InputItem") && serverData.contains("OutputItem")) {
                var context = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
                ItemStack inputStack = ItemStack.CODEC.parse(context, serverData.getCompoundOrEmpty("InputItem")).result().orElse(ItemStack.EMPTY);
                ItemStack outputStack = ItemStack.CODEC.parse(context, serverData.getCompoundOrEmpty("OutputItem")).result().orElse(ItemStack.EMPTY);

                int progress = serverData.getIntOr("Progress", 0);
                int maxProgress = serverData.getIntOr("MaxProgress", 20);
                float progressRatio = maxProgress > 0 ? Mth.clamp((float) progress / (float) maxProgress, 0.0F, 1.0F) : 0.0F;

                if (!inputStack.isEmpty() && !outputStack.isEmpty()) {
                    // [Input Item] -> [Espaço 4px] -> [Seta Jade] -> [Espaço 4px] -> [Output Item]
                    tooltip.add(JadeUI.item(inputStack));
                    tooltip.append(JadeUI.spacer(4, 0));
                    tooltip.append(JadeUI.progressArrow(progressRatio));
                    tooltip.append(JadeUI.spacer(4, 0));
                    tooltip.append(JadeUI.item(outputStack));
                }

                int mode = serverData.getIntOr("Mode", 0);
                if (mode == 1 && serverData.contains("SingularityCost")) {
                    int count = serverData.getIntOr("SingularityCount", 0);
                    int cost = serverData.getIntOr("SingularityCost", 1000);
                    tooltip.add(Component.literal("Condensed: " + String.format("%,d", count) + " / " + String.format("%,d", cost)).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "multifunctional_compressor");
        }
    }

    // =========================================================================
    // 3. ALCHEMICAL SYNTHESIZER (Input -> Native Progress Arrow -> Output)
    // =========================================================================
    @SuppressWarnings("removal")
    public enum AlchemicalSynthesizerServerDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
            BlockEntity be = accessor.getBlockEntity();
            if (be instanceof AlchemicalSynthesizerBlockEntity synthBe) {
                tag.putInt("Progress", synthBe.getProgress());
                tag.putInt("MaxProgress", synthBe.getMaxProgress());

                ItemStack input = synthBe.inventory.getStackInSlot(1);
                ItemStack tool = synthBe.inventory.getStackInSlot(2);
                Fluid fluid = synthBe.fluidTank.getResource(0).getFluid();

                var recipe = AlchemicalRecipeHelper.getRecipe(
                        fluid,
                        input,
                        tool,
                        accessor.getLevel().getServer() != null ? accessor.getLevel().getServer().getRecipeManager() : null
                );

                var context = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);

                if (!input.isEmpty()) {
                    ItemStack.CODEC.encodeStart(context, input).result().ifPresent(t -> {
                        if (t instanceof CompoundTag compound) tag.put("InputItem", compound);
                    });
                }

                if (recipe != null && !recipe.output().isEmpty()) {
                    ItemStack.CODEC.encodeStart(context, recipe.output()).result().ifPresent(t -> {
                        if (t instanceof CompoundTag compound) tag.put("OutputItem", compound);
                    });
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "alchemical_synthesizer_server");
        }
    }

    public enum AlchemicalSynthesizerComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();

            if (serverData.contains("InputItem") && serverData.contains("OutputItem")) {
                var context = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
                ItemStack inputStack = ItemStack.CODEC.parse(context, serverData.getCompoundOrEmpty("InputItem")).result().orElse(ItemStack.EMPTY);
                ItemStack outputStack = ItemStack.CODEC.parse(context, serverData.getCompoundOrEmpty("OutputItem")).result().orElse(ItemStack.EMPTY);

                int progress = serverData.getIntOr("Progress", 0);
                int maxProgress = serverData.getIntOr("MaxProgress", 20);
                float progressRatio = maxProgress > 0 ? Mth.clamp((float) progress / (float) maxProgress, 0.0F, 1.0F) : 0.0F;

                if (!inputStack.isEmpty() && !outputStack.isEmpty()) {
                    // [Input Item] -> [Espaço 4px] -> [Seta Jade] -> [Espaço 4px] -> [Output Item]
                    tooltip.add(JadeUI.item(inputStack));
                    tooltip.append(JadeUI.spacer(4, 0));
                    tooltip.append(JadeUI.progressArrow(progressRatio));
                    tooltip.append(JadeUI.spacer(4, 0));
                    tooltip.append(JadeUI.item(outputStack));
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "alchemical_synthesizer_client");
        }
    }

    // =========================================================================
    // 4. SYNTHESIS TABLE PROVIDERS (Remove 100% do lixo do Jade e mostra TODOS os itens)
    // =========================================================================
    @SuppressWarnings("removal")
    public enum SynthesisTableServerDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
            BlockEntity be = accessor.getBlockEntity();
            if (be instanceof SynthesisTableBlockEntity synthesisBe) {
                ItemStack result = synthesisBe.inventory.getStackInSlot(81);
                if (!result.isEmpty()) {
                    var context = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
                    ItemStack.CODEC.encodeStart(context, result).result().ifPresent(t -> {
                        if (t instanceof CompoundTag compound) {
                            tag.put("ResultItem", compound);
                        }
                    });

                    // 1. Soma todos os itens físicos colocados na grade 9x9
                    java.util.Map<net.minecraft.world.item.Item, Integer> totalItemCounts = new java.util.LinkedHashMap<>();
                    java.util.Map<net.minecraft.world.item.Item, ItemStack> itemSampleMap = new java.util.LinkedHashMap<>();

                    for (int i = 0; i < 81; i++) {
                        ItemStack in = synthesisBe.inventory.getStackInSlot(i);
                        if (!in.isEmpty()) {
                            totalItemCounts.merge(in.getItem(), in.getCount(), Integer::sum);
                            itemSampleMap.putIfAbsent(in.getItem(), in);
                        }
                    }

                    // 2. Se a mesa estiver vazia mas tiver receita no Cartão de Memória, lê a receita do cartão!
                    if (totalItemCounts.isEmpty()) {
                        ItemStack card = synthesisBe.inventory.getStackInSlot(82);
                        if (!card.isEmpty()) {
                            ItemStack[] ghostGrid = dev.davidklgames.puremashtweaks.util.SynthesisTableHelper.readGridFromCard(card, accessor.getLevel().registryAccess());
                            for (ItemStack ghost : ghostGrid) {
                                if (!ghost.isEmpty()) {
                                    totalItemCounts.merge(ghost.getItem(), Math.max(1, ghost.getCount()), Integer::sum);
                                    itemSampleMap.putIfAbsent(ghost.getItem(), ghost);
                                }
                            }
                        }
                    }

                    // 3. Serializa TODOS os ingredientes sem limite
                    net.minecraft.nbt.ListTag inputList = new net.minecraft.nbt.ListTag();
                    for (java.util.Map.Entry<net.minecraft.world.item.Item, Integer> entry : totalItemCounts.entrySet()) {
                        ItemStack sampleWithCount = itemSampleMap.get(entry.getKey()).copyWithCount(entry.getValue());
                        ItemStack.CODEC.encodeStart(context, sampleWithCount).result().ifPresent(inputList::add);
                    }

                    if (!inputList.isEmpty()) {
                        tag.put("InputItems", inputList);
                    }
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "synthesis_table_server");
        }
    }

    public enum CraftingComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public int getDefaultPriority() {
            // Prioridade TAIL (10000): Executa depois do Jade para remover a lista genérica
            return TooltipPosition.TAIL;
        }

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            // 1. Remove a lista genérica/padrão de itens do Jade
            tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE);
            tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE_DEFAULT);
            tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE_DETAILED_AMOUNT);
            tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE_NORMAL_AMOUNT);
            tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE_SHOW_NAME_AMOUNT);
            tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE_ITEMS_PER_LINE);
            tooltip.remove(JadeIds.UNIVERSAL_ITEM_STORAGE_SORT);

            CompoundTag serverData = accessor.getServerData();

            if (serverData.contains("ResultItem")) {
                var context = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
                ItemStack resultStack = ItemStack.CODEC.parse(context, serverData.getCompoundOrEmpty("ResultItem")).result().orElse(ItemStack.EMPTY);

                if (!resultStack.isEmpty()) {
                    // 1. Linha Superior: Texto formatado (Crafting: 1x Item)
                    Component textComp = Component.literal("Crafting: ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(resultStack.getCount() + "x ").withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(resultStack.getHoverName().getString()).withStyle(ChatFormatting.GOLD));

                    tooltip.add(JadeUI.text(textComp));

                    // 2. Fluxo Adaptável: 9 elementos por linha (Ingredientes + Seta + Resultado) com 3px de espaço
                    java.util.List<snownee.jade.api.ui.Element> flowElements = new java.util.ArrayList<>();

                    if (serverData.contains("InputItems")) {
                        net.minecraft.nbt.ListTag inputList = serverData.getListOrEmpty("InputItems");
                        for (int i = 0; i < inputList.size(); i++) {
                            ItemStack inStack = ItemStack.CODEC.parse(context, inputList.getCompoundOrEmpty(i)).result().orElse(ItemStack.EMPTY);
                            if (!inStack.isEmpty()) {
                                flowElements.add(JadeUI.item(inStack));
                            }
                        }
                    }

                    // Anexa a seta do Jade e o item resultante no final da fila da receita
                    flowElements.add(JadeUI.progressArrow(1.0F));
                    flowElements.add(JadeUI.item(resultStack));

                    int itemsPerLine = 9;
                    int currentInLine = 0;

                    for (snownee.jade.api.ui.Element elem : flowElements) {
                        if (currentInLine == 0) {
                            tooltip.add(elem);
                            currentInLine = 1;
                        } else {
                            tooltip.append(JadeUI.spacer(3, 0)); // 3 píxels exatos de espaçamento
                            tooltip.append(elem);
                            currentInLine++;

                            if (currentInLine >= itemsPerLine) {
                                currentInLine = 0; // Pula de linha apenas se ultrapassar 9 itens
                            }
                        }
                    }
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "crafting_table");
        }
    }
    // =========================================================================
    // 5. UNIVERSAL CABLE PROVIDERS (Modo, Lado e Taxa de Transferência no Jade)
    // =========================================================================
    public enum UniversalCableServerDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
            BlockEntity be = accessor.getBlockEntity();
            if (be instanceof UniversalCableBlockEntity cableBe) {
                Direction targetSide = accessor.getSide();

                // Se o lado mirado não estiver extraindo, busca o lado ativo de extração
                if (!cableBe.isExtracting(targetSide)) {
                    for (Direction dir : Direction.values()) {
                        if (cableBe.isExtracting(dir)) {
                            targetSide = dir;
                            break;
                        }
                    }
                }

                boolean isExtracting = cableBe.isExtracting(targetSide);
                tag.putBoolean("Extracting", isExtracting);
                tag.putInt("TargetSide", targetSide.get3DDataValue());

                if (isExtracting) {
                    int mode = cableBe.getSelectedTab(targetSide);
                    tag.putInt("SelectedTab", mode);
                    tag.putInt("Tier", cableBe.getTier());

                    int multiplier = cableBe.getTransferRateMultiplier();
                    tag.putInt("Multiplier", multiplier);

                    // Taxas calculadas dinamicamente com base nas configs e no multiplicador de upgrade
                    int baseEnergy = (cableBe.getTier() == 2) ?
                            PureMashTweaksConfig.COMMON.moldelonianCableTransferRate.get() :
                            PureMashTweaksConfig.COMMON.synthoriumCableTransferRate.get();
                    int maxEnergy = baseEnergy * multiplier;

                    int baseItem = (cableBe.getTier() == 2) ?
                            PureMashTweaksConfig.COMMON.moldelonianCableItemRate.get() :
                            PureMashTweaksConfig.COMMON.synthoriumCableItemRate.get();
                    int maxItem = baseItem * multiplier;

                    int baseFluid = (cableBe.getTier() == 2) ?
                            PureMashTweaksConfig.COMMON.moldelonianCableFluidRate.get() :
                            PureMashTweaksConfig.COMMON.synthoriumCableFluidRate.get();
                    int maxFluid = baseFluid * multiplier;

                    tag.putInt("EnergyRate", maxEnergy);
                    tag.putInt("ItemRate", maxItem);
                    tag.putInt("FluidRate", maxFluid);
                }
            }
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "universal_cable_server");
        }
    }

    public enum UniversalCableComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();

            if (serverData.contains("Extracting") && serverData.getBooleanOr("Extracting", false)) {
                int selectedTab = serverData.getIntOr("SelectedTab", 1);
                Direction side = Direction.from3DDataValue(serverData.getIntOr("TargetSide", 0));
                String sideName = getReadableExtractSide(side);

                int energyRate = serverData.getIntOr("EnergyRate", 50000);
                int itemRate = serverData.getIntOr("ItemRate", 8);
                int fluidRate = serverData.getIntOr("FluidRate", 1000);

                switch (selectedTab) {
                    case 0 -> {
                        tooltip.add(Component.literal("Mode: Energy").withStyle(ChatFormatting.GOLD)
                                .append(Component.literal(" (" + sideName + ")").withStyle(ChatFormatting.GRAY)));
                        tooltip.add(Component.literal("Transfer Rate: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.format("%,d", energyRate) + " FE/t").withStyle(ChatFormatting.YELLOW)));
                    }
                    case 1 -> {
                        tooltip.add(Component.literal("Mode: Item").withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(" (" + sideName + ")").withStyle(ChatFormatting.GRAY)));
                        tooltip.add(Component.literal("Transfer Rate: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(itemRate + " items/cycle").withStyle(ChatFormatting.WHITE)));
                    }
                    case 2 -> {
                        tooltip.add(Component.literal("Mode: Fluid").withStyle(ChatFormatting.BLUE)
                                .append(Component.literal(" (" + sideName + ")").withStyle(ChatFormatting.GRAY)));
                        tooltip.add(Component.literal("Transfer Rate: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.format("%,d", fluidRate) + " mB/t").withStyle(ChatFormatting.AQUA)));
                    }
                }
            }
        }

        private static String getReadableExtractSide(Direction side) {
            return switch (side) {
                case DOWN -> "Bottom Face (DOWN)";
                case UP -> "Top Face (UP)";
                case NORTH -> "North Face (NORTH)";
                case SOUTH -> "South Face (SOUTH)";
                case WEST -> "West Face (WEST)";
                case EAST -> "East Face (EAST)";
            };
        }

        @Override
        public @NonNull Identifier getUid() {
            return Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "universal_cable_client");
        }
    }
}