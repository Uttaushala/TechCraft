package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;

import java.util.Collections;
import java.util.List;

/** Stores energy: accepts it on every side and outputs it through the front face. */
public class TileBatteryBox extends TileMachineBase {
    private final int transferRate;

    public TileBatteryBox() {
        this(ModConfig.batteryBox);
    }

    protected TileBatteryBox(ModConfig.Battery config) {
        super(0, 0, config.capacity, config.transferRate, config.transferRate);
        this.transferRate = config.transferRate;
    }

    @Override
    protected void tickServer() {
        pushEnergy(transferRate);
    }

    public static class Advanced extends TileBatteryBox {
        public Advanced() {
            super(ModConfig.batteryBoxAdvanced);
        }
    }

    public static class Ultimate extends TileBatteryBox {
        public Ultimate() {
            super(ModConfig.batteryBoxUltimate);
        }
    }

    @Override
    protected boolean defaultEnergyInput(EnumFacing side) {
        return side != getFacing();
    }

    @Override
    protected boolean defaultEnergyOutput(EnumFacing side) {
        return side == getFacing();
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
