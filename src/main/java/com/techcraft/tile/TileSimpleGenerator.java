package com.techcraft.tile;

import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;

import java.util.Collections;
import java.util.List;

/** A generator without inventory that produces a computed amount of energy every tick. */
public abstract class TileSimpleGenerator extends TileMachineBase {
    private final int maxOutput;

    protected TileSimpleGenerator(int capacity, int maxOutput) {
        super(0, 0, capacity, 0, maxOutput);
        this.maxOutput = maxOutput;
    }

    protected abstract int computeOutput();

    @Override
    protected void tickServer() {
        int output = energy.getEnergyStored() < energy.getMaxEnergyStored() ? computeOutput() : 0;
        if (output > 0) {
            energy.generate(output);
        }
        rate = output;
        updateActive(output > 0);
        pushEnergy(maxOutput);
    }

    @Override
    protected boolean defaultEnergyInput(EnumFacing side) {
        return false;
    }

    @Override
    protected boolean defaultEnergyOutput(EnumFacing side) {
        return true;
    }

    @Override
    public List<Slot> createSlots() {
        return Collections.emptyList();
    }

    @Override
    public int getInputSlotCount() {
        return 0;
    }

    @Override
    public int getRateSign() {
        return 1;
    }
}
