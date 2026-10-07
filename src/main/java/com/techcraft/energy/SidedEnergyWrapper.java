package com.techcraft.energy;

import net.minecraftforge.energy.IEnergyStorage;

/** Restricts a storage to input-only or output-only access on a given side. */
public class SidedEnergyWrapper implements IEnergyStorage {
    private final IEnergyStorage storage;
    private final boolean allowReceive;
    private final boolean allowExtract;

    public SidedEnergyWrapper(IEnergyStorage storage, boolean allowReceive, boolean allowExtract) {
        this.storage = storage;
        this.allowReceive = allowReceive;
        this.allowExtract = allowExtract;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return allowReceive ? storage.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return allowExtract ? storage.extractEnergy(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return allowExtract && storage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return allowReceive && storage.canReceive();
    }
}
