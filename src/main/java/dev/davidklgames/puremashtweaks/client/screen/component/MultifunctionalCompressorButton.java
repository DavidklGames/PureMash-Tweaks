package dev.davidklgames.puremashtweaks.client.screen.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;

public class MultifunctionalCompressorButton extends Button {
    private final Identifier texture;
    private final int texU, texV;

    public MultifunctionalCompressorButton(int x, int y, int width, int height, Component message, Identifier texture, int u, int v, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.texU = u;
        this.texV = v;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Hover logic with offset for V: v = texV + height + 3 in hover state (69 + 14 + 3 = 86)
        int v = this.isHovered() ? this.texV + this.height + 3 : this.texV;

        // Draws the texture using the exact 256x256 scale of the Compressor!
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), (float)this.texU, (float)v, this.width, this.height, 256, 256);

        // Centered text color with 100% Alpha opacity
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