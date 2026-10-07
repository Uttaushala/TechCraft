package com.techcraft.tile;

import com.techcraft.energy.SidedEnergyWrapper;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/** A generator without inventory that produces a computed amount of energy every tick. */
public abstract class TileSimpleGenerator extends TileMachineBase {
    private static final EnumFacing[] OUTPUT_SIDES = {
            EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};

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
        pushEnergy(maxOutput, OUTPUT_SIDES);
    }

    @Override
    protected IEnergyStorage getEnergyCapability(@Nullable EnumFacing side) {
        return new SidedEnergyWrapper(energy, false, true);
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
