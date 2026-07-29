package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.menu.SynthesisTableMenu;
import dev.davidklgames.puremashtweaks.block.entity.SynthesisTableBlockEntity;
import dev.davidklgames.puremashtweaks.client.screen.component.SynthesisTableButton;
import dev.davidklgames.puremashtweaks.component.ModDataComponents;
import dev.davidklgames.puremashtweaks.registry.ModItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.client.renderer.RenderPipelines;
import org.jspecify.annotations.NonNull;

@SuppressWarnings({"UnclearExpression", "removal"})
public class SynthesisTableScreen extends BaseContainerScreen<SynthesisTableMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/crafting/synthesis_table_gui.png");

    private SynthesisTableButton automationToggleBtn;
    private SynthesisTableButton modeToggleBtn;
    private SynthesisTableButton saveBtn;
    private SynthesisTableButton changeBtn;

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

        // 1. Main Button: Toggle Automation (On / Off)
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

        // 2. Mode Button: Toggle between Craft and Add
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

        // 3. Action Button: Write recipe pattern to Card (Save)
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

        // 4. Recipe Toggle Button: Change (positioned 3 pixels below the large 26x26 Slot 81)
        this.changeBtn = this.addRenderableWidget(new SynthesisTableButton(
                this.leftPos + 199,
                this.topPos + 113,
                30,
                14,
                Component.literal("Change"),
                GUI_TEXTURE,
                237,
                0,
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 4);
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

        // Dynamically updates button labels and visibilities
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

        if (this.changeBtn != null) {
            this.changeBtn.visible = this.getMenu().getMatchingVanillaRecipesCount() > 1;
        }
    }

    // We draw the dynamic card slot background in the background method
    // This ensures that the background is drawn BEHIND the actual slots and items
    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        SynthesisTableBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile != null && tile.isAutomationActive()) {
            // Draws the slot border whenever automation is active (On), regardless of being Craft or Add
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
        if (tile == null) return;

        @SuppressWarnings("removal") ItemStack card = tile.inventory.getStackInSlot(82);

        if (!card.isEmpty()) {
            CompoundTag data = card.get(ModDataComponents.RECIPE_CARD_DATA.get());

            if (data != null) {
                ListTag itemsList = data.getListOrEmpty("GridItems");

                // Temporary list to store the coordinates where the ghost items are drawn
                java.util.List<int[]> renderedSlots = new java.util.ArrayList<>();

                // 1. FIRST PHASE: Renders the ghost items (Grid) and the result in a solid and stable manner
                for (int i = 0; i < itemsList.size(); i++) {
                    CompoundTag itemTag = itemsList.getCompoundOrEmpty(i);
                    int slot = itemTag.getIntOr("Slot", -1);

                    if (slot != -1 && slot < 81 && tile.inventory.getStackInSlot(slot).isEmpty()) {
                        if (this.minecraft.level != null) {
                            ItemStack.CODEC.parse(this.minecraft.level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), itemTag.getCompoundOrEmpty("Item"))
                                    .result()
                                    .ifPresent(stack -> {
                                        int gx = this.leftPos + 8 + (slot % 9) * 18;
                                        int gy = this.topPos + 18 + (slot / 9) * 18;
                                        graphics.fakeItem(stack, gx, gy);
                                        renderedSlots.add(new int[]{gx, gy}); // Stores the coordinate for the overlay
                                    });
                        }
                    }
                }

                // Renders the ghost item for the result (slot 81) if the actual slot is empty
                if (tile.inventory.getStackInSlot(81).isEmpty() && data.contains("OutputItem")) {
                    if (this.minecraft.level != null) {
                        ItemStack.CODEC.parse(this.minecraft.level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), data.getCompoundOrEmpty("OutputItem"))
                                .result()
                                .ifPresent(stack -> {
                                    int gx = this.leftPos + 206;
                                    int gy = this.topPos + 89;
                                    graphics.fakeItem(stack, gx, gy);
                                    renderedSlots.add(new int[]{gx, gy}); // Stores the coordinate for the overlay
                                });
                    }
                }

                // 2. SECOND PHASE: Advances to the next Stratum and applies the translucent Liquid Glass effect
                if (!renderedSlots.isEmpty()) {
                    graphics.nextStratum(); // Advances to a higher layer for separate rendering

                    for (int[] pos : renderedSlots) {
                        int gx = pos[0];
                        int gy = pos[1];
                        // Draws a semi-transparent gray/white square over the item
                        // 0x99FFFFFF -> 99 is the opacity (alpha of 60%), FFFFFF is white. Creates the glass mist effect!
                        graphics.fill(gx, gy, gx + 16, gy + 16, 0x448B8B8B);
                    }
                }
            }
        }
    }
}