package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu;
import dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity;
import dev.davidklgames.puremashtweaks.client.screen.component.MultifunctionalCompressorButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.RenderPipelines;
import org.jspecify.annotations.NonNull;

public class MultifunctionalCompressorScreen extends BaseContainerCompressionScreen<MultifunctionalCompressorMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/multifunctional_compressor/multifunctional_compressor_gui.png");

    private ModeToggleButton modeBtn;
    private LockButton lockBtn;
    private final MultifunctionalCompressorButton[] sideBtns = new MultifunctionalCompressorButton[6];

    public MultifunctionalCompressorScreen(MultifunctionalCompressorMenu menu, Inventory inv, Component title) {
        // Aligned with the 202x166 size of your layout
        super(menu, inv, title, GUI_TEXTURE, 202, 166);
    }

    @Override
    protected void init() {
        super.init(); // Let Minecraft calculate the center automatically!
        int leftSideX = this.leftPos - 34; // Side buttons on the left panel

        MultifunctionalCompressorBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile == null) return;

        // 1. Mode Button (Change "+ 41" for X, and "+ 20" for Y relative to the GUI)
        this.modeBtn = this.addRenderableWidget(new ModeToggleButton(
                this.leftPos + 41, this.topPos + 20, // <-- CHANGE THE NUMBERS HERE!
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                }
        ));

        // 2. Lock Button (Change "+ 41" for X, and "+ 55" for Y relative to the GUI)
        this.lockBtn = this.addRenderableWidget(new LockButton(
                this.leftPos + 41, this.topPos + 55, // <-- CHANGE THE NUMBERS HERE!
                b -> {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
                }
        ));

        // 3. Left Panel Buttons
        for (int i = 0; i < 6; i++) {
            Direction dir = Direction.values()[i];
            int finalI = i;
            this.sideBtns[i] = this.addRenderableWidget(new MultifunctionalCompressorButton(
                    leftSideX, this.topPos + 10 + (i * 18), 30, 14,
                    Component.literal(dir.name().charAt(0) + ":" + getSideConfigName(this.getMenu().getSideConfig(dir))),
                    GUI_TEXTURE, 217, 69,
                    b -> {
                        assert this.minecraft.gameMode != null;
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2 + finalI);
                    }
            ));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateUI();
    }

    private void updateUI() {
        if (this.modeBtn != null) {
            this.modeBtn.setMessage(Component.literal(getModeName(this.getMenu().getMode())));
        }

        if (this.lockBtn != null) {
            this.lockBtn.setMessage(Component.literal(this.getMenu().isLocked() ? "Lock" : "Free"));
        }

        for (int i = 0; i < 6; i++) {
            Direction dir = Direction.values()[i];
            if (this.sideBtns[i] != null) {
                this.sideBtns[i].setMessage(Component.literal(dir.name().charAt(0) + ":" + getSideConfigName(this.getMenu().getSideConfig(dir))));
            }
        }
    }

    private String getModeName(int mode) {
        return switch (mode) {
            case 0 -> "Comp";
            case 1 -> "Sing";
            case 2 -> "Dust";
            default -> "Err";
        };
    }

    private String getSideConfigName(int config) {
        return switch (config) {
            case 0 -> "Off";
            case 1 -> "In";
            case 2 -> "Out";
            default -> "?";
        };
    }

    // Returns the name of the Block/Item produced as the final result for each recipe
    private String getRecipeOutputName(ItemStack input, int mode) {
        if (input.isEmpty()) return "No Recipe";
        if (mode == 1) return "Singularity"; // Singularity Mode

        // Dynamic mapping based in 1.21.1 recipe table
        if (input.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_INGOT.get())) {
            return mode == 0 ? "Synthorium Block" : "Synthorium Dust";
        }
        if (input.is(dev.davidklgames.puremashtweaks.registry.ModItems.MOLDELONIAN_INGOT.get())) {
            return "Moldelonian Block";
        }
        if (input.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_SCRAP.get())) {
            return "Synthorium Ingot";
        }
        return input.getHoverName().getString();
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // 1. BaseContainerCompressionScreen draws the texture in 256x256 and the dark tint properly!
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        MultifunctionalCompressorBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile != null) {
            int mode = this.getMenu().getMode();

            // 2. SINGULARITY SILHOUETTE (Middle slot - X=63, Y=35 | Coordinates: 226, 52 to 241, 67)
            if (mode == 1) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 63, y + 35, 226.0F, 52.0F, 16, 16, 256, 256);
            }
            // 3. DUST MODE SILHOUETTE (Middle slot - X=63, Y=35 | Coordinates: 217, 35 to 232, 50)
            else if (mode == 2) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 63, y + 35, 217.0F, 35.0F, 16, 16, 256, 256);
            }

            // 4. RESERVOIR FILL BAR (X=63, Y=35)
            int scaleWidth = 0;
            if (mode == 0) { // Compression Mode
                // Using getSlot(0) directly and securely in the Menu
                ItemStack inputStack = this.getMenu().getSlot(0).getItem();
                if (!inputStack.isEmpty()) {
                    // Ensures that if there are items, it draws at least 1 pixel for visual feedback
                    scaleWidth = Math.max(1, (int) (Math.min(inputStack.getCount(), 9) * 16.0f / 9.0f));
                }
            } else if (mode == 1) { // Singularity Mode
                net.minecraft.world.item.Item singItem = this.getMenu().getSingularityItem();
                if (singItem != net.minecraft.world.item.Items.AIR && this.minecraft.level != null) {
                    var recipe = dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.minecraft.level, new ItemStack(singItem), mode);
                    if (recipe != null) {
                        int currentCount = this.getMenu().getSingularityCount();
                        if (currentCount > 0) {
                            // Ensures at least 1 pixel of filling if there are accumulated items
                            scaleWidth = Math.max(1, (int) ((float) currentCount * 16.0f / recipe.cost()));
                        }
                    }
                }
            } else if (mode == 2) { // Dust Mode
                // Using getSlot(0) directly
                ItemStack inputStack = this.getMenu().getSlot(0).getItem();
                if (!inputStack.isEmpty()) {
                    scaleWidth = 16;
                }
            }

            // strictly clamps the filling between 0 and 16 pixels
            scaleWidth = Math.clamp(scaleWidth, 0, 16);

            if (scaleWidth > 0) {
                int uSrc;
                int vSrc;
                if (mode == 1) {
                    uSrc = 217; // Singularity Filling (217, 18)
                    vSrc = 18;
                } else if (mode == 0) {
                    uSrc = 234; // Compression Mode Filling (234, 18)
                    vSrc = 18;
                } else { // Dust Mode (2)
                    uSrc = 234; // Dust Mode Filling (234, 35)
                    vSrc = 35;
                }
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 63, y + 35, (float) uSrc, (float) vSrc, scaleWidth, 16, 256, 256);
            }

            // 5. CRAFTING PROGRESS ARROW ADJUSTED WITH COORDINATES FROM 1.21.1 (X=89, Y=35 | Coordinates: 222, 0 to 243, 15)
            if (this.getMenu().getProgress() > 0 && this.getMenu().getMaxProgress() > 0) {
                int i2 = (int) (((float) this.getMenu().getProgress() / this.getMenu().getMaxProgress()) * 22);
                if (i2 > 0) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 89, y + 35, 222.0F, 0.0F, i2 + 1, 16, 256, 256);
                }
            }
        }
    }

    // --- MODERN 26.1.2 LOGIC TO DETECT THE MOUSE OVER THE RESERVOIR AND DISPLAY THE TOOLTIP ---
    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        // Absolute position of the reservoir on the screen (X=63, Y=35, size 16x16)
        int rx = this.leftPos + 63;
        int ry = this.topPos + 35;

        // If the mouse is hovering over the reservoir
        if (mouseX >= rx && mouseX <= rx + 16 && mouseY >= ry && mouseY <= ry + 16) {
            java.util.List<Component> tooltip = new java.util.ArrayList<>();
            ItemStack inputStack = this.getMenu().slots.getFirst().getItem(); // Menu Input Slot
            int mode = this.getMenu().getMode();

            // Queries the active recipe on the client to get dynamic costs and results
            dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.CustomRecipeData recipe = null;

            if (mode == 1) {
                net.minecraft.world.item.Item singItem = this.getMenu().getSingularityItem();
                if (singItem != net.minecraft.world.item.Items.AIR && this.minecraft.level != null) {
                    recipe = dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.minecraft.level, new ItemStack(singItem), mode);
                }
            } else if (!inputStack.isEmpty() && this.minecraft.level != null) {
                recipe = dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper.getRecipe(this.minecraft.level, inputStack, mode);
            }

            // If the reservoir is completely empty
            if (inputStack.isEmpty() && (mode != 1 || this.getMenu().getSingularityCount() == 0)) {
                tooltip.add(Component.translatable("tooltip.puremash.multifunctional_compressor.empty").withStyle(net.minecraft.ChatFormatting.WHITE));
            }
            // If the reservoir has an item inserted that does NOT have a valid recipe for the current mode
            else if (!inputStack.isEmpty() && recipe == null) {
                String key = (mode == 2) ? "tooltip.puremashtweaks.multifunctional_compressor.not_suitable_crushing" : "tooltip.puremashtweaks.multifunctional_compressor.not_suitable_compression";
                tooltip.add(Component.translatable(key, inputStack.getHoverName()).withStyle(net.minecraft.ChatFormatting.RED));
            }
            // If there is an item and a valid recipe!
            else {
                // If the Detailed Tooltip config is enabled, gets the actual name of the result from the JSON
                String recipeName;
                if (recipe != null) {
                    recipeName = recipe.result().getHoverName().getString();
                } else {
                    recipeName = getRecipeOutputName(inputStack, mode);
                }

                // Loads the configured cost in the JSON or falls back to the mod's defaults
                int totalQty = recipe != null ? recipe.cost() : (mode == 0 ? 9 : (mode == 1 ? 1000 : 1));
                int currentQty;

                if (mode == 1) {
                    currentQty = this.getMenu().getSingularityCount();
                } else {
                    currentQty = inputStack.getCount();
                }

                // Line 1: Real recipe/result name
                tooltip.add(Component.literal(recipeName).withStyle(net.minecraft.ChatFormatting.WHITE));
                // Line 2: Quantity / Total (e.g., "450 / 1000")
                tooltip.add(Component.literal(currentQty + " / " + totalQty).withStyle(net.minecraft.ChatFormatting.WHITE));
            }

            // Displays the floating tooltip exactly where the mouse is positioned
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // NEW BUTTON CLASSES EXCLUSIVE TO THE COMPRESSOR (256x256 SCALE WITH TRANSLATABLE TOOLTIPS)
    // ----------------------------------------------------------------------------------------------------

    private static class ModeToggleButton extends net.minecraft.client.gui.components.Button {
        private final java.util.List<Component> tips = new java.util.ArrayList<>();

        public ModeToggleButton(int x, int y, OnPress onPress) {
            super(x, y, 12, 11, Component.empty(), onPress, DEFAULT_NARRATION);
            // OFFICIAL TRANSLATION KEYS FOR THE MODE
            this.tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.mode.title").withStyle(net.minecraft.ChatFormatting.AQUA));
            this.tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.mode.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float alpha) {
            // --------------------------------------------------------------------
            // COORDINATE CONFIGURATION AREA FOR THE MODE BUTTON
            // --------------------------------------------------------------------
            int baseU = 220;       // <-- X Coordinate (U) of the button in the normal image
            int baseV = 102;        // <-- Y Coordinate (V) of the button in the normal image
            int hoverUOffset = 13; // <-- How many pixels to offset to the right on HOVER
            // --------------------------------------------------------------------

            int u = baseU;

            if (this.isHovered()) {
                u += hoverUOffset; // Offsets horizontally to show the illuminated hover
                // Renders the dynamic tooltip
                graphics.setTooltipForNextFrame(net.minecraft.client.Minecraft.getInstance().font, this.tips, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), (float) u, (float) baseV, this.width, this.height, 256, 256);
        }
    }

    private class LockButton extends net.minecraft.client.gui.components.Button {
        public LockButton(int x, int y, OnPress onPress) {
            super(x, y, 12, 12, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float alpha) {
            MultifunctionalCompressorBlockEntity tile = MultifunctionalCompressorScreen.this.getMenu().getBlockEntity();
            if (tile == null) return;

            // --------------------------------------------------------------------
            // COORDINATE CONFIGURATION AREA FOR THE LOCK BUTTON
            // --------------------------------------------------------------------
            int baseU = 220;       // <-- Normal X Coordinate (U)
            int vUnlocked = 114;    // <-- Y Coordinate (V) when UNLOCKED (Lock open)
            int vLocked = 128;      // <-- Y Coordinate (V) when LOCKED (Lock closed)
            int hoverUOffset = 13; // <-- How many pixels to offset to the right on HOVER
            // --------------------------------------------------------------------

            // Dynamically detects if the recipe is locked or free
            boolean locked = MultifunctionalCompressorScreen.this.getMenu().isLocked();
            int u = baseU;
            int v = locked ? vLocked : vUnlocked;

            if (this.isHovered()) {
                u += hoverUOffset; // Offsets horizontally to show the open or closed illuminated hover

                // Builds the translation tooltips dynamically
                java.util.List<Component> tips = new java.util.ArrayList<>();
                if (locked) {
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.locked").withStyle(net.minecraft.ChatFormatting.GREEN));
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.locked.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
                } else {
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.free").withStyle(net.minecraft.ChatFormatting.YELLOW));
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.free.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
                }
                graphics.setTooltipForNextFrame(net.minecraft.client.Minecraft.getInstance().font, tips, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), (float) u, (float) v, this.width, this.height, 256, 256);
        }
    }

    // --- CENTRALIZED DYNAMIC TITLE LOGIC WITH TRANSLATION KEYS ---
    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        int mode = this.getMenu().getMode();

        // Defines the translatable text component based on the active operation mode
        Component titleComponent = switch (mode) {
            case 0 -> Component.translatable("container.puremashtweaks.multifunctional_compressor");
            case 1 -> Component.translatable("container.puremashtweaks.multifunctional_compressor.singularity");
            case 2 -> Component.translatable("container.puremashtweaks.multifunctional_compressor.dust");
            default -> this.title;
        };

        String titleStr = titleComponent.getString();

        // Render dynamic centered block title in light gray color
        graphics.text(this.font, titleComponent, (this.imageWidth / 2 - this.font.width(titleStr) / 2) - 10, 6, 0xFFE0E0E0, false);

        // Render the player inventory title in light gray color
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFE0E0E0, false);
    }
}