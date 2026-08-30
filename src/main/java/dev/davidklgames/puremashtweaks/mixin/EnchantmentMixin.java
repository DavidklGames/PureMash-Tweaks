package dev.davidklgames.puremashtweaks.mixin;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    // Bright Cyan (#00E5FF) - Signature PureMash / Synthorium Glow
    private static final TextColor OVERCLOCK_CYAN_COLOR = TextColor.fromRgb(0x00E5FF);

    @Inject(
            method = "getFullname",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void puremash$modifyEnchColorAboveMaxLevel(Holder<Enchantment> ench, int level, CallbackInfoReturnable<Component> cir) {
        // If Apothic Enchanting or Zenith is loaded, let it handle its own colors
        if (ModList.get().isLoaded("apothic_enchanting") || ModList.get().isLoaded("zenith")) {
            return;
        }

        if (ench != null && ench.isBound()) {
            int maxLevel = ench.value().getMaxLevel();
            // If the level exceeds vanilla max level (due to Overclock)
            if (level > maxLevel) {
                Component original = cir.getReturnValue();
                // Renders the full name & numeral in glowing bright cyan
                cir.setReturnValue(original.copy().withStyle(Style.EMPTY.withColor(OVERCLOCK_CYAN_COLOR)));
            }
        }
    }
}