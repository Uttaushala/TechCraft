package com.techcraft.tile;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/** What the GUI needs to draw a gas tank, without touching Mekanism classes (which may not be installed). */
public interface IGasDisplay {
    int getGasAmount();

    int getGasCapacity();

    @Nullable
    String getGasName();

    @Nullable
    ResourceLocation getGasIcon();

    /** 0xRRGGBB tint for the icon. */
    int getGasTint();

    /** Wide display for a plain tank, narrow bar next to the energy bar for a machine. */
    boolean isBigDisplay();
}
