package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.energy.SidedEnergyWrapper;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
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
        pushEnergy(transferRate, getFacing());
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
    protected IEnergyStorage getEnergyCapability(@Nullable EnumFacing side) {
        if (side == null) {
            return energy;
        }
        boolean front = side == getFacing();
        return new SidedEnergyWrapper(energy, !front, front);
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
