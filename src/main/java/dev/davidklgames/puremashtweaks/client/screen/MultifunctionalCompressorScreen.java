package dev.davidklgames.puremashtweaks.client.screen;

import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.api.CompressorRecipeHelper;
import dev.davidklgames.puremashtweaks.block.entity.MultifunctionalCompressorBlockEntity;
import dev.davidklgames.puremashtweaks.menu.MultifunctionalCompressorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MultifunctionalCompressorScreen extends BaseContainerCompressionScreen<MultifunctionalCompressorMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(
            PureMashTweaks.MODID,
            "textures/gui/multifunctional_compressor/multifunctional_compressor_gui.png"
    );

    private ModeToggleButton modeBtn;
    private LockButton lockBtn;

    public MultifunctionalCompressorScreen(MultifunctionalCompressorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, GUI_TEXTURE, 202, 181);
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 87;
    }

    @Override
    protected void init() {
        super.init();

        MultifunctionalCompressorBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile == null) return;

        // 1. Mode Button (Moved down +9px -> X=41, Y=29)
        this.modeBtn = this.addRenderableWidget(new ModeToggleButton(
                this.leftPos + 41, this.topPos + 29,
                b -> {
                    int nextMode = (this.getMenu().getMode() + 1) % 3;
                    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
                            new dev.davidklgames.puremashtweaks.network.ToggleCompressorModePayload(
                                    this.getMenu().getBlockPos(),
                                    nextMode
                            )
                    );
                }
        ));

        // 2. Lock Button (Moved down +9px -> X=41, Y=64)
        this.lockBtn = this.addRenderableWidget(new LockButton(
                this.leftPos + 41, this.topPos + 64,
                b -> {
                    boolean nextLock = !this.getMenu().isLocked();
                    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
                            new dev.davidklgames.puremashtweaks.network.ToggleCompressorLockPayload(
                                    this.getMenu().getBlockPos(),
                                    nextLock
                            )
                    );
                }
        ));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateUI();
    }

    private void updateUI() {
        if (this.modeBtn != null) {
            this.modeBtn.setMessage(Component.literal(getModeName(this.getMenu().getMode())));
        }

        if (this.lockBtn != null) {
            this.lockBtn.setMessage(Component.literal(this.getMenu().isLocked() ? "Lock" : "Free"));
        }
    }

    private String getModeName(int mode) {
        return switch (mode) {
            case 0 -> "Comp";
            case 1 -> "Sing";
            case 2 -> "Dust";
            default -> "Err";
        };
    }

    private String getRecipeOutputName(ItemStack input, int mode) {
        if (input.isEmpty()) return "No Recipe";
        if (mode == 1) return "Singularity";

        if (input.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_INGOT.get())) {
            return mode == 0 ? "Synthorium Block" : "Synthorium Dust";
        }
        if (input.is(dev.davidklgames.puremashtweaks.registry.ModItems.MOLDELONIAN_INGOT.get())) {
            return "Moldelonian Block";
        }
        if (input.is(dev.davidklgames.puremashtweaks.registry.ModItems.SYNTHORIUM_SCRAP.get())) {
            return "Synthorium Ingot";
        }
        return input.getHoverName().getString();
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        MultifunctionalCompressorBlockEntity tile = this.getMenu().getBlockEntity();
        if (tile != null) {
            int mode = this.getMenu().getMode();

            // 1. FE Energy Bar Rendering (X=7, Y=21, 14x60, Color texture at U=242, V=141)
            long energyAmount = this.getMenu().getEnergyAmountLong();
            long energyCapacity = this.getMenu().getEnergyCapacityLong();

            if (energyAmount > 0 && energyCapacity > 0) {
                int energyH = (int) ((energyAmount * 60L) / energyCapacity);
                energyH = Math.clamp(energyH, 0, 60);
                if (energyH > 0) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 7, y + 21 + 60 - energyH, 242.0F, 141.0F + 60.0F - energyH, 14, energyH, 256, 256);
                }
            }

            // 2. Silhouettes in Middle Slot (Moved down +9px -> X=63, Y=44)
            if (mode == 1) { // Singularity Silhouette
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 63, y + 44, 226.0F, 52.0F, 16, 16, 256, 256);
            } else if (mode == 2) { // Dust Silhouette
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 63, y + 44, 217.0F, 35.0F, 16, 16, 256, 256);
            }

            // 3. Reservoir Fill Level (Moved down +9px -> X=63, Y=44)
            int scaleWidth = 0;
            if (mode == 0) { // Compression Mode
                ItemStack inputStack = this.getMenu().getSlot(0).getItem();
                if (!inputStack.isEmpty()) {
                    scaleWidth = Math.max(1, (int) (Math.min(inputStack.getCount(), 9) * 16.0f / 9.0f));
                }
            } else if (mode == 1) { // Singularity Mode
                Item singItem = this.getMenu().getSingularityItem();
                if (singItem != Items.AIR && this.minecraft.level != null) {
                    var recipe = CompressorRecipeHelper.getRecipe(this.minecraft.level, new ItemStack(singItem), mode);
                    if (recipe != null) {
                        int currentCount = this.getMenu().getSingularityCount();
                        if (currentCount > 0) {
                            scaleWidth = Math.max(1, (int) ((float) currentCount * 16.0f / recipe.cost()));
                        }
                    }
                }
            } else if (mode == 2) { // Dust Mode
                ItemStack inputStack = this.getMenu().getSlot(0).getItem();
                if (!inputStack.isEmpty()) {
                    scaleWidth = 16;
                }
            }

            scaleWidth = Math.clamp(scaleWidth, 0, 16);

            if (scaleWidth > 0) {
                int uSrc;
                int vSrc;
                if (mode == 1) {
                    uSrc = 217;
                    vSrc = 18;
                } else if (mode == 0) {
                    uSrc = 234;
                    vSrc = 18;
                } else {
                    uSrc = 234;
                    vSrc = 35;
                }
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 63, y + 44, (float) uSrc, (float) vSrc, scaleWidth, 16, 256, 256);
            }

            // 4. Crafting Progress Arrow (Moved down +9px -> X=89, Y=44)
            if (this.getMenu().getProgress() > 0 && this.getMenu().getMaxProgress() > 0) {
                int i2 = (int) (((float) this.getMenu().getProgress() / this.getMenu().getMaxProgress()) * 22);
                if (i2 > 0) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + 89, y + 44, 222.0F, 0.0F, i2 + 1, 16, 256, 256);
                }
            }
        }
    }

    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Energy Bar Tooltip (X=7 to 20, Y=21 to 80)
        if (mouseX >= x + 7 && mouseX <= x + 20 && mouseY >= y + 21 && mouseY <= y + 80) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Forge Energy").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Stored: " + String.format("%,d", this.getMenu().getEnergyAmountLong()) + " / " + String.format("%,d", this.getMenu().getEnergyCapacityLong()) + " FE").withStyle(ChatFormatting.GRAY));
            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }

        // Reservoir Tooltip (X=63, Y=44)
        int rx = this.leftPos + 63;
        int ry = this.topPos + 44;

        if (mouseX >= rx && mouseX <= rx + 16 && mouseY >= ry && mouseY <= ry + 16) {
            List<Component> tooltip = new ArrayList<>();
            ItemStack inputStack = this.getMenu().slots.getFirst().getItem();
            int mode = this.getMenu().getMode();

            CompressorRecipeHelper.CustomRecipeData recipe = null;

            if (mode == 1) {
                Item singItem = this.getMenu().getSingularityItem();
                if (singItem != Items.AIR && this.minecraft.level != null) {
                    recipe = CompressorRecipeHelper.getRecipe(this.minecraft.level, new ItemStack(singItem), mode);
                }
            } else if (!inputStack.isEmpty() && this.minecraft.level != null) {
                recipe = CompressorRecipeHelper.getRecipe(this.minecraft.level, inputStack, mode);
            }

            if (inputStack.isEmpty() && (mode != 1 || this.getMenu().getSingularityCount() == 0)) {
                tooltip.add(Component.translatable("tooltip.puremash.multifunctional_compressor.empty").withStyle(ChatFormatting.WHITE));
            } else if (!inputStack.isEmpty() && recipe == null) {
                String key = (mode == 2) ? "tooltip.puremashtweaks.multifunctional_compressor.not_suitable_crushing" : "tooltip.puremashtweaks.multifunctional_compressor.not_suitable_compression";
                tooltip.add(Component.translatable(key, inputStack.getHoverName()).withStyle(ChatFormatting.RED));
            } else {
                String recipeName = (recipe != null) ? recipe.result().getHoverName().getString() : getRecipeOutputName(inputStack, mode);
                int totalQty = recipe != null ? recipe.cost() : (mode == 0 ? 9 : (mode == 1 ? 1000 : 1));
                int currentQty = (mode == 1) ? this.getMenu().getSingularityCount() : inputStack.getCount();

                tooltip.add(Component.literal(recipeName).withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal(currentQty + " / " + totalQty).withStyle(ChatFormatting.WHITE));
            }

            graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
        }
    }

    private static class ModeToggleButton extends net.minecraft.client.gui.components.Button {
        private final List<Component> tips = new ArrayList<>();

        public ModeToggleButton(int x, int y, OnPress onPress) {
            super(x, y, 12, 11, Component.empty(), onPress, DEFAULT_NARRATION);
            this.tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.mode.title").withStyle(ChatFormatting.AQUA));
            this.tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.mode.desc").withStyle(ChatFormatting.GRAY));
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float alpha) {
            int baseU = 220;
            int baseV = 102;
            int hoverUOffset = 13;

            int u = baseU;

            if (this.isHovered()) {
                u += hoverUOffset;
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.tips, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), (float) u, (float) baseV, this.width, this.height, 256, 256);
        }
    }

    private class LockButton extends net.minecraft.client.gui.components.Button {
        public LockButton(int x, int y, OnPress onPress) {
            super(x, y, 12, 12, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float alpha) {
            MultifunctionalCompressorBlockEntity tile = MultifunctionalCompressorScreen.this.getMenu().getBlockEntity();
            if (tile == null) return;

            int baseU = 220;
            int vUnlocked = 114;
            int vLocked = 128;
            int hoverUOffset = 13;

            boolean locked = MultifunctionalCompressorScreen.this.getMenu().isLocked();
            int u = baseU;
            int v = locked ? vLocked : vUnlocked;

            if (this.isHovered()) {
                u += hoverUOffset;

                List<Component> tips = new ArrayList<>();
                if (locked) {
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.locked").withStyle(ChatFormatting.GREEN));
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.locked.desc").withStyle(ChatFormatting.GRAY));
                } else {
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.free").withStyle(ChatFormatting.YELLOW));
                    tips.add(Component.translatable("tooltip.puremashtweaks.multifunctional_compressor.lock.free.desc").withStyle(ChatFormatting.GRAY));
                }
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, tips, java.util.Optional.empty(), ItemStack.EMPTY, mouseX, mouseY, null);
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, this.getX(), this.getY(), (float) u, (float) v, this.width, this.height, 256, 256);
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        int mode = this.getMenu().getMode();

        Component titleComponent = switch (mode) {
            case 0 -> Component.translatable("container.puremashtweaks.multifunctional_compressor");
            case 1 -> Component.translatable("container.puremashtweaks.multifunctional_compressor.singularity");
            case 2 -> Component.translatable("container.puremashtweaks.multifunctional_compressor.dust");
            default -> this.title;
        };

        String titleStr = titleComponent.getString();
        graphics.text(this.font, titleComponent, (this.imageWidth / 2 - this.font.width(titleStr) / 2) - 10, 6, 0xFFE0E0E0, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFE0E0E0, false);
    }
}