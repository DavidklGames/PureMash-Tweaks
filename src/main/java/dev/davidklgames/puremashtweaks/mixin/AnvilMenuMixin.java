package dev.davidklgames.puremashtweaks.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @ModifyConstant(
            method = {"createResult", "createResultInternal"},
            constant = @Constant(intValue = 40),
            require = 0
    )
    public int puremash$removeLevelCap(int old) {
        return Integer.MAX_VALUE;
    }
}