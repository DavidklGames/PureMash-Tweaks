package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.menu.ChunkLoaderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("all")
public class ChunkLoaderScreen extends BaseContainerCompressionScreen<ChunkLoaderMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/chunk_loader/chunk_loader_gui.png");

    private ChunkLoaderButton decLevelBtn;
    private ChunkLoaderButton incLevelBtn;
    private ChunkLoaderButton boundaryBtn;

    public ChunkLoaderScreen(ChunkLoaderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, GUI_TEXTURE, 176, 194);
        this.inventoryLabelY = 101;
    }

    @Override
    protected void init() {
        super.init();
        this.rebuildButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButtonStates();
    }

    private void rebuildButtons() {
        this.clearWidgets();

        this.decLevelBtn = this.addRenderableWidget(new ChunkLoaderButton(
                this.leftPos + 40, this.topPos + 57, 30, 14,
                Component.literal("-"), GUI_TEXTURE, 179, 0,
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                }
        ));

        this.incLevelBtn = this.addRenderableWidget(new ChunkLoaderButton(
                this.leftPos + 106, this.topPos + 57, 30, 14,
                Component.literal("+"), GUI_TEXTURE, 179, 0,
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
                }
        ));

        this.boundaryBtn = this.addRenderableWidget(new ChunkLoaderButton(
                this.leftPos + 73, this.topPos + 21, 30, 14,
                Component.literal("B"), GUI_TEXTURE, 179, 0,
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
                }
        ));

        this.updateButtonStates();
    }

    private void updateButtonStates() {
        int activeLevel = this.getMenu().getActiveLevel();

        if (this.decLevelBtn != null) {
            this.decLevelBtn.active = (activeLevel > 0);
        }

        if (this.incLevelBtn != null) {
            this.incLevelBtn.active = (activeLevel < 5);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        int titleWidth = this.font.width(this.title);
        graphics.text(this.font, this.title, (this.imageWidth / 2) - (titleWidth / 2), 6, 0xFFE0E0E0, false);

        graphics.text(this.font, this.playerInventoryTitle, 9, 101, 0xFFE0E0E0, false);

        boolean showingBorders = this.getMenu().isShowingBoundary();
        Component borderText = showingBorders ?
                Component.literal("Borders: ON").withStyle(net.minecraft.ChatFormatting.GREEN) :
                Component.literal("Borders: OFF").withStyle(net.minecraft.ChatFormatting.RED);
        int borderTextWidth = this.font.width(borderText);
        graphics.text(this.font, borderText, (this.imageWidth / 2) - (borderTextWidth / 2), 38, 0xFFFFFFFF, false);

        int activeLevel = this.getMenu().getActiveLevel();
        String areaText = switch (activeLevel) {
            case 1 -> "Level 2 (3x3 Chunks)";
            case 2 -> "Level 3 (5x5 Chunks)";
            case 3 -> "Level 4 (9x9 Chunks)";
            case 4 -> "Level 5 (15x15 Chunks)";
            case 5 -> "Level 6 (17x17 Chunks)";
            default -> "Level 1 (1x1 Chunk)";
        };
        int textWidth = this.font.width(areaText);
        graphics.text(this.font, areaText, (this.imageWidth / 2) - (textWidth / 2), 79, 0xFF55FFFF, false);

        if (activeLevel >= 3 && !this.getMenu().hasCoreInstalled()) {
            String warningText = "* Moldelonian Core Required";
            int warnWidth = this.font.width(warningText);
            graphics.text(this.font, warningText, (this.imageWidth / 2) - (warnWidth / 2), 91, 0xFFFF5555, false);
        }
    }

    private static class ChunkLoaderButton extends Button {
        private final Identifier texture;
        private final int texU, texV;

        public ChunkLoaderButton(int x, int y, int width, int height, Component message, Identifier texture, int u, int v, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.texture = texture;
            this.texU = u;
            this.texV = v;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            int v = this.isHovered() ? this.texV + 17 : this.texV;

            graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), (float)this.texU, (float)v, this.width, this.height, 256, 256);

            int textColor = this.active ? (this.isHovered() ? 16777120 : 14737632) : 10526880;
            textColor |= 0xFF000000;

            graphics.centeredText(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    this.getX() + this.width / 2,
                    this.getY() + (this.height - 8) / 2,
                    textColor
            );
        }
    }
}