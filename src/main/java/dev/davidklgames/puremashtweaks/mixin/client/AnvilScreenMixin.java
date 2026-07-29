package dev.davidklgames.puremashtweaks.mixin.client;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

    @ModifyConstant(
            method = "extractLabels(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            constant = @Constant(intValue = 40),
            require = 0
    )
    public int puremash$removeLevelCapUI(int old) {
        return Integer.MAX_VALUE;
    }
}