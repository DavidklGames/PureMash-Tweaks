package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;
import dev.davidklgames.puremashtweaks.client.screen.component.SynthesisTableButton;
import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import dev.davidklgames.puremashtweaks.util.SynthesisTableHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnclearExpression", "removal"})
public class SynthesisTableScreen extends BaseContainerScreen<SynthesisTableMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(
            PureMashTweaks.MODID,
            "textures/gui/synthesis_table/synthesis_table_gui.png"
    );

    private SynthesisTableButton automationToggleBtn;
    private SynthesisTableButton modeToggleBtn;
    private SynthesisTableButton saveBtn;

    public SynthesisTableScreen(SynthesisTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, GUI_TEXTURE, 234, 278);
        this.inventoryLabelX = 39;
        this.inventoryLabelY = 186;
    }

    @Override
    protected void init() {
        super.init();
        int buttonsX = this.leftPos - 34;

        SynthesisTableBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile == null) return;

        // 1. Automation Toggle Button (On / Off)
        this.automationToggleBtn = this.addRenderableWidget(new SynthesisTableButton(
                buttonsX,
                this.topPos + 24,
                30,
                14,
                Component.literal(tile.isAutomationActive() ? "On" : "Off"),
                GUI_TEXTURE,
                237,
                0,
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                }
        ));

        // 2. Mode Toggle Button (Craft / Add)
        this.modeToggleBtn = this.addRenderableWidget(new SynthesisTableButton(
                buttonsX,
                this.topPos + 42,
                30,
                14,
                Component.literal(tile.getAutomationMode() == 0 ? "Craft" : "Add"),
                GUI_TEXTURE,
                237,
                0,
                b -> {
                    int nextMode = tile.getAutomationMode() == 0 ? 1 : 0;
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, nextMode == 0 ? 1 : 2);
                }
        ));

        // 3. Save Recipe Pattern Button (Save)
        this.saveBtn = this.addRenderableWidget(new SynthesisTableButton(
                buttonsX,
                this.topPos + 60,
                30,
                14,
                Component.literal("Save"),
                GUI_TEXTURE,
                237,
                0,
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3);
                }
        ));

        updateButtonVisibility();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        SynthesisTableBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile == null) return;

        boolean active = tile.isAutomationActive();
        int mode = tile.getAutomationMode();

        if (this.automationToggleBtn != null) {
            this.automationToggleBtn.setMessage(Component.literal(active ? "On" : "Off"));
        }

        if (this.modeToggleBtn != null) {
            this.modeToggleBtn.visible = active;
            this.modeToggleBtn.setMessage(Component.literal(mode == 0 ? "Craft" : "Add"));
        }

        if (this.saveBtn != null) {
            ItemStack card = tile.inventory.getStackInSlot(82);
            ItemStack result = tile.inventory.getStackInSlot(81);
            this.saveBtn.visible = active && (mode == 1) && !card.isEmpty() && card.is(ModItems.MEMORY_CARD.get()) && !result.isEmpty();
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        SynthesisTableBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile != null && tile.isAutomationActive()) {
            // Draw dedicated memory card slot border when automation is enabled
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.leftPos + 205, this.topPos + 49, 7.0F, 17.0F, 18, 18, 512, 512);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, this.inventoryLabelX + 3, 6, 0xFFE0E0E0, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFE0E0E0, false);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        SynthesisTableBlockEntity tile = this.getMenu().getBlockEntity();

        if (tile != null && tile.isAutomationActive()) {
            this.renderGhostItems(graphics);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void renderGhostItems(GuiGraphicsExtractor graphics) {
        SynthesisTableBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile == null || this.minecraft.level == null) return;

        ItemStack card = tile.inventory.getStackInSlot(82);
        if (!SynthesisTableHelper.hasEncodedRecipe(card)) return;

        var registries = this.minecraft.level.registryAccess();
        ItemStack[] ghostGrid = SynthesisTableHelper.readGridFromCard(card, registries);
        ItemStack ghostOutput = SynthesisTableHelper.readOutputFromCard(card, registries);

        List<int[]> renderedSlots = new ArrayList<>();

        // 1. Render Ghost Grid Items
        for (int slot = 0; slot < 81; slot++) {
            ItemStack expectedStack = ghostGrid[slot];
            if (!expectedStack.isEmpty() && tile.inventory.getStackInSlot(slot).isEmpty()) {
                int gx = this.leftPos + 8 + (slot % 9) * 18;
                int gy = this.topPos + 18 + (slot / 9) * 18;
                graphics.fakeItem(expectedStack, gx, gy);
                renderedSlots.add(new int[]{gx, gy});
            }
        }

        // 2. Render Ghost Output Item if output slot is empty
        if (!ghostOutput.isEmpty() && tile.inventory.getStackInSlot(81).isEmpty()) {
            int gx = this.leftPos + 206;
            int gy = this.topPos + 89;
            graphics.fakeItem(ghostOutput, gx, gy);
            renderedSlots.add(new int[]{gx, gy});
        }

        // 3. Liquid Glass Overlay Effect
        if (!renderedSlots.isEmpty()) {
            graphics.nextStratum();
            for (int[] pos : renderedSlots) {
                graphics.fill(pos[0], pos[1], pos[0] + 16, pos[1] + 16, 0x448B8B8B);
            }
        }
    }
}