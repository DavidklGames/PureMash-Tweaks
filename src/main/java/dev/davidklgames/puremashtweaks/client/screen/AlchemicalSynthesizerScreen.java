package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.menu.AlchemicalSynthesizerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.RenderPipelines;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AlchemicalSynthesizerScreen extends BaseContainerCompressionScreen<AlchemicalSynthesizerMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/alchemical_synthesizer/alchemical_synthesizer_gui.png");

    public AlchemicalSynthesizerScreen(AlchemicalSynthesizerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, GUI_TEXTURE, 202, 194);
        this.inventoryLabelY = 104;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int progress = this.getMenu().getProgress();
        int maxProgress = this.getMenu().getMaxProgress();

        boolean arrowTop = this.getMenu().isArrowTopActive();
        boolean arrowMiddle = this.getMenu().isArrowMiddleActive();
        boolean arrowBottom = this.getMenu().isArrowBottomActive();

        if (progress > 0 && maxProgress > 0) {
            if (arrowMiddle) {
                int straightW = (progress * 24) / maxProgress;
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 51, y + 56, 232.0F, 79.0F, straightW, 17, 256, 256);
            }

            if (arrowTop) {
                int curveDownH = (progress * 16) / maxProgress;
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 55, y + 35, 236.0F, 97.0F, 18, arrowHeightClamped(arrowWToPercent(progress, maxProgress)), 256, 256);
            }

            if (arrowBottom) {
                int curveUpH = (progress * 16) / maxProgress;
                // Changed from y + 77 to y + 78
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 54, y + 78 + 16 - curveUpH, 236.0F, 114.0F + 16 - curveUpH, 18, curveUpH, 256, 256);
            }
        }

        // FE Lateral Energy Bar Rendering (H=78, W=14)
        int energyAmount = this.getMenu().getEnergyAmount();
        int energyCapacity = this.getMenu().getEnergyCapacity();

        if (energyAmount > 0 && energyCapacity > 0) {
            int energyH = (energyAmount * 78) / energyCapacity;
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 7, y + 18 + 78 - energyH, 242.0F, 78.0F - energyH, 14, energyH, 256, 256);
        }

        // Circulating active fluid rendering (overlay over the fluid slot)
        int fluidAmount = this.getMenu().getFluidAmount();
        int fluidType = this.getMenu().getFluidType();

        if (fluidAmount > 0) {
            int fillH = (fluidAmount * 16) / 8000;
            fillH = Math.clamp(fillH, 0, 16);

            if (fillH > 0) {
                Identifier stillTexture = (fluidType == 1) ?
                        Identifier.fromNamespaceAndPath("minecraft", "textures/block/lava_still.png") :
                        Identifier.fromNamespaceAndPath("minecraft", "textures/block/water_still.png");

                graphics.blit(RenderPipelines.GUI_TEXTURED, stillTexture, x + 30, y + 35 + 16 - fillH, 0.0F, 16.0F - fillH, 16, fillH, 16, 16);

                if (fluidType != 1) {
                    graphics.fill(x + 30, y + 35 + 16 - fillH, x + 46, y + 35 + 16, 0x553F76E4);
                }
            }
        }
    }

    private int arrowHeightClamped(float percent) {
        return Math.clamp(16, 0, (int) (percent * 16));
    }

    private float arrowWToPercent(int progress, int maxProgress) {
        return maxProgress > 0 ? (float) progress / maxProgress : 0.0f;
    }

    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // FE Lateral Energy Bar Tooltip
        if (mouseX >= x + 7 && mouseX <= x + 21 && mouseY >= y + 18 && mouseY <= y + 96) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Forge Energy").withStyle(net.minecraft.ChatFormatting.RED));
            tooltip.add(Component.literal(this.getMenu().getEnergyAmount() + " / " + this.getMenu().getEnergyCapacity() + " FE").withStyle(net.minecraft.ChatFormatting.GRAY));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // Catalyst Fluid Tooltip
        if (mouseX >= x + 30 && mouseX <= x + 46 && mouseY >= y + 35 && mouseY <= y + 51) {
            List<Component> tooltip = new ArrayList<>();
            int fluidType = this.getMenu().getFluidType();
            if (fluidType == 1) {
                tooltip.add(Component.literal("Lava").withStyle(net.minecraft.ChatFormatting.RED));
            } else if (fluidType == 2) {
                tooltip.add(Component.literal("Water").withStyle(net.minecraft.ChatFormatting.BLUE));
            } else {
                tooltip.add(Component.literal("Empty").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            tooltip.add(Component.literal(this.getMenu().getFluidAmount() + " / " + this.getMenu().getFluidCapacity() + " mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        // Render the machine title in light gray color
        graphics.text(this.font, this.title, 18, 6, 0xFFE0E0E0, false);

        // Render the player inventory title in light gray color
        graphics.text(this.font, this.playerInventoryTitle, 10, 101, 0xFFE0E0E0, false);
    }
}