package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.compat.ic2.Ic2Support;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;

import java.util.Collections;
import java.util.List;

/** Takes EU from an IC2 cable on any side but the front and hands out Forge Energy through the front. */
@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "ic2")
public class TileEuToFe extends TileMachineBase implements IEnergySink {
    private boolean joined;

    public TileEuToFe() {
        super(0, 0, ModConfig.ic2.capacity, 0, ModConfig.ic2.maxFeOutput);
    }

    @Override
    public void onLoad() {
        if (!world.isRemote && Ic2Support.isLoaded() && !joined) {
            joined = true;
            Ic2Support.join(this);
        }
    }

    private void leaveNet() {
        if (joined && Ic2Support.isLoaded()) {
            joined = false;
            Ic2Support.leave(this);
        }
    }

    @Override
    public void invalidate() {
        leaveNet();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        leaveNet();
        super.onChunkUnload();
    }

    @Override
    protected void tickServer() {
        int before = energy.getEnergyStored();
        pushEnergy(ModConfig.ic2.maxFeOutput);
        rate = before - energy.getEnergyStored();
        updateActive(rate > 0);
    }

    @Override
    protected boolean defaultEnergyInput(EnumFacing side) {
        return false;
    }

    @Override
    protected boolean defaultEnergyOutput(EnumFacing side) {
        return side == getFacing();
    }

    @Override
    @Optional.Method(modid = "ic2")
    public double getDemandedEnergy() {
        return (double) (energy.getMaxEnergyStored() - energy.getEnergyStored()) / ModConfig.ic2.feePerEu;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public int getSinkTier() {
        return ModConfig.ic2.tier;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public double injectEnergy(EnumFacing from, double amount, double voltage) {
        int perEu = ModConfig.ic2.feePerEu;
        int accepted = energy.generate((int) Math.min(amount * perEu, Integer.MAX_VALUE));
        return amount - (double) accepted / perEu;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
        return side != getFacing();
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
