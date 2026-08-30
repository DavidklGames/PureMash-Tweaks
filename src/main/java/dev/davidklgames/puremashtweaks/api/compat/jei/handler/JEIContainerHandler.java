package dev.davidklgames.puremashtweaks.api.compat.jei.handler;

import dev.davidklgames.puremashtweaks.client.screen.BaseContainerScreen;
import dev.davidklgames.puremashtweaks.client.screen.component.SynthesisTableButton;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JEIContainerHandler implements IGuiContainerHandler<BaseContainerScreen<?>> {
    public JEIContainerHandler() {}

    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(BaseContainerScreen<?> containerScreen) {
        List<Rect2i> areas = new ArrayList<>();
        for (Renderable renderable : containerScreen.renderables) {
            if (renderable instanceof SynthesisTableButton) {
                areas.add(new Rect2i(containerScreen.getLeftPos() - 34, containerScreen.getTopPos() + 10, 30, 110));
            }
        }
        return areas;
    }
}