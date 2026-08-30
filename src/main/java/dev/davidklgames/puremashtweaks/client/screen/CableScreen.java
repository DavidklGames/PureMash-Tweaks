package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.entity.cable.CableFilter;
import dev.davidklgames.puremashtweaks.block.entity.cable.CableRedstoneMode;
import dev.davidklgames.puremashtweaks.block.entity.cable.DistributionMode;
import dev.davidklgames.puremashtweaks.client.renderer.FluidRenderHelper;
import dev.davidklgames.puremashtweaks.menu.CableMenu;
import dev.davidklgames.puremashtweaks.network.DeleteCableFilterPayload;
import dev.davidklgames.puremashtweaks.network.OpenCableFilterPayload;
import dev.davidklgames.puremashtweaks.network.UpdateCableFilterPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Unique GUI Screen for PureMash Universal Cables (176x189).
 */
public class CableScreen extends BaseContainerCompressionScreen<CableMenu> {

    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(
            PureMashTweaks.MODID,
            "textures/gui/cables/cable_gui.png"
    );

    private CustomLargeButton addFilterBtn;
    private CustomLargeButton editFilterBtn;
    private int selectedFilterIndex = -1;

    private float scrollOffs = 0.0F;
    private boolean isScrolling = false;

    public CableScreen(CableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, GUI_TEXTURE, 176, 189);
        this.inventoryLabelX = 10;
        this.inventoryLabelY = 95;
    }

    private boolean isFluidTab() {
        return this.getMenu().getSelectedTab() == 2;
    }

    private boolean isItemTab() {
        return this.getMenu().getSelectedTab() == 1;
    }

    @Override
    protected void init() {
        super.init();

        // 1. Redstone Mode Button (X=102, Y=80 | 20x20)
        this.addRenderableWidget(new SmallIconButton(
                this.leftPos + 102, this.topPos + 80,
                () -> this.getMenu().hasUpgrade(),
                () -> {
                    CableRedstoneMode mode = this.getMenu().getRedstoneMode();
                    return new int[]{mode.getU(), mode.getV()};
                },
                b -> {
                    if (this.getMenu().hasUpgrade()) {
                        assert this.minecraft.gameMode != null;
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
                    }
                },
                () -> List.of(
                        Component.literal("Redstone Control: ").withStyle(ChatFormatting.GRAY)
                                .append(this.getMenu().hasUpgrade() ?
                                        this.getMenu().getRedstoneMode().getDisplayName().copy().withStyle(ChatFormatting.RED) :
                                        Component.literal("Upgrade Required").withStyle(ChatFormatting.DARK_RED))
                )
        ));

        // 2. Distribution Mode Button (X=125, Y=80 | 20x20)
        this.addRenderableWidget(new SmallIconButton(
                this.leftPos + 125, this.topPos + 80,
                () -> this.getMenu().hasUpgrade(),
                () -> {
                    DistributionMode mode = this.getMenu().getDistributionMode();
                    return new int[]{mode.getU(), mode.getV()};
                },
                b -> {
                    if (this.getMenu().hasUpgrade()) {
                        assert this.minecraft.gameMode != null;
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                },
                () -> List.of(
                        Component.literal("Distribution Mode: ").withStyle(ChatFormatting.GRAY)
                                .append(this.getMenu().hasUpgrade() ?
                                        this.getMenu().getDistributionMode().getDisplayName().copy().withStyle(ChatFormatting.AQUA) :
                                        Component.literal("Upgrade Required").withStyle(ChatFormatting.DARK_RED))
                )
        ));

        // 3. Filter Mode Button (X=148, Y=80 | 20x20)
        this.addRenderableWidget(new SmallIconButton(
                this.leftPos + 148, this.topPos + 80,
                () -> this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0,
                () -> this.getMenu().isBlacklist() ? new int[]{224, 32} : new int[]{208, 32},
                b -> {
                    if (this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0) {
                        assert this.minecraft.gameMode != null;
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
                    }
                },
                () -> List.of(
                        Component.literal("Filter Policy: ").withStyle(ChatFormatting.GRAY)
                                .append(!this.getMenu().hasUpgrade() ?
                                        Component.literal("Upgrade Required").withStyle(ChatFormatting.DARK_RED) :
                                        (this.getMenu().getSelectedTab() == 0 ?
                                                Component.literal("N/A for Energy").withStyle(ChatFormatting.DARK_GRAY) :
                                                (this.getMenu().isBlacklist() ?
                                                        Component.literal("Blacklist").withStyle(ChatFormatting.RED) :
                                                        Component.literal("Whitelist").withStyle(ChatFormatting.GREEN))))
                )
        ));

        // 4. Add Filter Button
        this.addFilterBtn = this.addRenderableWidget(new CustomLargeButton(
                this.leftPos + 7, this.topPos + 77, 30, 14,
                Component.literal("Add"),
                () -> this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0,
                b -> {
                    if (this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0 && this.getMenu().getBlockEntity() != null) {
                        ClientPacketDistributor.sendToServer(
                                new OpenCableFilterPayload(
                                        this.getMenu().getBlockEntity().getBlockPos(),
                                        this.getMenu().getSide(),
                                        -1
                                )
                        );
                    }
                }
        ));

        // 5. Edit Filter Button
        this.editFilterBtn = this.addRenderableWidget(new CustomLargeButton(
                this.leftPos + 41, this.topPos + 77, 30, 14,
                Component.literal("Edit"),
                () -> this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0 && selectedFilterIndex >= 0 && selectedFilterIndex < getFilters().size(),
                b -> {
                    if (this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0 && selectedFilterIndex >= 0 && selectedFilterIndex < getFilters().size() && this.getMenu().getBlockEntity() != null) {
                        ClientPacketDistributor.sendToServer(
                                new OpenCableFilterPayload(
                                        this.getMenu().getBlockEntity().getBlockPos(),
                                        this.getMenu().getSide(),
                                        selectedFilterIndex
                                )
                        );
                    }
                }
        ));

        // 6. Delete Button
        this.addRenderableWidget(new DeleteIconButton(
                this.leftPos + 4, this.topPos + 6,
                () -> this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0 && selectedFilterIndex >= 0 && selectedFilterIndex < getFilters().size(),
                b -> {
                    if (this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0 && selectedFilterIndex >= 0 && selectedFilterIndex < getFilters().size() && this.getMenu().getBlockEntity() != null) {
                        ClientPacketDistributor.sendToServer(
                                new DeleteCableFilterPayload(
                                        this.getMenu().getBlockEntity().getBlockPos(),
                                        this.getMenu().getSide(),
                                        selectedFilterIndex
                                )
                        );
                        selectedFilterIndex = -1;
                    }
                }
        ));

        // 7. Appendix Side Tab Buttons
        this.addRenderableWidget(new AppendixTabButton(
                this.leftPos - 25, this.topPos + 5, 0,
                () -> this.getMenu().getSelectedTab() == 0,
                new int[]{241, 81, 13, 12},
                Component.literal("Energy Cable Mode").withStyle(ChatFormatting.GOLD),
                b -> {
                    selectedFilterIndex = -1;
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 10);
                }
        ));

        this.addRenderableWidget(new AppendixTabButton(
                this.leftPos - 25, this.topPos + 29, 1,
                () -> this.getMenu().getSelectedTab() == 1,
                new int[]{225, 81, 14, 14},
                Component.literal("Item Cable Mode").withStyle(ChatFormatting.AQUA),
                b -> {
                    selectedFilterIndex = -1;
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 11);
                }
        ));

        this.addRenderableWidget(new AppendixTabButton(
                this.leftPos - 25, this.topPos + 53, 2,
                () -> this.getMenu().getSelectedTab() == 2,
                new int[]{211, 81, 12, 14},
                Component.literal("Fluid Cable Mode").withStyle(ChatFormatting.BLUE),
                b -> {
                    selectedFilterIndex = -1;
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 12);
                }
        ));
    }

    private List<CableFilter> getFilters() {
        return this.getMenu().getBlockEntity() != null ?
                this.getMenu().getBlockEntity().getFilters(this.getMenu().getSide(), this.getMenu().getSelectedTab()) : List.of();
    }

    private boolean canScroll() {
        return this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0 && this.getFilters().size() > 3;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean handled) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (event.button() == 0) {
            int scrollbarX = this.leftPos + 146;
            int scrollbarY = this.topPos + 7;
            if (mouseX >= scrollbarX && mouseX < scrollbarX + 10 && mouseY >= scrollbarY && mouseY < scrollbarY + 66) {
                if (canScroll()) {
                    this.isScrolling = true;
                    return true;
                }
            }

            if (mouseX >= this.leftPos + 20 && mouseX <= this.leftPos + 144 &&
                    mouseY >= this.topPos + 7 && mouseY <= this.topPos + 72) {
                List<CableFilter> list = getFilters();
                int visibleOffset = getOffset();
                int clickedRow = (mouseY - (this.topPos + 7)) / 22;
                int targetIdx = visibleOffset + clickedRow;

                if (targetIdx >= 0 && targetIdx < list.size()) {
                    this.selectedFilterIndex = targetIdx;
                    return true;
                } else {
                    this.selectedFilterIndex = -1;
                }
            }
        }

        // Quick Filter addition by Shift-clicking inventory items
        if (event.hasShiftDown() && this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0) {
            Slot hovered = this.getHoveredSlot();
            if (hovered != null && hovered.hasItem() && hovered.index >= 1) {
                addQuickFilter(hovered.getItem());
                return true;
            }
        }

        return super.mouseClicked(event, handled);
    }

    private void addQuickFilter(ItemStack stack) {
        if (stack == null || stack.isEmpty() || this.getMenu().getBlockEntity() == null) return;

        CableFilter newFilter = new CableFilter();

        if (isFluidTab()) {
            Fluid fluid = Fluids.EMPTY;
            if (stack.getItem() instanceof net.minecraft.world.item.BucketItem bucket && bucket.content != Fluids.EMPTY) {
                fluid = bucket.content;
            } else {
                FluidStack contained = FluidUtil.getFirstStackContained(stack);
                if (!contained.isEmpty()) fluid = contained.getFluid();
            }

            if (fluid != Fluids.EMPTY) {
                newFilter.setTagString(BuiltInRegistries.FLUID.getKey(fluid).toString());
                newFilter.setFilterStack(new ItemStack(fluid.getBucket()));
            } else {
                return; // Non-fluid items ignored in fluid tab
            }
        } else if (isItemTab()) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            newFilter.setFilterStack(copy);
            newFilter.setTagString(BuiltInRegistries.ITEM.getKey(copy.getItem()).toString());
        } else {
            return;
        }

        ClientPacketDistributor.sendToServer(new UpdateCableFilterPayload(
                this.getMenu().getBlockEntity().getBlockPos(),
                this.getMenu().getSide(),
                -1,
                newFilter.serializeNBT()
        ));
    }

    private int getOffset() {
        List<CableFilter> f = this.getFilters();
        if (f.size() <= 3) return 0;
        int max = f.size() - 3;
        return Math.clamp((int) Math.round(this.scrollOffs * max), 0, max);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.isScrolling = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (canScroll()) {
            int extraItems = this.getFilters().size() - 3;
            float scrollStep = 1.0F / (float) extraItems;
            this.scrollOffs = Mth.clamp(this.scrollOffs - (float) deltaY * scrollStep, 0.0F, 1.0F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private void drawStringScaled(GuiGraphicsExtractor guiGraphics, int x, int y, Component text, float scale) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float) x, (float) y);
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.text(this.font, text, 0, 0, 0xFFFFFFFF, false);
        guiGraphics.pose().popMatrix();
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = this.leftPos;
        int y = this.topPos;

        if (this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0) {
            List<CableFilter> filters = this.getFilters();
            int offset = getOffset();

            for (int i = 0; i < 3 && (offset + i) < filters.size(); i++) {
                int filterIdx = offset + i;
                CableFilter filter = filters.get(filterIdx);
                int rowY = y + 7 + (i * 22);
                int rowX = x + 20;

                // 1. Textura de Fundo do Filtro
                if (filterIdx == this.selectedFilterIndex) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, rowX, rowY, 0.0F, 212.0F, 125, 22, 256, 256);
                } else {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, rowX, rowY, 0.0F, 190.0F, 125, 22, 256, 256);
                }

                // 2. Caixa de seleção branca no hover
                if (mouseX >= rowX && mouseX < rowX + 125 && mouseY >= rowY && mouseY < rowY + 22) {
                    graphics.fill(rowX, rowY, rowX + 125, rowY + 22, 0x80FFFFFF);
                }

                // 3. Ícone (Renderização de Fluido ou Item)
                if (isFluidTab()) {
                    Fluid fluid = filter.getDisplayFluid(this.minecraft.level);
                    if (fluid != Fluids.EMPTY) {
                        TextureAtlasSprite sprite = FluidRenderHelper.getFluidTexture(fluid);
                        int color = FluidRenderHelper.getFluidColor(fluid, 1000);
                        if (sprite != null) {
                            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, rowX + 2, rowY + 3, 16, 16, color);
                        }
                    }
                } else {
                    ItemStack displayStack = filter.getDisplayStack(this.minecraft.level);
                    if (!displayStack.isEmpty()) {
                        graphics.fakeItem(displayStack, rowX + 2, rowY + 3);
                    }
                }

                // 4. Linha 1: Nome / Mod ID / Tag
                if (filter.getModNamespace() != null && !filter.getModNamespace().isEmpty()) {
                    String modName = CableFilter.getModDisplayName(filter.getModNamespace());
                    String formatted = modName + " (" + filter.getModNamespace() + ")";
                    drawStringScaled(graphics, rowX + 21, rowY + 3, Component.literal(formatted).withStyle(ChatFormatting.AQUA), 0.65F);
                } else if (filter.getTagString() != null && filter.getTagString().startsWith("@")) {
                    String modName = CableFilter.getModDisplayName(filter.getTagString().substring(1));
                    String formatted = modName + " (" + filter.getTagString() + ")";
                    drawStringScaled(graphics, rowX + 21, rowY + 3, Component.literal(formatted).withStyle(ChatFormatting.AQUA), 0.65F);
                } else if (filter.getTagString() != null && filter.getTagString().startsWith("#")) {
                    drawStringScaled(graphics, rowX + 21, rowY + 3, Component.literal(filter.getTagString()).withStyle(ChatFormatting.BLUE), 0.7F);
                } else if (isFluidTab()) {
                    Fluid fluid = filter.getDisplayFluid(this.minecraft.level);
                    if (fluid != Fluids.EMPTY) {
                        drawStringScaled(graphics, rowX + 21, rowY + 3, fluid.getFluidType().getDescription().copy().withStyle(ChatFormatting.WHITE), 0.7F);
                    } else {
                        drawStringScaled(graphics, rowX + 21, rowY + 3, Component.literal("Any Fluid").withStyle(ChatFormatting.GRAY), 0.7F);
                    }
                } else {
                    ItemStack displayStack = filter.getDisplayStack(this.minecraft.level);
                    if (!displayStack.isEmpty()) {
                        drawStringScaled(graphics, rowX + 21, rowY + 3, Component.literal(displayStack.getHoverName().getString()).withStyle(ChatFormatting.WHITE), 0.7F);
                    } else {
                        drawStringScaled(graphics, rowX + 21, rowY + 3, Component.literal("Any Item").withStyle(ChatFormatting.GRAY), 0.7F);
                    }
                }

                // 5. Linha 2: NBT / Durabilidade
                CableFilter.DurabilityCondition cond = filter.parseCustomDurability();

                if (filter.getMetadata() != null && !filter.getMetadata().isEmpty()) {
                    int tagCount = filter.getMetadata().size();
                    MutableComponent nbtTags = Component.literal(tagCount + " tag" + (tagCount != 1 ? "s" : "")).withStyle(ChatFormatting.DARK_PURPLE);
                    MutableComponent nbtStr = Component.literal("NBT: ").withStyle(ChatFormatting.WHITE).append(nbtTags);
                    if (filter.getNbtMode() == 1) {
                        nbtStr.append(Component.literal(" (Exact)").withStyle(ChatFormatting.GRAY));
                    } else if (filter.getNbtMode() == 2) {
                        nbtStr.append(Component.literal(" (Fuzzy)").withStyle(ChatFormatting.YELLOW));
                    }
                    drawStringScaled(graphics, rowX + 21, rowY + 10, nbtStr, 0.5F);
                } else if (cond != null) {
                    drawStringScaled(graphics, rowX + 21, rowY + 10, Component.literal("Durability: " + cond.formatDisplay()).withStyle(ChatFormatting.GREEN), 0.5F);
                } else if (filter.getMinDurabilityPercent() > 0) {
                    drawStringScaled(graphics, rowX + 21, rowY + 10, Component.literal("Durability: <= " + filter.getMinDurabilityPercent() + "%").withStyle(ChatFormatting.GREEN), 0.5F);
                }

                // 6. Linha 3: Política / Prioridade / Stock Limit
                if (filter.isInvert()) {
                    drawStringScaled(graphics, rowX + 21, rowY + 15, Component.literal("Inverted (Blacklist)").withStyle(ChatFormatting.DARK_RED), 0.5F);
                } else if (filter.getPriority() > 0) {
                    drawStringScaled(graphics, rowX + 21, rowY + 15, Component.literal("Priority: " + filter.getPriority()).withStyle(ChatFormatting.YELLOW), 0.5F);
                } else if (filter.getStockLimit() > 0) {
                    drawStringScaled(graphics, rowX + 21, rowY + 15, Component.literal("Stock Limit: " + filter.getStockLimit()).withStyle(ChatFormatting.GOLD), 0.5F);
                }

                // 7. Marcador de Destino (Textura 7x10 em U=181, V=83)
                CompoundTag destTag = filter.getDestinationTag();
                if (destTag != null && destTag.contains("X")) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, rowX + 114, rowY + 5, 181.0F, 83.0F, 7, 10, 256, 256);
                }
            }
        }

        // Scrollbar
        int scrollbarX = x + 146;
        int initialY = y + 7;

        if (canScroll()) {
            if (this.isScrolling) {
                float rawY = (float) (mouseY - initialY - 8.5F);
                this.scrollOffs = Mth.clamp(rawY / 49.0F, 0.0F, 1.0F);
            }
            int thumbY = initialY + (int) (this.scrollOffs * 49.0F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, scrollbarX, thumbY, 125.0F, 190.0F, 10, 17, 256, 256);
        } else {
            this.scrollOffs = 0.0F;
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, scrollbarX, initialY, 135.0F, 190.0F, 10, 17, 256, 256);
        }
    }

    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.getMenu().hasUpgrade() && this.getMenu().getSelectedTab() != 0) {
            List<CableFilter> filters = this.getFilters();
            int offset = getOffset();

            for (int i = 0; i < 3 && (offset + i) < filters.size(); i++) {
                int filterIdx = offset + i;
                CableFilter filter = filters.get(filterIdx);
                int rowY = this.topPos + 7 + (i * 22);
                int rowX = this.leftPos + 20;

                // Hover no Ícone
                if (mouseX >= rowX + 2 && mouseX <= rowX + 18 &&
                        mouseY >= rowY + 2 && mouseY <= rowY + 20) {

                    List<Component> tooltip = new ArrayList<>();

                    if (isFluidTab()) {
                        Fluid fluid = filter.getDisplayFluid(this.minecraft.level);
                        if (fluid != Fluids.EMPTY) {
                            tooltip.add(fluid.getFluidType().getDescription().copy().withStyle(ChatFormatting.AQUA));
                            if (filter.getTagString() != null && filter.getTagString().startsWith("#")) {
                                tooltip.add(Component.literal("Accepts Fluid Tag: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(filter.getTagString()).withStyle(ChatFormatting.BLUE)));
                            }
                            if (filter.getModNamespace() != null && !filter.getModNamespace().isEmpty()) {
                                tooltip.add(Component.literal("Mod: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(CableFilter.getModDisplayName(filter.getModNamespace())).withStyle(ChatFormatting.AQUA)));
                            }
                            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
                        }
                    } else {
                        ItemStack display = filter.getDisplayStack(this.minecraft.level);
                        if (!display.isEmpty()) {
                            tooltip.addAll(net.minecraft.client.gui.screens.Screen.getTooltipFromItem(this.minecraft, display));
                            if (filter.getModNamespace() != null && !filter.getModNamespace().isEmpty()) {
                                tooltip.add(Component.literal("Mod: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(CableFilter.getModDisplayName(filter.getModNamespace()) + " (" + filter.getModNamespace() + ")").withStyle(ChatFormatting.AQUA)));
                            } else if (filter.getTagString() != null && filter.getTagString().startsWith("#")) {
                                tooltip.add(Component.literal("Accepts Tag: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(filter.getTagString()).withStyle(ChatFormatting.BLUE)));
                            }
                            if (filter.getMetadata() != null && !filter.getMetadata().isEmpty()) {
                                tooltip.add(Component.literal("NBT Tags: " + filter.getMetadata().size()).withStyle(ChatFormatting.DARK_PURPLE));
                            }
                            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), display, mouseX, mouseY, null);
                        }
                    }
                }

                // Hover no Marcador de Destino
                CompoundTag destTag = filter.getDestinationTag();
                if (destTag != null && destTag.contains("X") &&
                        mouseX >= rowX + 108 && mouseX <= rowX + 124 &&
                        mouseY >= rowY + 2 && mouseY <= rowY + 20) {
                    List<Component> destTooltip = new ArrayList<>();
                    int dx = destTag.getIntOr("X", 0);
                    int dy = destTag.getIntOr("Y", 0);
                    int dz = destTag.getIntOr("Z", 0);

                    MutableComponent coordsFormatted = Component.empty()
                            .append(Component.literal("X=").withStyle(ChatFormatting.RED))
                            .append(Component.literal(String.valueOf(dx)).withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("Y=").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(String.valueOf(dy)).withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("Z=").withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(String.valueOf(dz)).withStyle(ChatFormatting.GREEN));

                    destTooltip.add(Component.literal("Destination: [").withStyle(ChatFormatting.GRAY)
                            .append(coordsFormatted)
                            .append(Component.literal("]").withStyle(ChatFormatting.GRAY)));

                    graphics.setTooltipForNextFrame(this.font, destTooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
                }
            }
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.playerInventoryTitle, 10, 95, 0xFFE0E0E0, false);
    }

    private static class SmallIconButton extends AbstractButton {
        private final java.util.function.Supplier<Boolean> activeSupplier;
        private final java.util.function.Supplier<int[]> uvSupplier;
        private final java.util.function.Consumer<SmallIconButton> onPress;
        private final java.util.function.Supplier<List<Component>> tooltipSupplier;

        public SmallIconButton(int x, int y, java.util.function.Supplier<Boolean> activeSupplier, java.util.function.Supplier<int[]> uvSupplier, java.util.function.Consumer<SmallIconButton> onPress, java.util.function.Supplier<List<Component>> tooltipSupplier) {
            super(x, y, 20, 20, Component.empty());
            this.activeSupplier = activeSupplier;
            this.uvSupplier = uvSupplier;
            this.onPress = onPress;
            this.tooltipSupplier = tooltipSupplier;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            boolean isActive = this.activeSupplier.get();
            int buttonU = isActive ? (this.isHovered() ? 233 : 212) : 150;
            int buttonV = isActive ? 55 : 192;

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), (float) buttonU, (float) buttonV, 20, 20, 256, 256);

            int[] uv = this.uvSupplier.get();
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX() + 2, this.getY() + 2, (float) uv[0], (float) uv[1], 16, 16, 256, 256);

            if (this.isHovered() && this.tooltipSupplier != null) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.tooltipSupplier.get(), java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            if (this.activeSupplier.get()) {
                this.onPress.accept(this);
            }
        }
    }

    private static class CustomLargeButton extends AbstractButton {
        private final java.util.function.Supplier<Boolean> activeSupplier;
        private final java.util.function.Consumer<CustomLargeButton> onPress;

        public CustomLargeButton(int x, int y, int width, int height, Component message, java.util.function.Supplier<Boolean> activeSupplier, java.util.function.Consumer<CustomLargeButton> onPress) {
            super(x, y, width, height, message);
            this.activeSupplier = activeSupplier;
            this.onPress = onPress;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            boolean isActive = this.activeSupplier.get();
            int u = isActive ? 179 : 145;
            int v = isActive ? (this.isHovered() ? 66 : 49) : 213;

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), (float) u, (float) v, this.width, this.height, 256, 256);

            int textColor = isActive ? (this.isHovered() ? 0xFFFFFFA0 : 0xFFE0E0E0) : 0xFF707070;
            graphics.centeredText(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    this.getX() + this.width / 2,
                    this.getY() + (this.height - 8) / 2,
                    textColor
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            if (this.activeSupplier.get()) {
                this.onPress.accept(this);
            }
        }
    }

    private static class DeleteIconButton extends AbstractButton {
        private final java.util.function.Supplier<Boolean> activeSupplier;
        private final java.util.function.Consumer<DeleteIconButton> onPress;

        public DeleteIconButton(int x, int y, java.util.function.Supplier<Boolean> activeSupplier, java.util.function.Consumer<DeleteIconButton> onPress) {
            super(x, y, 14, 14, Component.empty());
            this.activeSupplier = activeSupplier;
            this.onPress = onPress;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            boolean isActive = this.activeSupplier.get();
            int u = isActive ? (this.isHovered() ? 193 : 129) : 129;
            int v = isActive ? (this.isHovered() ? 81 : 209) : 224;

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), (float) u, (float) v, 14, 14, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX() - 1, this.getY() - 1, 240.0F, 32.0F, 16, 16, 256, 256);

            if (this.isHovered() && isActive) {
                graphics.setTooltipForNextFrame(
                        Minecraft.getInstance().font,
                        List.of(Component.literal("Delete Filter").withStyle(ChatFormatting.RED)),
                        java.util.Optional.empty(),
                        ItemStack.EMPTY,
                        mouseX, mouseY, null
                );
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            if (this.activeSupplier.get()) {
                this.onPress.accept(this);
            }
        }
    }

    private static class AppendixTabButton extends AbstractButton {
        private final java.util.function.Supplier<Boolean> inUseSupplier;
        private final int[] iconUv;
        private final Component tooltipText;
        private final java.util.function.Consumer<AppendixTabButton> onPress;

        public AppendixTabButton(int x, int y, int tabIndex, java.util.function.Supplier<Boolean> inUseSupplier, int[] iconUv, Component tooltipText, java.util.function.Consumer<AppendixTabButton> onPress) {
            super(x, y, 25, 24, Component.empty());
            this.inUseSupplier = inUseSupplier;
            this.iconUv = iconUv;
            this.tooltipText = tooltipText;
            this.onPress = onPress;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            boolean inUse = this.inUseSupplier.get();

            if (inUse) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), 177.0F, 0.0F, 25, 24, 256, 256);
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX() + 2, this.getY(), 179.0F, 24.0F, 23, 24, 256, 256);
            }

            int iconX = this.getX() + (inUse ? 6 : 7);
            int iconY = this.getY() + 5;
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, iconX, iconY, (float) this.iconUv[0], (float) this.iconUv[1], this.iconUv[2], this.iconUv[3], 256, 256);

            if (this.isHovered()) {
                graphics.setTooltipForNextFrame(
                        Minecraft.getInstance().font,
                        List.of(this.tooltipText),
                        java.util.Optional.empty(),
                        ItemStack.EMPTY,
                        mouseX, mouseY, null
                );
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.onPress.accept(this);
        }
    }
}