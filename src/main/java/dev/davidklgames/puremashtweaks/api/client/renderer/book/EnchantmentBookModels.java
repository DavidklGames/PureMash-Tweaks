package dev.davidklgames.puremashtweaks.api.client.renderer.book;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("deprecation")
public class EnchantmentBookModels implements ItemModel {
    private final ItemModel baseModel;
    private final ItemModel overloadModel;
    private final ItemModel overclockModel;
    private final ItemModel overdriveModel;

    public EnchantmentBookModels(ItemModel baseModel, ItemModel overloadModel, ItemModel overclockModel, ItemModel overdriveModel) {
        this.baseModel = baseModel;
        this.overloadModel = overloadModel;
        this.overclockModel = overclockModel;
        this.overdriveModel = overdriveModel;
    }

    @Override
    public void update(
            @NonNull ItemStackRenderState output,
            @NonNull ItemStack item,
            @NonNull ItemModelResolver resolver,
            @NonNull ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        boolean hasOverload = false;
        boolean hasOverclock = false;
        boolean hasOverdrive = false;

        if (level != null) {
            var reg = level.registryAccess().lookup(Registries.ENCHANTMENT);
            if (reg.isPresent()) {
                var overloadOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERLOAD);
                if (overloadOpt.isPresent()) {
                    var overload = overloadOpt.get();
                    int activeLvl = EnchantmentHelper.getItemEnchantmentLevel(overload, item);
                    int storedLvl = 0;
                    ItemEnchantments stored = item.get(DataComponents.STORED_ENCHANTMENTS);
                    if (stored != null) {
                        storedLvl = stored.getLevel(overload);
                    }
                    if (activeLvl > 0 || storedLvl > 0) {
                        hasOverload = true;
                    }
                }

                var overclockOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERCLOCK);
                if (overclockOpt.isPresent()) {
                    var overclock = overclockOpt.get();
                    int activeLvl = EnchantmentHelper.getItemEnchantmentLevel(overclock, item);
                    int storedLvl = 0;
                    ItemEnchantments stored = item.get(DataComponents.STORED_ENCHANTMENTS);
                    if (stored != null) {
                        storedLvl = stored.getLevel(overclock);
                    }
                    if (activeLvl > 0 || storedLvl > 0) {
                        hasOverclock = true;
                    }
                }

                var overdriveOpt = reg.get().get(dev.davidklgames.puremashtweaks.registry.ModEnchantments.OVERDRIVE);
                if (overdriveOpt.isPresent()) {
                    var overdrive = overdriveOpt.get();
                    int activeLvl = EnchantmentHelper.getItemEnchantmentLevel(overdrive, item);
                    int storedLvl = 0;
                    ItemEnchantments stored = item.get(DataComponents.STORED_ENCHANTMENTS);
                    if (stored != null) {
                        storedLvl = stored.getLevel(overdrive);
                    }
                    if (activeLvl > 0 || storedLvl > 0) {
                        hasOverdrive = true;
                    }
                }
            }
        }

        if (hasOverload) {
            this.overloadModel.update(output, item, resolver, displayContext, level, owner, seed);
        } else if (hasOverclock) {
            this.overclockModel.update(output, item, resolver, displayContext, level, owner, seed);
        } else if (hasOverdrive) {
            this.overdriveModel.update(output, item, resolver, displayContext, level, owner, seed);
        } else {
            this.baseModel.update(output, item, resolver, displayContext, level, owner, seed);
        }
    }
}