package com.techcraft.client;

import com.techcraft.inventory.ContainerMachine;
import com.techcraft.tile.TileAlloyFurnace;
import com.techcraft.tile.TileBatteryBox;
import com.techcraft.tile.TileCharger;
import com.techcraft.tile.TileCoalGenerator;
import com.techcraft.tile.TileFluidBase;
import com.techcraft.tile.TileFluidTank;
import com.techcraft.tile.TileMachineBase;
import com.techcraft.tile.TileProcessor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Machine screen drawn entirely in code, in the vanilla style. */
public class GuiMachine extends GuiContainer {
    private static final int BAR_X = 10;
    private static final int BAR_Y = 17;
    private static final int BAR_W = 14;
    private static final int BAR_H = 52;

    private static final int COLOR_PANEL = 0xFFC6C6C6;
    private static final int COLOR_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_SHADOW = 0xFF555555;
    private static final int COLOR_DARK = 0xFF373737;
    private static final int COLOR_SLOT = 0xFF8B8B8B;
    private static final int COLOR_TEXT = 0x404040;

    private final ContainerMachine container;
    private final TileMachineBase tile;
    private final TileFluidBase fluidTile;
    private final boolean bigTank;

    public GuiMachine(ContainerMachine container) {
        super(container);
        this.container = container;
        this.tile = container.getTile();
        this.fluidTile = tile instanceof TileFluidBase ? (TileFluidBase) tile : null;
        this.bigTank = tile instanceof TileFluidTank;
        this.xSize = 176;
        this.ySize = 166;
    }

    private boolean hasEnergyBar() {
        return container.getField(TileMachineBase.FIELD_CAPACITY) > 0;
    }

    private int fluidX() {
        return bigTank ? 62 : 34;
    }

    private int fluidW() {
        return bigTank ? 52 : BAR_W;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        if (hasEnergyBar() && x >= BAR_X && x < BAR_X + BAR_W && y >= BAR_Y && y < BAR_Y + BAR_H) {
            drawHoveringText(Collections.singletonList(energyText()), mouseX, mouseY);
        }
        if (fluidTile != null && x >= fluidX() && x < fluidX() + fluidW() && y >= BAR_Y && y < BAR_Y + BAR_H) {
            drawHoveringText(fluidText(), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int left = guiLeft;
        int top = guiTop;

        // Panel
        drawRect(left, top, left + xSize, top + ySize, 0xFF000000);
        drawRect(left + 1, top + 1, left + xSize - 1, top + ySize - 1, COLOR_LIGHT);
        drawRect(left + 3, top + 3, left + xSize - 1, top + ySize - 1, COLOR_SHADOW);
        drawRect(left + 3, top + 3, left + xSize - 3, top + ySize - 3, COLOR_PANEL);

        // Slots
        for (Slot slot : inventorySlots.inventorySlots) {
            drawInset(left + slot.xPos - 1, top + slot.yPos - 1, 18, 18, COLOR_SLOT);
        }

        // Energy bar
        if (hasEnergyBar()) {
            drawInset(left + BAR_X - 1, top + BAR_Y - 1, BAR_W + 2, BAR_H + 2, 0xFF2B2B2B);
            int filled = scaled(container.getField(TileMachineBase.FIELD_ENERGY),
                    container.getField(TileMachineBase.FIELD_CAPACITY), BAR_H);
            if (filled > 0) {
                drawGradientRect(left + BAR_X, top + BAR_Y + BAR_H - filled, left + BAR_X + BAR_W, top + BAR_Y + BAR_H,
                        0xFFFF5040, 0xFFA01010);
            }
        }

        if (fluidTile != null) {
            drawFluidBar(left + fluidX(), top + BAR_Y, fluidW(), BAR_H);
        }

        int progress = container.getField(TileMachineBase.FIELD_PROGRESS);
        int progressMax = container.getField(TileMachineBase.FIELD_PROGRESS_MAX);

        if (tile instanceof TileCoalGenerator) {
            // Flame above the fuel slot, burning down.
            int fx = left + 81;
            int fy = top + 36;
            drawInset(fx - 1, fy - 1, 16, 16, 0xFF6B6B6B);
            int flame = scaled(progress, progressMax, 14);
            if (flame > 0) {
                drawGradientRect(fx, fy + 14 - flame, fx + 14, fy + 14, 0xFFFFD040, 0xFFE04000);
            }
        } else if (tile instanceof TileProcessor || tile instanceof TileAlloyFurnace) {
            // Progress arrow between input and output.
            int ax = left + 80;
            int ay = top + 39;
            drawInset(ax - 1, ay - 1, 26, 10, 0xFF6B6B6B);
            int arrow = scaled(progress, progressMax, 24);
            if (arrow > 0) {
                drawRect(ax, ay, ax + arrow, ay + 8, 0xFFFFFFFF);
            }
        }
    }

    private void drawFluidBar(int x, int y, int w, int h) {
        drawInset(x - 1, y - 1, w + 2, h + 2, 0xFF2B2B2B);
        FluidTank tank = fluidTile.getTank();
        FluidStack stack = tank.getFluid();
        if (stack == null || stack.amount <= 0 || tank.getCapacity() <= 0) {
            return;
        }
        int filled = scaled(stack.amount, tank.getCapacity(), h);
        Fluid fluid = stack.getFluid();
        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getStill(stack).toString());
        int color = fluid.getColor(stack);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, 1.0F);
        for (int dy = 0; dy < filled; dy += 16) {
            int th = Math.min(16, filled - dy);
            for (int dx = 0; dx < w; dx += 16) {
                drawTexturedModalRect(x + dx, y + h - dy - th, sprite, Math.min(16, w - dx), th);
            }
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format(tile.getBlockType().getTranslationKey() + ".name");
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 6, COLOR_TEXT);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 94, COLOR_TEXT);

        int sign = tile.getRateSign();
        if (sign != 0) {
            int rate = container.getField(TileMachineBase.FIELD_RATE);
            String text = (sign > 0 ? "+" : "-") + rate + " FE/t";
            int x = 34;
            int y = 24;
            if (tile instanceof TileAlloyFurnace) {
                x = 74;
                y = 58;
            } else if (tile instanceof TileProcessor) {
                x = 62;
                y = 58;
            } else if (tile instanceof TileCharger) {
                x = 104;
                y = 40;
            } else if (fluidTile != null) {
                x = 56;
            }
            fontRenderer.drawString(text, x, y, rate > 0 ? 0x207020 : 0x707070);
        }

        if (tile instanceof TileBatteryBox) {
            int energy = container.getField(TileMachineBase.FIELD_ENERGY);
            int capacity = container.getField(TileMachineBase.FIELD_CAPACITY);
            fontRenderer.drawString(I18n.format("gui.techcraft.stored"), 34, 24, COLOR_TEXT);
            fontRenderer.drawString(String.format("%,d / %,d FE", energy, capacity), 34, 36, COLOR_TEXT);
            int percent = capacity > 0 ? (int) (100L * energy / capacity) : 0;
            fontRenderer.drawString(percent + "%", 34, 48, COLOR_TEXT);
            fontRenderer.drawString(I18n.format("gui.techcraft.output_front"), 34, 60, 0x707070);
        }
    }

    private String energyText() {
        return String.format("%,d / %,d FE",
                container.getField(TileMachineBase.FIELD_ENERGY),
                container.getField(TileMachineBase.FIELD_CAPACITY));
    }

    private List<String> fluidText() {
        List<String> lines = new ArrayList<>();
        FluidTank tank = fluidTile.getTank();
        FluidStack stack = tank.getFluid();
        if (stack == null || stack.amount <= 0) {
            lines.add(I18n.format("gui.techcraft.empty"));
        } else {
            lines.add(stack.getLocalizedName());
        }
        lines.add(String.format("%,d / %,d mB", stack == null ? 0 : stack.amount, tank.getCapacity()));
        return lines;
    }

    /** Recessed box: dark top-left edge, light bottom-right edge. */
    private void drawInset(int x, int y, int w, int h, int fill) {
        drawRect(x, y, x + w, y + h, COLOR_LIGHT);
        drawRect(x, y, x + w - 1, y + h - 1, COLOR_DARK);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    private static int scaled(int value, int max, int pixels) {
        if (max <= 0 || value <= 0) {
            return 0;
        }
        return (int) Math.min(pixels, (long) value * pixels / max);
    }
}
