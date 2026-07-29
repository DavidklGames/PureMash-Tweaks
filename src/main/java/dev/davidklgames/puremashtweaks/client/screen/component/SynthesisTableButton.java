package dev.davidklgames.puremashtweaks.client.screen.component;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;

public class SynthesisTableButton extends Button {
    private final Identifier texture;
    private final int texU, texV;

    public SynthesisTableButton(int x, int y, int width, int height, Component message, Identifier texture, int u, int v, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.texU = u;
        this.texV = v;
    }

    // 1. Mandatory override required by the parent class
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Hover logic (lighting) coming from your original code
        int v = this.isHovered() ? this.texV + this.height + 3 : this.texV;

        // 2. Draws the button using the Extractor's blit
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), (float)this.texU, (float)v, this.width, this.height, 512, 512);

        // 3. Defines the text color inheriting the decimals from 1.21.1
        // and applying the mandatory alpha opacity of 255 (0xFF000000) in the 26.1.2 engine
        int textColor = this.active ? (this.isHovered() ? 16777120 : 14737632) : 10526880;
        textColor |= 0xFF000000; // Sets opacity to 100% (prevents the text from becoming invisible)

        // 4. Draws the text centered on top
        graphics.centeredText(
                net.minecraft.client.Minecraft.getInstance().font,
                this.getMessage(),
                this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2,
                textColor
        );
    }
}