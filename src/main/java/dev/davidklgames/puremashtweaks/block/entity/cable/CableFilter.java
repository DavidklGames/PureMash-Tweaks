package dev.davidklgames.puremashtweaks.block.entity.cable;

import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

/**
 * Extended Cable Filter entry structure for PureMash Universal Cables (Minecraft 26.1.2).
 */
public class CableFilter {

    private final UUID id;
    private String tagString;
    private ItemStack filterStack;
    private CompoundTag metadata;
    private int nbtMode; // 0 = Ignore, 1 = Exact, 2 = Fuzzy
    private CompoundTag destinationTag;
    private boolean invert; // true = Blacklist, false = Whitelist

    private int priority;
    private int stockLimit;
    private String modNamespace;
    private String targetSlots;
    private int minDurabilityPercent;
    private String customDurabilityString; // Supports "<2500", "<= 2499", "> 1000", "= 2500", "2499"

    public enum Operator {
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        EQUAL("=");

        private final String symbol;
        Operator(String symbol) { this.symbol = symbol; }
        public String getSymbol() { return this.symbol; }
    }

    private ItemStack destinationTool;

    public ItemStack getDestinationTool() {
        return this.destinationTool != null ? this.destinationTool.copy() : ItemStack.EMPTY;
    }

    public void setDestinationTool(ItemStack destinationTool) {
        this.destinationTool = destinationTool != null ? destinationTool.copy() : ItemStack.EMPTY;
    }

    public record DurabilityCondition(Operator operator, int value) {
        public boolean test(int remaining) {
            return switch (operator) {
                case LESS_THAN -> remaining < value;
                case LESS_THAN_OR_EQUAL -> remaining <= value;
                case GREATER_THAN -> remaining > value;
                case GREATER_THAN_OR_EQUAL -> remaining >= value;
                case EQUAL -> remaining == value;
            };
        }

        public String formatDisplay() {
            return operator.getSymbol() + " " + value;
        }
    }

    public CableFilter() {
        this.id = UUID.randomUUID();
        this.tagString = "";
        this.filterStack = ItemStack.EMPTY;
        this.metadata = null;
        this.nbtMode = 0;
        this.destinationTag = null;
        this.invert = false;

        this.priority = 0;
        this.stockLimit = 0;
        this.modNamespace = "";
        this.targetSlots = "";
        this.minDurabilityPercent = 0;
        this.customDurabilityString = "";
        this.destinationTool = ItemStack.EMPTY;
    }

    public CableFilter(UUID id, String tagString, ItemStack filterStack, CompoundTag metadata, int nbtMode, CompoundTag destinationTag, boolean invert, int priority, int stockLimit, String modNamespace, String targetSlots, int minDurabilityPercent, String customDurabilityString) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tagString = tagString != null ? tagString : "";
        this.filterStack = filterStack != null ? filterStack.copy() : ItemStack.EMPTY;
        this.metadata = metadata;
        this.nbtMode = nbtMode;
        this.destinationTag = destinationTag;
        this.invert = invert;

        this.priority = priority;
        this.stockLimit = stockLimit;
        this.modNamespace = modNamespace != null ? modNamespace : "";
        this.targetSlots = targetSlots != null ? targetSlots : "";
        this.minDurabilityPercent = minDurabilityPercent;
        this.customDurabilityString = customDurabilityString != null ? customDurabilityString : "";
        this.destinationTool = ItemStack.EMPTY;
    }

    public UUID getId() {
        return this.id;
    }

    public String getTagString() {
        return this.tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString != null ? tagString.trim() : "";
    }

    public ItemStack getFilterStack() {
        return this.filterStack.copy();
    }

    public void setFilterStack(ItemStack stack) {
        this.filterStack = stack != null ? stack.copy() : ItemStack.EMPTY;
    }

    public static ItemStack getModIcon(String modNamespace) {
        if (modNamespace == null || modNamespace.isEmpty()) return ItemStack.EMPTY;
        String clean = modNamespace.startsWith("@") ? modNamespace.substring(1) : modNamespace;

        if (clean.equalsIgnoreCase("minecraft") || clean.equalsIgnoreCase("c")) {
            return new ItemStack(Items.GRASS_BLOCK);
        }

        for (Map.Entry<ResourceKey<CreativeModeTab>, CreativeModeTab> entry : BuiltInRegistries.CREATIVE_MODE_TAB.entrySet()) {
            ResourceKey<CreativeModeTab> key = entry.getKey();
            if (key != null && key.identifier().getNamespace().equalsIgnoreCase(clean)) {
                ItemStack icon = entry.getValue().getIconItem();
                if (!icon.isEmpty()) {
                    return icon;
                }
            }
        }

        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceKey<Item> key = entry.getKey();
            if (key != null && key.identifier().getNamespace().equalsIgnoreCase(clean)) {
                return new ItemStack(entry.getValue());
            }
        }
        return ItemStack.EMPTY;
    }

    public static String getModDisplayName(String modNamespace) {
        if (modNamespace == null || modNamespace.isEmpty()) return "";
        String clean = modNamespace.startsWith("@") ? modNamespace.substring(1) : modNamespace;
        if (clean.equalsIgnoreCase("minecraft")) {
            return "Minecraft";
        }
        return net.neoforged.fml.ModList.get().getModContainerById(clean)
                .map(c -> c.getModInfo().getDisplayName())
                .orElse(clean);
    }

    public static Fluid resolveFluid(String input) {
        if (input == null || input.trim().isEmpty()) return Fluids.EMPTY;
        String trimmed = input.trim();

        if (trimmed.startsWith("#") || trimmed.startsWith("@")) return Fluids.EMPTY;

        Identifier parsed = Identifier.tryParse(trimmed);
        if (parsed != null && BuiltInRegistries.FLUID.containsKey(parsed)) {
            return BuiltInRegistries.FLUID.get(parsed).map(Holder::value).orElse(Fluids.EMPTY);
        }

        if (!trimmed.contains(":")) {
            Identifier mcId = Identifier.fromNamespaceAndPath("minecraft", trimmed);
            if (BuiltInRegistries.FLUID.containsKey(mcId)) {
                return BuiltInRegistries.FLUID.get(mcId).map(Holder::value).orElse(Fluids.EMPTY);
            }

            Identifier pmtId = Identifier.fromNamespaceAndPath("puremashtweaks", trimmed);
            if (BuiltInRegistries.FLUID.containsKey(pmtId)) {
                return BuiltInRegistries.FLUID.get(pmtId).map(Holder::value).orElse(Fluids.EMPTY);
            }

            for (Fluid fluid : BuiltInRegistries.FLUID) {
                Identifier key = BuiltInRegistries.FLUID.getKey(fluid);
                if (key.getPath().equalsIgnoreCase(trimmed)) {
                    return fluid;
                }
            }
        }
        return Fluids.EMPTY;
    }

    public static Item resolveItem(String input) {
        if (input == null || input.trim().isEmpty()) return Items.AIR;
        String trimmed = input.trim();

        if (trimmed.startsWith("#") || trimmed.startsWith("@")) return Items.AIR;

        Identifier parsed = Identifier.tryParse(trimmed);
        if (parsed != null && BuiltInRegistries.ITEM.containsKey(parsed)) {
            return BuiltInRegistries.ITEM.get(parsed).map(Holder::value).orElse(Items.AIR);
        }

        if (!trimmed.contains(":")) {
            Identifier mcId = Identifier.fromNamespaceAndPath("minecraft", trimmed);
            if (BuiltInRegistries.ITEM.containsKey(mcId)) {
                return BuiltInRegistries.ITEM.get(mcId).map(Holder::value).orElse(Items.AIR);
            }

            Identifier pmtId = Identifier.fromNamespaceAndPath("puremashtweaks", trimmed);
            if (BuiltInRegistries.ITEM.containsKey(pmtId)) {
                return BuiltInRegistries.ITEM.get(pmtId).map(Holder::value).orElse(Items.AIR);
            }

            for (Item item : BuiltInRegistries.ITEM) {
                Identifier key = BuiltInRegistries.ITEM.getKey(item);
                if (key.getPath().equalsIgnoreCase(trimmed)) {
                    return item;
                }
            }
        }
        return Items.AIR;
    }

    public ItemStack getDisplayStack(Level level) {
        if (this.tagString != null && this.tagString.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(this.tagString.substring(1));
            if (tagId != null) {
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                var tagHolderSet = BuiltInRegistries.ITEM.get(tagKey);
                if (tagHolderSet.isPresent()) {
                    List<Holder<Item>> items = tagHolderSet.get().stream().toList();
                    if (!items.isEmpty()) {
                        long time = level != null ? level.getGameTime() : 0;
                        int index = (int) ((time / 20L) % items.size());
                        return new ItemStack(items.get(index).value());
                    }
                }
            }
        } else if (this.modNamespace != null && !this.modNamespace.isEmpty()) {
            ItemStack icon = getModIcon(this.modNamespace);
            if (!icon.isEmpty()) return icon;
        } else if (this.tagString != null && this.tagString.startsWith("@")) {
            ItemStack icon = getModIcon(this.tagString.substring(1));
            if (!icon.isEmpty()) return icon;
        }
        return this.filterStack.isEmpty() ? ItemStack.EMPTY : this.filterStack;
    }

    public Fluid getDisplayFluid(Level level) {
        if (this.tagString != null && this.tagString.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(this.tagString.substring(1));
            if (tagId != null) {
                TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagId);
                var tagHolderSet = BuiltInRegistries.FLUID.get(tagKey);
                if (tagHolderSet.isPresent()) {
                    List<Holder<Fluid>> fluids = tagHolderSet.get().stream().toList();
                    if (!fluids.isEmpty()) {
                        long time = level != null ? level.getGameTime() : 0;
                        int index = (int) ((time / 20L) % fluids.size());
                        return fluids.get(index).value();
                    }
                }
            }
        }

        Fluid resolved = resolveFluid(this.tagString);
        if (resolved != Fluids.EMPTY) {
            return resolved;
        }

        if (!this.filterStack.isEmpty() && this.filterStack.getItem() instanceof net.minecraft.world.item.BucketItem bucket) {
            return bucket.content;
        }

        return Fluids.EMPTY;
    }

    public CompoundTag getMetadata() {
        return this.metadata;
    }

    public void setMetadata(CompoundTag metadata) {
        this.metadata = metadata;
    }

    public int getNbtMode() {
        return this.nbtMode;
    }

    public void setNbtMode(int nbtMode) {
        this.nbtMode = nbtMode;
    }

    public CompoundTag getDestinationTag() {
        return this.destinationTag;
    }

    public void setDestinationTag(CompoundTag destinationTag) {
        this.destinationTag = destinationTag;
    }

    public boolean isInvert() {
        return this.invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStockLimit() {
        return this.stockLimit;
    }

    public void setStockLimit(int stockLimit) {
        this.stockLimit = Math.max(0, stockLimit);
    }

    public String getModNamespace() {
        return this.modNamespace;
    }

    public void setModNamespace(String modNamespace) {
        this.modNamespace = modNamespace != null ? modNamespace.trim().toLowerCase() : "";
    }

    public String getTargetSlots() {
        return this.targetSlots;
    }

    public void setTargetSlots(String targetSlots) {
        this.targetSlots = targetSlots != null ? targetSlots.trim() : "";
    }

    public List<Integer> getTargetSlotList() {
        List<Integer> slots = new ArrayList<>();
        if (this.targetSlots == null || this.targetSlots.isEmpty()) return slots;

        String[] parts = this.targetSlots.split("[,;\\s]+");
        for (String part : parts) {
            try {
                if (!part.trim().isEmpty()) {
                    int slot = Integer.parseInt(part.trim());
                    if (slot >= 0) {
                        slots.add(slot);
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
        return slots;
    }

    public int getMinDurabilityPercent() {
        return this.minDurabilityPercent;
    }

    public void setMinDurabilityPercent(int minDurabilityPercent) {
        this.minDurabilityPercent = Math.clamp(minDurabilityPercent, 0, 100);
    }

    public String getCustomDurabilityString() {
        return this.customDurabilityString;
    }

    public void setCustomDurabilityString(String customDurabilityString) {
        this.customDurabilityString = customDurabilityString != null ? customDurabilityString.trim() : "";
    }

    public DurabilityCondition parseCustomDurability() {
        if (this.customDurabilityString == null || this.customDurabilityString.isEmpty()) return null;

        String trimmed = this.customDurabilityString.replaceAll("\\s+", "");
        Operator op = Operator.EQUAL;
        String numberPart = trimmed;

        if (trimmed.startsWith("<=")) {
            op = Operator.LESS_THAN_OR_EQUAL;
            numberPart = trimmed.substring(2);
        } else if (trimmed.startsWith(">=")) {
            op = Operator.GREATER_THAN_OR_EQUAL;
            numberPart = trimmed.substring(2);
        } else if (trimmed.startsWith("<")) {
            op = Operator.LESS_THAN;
            numberPart = trimmed.substring(1);
        } else if (trimmed.startsWith(">")) {
            op = Operator.GREATER_THAN;
            numberPart = trimmed.substring(1);
        } else if (trimmed.startsWith("==")) {
            numberPart = trimmed.substring(2);
        } else if (trimmed.startsWith("=")) {
            numberPart = trimmed.substring(1);
        }

        try {
            int val = Integer.parseInt(numberPart);
            return new DurabilityCondition(op, val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getEffectiveDurabilityPercent() {
        DurabilityCondition cond = parseCustomDurability();
        if (cond != null && !this.filterStack.isEmpty() && this.filterStack.isDamageableItem()) {
            return Math.clamp(Math.round(((float) cond.value() / (float) this.filterStack.getMaxDamage()) * 100.0F), 1, 100);
        }
        return this.minDurabilityPercent;
    }

    public boolean matchesItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 1. Mod ID Namespace Check
        if (!this.modNamespace.isEmpty()) {
            String clean = this.modNamespace.startsWith("@") ? this.modNamespace.substring(1) : this.modNamespace;
            String stackModId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
            boolean modMatches = stackModId.equalsIgnoreCase(clean);
            if (!modMatches) return this.invert;
        }

        // 2. Durability Condition Check
        if (stack.isDamageableItem()) {
            int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
            DurabilityCondition cond = parseCustomDurability();

            if (cond != null) {
                if (!cond.test(remainingDurability)) {
                    return this.invert;
                }
            } else if (this.minDurabilityPercent > 0) {
                int currentPercent = (int) (((float) remainingDurability / (float) stack.getMaxDamage()) * 100.0F);
                if (currentPercent > this.minDurabilityPercent) {
                    return this.invert;
                }
            }
        }

        // 3. Tag or Item Stack Matching
        if (this.tagString != null && !this.tagString.isEmpty()) {
            if (this.tagString.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(this.tagString.substring(1));
                if (tagId != null) {
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                    if (!stack.is(tagKey)) return this.invert;
                }
            } else if (!this.tagString.startsWith("@")) {
                Item expectedItem = resolveItem(this.tagString);
                if (expectedItem != Items.AIR) {
                    if (stack.getItem() != expectedItem) return this.invert;
                } else if (!this.filterStack.isEmpty()) {
                    if (!ItemStack.isSameItem(this.filterStack, stack)) return this.invert;
                }
            }
        } else if (!this.filterStack.isEmpty()) {
            boolean itemMatch = ItemStack.isSameItem(this.filterStack, stack);
            if (!itemMatch) return this.invert;
        }

        // 4. NBT Matching
        if (this.nbtMode > 0 && this.metadata != null && !this.metadata.isEmpty()) {
            var patch = stack.getComponentsPatch();
            CompoundTag itemTag = patch.isEmpty() ? new CompoundTag() : (CompoundTag) net.minecraft.core.component.DataComponentPatch.CODEC
                    .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, patch).result().orElseGet(CompoundTag::new);

            if (this.nbtMode == 1) {
                if (!deepExactCompare(this.metadata, itemTag)) return this.invert;
            } else if (this.nbtMode == 2) {
                if (!deepFuzzyCompare(this.metadata, itemTag)) return this.invert;
            }
        }

        return !this.invert;
    }

    public boolean matchesFluid(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.isEmpty() || fluidStack.getFluid() == Fluids.EMPTY) return false;

        Fluid actualFluid = fluidStack.getFluid();
        Identifier actualFluidId = BuiltInRegistries.FLUID.getKey(actualFluid);

        // 1. Mod ID Namespace Check
        if (!this.modNamespace.isEmpty()) {
            String clean = this.modNamespace.startsWith("@") ? this.modNamespace.substring(1) : this.modNamespace;
            boolean modMatches = actualFluidId.getNamespace().equalsIgnoreCase(clean);
            if (!modMatches) return this.invert;
        }

        // 2. Tag or Fluid Name Check
        if (this.tagString != null && !this.tagString.isEmpty()) {
            if (this.tagString.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(this.tagString.substring(1));
                if (tagId != null) {
                    TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagId);
                    if (!actualFluid.defaultFluidState().is(tagKey)) return this.invert;
                }
            } else if (!this.tagString.startsWith("@")) {
                Fluid expectedFluid = resolveFluid(this.tagString);
                if (expectedFluid != Fluids.EMPTY) {
                    if (actualFluid != expectedFluid) return this.invert;
                } else {
                    String fluidPath = actualFluidId.getPath();
                    String fullId = actualFluidId.toString();
                    if (!fullId.equalsIgnoreCase(this.tagString) && !fluidPath.equalsIgnoreCase(this.tagString)) {
                        return this.invert;
                    }
                }
            }
        } else if (!this.filterStack.isEmpty()) {
            if (this.filterStack.getItem() instanceof net.minecraft.world.item.BucketItem bucket) {
                if (bucket.content != actualFluid) return this.invert;
            }
        }

        return !this.invert;
    }

    public static boolean deepExactCompare(Tag meta, Tag item) {
        if (meta instanceof CompoundTag cMeta) {
            if (!(item instanceof CompoundTag cItem)) return false;
            Set<String> allKeys = new HashSet<>(cMeta.keySet());
            allKeys.addAll(cItem.keySet());
            for (String key : allKeys) {
                if (!cMeta.contains(key) || !cItem.contains(key)) return false;
                if (!deepExactCompare(cMeta.get(key), cItem.get(key))) return false;
            }
            return true;
        } else if (meta instanceof ListTag lMeta) {
            if (!(item instanceof ListTag lItem)) return false;
            if (lMeta.size() != lItem.size()) return false;
            for (int i = 0; i < lMeta.size(); i++) {
                if (!deepExactCompare(lMeta.get(i), lItem.get(i))) return false;
            }
            return true;
        } else {
            return meta != null && meta.equals(item);
        }
    }

    public static boolean deepFuzzyCompare(Tag meta, Tag item) {
        if (meta instanceof CompoundTag cMeta) {
            if (!(item instanceof CompoundTag cItem)) return false;
            for (String key : cMeta.keySet()) {
                Tag nbtMeta = cMeta.get(key);
                Tag nbtItem = cItem.get(key);
                if (nbtMeta == null || nbtItem == null || nbtItem.getId() != nbtMeta.getId()) return false;
                if (!deepFuzzyCompare(nbtMeta, nbtItem)) return false;
            }
            return true;
        } else if (meta instanceof ListTag lMeta) {
            if (!(item instanceof ListTag lItem)) return false;
            return lMeta.stream().allMatch(inbt -> lItem.stream().anyMatch(inbt1 -> deepFuzzyCompare(inbt, inbt1)));
        } else {
            return meta != null && meta.equals(item);
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.store("Id", UUIDUtil.CODEC, this.id);
        if (this.tagString != null && !this.tagString.isEmpty()) {
            tag.putString("TagString", this.tagString);
        }
        if (!this.filterStack.isEmpty()) {
            tag.putString("Item", BuiltInRegistries.ITEM.getKey(this.filterStack.getItem()).toString());
        }
        if (this.metadata != null) {
            tag.put("Metadata", this.metadata);
        }
        tag.putInt("NbtMode", this.nbtMode);
        if (this.destinationTag != null) {
            tag.put("Destination", this.destinationTag);
        }
        if (this.destinationTool != null && !this.destinationTool.isEmpty()) {
            tag.store("DestinationTool", ItemStack.CODEC, this.destinationTool);
        }
        tag.putBoolean("Invert", this.invert);

        tag.putInt("Priority", this.priority);
        tag.putInt("StockLimit", this.stockLimit);
        tag.putString("ModNamespace", this.modNamespace);
        tag.putString("TargetSlots", this.targetSlots);
        tag.putInt("MinDurabilityPercent", this.minDurabilityPercent);
        tag.putString("CustomDurabilityString", this.customDurabilityString);

        return tag;
    }

    public static CableFilter deserializeNBT(CompoundTag tag) {
        UUID id = tag.read("Id", UUIDUtil.CODEC).orElseGet(UUID::randomUUID);
        String tagStr = tag.getStringOr("TagString", "");
        ItemStack stack = ItemStack.EMPTY;
        if (tag.contains("Item")) {
            Identifier itemLoc = Identifier.tryParse(tag.getStringOr("Item", ""));
            if (itemLoc != null) {
                var itemHolder = BuiltInRegistries.ITEM.get(itemLoc);
                if (itemHolder.isPresent()) {
                    stack = new ItemStack(itemHolder.get().value());
                }
            }
        }
        CompoundTag meta = tag.contains("Metadata") ? tag.getCompoundOrEmpty("Metadata") : null;
        int nbtMode = tag.getIntOr("NbtMode", 0);
        CompoundTag dest = tag.contains("Destination") ? tag.getCompoundOrEmpty("Destination") : null;
        boolean invert = tag.getBooleanOr("Invert", false);

        int priority = tag.getIntOr("Priority", 0);
        int stockLimit = tag.getIntOr("StockLimit", 0);
        String modNamespace = tag.getStringOr("ModNamespace", "");

        String targetSlots = tag.getStringOr("TargetSlots", "");
        if (targetSlots.isEmpty() && tag.contains("TargetSlot")) {
            int oldSlot = tag.getIntOr("TargetSlot", -1);
            if (oldSlot >= 0) targetSlots = String.valueOf(oldSlot);
        }

        int minDurability = tag.getIntOr("MinDurabilityPercent", 0);

        String customDurStr = tag.getStringOr("CustomDurabilityString", "");
        if (customDurStr.isEmpty() && tag.contains("CustomDurabilityValue")) {
            int oldVal = tag.getIntOr("CustomDurabilityValue", 0);
            if (oldVal > 0) customDurStr = String.valueOf(oldVal);
        }

        // Lê o Distribution Filter salvo no NBT
        ItemStack destTool = tag.contains("DestinationTool") ?
                tag.read("DestinationTool", ItemStack.CODEC).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;

        CableFilter filter = new CableFilter(id, tagStr, stack, meta, nbtMode, dest, invert, priority, stockLimit, modNamespace, targetSlots, minDurability, customDurStr);
        filter.setDestinationTool(destTool);
        return filter;
    }
}