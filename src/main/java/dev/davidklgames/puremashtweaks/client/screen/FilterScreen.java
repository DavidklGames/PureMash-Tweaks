package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.entity.cable.CableFilter;
import dev.davidklgames.puremashtweaks.client.renderer.FluidRenderHelper;
import dev.davidklgames.puremashtweaks.menu.FilterMenu;
import dev.davidklgames.puremashtweaks.network.CloseFilterToCablePayload;
import dev.davidklgames.puremashtweaks.network.UpdateCableFilterPayload;
import dev.davidklgames.puremashtweaks.registry.PureMashDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated Cable Filter Screen ('Cable Filter' GUI - 176x222).
 */
public class FilterScreen extends BaseContainerCompressionScreen<FilterMenu> {

    private static final Identifier FILTER_GUI_TEXTURE = Identifier.fromNamespaceAndPath(
            PureMashTweaks.MODID,
            "textures/gui/cables/cable_filter_gui.png"
    );

    private EditBox tagInput;
    private EditBox nbtInput;
    private EditBox modNamespaceInput;
    private EditBox targetSlotInput;
    private EditBox priorityInput;
    private EditBox stockLimitInput;
    private EditBox durabilityCustomInput;

    public FilterScreen(FilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, FILTER_GUI_TEXTURE, 176, 222);
        this.inventoryLabelX = 10;
        this.inventoryLabelY = 129;
    }

    private int getActiveTab() {
        if (this.getMenu().getBlockEntity() != null) {
            return this.getMenu().getBlockEntity().getSelectedTab(this.getMenu().getSide());
        }
        return 1; // Default to Item Mode (Tab 1)
    }

    private boolean isFluidMode() {
        return getActiveTab() == 2;
    }

    @Override
    protected void init() {
        super.init();

        CableFilter filter = this.getMenu().getFilter();

        // 1. Item / Tag EditBox (X=29, Y=10, Width 140, Height 18)
        this.tagInput = new EditBox(this.font, this.leftPos + 29, this.topPos + 10, 140, 18, Component.empty());
        this.tagInput.setTextColor(0xFFFFFFFF);
        this.tagInput.setBordered(true);
        this.tagInput.setMaxLength(256);
        this.tagInput.setResponder(this::onTagInputChanged);
        if (filter.getTagString() != null && !filter.getTagString().isEmpty()) {
            this.tagInput.setValue(filter.getTagString());
        } else if (isFluidMode()) {
            Fluid f = filter.getDisplayFluid(this.minecraft.level);
            if (f != Fluids.EMPTY) {
                this.tagInput.setValue(BuiltInRegistries.FLUID.getKey(f).toString());
            }
        } else if (!filter.getFilterStack().isEmpty()) {
            this.tagInput.setValue(BuiltInRegistries.ITEM.getKey(filter.getFilterStack().getItem()).toString());
        }
        this.addRenderableWidget(this.tagInput);

        // 2. NBT Data EditBox (X=7, Y=38, Width 162, Height 18)
        this.nbtInput = new EditBox(this.font, this.leftPos + 7, this.topPos + 38, 162, 18, Component.empty());
        this.nbtInput.setTextColor(0xFFFFFFFF);
        this.nbtInput.setBordered(true);
        this.nbtInput.setMaxLength(1024);
        this.nbtInput.setResponder(this::onNbtInputChanged);
        if (filter.getMetadata() != null && !filter.getMetadata().isEmpty()) {
            this.nbtInput.setValue(filter.getMetadata().toString());
        }
        this.addRenderableWidget(this.nbtInput);

        // 3. Mod ID Namespace Small Button (X=18, Y=58) & EditBox (X=40, Y=61, Width 64, Height 14)
        this.addRenderableWidget(new FilterSmallButton(
                this.leftPos + 18, this.topPos + 58,
                true,
                () -> new int[]{240, 0},
                b -> {},
                () -> List.of(Component.literal("Mod ID Namespace Filter (@modid or modid)").withStyle(net.minecraft.ChatFormatting.AQUA))
        ));

        this.modNamespaceInput = new EditBox(this.font, this.leftPos + 40, this.topPos + 61, 64, 14, Component.empty());
        this.modNamespaceInput.setTextColor(0xFFFFFFFF);
        this.modNamespaceInput.setBordered(true);
        this.modNamespaceInput.setMaxLength(64);
        this.modNamespaceInput.setValue(filter.getModNamespace());
        this.addRenderableWidget(this.modNamespaceInput);

        // 4. Target Slot Small Button (X=111, Y=58) & EditBox (X=133, Y=61, Width 28, Height 14)
        this.addRenderableWidget(new FilterSmallButton(
                this.leftPos + 111, this.topPos + 58,
                true,
                () -> new int[]{232, 16, 14, 16, 3, 2},
                b -> {},
                () -> List.of(Component.literal("Target Slots / Sequence (e.g. 6, 1, 4)").withStyle(net.minecraft.ChatFormatting.YELLOW))
        ));

        this.targetSlotInput = new EditBox(this.font, this.leftPos + 133, this.topPos + 61, 28, 14, Component.empty());
        this.targetSlotInput.setTextColor(0xFFFFFFFF);
        this.targetSlotInput.setBordered(true);
        this.targetSlotInput.setMaxLength(64);
        this.targetSlotInput.setFilter(s -> s.matches("[0-9,;\\s]*"));
        this.targetSlotInput.setValue(filter.getTargetSlots());
        this.addRenderableWidget(this.targetSlotInput);

        // 5. NBT Matching Mode Button (X=47, Y=80 | 20x20)
        this.addRenderableWidget(new FilterSmallButton(
                this.leftPos + 47, this.topPos + 80,
                () -> this.nbtInput != null && !this.nbtInput.getValue().trim().isEmpty(),
                () -> {
                    boolean active = this.nbtInput != null && !this.nbtInput.getValue().trim().isEmpty();
                    if (!active) return new int[]{176, 16};
                    return filter.getNbtMode() == 1 ? new int[]{192, 16} : new int[]{176, 16};
                },
                b -> {
                    int nextMode = (filter.getNbtMode() == 1) ? 2 : 1;
                    filter.setNbtMode(nextMode);
                },
                () -> {
                    boolean active = this.nbtInput != null && !this.nbtInput.getValue().trim().isEmpty();
                    if (!active) {
                        return List.of(
                                Component.literal("NBT Match Mode: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                                        .append(Component.literal("Disabled").withStyle(net.minecraft.ChatFormatting.DARK_RED)),
                                Component.literal("Type or insert NBT Data in the field above to enable.").withStyle(net.minecraft.ChatFormatting.DARK_GRAY)
                        );
                    }
                    return List.of(
                            Component.literal("NBT Match Mode: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                                    .append(filter.getNbtMode() == 1 ?
                                            Component.literal("Match exact NBT Data").withStyle(net.minecraft.ChatFormatting.GREEN) :
                                            Component.literal("Match provided NBT tags (Fuzzy)").withStyle(net.minecraft.ChatFormatting.YELLOW))
                    );
                }
        ));

        // 6. Whitelist / Blacklist Policy Small Button (X=111, Y=80 | 20x20)
        this.addRenderableWidget(new FilterSmallButton(
                this.leftPos + 111, this.topPos + 80,
                true,
                () -> filter.isInvert() ? new int[]{192, 32} : new int[]{176, 32},
                b -> filter.setInvert(!filter.isInvert()),
                () -> List.of(Component.literal("Filter Policy: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                        .append(filter.isInvert() ?
                                Component.literal("Blacklist (Inverted)").withStyle(net.minecraft.ChatFormatting.RED) :
                                Component.literal("Whitelist (Allow)").withStyle(net.minecraft.ChatFormatting.GREEN)))
        ));

        // 7. Priority Button (X=18, Y=80) & EditBox (X=18, Y=102, Width 20, Height 18)
        this.addRenderableWidget(new FilterSmallButton(
                this.leftPos + 18, this.topPos + 80,
                () -> this.getMenu().hasBoundDestination(),
                () -> new int[]{214, 2, 7, 13, 6, 4},
                b -> {},
                () -> {
                    if (!this.getMenu().hasBoundDestination()) {
                        return List.of(Component.literal("Filter Priority: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                                .append(Component.literal("Requires Bound Distribution Filter").withStyle(net.minecraft.ChatFormatting.DARK_RED)));
                    }
                    return List.of(Component.literal("Filter Priority (Processed First)").withStyle(net.minecraft.ChatFormatting.YELLOW));
                }
        ));

        this.priorityInput = new EditBox(this.font, this.leftPos + 18, this.topPos + 102, 20, 18, Component.empty());
        this.priorityInput.setTextColor(0xFFFFFFFF);
        this.priorityInput.setBordered(true);
        this.priorityInput.setMaxLength(2);
        this.priorityInput.setFilter(s -> s.matches("[0-9]*"));

        int prio = filter.getPriority();
        this.priorityInput.setValue(String.format("%02d", Math.clamp(prio, 0, 99)));
        this.addRenderableWidget(this.priorityInput);

        // 8. Stock Limit Button (X=138, Y=80) & EditBox (X=138, Y=102, Width 20, Height 18)
        this.addRenderableWidget(new FilterSmallButton(
                this.leftPos + 138, this.topPos + 80,
                () -> this.getMenu().hasBoundDestination(),
                () -> new int[]{224, 0},
                b -> {},
                () -> {
                    if (!this.getMenu().hasBoundDestination()) {
                        return List.of(Component.literal("Stock Limit: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                                .append(Component.literal("Requires Bound Distribution Filter").withStyle(net.minecraft.ChatFormatting.DARK_RED)));
                    }
                    return List.of(Component.literal("Destination Stock Limit (Cap)").withStyle(net.minecraft.ChatFormatting.GOLD));
                }
        ));

        this.stockLimitInput = new EditBox(this.font, this.leftPos + 138, this.topPos + 102, 20, 18, Component.empty());
        this.stockLimitInput.setTextColor(0xFFFFFFFF);
        this.stockLimitInput.setBordered(true);
        this.stockLimitInput.setMaxLength(8);
        this.stockLimitInput.setFilter(s -> s.isEmpty() || s.matches("\\d+"));

        int stock = filter.getStockLimit();
        if (this.getMenu().hasBoundDestination()) {
            if (stock <= 0) {
                stock = 64;
                filter.setStockLimit(64);
            }
            this.stockLimitInput.setValue(String.valueOf(stock));
        } else {
            this.stockLimitInput.setValue("00");
        }
        this.addRenderableWidget(this.stockLimitInput);

        // 9. Durability Stepped Button (X=46, Y=104 | 22x14)
        this.addRenderableWidget(new DurabilityConditionButton(
                this.leftPos + 46, this.topPos + 104,
                () -> !isFluidMode() && !getMenu().getFilter().getFilterStack().isEmpty() && getMenu().getFilter().getFilterStack().isDamageableItem(),
                filter,
                b -> {
                    int current = filter.getMinDurabilityPercent();
                    int next;
                    if (current == 0) next = 5;
                    else if (current == 5) next = 25;
                    else if (current == 25) next = 50;
                    else if (current == 50) next = 75;
                    else if (current == 75) next = 100;
                    else next = 0;

                    filter.setMinDurabilityPercent(next);
                    filter.setCustomDurabilityString("");
                    if (this.durabilityCustomInput != null) {
                        this.durabilityCustomInput.setValue("");
                    }
                }
        ));

        // 10. Durability Custom EditBox (X=70, Y=104, Width 62, Height 14)
        this.durabilityCustomInput = new EditBox(this.font, this.leftPos + 70, this.topPos + 104, 62, 14, Component.empty());
        this.durabilityCustomInput.setTextColor(0xFFFFFFFF);
        this.durabilityCustomInput.setBordered(true);
        this.durabilityCustomInput.setMaxLength(16);
        this.durabilityCustomInput.setFilter(s -> s.matches("[0-9<>=!\\s]*"));
        this.durabilityCustomInput.setValue(filter.getCustomDurabilityString());
        this.durabilityCustomInput.setResponder(val -> {
            filter.setCustomDurabilityString(val);
            if (!val.trim().isEmpty()) {
                filter.setMinDurabilityPercent(0);
            }
        });
        this.addRenderableWidget(this.durabilityCustomInput);

        // 11. Cancel Button (X=87, Y=122 | 37x14)
        this.addRenderableWidget(new CustomLargeActionBtn(
                this.leftPos + 87, this.topPos + 122, 37, 14,
                Component.literal("Cancel"),
                true,
                b -> {
                    if (this.getMenu().getBlockEntity() != null) {
                        ClientPacketDistributor.sendToServer(
                                new CloseFilterToCablePayload(
                                        this.getMenu().getBlockEntity().getBlockPos(),
                                        this.getMenu().getSide()
                                )
                        );
                    }
                }
        ));

        // 12. Submit Button (X=128, Y=122 | 37x14)
        this.addRenderableWidget(new CustomLargeActionBtn(
                this.leftPos + 128, this.topPos + 122, 37, 14,
                Component.literal("Submit"),
                true,
                b -> {
                    String tagTxt = this.tagInput.getValue().trim();
                    filter.setTagString(tagTxt);

                    if (isFluidMode()) {
                        Fluid fluid = CableFilter.resolveFluid(tagTxt);
                        if (fluid != Fluids.EMPTY) {
                            filter.setFilterStack(new ItemStack(fluid.getBucket()));
                        }
                    } else {
                        Item item = CableFilter.resolveItem(tagTxt);
                        if (item != null && item != Items.AIR) {
                            filter.setFilterStack(new ItemStack(item));
                        }
                    }

                    String nbtTxt = this.nbtInput.getValue().trim();
                    if (!nbtTxt.isEmpty()) {
                        try {
                            CompoundTag parsedNbt = TagParser.parseCompoundFully(nbtTxt);
                            filter.setMetadata(parsedNbt);
                            if (filter.getNbtMode() == 0) {
                                filter.setNbtMode(1);
                            }
                        } catch (Exception e) {
                            filter.setMetadata(null);
                            filter.setNbtMode(0);
                        }
                    } else {
                        filter.setMetadata(null);
                        filter.setNbtMode(0);
                    }

                    ItemStack toolInSlot = this.getMenu().getSlot(0).getItem();

                    if (FilterMenu.isBoundFilter(toolInSlot)) {
                        filter.setDestinationTool(toolInSlot.copy());
                        filter.setDestinationTag(toolInSlot.get(PureMashDataComponents.BOUND_CONTAINER.get()));

                        String prioVal = this.priorityInput.getValue().trim();
                        int p = 1;
                        try {
                            p = prioVal.isEmpty() ? 1 : Integer.parseInt(prioVal);
                        } catch (NumberFormatException ignored) {}
                        filter.setPriority(Math.clamp(p, 1, 99));

                        String stockVal = this.stockLimitInput.getValue().trim();
                        int parsedStock = 64;
                        try {
                            parsedStock = stockVal.isEmpty() ? 64 : Integer.parseInt(stockVal);
                        } catch (NumberFormatException ignored) {}
                        filter.setStockLimit(Math.clamp(parsedStock, 1, 10000000));
                    } else {
                        filter.setDestinationTool(ItemStack.EMPTY);
                        filter.setDestinationTag(null);
                        filter.setPriority(0);
                        filter.setStockLimit(0);
                    }

                    filter.setModNamespace(this.modNamespaceInput.getValue());
                    filter.setTargetSlots(this.targetSlotInput.getValue().trim());
                    filter.setCustomDurabilityString(this.durabilityCustomInput.getValue().trim());

                    if (this.getMenu().getBlockEntity() != null) {
                        ClientPacketDistributor.sendToServer(new UpdateCableFilterPayload(
                                this.getMenu().getBlockEntity().getBlockPos(),
                                this.getMenu().getSide(),
                                this.getMenu().getFilterIndex(),
                                filter.serializeNBT()
                        ));
                    }
                }
        ));

        this.updateState();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateState();
    }

    private void updateState() {
        boolean hasDest = this.getMenu().hasBoundDestination();

        if (this.priorityInput != null) {
            this.priorityInput.setEditable(hasDest);
            if (!hasDest) {
                this.priorityInput.setValue("00");
                this.priorityInput.setTextColor(0xFF888888);
                this.getMenu().getFilter().setPriority(0);
            } else {
                this.priorityInput.setTextColor(0xFFFFFFFF);
                String currentVal = this.priorityInput.getValue().trim();
                if (currentVal.equals("00") && !this.priorityInput.isFocused()) {
                    this.priorityInput.setValue("01");
                }
            }
        }

        if (this.stockLimitInput != null) {
            this.stockLimitInput.setEditable(hasDest);
            if (!hasDest) {
                if (!this.stockLimitInput.isFocused()) {
                    this.stockLimitInput.setValue("00");
                    this.stockLimitInput.setTextColor(0xFF888888);
                }
            } else {
                this.stockLimitInput.setTextColor(0xFFFFFFFF);
                String curVal = this.stockLimitInput.getValue().trim();
                // Se estiver com "00" ou em branco e o destino foi vinculado, assume 64 por padrão
                if (curVal.equals("00") || (curVal.isEmpty() && !this.stockLimitInput.isFocused())) {
                    this.stockLimitInput.setValue("64");
                    this.getMenu().getFilter().setStockLimit(64);
                }
            }
        }

        boolean isDamageable = !isFluidMode() && !getMenu().getFilter().getFilterStack().isEmpty() && getMenu().getFilter().getFilterStack().isDamageableItem();
        if (this.durabilityCustomInput != null) {
            this.durabilityCustomInput.setEditable(isDamageable);
            this.durabilityCustomInput.setTextColor(isDamageable ? 0xFFFFFFFF : 0xFF888888);
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = this.leftPos;
        int y = this.topPos;

        if (isFluidMode()) {
            Fluid fluid = this.getMenu().getFilter().getDisplayFluid(this.minecraft.level);
            if (fluid != Fluids.EMPTY) {
                TextureAtlasSprite sprite = FluidRenderHelper.getFluidTexture(fluid);
                int color = FluidRenderHelper.getFluidColor(fluid, 1000);
                if (sprite != null) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + 8, y + 11, 16, 16, color);
                }
            }
        } else {
            ItemStack ghostStackToRender = this.getMenu().getFilter().getDisplayStack(this.minecraft.level);
            if (!ghostStackToRender.isEmpty()) {
                graphics.fakeItem(ghostStackToRender, x + 8, y + 11);
            }
        }

        if (mouseX >= x + 8 && mouseX <= x + 24 && mouseY >= y + 11 && mouseY <= y + 27) {
            graphics.fill(x + 8, y + 11, x + 24, y + 27, 0x80FFFFFF);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.playerInventoryTitle, 10, 129, 0xFFE0E0E0, false);
    }

    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (mouseX >= this.leftPos + 8 && mouseX <= this.leftPos + 24 &&
                mouseY >= this.topPos + 11 && mouseY <= this.topPos + 27) {
            if (isFluidMode()) {
                Fluid fluid = this.getMenu().getFilter().getDisplayFluid(this.minecraft.level);
                if (fluid != Fluids.EMPTY) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(fluid.getFluidType().getDescription().copy().withStyle(net.minecraft.ChatFormatting.AQUA));
                    if (this.getMenu().getFilter().getTagString() != null && this.getMenu().getFilter().getTagString().startsWith("#")) {
                        tooltip.add(Component.literal("Accepts Fluid Tag: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                                .append(Component.literal(this.getMenu().getFilter().getTagString()).withStyle(net.minecraft.ChatFormatting.BLUE)));
                    }
                    graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
                }
            } else {
                ItemStack ghostStack = this.getMenu().getFilter().getDisplayStack(this.minecraft.level);
                if (!ghostStack.isEmpty()) {
                    List<Component> tooltip = new ArrayList<>(net.minecraft.client.gui.screens.Screen.getTooltipFromItem(this.minecraft, ghostStack));
                    if (this.getMenu().getFilter().getTagString() != null && this.getMenu().getFilter().getTagString().startsWith("#")) {
                        tooltip.add(Component.literal("Accepts Tag: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                                .append(Component.literal(this.getMenu().getFilter().getTagString()).withStyle(net.minecraft.ChatFormatting.BLUE)));
                    }
                    graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ghostStack, mouseX, mouseY, null);
                }
            }
        }

        if (mouseX >= this.leftPos + 29 && mouseX <= this.leftPos + 169 &&
                mouseY >= this.topPos + 10 && mouseY <= this.topPos + 28) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    List.of(
                            Component.translatable("tooltip.puremashtweaks.filter.item_tag.title").withStyle(net.minecraft.ChatFormatting.AQUA),
                            Component.translatable("tooltip.puremashtweaks.filter.item_tag.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                    ),
                    java.util.Optional.empty(),
                    ItemStack.EMPTY,
                    mouseX, mouseY, null
            );
        }

        if (mouseX >= this.leftPos + 7 && mouseX <= this.leftPos + 169 &&
                mouseY >= this.topPos + 38 && mouseY <= this.topPos + 56) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    List.of(
                            Component.translatable("tooltip.puremashtweaks.filter.nbt.title").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE),
                            Component.translatable("tooltip.puremashtweaks.filter.nbt.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                    ),
                    java.util.Optional.empty(),
                    ItemStack.EMPTY,
                    mouseX, mouseY, null
            );
        }

        if (mouseX >= this.leftPos + 40 && mouseX <= this.leftPos + 104 &&
                mouseY >= this.topPos + 61 && mouseY <= this.topPos + 75) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    List.of(
                            Component.translatable("tooltip.puremashtweaks.filter.input_mod_id.title").withStyle(net.minecraft.ChatFormatting.AQUA),
                            Component.translatable("tooltip.puremashtweaks.filter.input_mod_id.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                    ),
                    java.util.Optional.empty(),
                    ItemStack.EMPTY,
                    mouseX, mouseY, null
            );
        }

        if (mouseX >= this.leftPos + 133 && mouseX <= this.leftPos + 161 &&
                mouseY >= this.topPos + 61 && mouseY <= this.topPos + 75) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    List.of(
                            Component.translatable("tooltip.puremashtweaks.filter.input_target_slot.title").withStyle(net.minecraft.ChatFormatting.YELLOW),
                            Component.translatable("tooltip.puremashtweaks.filter.input_target_slot.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                    ),
                    java.util.Optional.empty(),
                    ItemStack.EMPTY,
                    mouseX, mouseY, null
            );
        }

        if (mouseX >= this.leftPos + 80 && mouseX <= this.leftPos + 98 &&
                mouseY >= this.topPos + 81 && mouseY <= this.topPos + 99) {
            ItemStack slotItem = this.getMenu().getSlot(0).getItem();
            if (slotItem.isEmpty()) {
                graphics.setTooltipForNextFrame(
                        this.font,
                        List.of(
                                Component.translatable("tooltip.puremashtweaks.filter.destination_slot.title").withStyle(net.minecraft.ChatFormatting.GOLD),
                                Component.translatable("tooltip.puremashtweaks.filter.destination_slot.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                        ),
                        java.util.Optional.empty(),
                        ItemStack.EMPTY,
                        mouseX, mouseY, null
                );
            }
        }

        if (mouseX >= this.leftPos + 18 && mouseX <= this.leftPos + 38 &&
                mouseY >= this.topPos + 102 && mouseY <= this.topPos + 120) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    List.of(
                            Component.translatable("tooltip.puremashtweaks.filter.input_priority.title").withStyle(net.minecraft.ChatFormatting.YELLOW),
                            Component.translatable("tooltip.puremashtweaks.filter.input_priority.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                    ),
                    java.util.Optional.empty(),
                    ItemStack.EMPTY,
                    mouseX, mouseY, null
            );
        }

        if (mouseX >= this.leftPos + 70 && mouseX <= this.leftPos + 132 &&
                mouseY >= this.topPos + 104 && mouseY <= this.topPos + 118) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    List.of(
                            Component.translatable("tooltip.puremashtweaks.filter.input_durability.title").withStyle(net.minecraft.ChatFormatting.GREEN),
                            Component.translatable("tooltip.puremashtweaks.filter.input_durability.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                    ),
                    java.util.Optional.empty(),
                    ItemStack.EMPTY,
                    mouseX, mouseY, null
            );
        }

        if (mouseX >= this.leftPos + 138 && mouseX <= this.leftPos + 158 &&
                mouseY >= this.topPos + 102 && mouseY <= this.topPos + 120) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    List.of(
                            Component.translatable("tooltip.puremashtweaks.filter.input_stock_limit.title").withStyle(net.minecraft.ChatFormatting.GOLD),
                            Component.translatable("tooltip.puremashtweaks.filter.input_stock_limit.desc").withStyle(net.minecraft.ChatFormatting.GRAY)
                    ),
                    java.util.Optional.empty(),
                    ItemStack.EMPTY,
                    mouseX, mouseY, null
            );
        }
    }

    private void onTagInputChanged(String text) {
        String trimmed = text.trim();
        this.getMenu().getFilter().setTagString(trimmed);

        if (trimmed.isEmpty()) {
            this.tagInput.setTextColor(0xFFFFFFFF);
            this.getMenu().getFilter().setFilterStack(ItemStack.EMPTY);
            return;
        }

        if (isFluidMode()) {
            if (trimmed.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(trimmed.substring(1));
                if (tagId != null) {
                    TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagId);
                    var tagHolderSet = BuiltInRegistries.FLUID.get(tagKey);
                    if (tagHolderSet.isPresent() && !tagHolderSet.get().stream().toList().isEmpty()) {
                        this.tagInput.setTextColor(0xFFFFFFFF);
                        Fluid fluid = tagHolderSet.get().stream().toList().getFirst().value();
                        this.getMenu().getFilter().setFilterStack(new ItemStack(fluid.getBucket()));
                        return;
                    }
                }
                this.tagInput.setTextColor(0xFFFF5555);
            } else {
                Fluid fluid = CableFilter.resolveFluid(trimmed);
                if (fluid != Fluids.EMPTY) {
                    this.tagInput.setTextColor(0xFFFFFFFF);
                    this.getMenu().getFilter().setFilterStack(new ItemStack(fluid.getBucket()));
                } else {
                    this.tagInput.setTextColor(0xFFFF5555);
                }
            }
        } else {
            if (trimmed.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(trimmed.substring(1));
                if (tagId != null) {
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                    var tagHolderSet = BuiltInRegistries.ITEM.get(tagKey);

                    if (tagHolderSet.isPresent() && !tagHolderSet.get().stream().toList().isEmpty()) {
                        this.tagInput.setTextColor(0xFFFFFFFF);
                        var items = tagHolderSet.get().stream().toList();
                        long time = this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0;
                        int index = (int) ((time / 20L) % items.size());
                        this.getMenu().getFilter().setFilterStack(new ItemStack(items.get(index).value()));
                        return;
                    }
                }
                this.tagInput.setTextColor(0xFFFF5555);
            } else {
                Item item = CableFilter.resolveItem(trimmed);
                if (item != Items.AIR) {
                    this.tagInput.setTextColor(0xFFFFFFFF);
                    this.getMenu().getFilter().setFilterStack(new ItemStack(item));
                } else {
                    this.tagInput.setTextColor(0xFFFF5555);
                }
            }
        }
    }

    private void onNbtInputChanged(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            this.nbtInput.setTextColor(0xFFFFFFFF);
            this.getMenu().getFilter().setMetadata(null);
            return;
        }

        try {
            CompoundTag parsed = TagParser.parseCompoundFully(trimmed);
            this.nbtInput.setTextColor(0xFFFFFFFF);
            this.getMenu().getFilter().setMetadata(parsed);
            if (this.getMenu().getFilter().getNbtMode() == 0) {
                this.getMenu().getFilter().setNbtMode(1);
            }
        } catch (Exception e) {
            this.nbtInput.setTextColor(0xFFFF5555);
            this.getMenu().getFilter().setMetadata(null);
        }
    }

    private String extractNbtString(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        if (this.minecraft.level != null) {
            var context = this.minecraft.level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var result = DataComponentPatch.CODEC.encodeStart(context, stack.getComponentsPatch()).result();
            if (result.isPresent() && result.get() instanceof CompoundTag tag && !tag.isEmpty()) {
                return tag.toString();
            }
        }
        return "";
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean handled) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        // 1. Clique no Slot Fantasma de Alvo Superior Esquerdo (X=8, Y=11)
        if (mouseX >= this.leftPos + 8 && mouseX <= this.leftPos + 23 &&
                mouseY >= this.topPos + 11 && mouseY <= this.topPos + 26) {

            ItemStack carried = this.getMenu().getCarried();

            if (event.hasShiftDown()) {
                this.getMenu().getFilter().setFilterStack(ItemStack.EMPTY);
                this.getMenu().getFilter().setTagString("");
                this.getMenu().getFilter().setMetadata(null);
                if (this.tagInput != null) this.tagInput.setValue("");
                if (this.nbtInput != null) this.nbtInput.setValue("");
            } else if (!carried.isEmpty()) {
                onInsertStack(carried);
            }
            return true;
        }

        // 2. Shift + Click no inventário do jogador
        if (event.hasShiftDown()) {
            net.minecraft.world.inventory.Slot hoveredSlot = this.getHoveredSlot();
            if (hoveredSlot != null && hoveredSlot.hasItem()) {
                ItemStack item = hoveredSlot.getItem();

                // Se NÃO for um Distribution Filter vinculado, define como alvo fantasma do filtro
                if (!FilterMenu.isBoundFilter(item) && hoveredSlot.index >= 1) {
                    onInsertStack(item);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, handled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.@NonNull KeyEvent event) {
        if ((this.tagInput != null && this.tagInput.isFocused()) ||
                (this.nbtInput != null && this.nbtInput.isFocused()) ||
                (this.modNamespaceInput != null && this.modNamespaceInput.isFocused()) ||
                (this.targetSlotInput != null && this.targetSlotInput.isFocused()) ||
                (this.priorityInput != null && this.priorityInput.isFocused()) ||
                (this.durabilityCustomInput != null && this.durabilityCustomInput.isFocused()) ||
                (this.stockLimitInput != null && this.stockLimitInput.isFocused())) {

            if (event.isEscape()) {
                if (this.getMenu().getBlockEntity() != null) {
                    ClientPacketDistributor.sendToServer(
                            new CloseFilterToCablePayload(
                                    this.getMenu().getBlockEntity().getBlockPos(),
                                    this.getMenu().getSide()
                            )
                    );
                }
                return true;
            }
            return this.getFocused() != null && this.getFocused().keyPressed(event);
        }
        return super.keyPressed(event);
    }

    private static class FilterSmallButton extends AbstractButton {
        private final java.util.function.Supplier<Boolean> activeSupplier;
        private final java.util.function.Supplier<int[]> uvSupplier;
        private final java.util.function.Consumer<FilterSmallButton> onPress;
        private final java.util.function.Supplier<List<Component>> tooltipSupplier;

        public FilterSmallButton(int x, int y, java.util.function.Supplier<Boolean> activeSupplier, java.util.function.Supplier<int[]> uvSupplier, java.util.function.Consumer<FilterSmallButton> onPress, java.util.function.Supplier<List<Component>> tooltipSupplier) {
            super(x, y, 20, 20, Component.empty());
            this.activeSupplier = activeSupplier;
            this.uvSupplier = uvSupplier;
            this.onPress = onPress;
            this.tooltipSupplier = tooltipSupplier;
        }

        public FilterSmallButton(int x, int y, boolean active, java.util.function.Supplier<int[]> uvSupplier, java.util.function.Consumer<FilterSmallButton> onPress, java.util.function.Supplier<List<Component>> tooltipSupplier) {
            this(x, y, () -> active, uvSupplier, onPress, tooltipSupplier);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            boolean isActive = this.activeSupplier.get();
            int buttonU = isActive ? (this.isHovered() ? 235 : 213) : 184;
            int buttonV = isActive ? 54 : 81;

            graphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_GUI_TEXTURE, this.getX(), this.getY(), (float) buttonU, (float) buttonV, 20, 20, 256, 256);

            int[] uv = this.uvSupplier.get();
            int iconU = uv[0];
            int iconV = uv[1];
            int iconW = uv.length > 2 ? uv[2] : 16;
            int iconH = uv.length > 3 ? uv[3] : 16;
            int offsetX = uv.length > 4 ? uv[4] : 2;
            int offsetY = uv.length > 5 ? uv[5] : 2;

            graphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_GUI_TEXTURE, this.getX() + offsetX, this.getY() + offsetY, (float) iconU, (float) iconV, iconW, iconH, 256, 256);

            if (this.isHovered() && this.tooltipSupplier != null) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.tooltipSupplier.get(), java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
            }
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress(@NonNull InputWithModifiers input) {
            if (this.activeSupplier.get()) {
                this.onPress.accept(this);
            }
        }
    }

    private static class DurabilityConditionButton extends AbstractButton {
        private final java.util.function.Supplier<Boolean> activeSupplier;
        private final CableFilter filter;
        private final java.util.function.Consumer<DurabilityConditionButton> onPress;

        public DurabilityConditionButton(int x, int y, java.util.function.Supplier<Boolean> activeSupplier, CableFilter filter, java.util.function.Consumer<DurabilityConditionButton> onPress) {
            super(x, y, 22, 14, Component.empty());
            this.activeSupplier = activeSupplier;
            this.filter = filter;
            this.onPress = onPress;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            boolean isActive = this.activeSupplier.get();
            int buttonU = isActive ? (this.isHovered() ? 234 : 210) : 183;
            int buttonV = isActive ? 35 : 102;

            graphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_GUI_TEXTURE, this.getX(), this.getY(), (float) buttonU, (float) buttonV, 22, 14, 256, 256);

            int pct = this.filter.getEffectiveDurabilityPercent();
            int iconU;
            int iconV;

            if (pct >= 76) {
                iconU = 208; iconV = 84;
            } else if (pct >= 51) {
                iconU = 224; iconV = 84;
            } else if (pct >= 26) {
                iconU = 240; iconV = 84;
            } else if (pct >= 6) {
                iconU = 232; iconV = 75;
            } else if (pct >= 1) {
                iconU = 216; iconV = 75;
            } else {
                iconU = 216; iconV = 75;
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_GUI_TEXTURE, this.getX() + 3, this.getY() + 3, (float) iconU, (float) iconV, 16, 8, 256, 256);

            if (this.isHovered()) {
                CableFilter.DurabilityCondition cond = filter.parseCustomDurability();
                Component durText;
                if (cond != null) {
                    durText = Component.literal("Durability: " + cond.formatDisplay()).withStyle(net.minecraft.ChatFormatting.GREEN);
                } else if (pct > 0) {
                    durText = Component.literal("Durability: <= " + pct + "%").withStyle(net.minecraft.ChatFormatting.GREEN);
                } else {
                    durText = Component.literal("Durability Condition: Disabled").withStyle(net.minecraft.ChatFormatting.GRAY);
                }

                graphics.setTooltipForNextFrame(
                        Minecraft.getInstance().font,
                        List.of(durText),
                        java.util.Optional.empty(),
                        ItemStack.EMPTY,
                        mouseX, mouseY, null
                );
            }
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress(@NonNull InputWithModifiers input) {
            if (this.activeSupplier.get()) {
                this.onPress.accept(this);
            }
        }
    }

    private static class CustomLargeActionBtn extends AbstractButton {
        private final boolean active;
        private final java.util.function.Consumer<CustomLargeActionBtn> onPress;

        public CustomLargeActionBtn(int x, int y, int width, int height, Component message, boolean active, java.util.function.Consumer<CustomLargeActionBtn> onPress) {
            super(x, y, width, height, message);
            this.active = active;
            this.onPress = onPress;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            int u = this.active ? 176 : 208;
            int v = this.active ? (this.isHovered() ? 66 : 50) : 96;

            graphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_GUI_TEXTURE, this.getX(), this.getY(), (float) u, (float) v, this.width, this.height, 256, 256);

            int textColor = this.active ? (this.isHovered() ? 0xFFFFFFA0 : 0xFFE0E0E0) : 0xFF707070;
            graphics.centeredText(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    this.getX() + this.width / 2,
                    this.getY() + (this.height - 8) / 2,
                    textColor
            );
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress(@NonNull InputWithModifiers input) {
            if (this.active) {
                this.onPress.accept(this);
            }
        }
    }

    public void onInsertFluid(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.isEmpty() || !isFluidMode()) return;
        String fluidId = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString();
        this.getMenu().getFilter().setTagString(fluidId);

        ItemStack bucketStack = new ItemStack(fluidStack.getFluid().getBucket());
        this.getMenu().getFilter().setFilterStack(bucketStack);

        if (this.tagInput != null) {
            this.tagInput.setValue(fluidId);
        }

        String snbt = extractNbtString(bucketStack);
        if (this.nbtInput != null) {
            this.nbtInput.setValue(snbt);
        }
    }

    public void onInsertStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        // Se estamos na aba de Fluidos (Aba 2)
        if (isFluidMode()) {
            if (stack.getItem() instanceof net.minecraft.world.item.BucketItem bucketItem) {
                Fluid fluid = bucketItem.content;
                if (fluid != Fluids.EMPTY) {
                    onInsertFluid(new FluidStack(fluid, 1000));
                    return;
                }
            }

            FluidStack contained = FluidUtil.getFirstStackContained(stack);
            if (!contained.isEmpty()) {
                onInsertFluid(contained);
                return;
            }

            return;
        }

        // Se estamos na aba de Itens (Aba 1)
        ItemStack copy = stack.copy();
        copy.setCount(1);
        this.getMenu().getFilter().setFilterStack(copy);
        String regName = BuiltInRegistries.ITEM.getKey(copy.getItem()).toString();
        this.getMenu().getFilter().setTagString(regName);

        if (this.tagInput != null) {
            this.tagInput.setValue(regName);
        }

        String snbt = extractNbtString(copy);
        if (this.nbtInput != null) {
            this.nbtInput.setValue(snbt);
        }
    }
}