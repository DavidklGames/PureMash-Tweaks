package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.client.renderer.FluidRenderHelper;
import dev.davidklgames.puremashtweaks.menu.AlchemicalSynthesizerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AlchemicalSynthesizerScreen extends BaseContainerCompressionScreen<AlchemicalSynthesizerMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(
            PureMashTweaks.MODID,
            "textures/gui/alchemical_synthesizer/alchemical_synthesizer_gui.png"
    );

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

        // 1. Central Reaction Arrow
        if (progress > 0 && maxProgress > 0) {
            if (arrowMiddle) {
                int straightW = (progress * 24) / maxProgress;
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 51, y + 56, 232.0F, 79.0F, straightW, 17, 256, 256);
            }

            // 2. Fluid Curve Arrow (Top)
            if (arrowTop) {
                int curveDownH = (progress * 16) / maxProgress;
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 55, y + 35, 236.0F, 97.0F, 18, Math.clamp(curveDownH, 0, 16), 256, 256);
            }

            // 3. Tool Curve Arrow (Bottom)
            if (arrowBottom) {
                int curveUpH = (progress * 16) / maxProgress;
                curveUpH = Math.clamp(curveUpH, 0, 16);
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 54, y + 78 + 16 - curveUpH, 236.0F, 114.0F + 16 - curveUpH, 18, curveUpH, 256, 256);
            }
        }

        // 4. FE Lateral Energy Bar (H=78, W=14)
        long energyAmount = this.getMenu().getEnergyAmountLong();
        long energyCapacity = this.getMenu().getEnergyCapacityLong();

        if (energyAmount > 0 && energyCapacity > 0) {
            int energyH = (int) ((energyAmount * 78L) / energyCapacity);
            energyH = Math.clamp(energyH, 0, 78);
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 7, y + 18 + 78 - energyH, 242.0F, 78.0F - energyH, 14, energyH, 256, 256);
        }

        // 5. Universal Dynamic Fluid Volume Overlay (Slot 0, X=30, Y=35, Size 16x16)
        int fluidAmount = this.getMenu().getFluidAmount();
        Fluid fluid = this.getMenu().getFluid();

        if (fluidAmount > 0 && fluid != Fluids.EMPTY) {
            int fillH = (fluidAmount * 16) / 16000;
            fillH = Math.clamp(fillH, 1, 16);

            TextureAtlasSprite sprite = FluidRenderHelper.getFluidTexture(fluid);
            int color = FluidRenderHelper.getFluidColor(fluid, fluidAmount);

            if (sprite != null) {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        sprite,
                        x + 30,
                        y + 35 + 16 - fillH,
                        16,
                        fillH,
                        color
                );
            }
        }

        // 6. Tool Cyclable Silhouettes in Tool Slot (Slot 2, X=30, Y=77)
        ItemStack toolStack = this.getMenu().getSlot(2).getItem();
        if (toolStack.isEmpty()) {
            // Ciclo de 4 ferramentas (P -> S -> A -> PX) a cada 1 segundo (1000ms)
            int cycleIndex = (int) ((System.currentTimeMillis() / 1000L) % 4);
            float u = 224.0F;
            float v = cycleIndex * 16.0F;

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 30, y + 77, u, v, 16, 16, 256, 256);
        }
    }

    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Energy Bar Tooltip
        if (mouseX >= x + 7 && mouseX <= x + 21 && mouseY >= y + 18 && mouseY <= y + 96) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Forge Energy").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Stored: " + String.format("%,d", this.getMenu().getEnergyAmountLong()) + " / " +
                    String.format("%,d", this.getMenu().getEnergyCapacityLong()) + " FE").withStyle(ChatFormatting.GRAY));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // Universal Fluid Tank Tooltip
        if (mouseX >= x + 30 && mouseX <= x + 46 && mouseY >= y + 35 && mouseY <= y + 51) {
            List<Component> tooltip = new ArrayList<>();
            Fluid fluid = this.getMenu().getFluid();
            int amount = this.getMenu().getFluidAmount();

            if (fluid != Fluids.EMPTY && amount > 0) {
                tooltip.add(fluid.getFluidType().getDescription().copy().withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.literal(String.format("%,d", amount) + " / " +
                        String.format("%,d", this.getMenu().getFluidCapacity()) + " mB").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("tooltip.puremashtweaks.fluid_tank.none").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("0 / 16,000 mB").withStyle(ChatFormatting.GRAY));
            }

            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, 18, 6, 0xFFE0E0E0, false);
        graphics.text(this.font, this.playerInventoryTitle, 10, 101, 0xFFE0E0E0, false);
    }
}