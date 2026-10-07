package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.inventory.Slot;

import java.util.Collections;
import java.util.List;

/** Plain fluid storage. Works with buckets and with any mod's fluid pipes. */
public class TileFluidTank extends TileFluidBase {
    public TileFluidTank() {
        super(0, 0, 0, 0, 0, ModConfig.fluidTank.capacity);
    }

    @Override
    protected void tickServer() {
        syncFluid();
    }

    @Override
    protected boolean hasEnergy() {
        return false;
    }

    @Override
    public List<Slot> createSlots() {
        return Collections.emptyList();
    }

    @Override
    public int getInputSlotCount() {
        return 0;
    }
}
