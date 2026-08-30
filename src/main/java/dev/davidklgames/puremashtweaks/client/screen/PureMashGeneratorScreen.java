package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.menu.PureMashGeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PureMashGeneratorScreen extends BaseContainerCompressionScreen<PureMashGeneratorMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "textures/gui/puremash_generator/puremash_generator_gui.png");

    public PureMashGeneratorScreen(PureMashGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, GUI_TEXTURE, 202, 194);
        this.inventoryLabelY = 101;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int burnTime = this.getMenu().getBurnTime();
        int maxBurnTime = this.getMenu().getMaxBurnTime();

        // Hover Overlay for Fuel Book Button (X=53 to 72, Y=32 to 49)
        if (mouseX >= x + 53 && mouseX <= x + 72 && mouseY >= y + 32 && mouseY <= y + 49) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 53, y + 32, 236.0F, 104.0F, 20, 18, 256, 256);
        }

        // 1. Animated Flame Rendering
        if (this.getMenu().isBurning() && maxBurnTime > 0) {
            int flameH = (int) Math.ceil((double) burnTime * 16.0 / (double) maxBurnTime);
            flameH = Math.clamp(flameH, 0, 16);
            if (flameH > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 55, y + 56 + 16 - flameH, 227.0F, 0.0F + 16.0F - flameH, 16, flameH, 256, 256);
            }
        }

        // 2. FE Energy Bar Rendering (64-bit Long Safe)
        long energyAmount = this.getMenu().getEnergyAmountLong();
        long energyCapacity = this.getMenu().getEnergyCapacityLong();

        if (energyAmount > 0 && energyCapacity > 0) {
            int energyH = (int) ((energyAmount * 76L) / energyCapacity);
            energyH = Math.clamp(energyH, 0, 76);
            if (energyH > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 8, y + 18 + 76 - energyH, 244.0F, 0.0F + 76.0F - energyH, 12, energyH, 256, 256);
            }
        }

        // 3. Time Bar Rendering
        if (this.getMenu().isBurning() && maxBurnTime > 0) {
            int timeH = (int) Math.ceil((double) burnTime * 48.0 / (double) maxBurnTime);
            timeH = Math.clamp(timeH, 0, 48);

            if (timeH > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 153, y + 36 + 48 - timeH, 231.0F, 17.0F + 48.0F - timeH, 12, timeH, 256, 256);
            }
        }

        // 4. Steam Bar Rendering
        int steamAmount = this.getMenu().getSteamAmount();
        if (steamAmount > 0) {
            int steamW = (int) ((steamAmount * 27L) / 100000L);
            steamW = Math.clamp(steamW, 0, 27);
            if (steamW > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 93, y + 29, 229.0F, 91.0F, steamW, 12, 256, 256);
            }
        }

        // 5. Coolant Water Bar Rendering
        int waterAmount = this.getMenu().getWaterAmount();
        if (waterAmount > 0) {
            int waterW = (int) ((waterAmount * 27L) / 20000L);
            waterW = Math.clamp(waterW, 0, 27);
            if (waterW > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 93, y + 80, 229.0F, 77.0F, waterW, 12, 256, 256);
            }
        }

        // 6. Temperature Bar (°C)
        int tempC = this.getMenu().getTemperature();
        if (tempC > 20) {
            int tempH = (int) Math.clamp((double) (tempC - 20) * 18.0 / 1480.0, 1.0, 18.0);
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 141, y + 55 + 18 - tempH, 226.0F, 17.0F + 18.0F - tempH, 4, tempH, 256, 256);
        }
    }

    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Fuel Book Button Tooltip
        if (mouseX >= x + 53 && mouseX <= x + 72 && mouseY >= y + 32 && mouseY <= y + 49) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("View Generator Fuels").withStyle(net.minecraft.ChatFormatting.GOLD));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // FE Energy Bar Tooltip
        if (mouseX >= x + 8 && mouseX <= x + 21 && mouseY >= y + 18 && mouseY <= y + 94) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Forge Energy").withStyle(net.minecraft.ChatFormatting.RED));
            tooltip.add(Component.literal("Stored: " + String.format("%,d", this.getMenu().getEnergyAmountLong()) + " / " + String.format("%,d", this.getMenu().getEnergyCapacityLong()) + " FE").withStyle(net.minecraft.ChatFormatting.GRAY));

            int genRate = this.getMenu().getGenerationRate();
            if (this.getMenu().isBurning() && genRate > 0) {
                tooltip.add(Component.literal("Generation: +" + String.format("%,d", genRate) + " FE/t").withStyle(net.minecraft.ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("Generation: 0 FE/t").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }

            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // Time Bar Tooltip
        if (mouseX >= x + 153 && mouseX <= x + 165 && mouseY >= y + 36 && mouseY <= y + 83) {
            List<Component> tooltip = new ArrayList<>();
            int secondsLeft = (this.getMenu().getBurnTime() + 19) / 20;
            tooltip.add(Component.literal("Remaining Time").withStyle(net.minecraft.ChatFormatting.AQUA));
            tooltip.add(Component.literal(secondsLeft + "s").withStyle(net.minecraft.ChatFormatting.GRAY));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // Steam Bar Tooltip
        if (mouseX >= x + 93 && mouseX <= x + 120 && mouseY >= y + 29 && mouseY <= y + 40) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("fluid.puremashtweaks.steam").withStyle(net.minecraft.ChatFormatting.WHITE));
            tooltip.add(Component.literal(String.format("%,d", this.getMenu().getSteamAmount()) + " / 100,000 mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // Water Tank Tooltip
        if (mouseX >= x + 93 && mouseX <= x + 120 && mouseY >= y + 80 && mouseY <= y + 92) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Coolant (Water)").withStyle(net.minecraft.ChatFormatting.BLUE));
            tooltip.add(Component.literal(String.format("%,d", this.getMenu().getWaterAmount()) + " / 20,000 mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // Temperature Tooltip (°C)
        if (mouseX >= x + 138 && mouseX <= x + 147 && mouseY >= y + 52 && mouseY <= y + 75) {
            List<Component> tooltip = new ArrayList<>();
            int temp = this.getMenu().getTemperature();
            net.minecraft.ChatFormatting tempColor = temp > 500 ? net.minecraft.ChatFormatting.RED : (temp > 100 ? net.minecraft.ChatFormatting.GOLD : net.minecraft.ChatFormatting.BLUE);
            tooltip.add(Component.literal("Temperature").withStyle(net.minecraft.ChatFormatting.GOLD));
            tooltip.add(Component.literal(temp + " °C").withStyle(tempColor));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, 17, 6, 0xFFE0E0E0, false);
        graphics.text(this.font, this.playerInventoryTitle, 12, 101, 0xFFE0E0E0, false);
    }
}