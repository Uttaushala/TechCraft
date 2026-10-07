package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.energy.SidedEnergyWrapper;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TileLavaGenerator extends TileFluidBase {
    private int burnTime;
    private int burnTimeTotal;

    public TileLavaGenerator() {
        super(0, 0, ModConfig.lavaGenerator.capacity, 0, ModConfig.lavaGenerator.maxOutput,
                ModConfig.lavaGenerator.tankCapacity);
    }

    @Override
    protected void tickServer() {
        ModConfig.LavaGenerator config = ModConfig.lavaGenerator;
        boolean full = energy.getEnergyStored() >= energy.getMaxEnergyStored();

        if (burnTime <= 0 && !full) {
            FluidStack drained = tank.drain(config.mbPerCycle, false);
            if (drained != null && drained.amount >= config.mbPerCycle) {
                tank.drain(config.mbPerCycle, true);
                burnTime = burnTimeTotal = config.cycleTicks;
            }
        }

        boolean burning = false;
        if (burnTime > 0 && !full) {
            burnTime--;
            energy.generate(config.energyPerTick);
            burning = true;
        }
        rate = burning ? config.energyPerTick : 0;
        updateActive(burning);
        pushEnergy(config.maxOutput, EnumFacing.VALUES);
        syncFluid();
    }

    @Override
    protected boolean isFluidValid(FluidStack fluid) {
        return fluid.getFluid() == FluidRegistry.LAVA;
    }

    @Override
    protected boolean allowExternalDrain() {
        return false;
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

    @Override
    protected int getProgress() {
        return burnTime;
    }

    @Override
    protected int getProgressMax() {
        return burnTimeTotal;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("BurnTime", burnTime);
        tag.setInteger("BurnTimeTotal", burnTimeTotal);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        burnTime = tag.getInteger("BurnTime");
        burnTimeTotal = tag.getInteger("BurnTimeTotal");
    }
}
