package dev.davidklgames.puremashtweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import dev.davidklgames.puremashtweaks.PureMashTweaks;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> OVERLOAD = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overload")
    );

    public static final ResourceKey<Enchantment> OVERCLOCK = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overclock")
    );

    public static final ResourceKey<Enchantment> OVERDRIVE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "overdrive")
    );
}