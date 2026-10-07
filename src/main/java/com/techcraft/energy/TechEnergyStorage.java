package com.techcraft.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

/**
 * Forge Energy buffer with internal helpers so machines can generate/consume energy
 * regardless of the external receive/extract limits.
 */
public class TechEnergyStorage extends EnergyStorage {
    private final Runnable onChanged;

    public TechEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) {
            onChanged.run();
        }
        return extracted;
    }

    public int generate(int amount) {
        int added = Math.min(capacity - energy, Math.max(0, amount));
        if (added > 0) {
            energy += added;
            onChanged.run();
        }
        return added;
    }

    public boolean consume(int amount) {
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        onChanged.run();
        return true;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setInteger("Energy", energy);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        setEnergy(tag.getInteger("Energy"));
    }
}
