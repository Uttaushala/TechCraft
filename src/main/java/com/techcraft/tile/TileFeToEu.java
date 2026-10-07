package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.compat.ic2.Ic2Support;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;

import java.util.Collections;
import java.util.List;

/** Takes Forge Energy on any side but the front and offers EU to an IC2 cable at the front. */
@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "ic2")
public class TileFeToEu extends TileMachineBase implements IEnergySource {
    private boolean joined;

    public TileFeToEu() {
        super(0, 0, ModConfig.ic2.capacity, ModConfig.ic2.maxFeOutput, 0);
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
        updateActive(rate > 0);
        rate = 0;
    }

    @Override
    protected boolean defaultEnergyInput(EnumFacing side) {
        return side != getFacing();
    }

    @Override
    @Optional.Method(modid = "ic2")
    public double getOfferedEnergy() {
        double stored = (double) energy.getEnergyStored() / ModConfig.ic2.feePerEu;
        return Math.min(stored, Ic2Support.packetSize(ModConfig.ic2.tier));
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void drawEnergy(double amount) {
        int fe = (int) Math.ceil(amount * ModConfig.ic2.feePerEu);
        energy.consume(Math.min(fe, energy.getEnergyStored()));
        rate = fe;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public int getSourceTier() {
        return ModConfig.ic2.tier;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
        return side == getFacing();
    }

    @Override
    public int getRateSign() {
        return 1;
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
