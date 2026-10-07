package com.techcraft.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

/**
 * Machine with a fluid tank exposed through the Forge fluid capability, so Mekanism pipes, Thermal ducts,
 * EnderIO conduits and buckets can all fill or drain it. The tank is synced to clients for the GUI.
 */
public abstract class TileFluidBase extends TileMachineBase {
    private static final int SYNC_INTERVAL = 10;

    protected final FluidTank tank;
    private final IFluidHandler handler;
    private int syncedAmount = -1;
    private String syncedFluid = "";
    private long lastSync;

    protected TileFluidBase(int slots, int inputSlots, int capacity, int maxReceive, int maxExtract, int tankCapacity) {
        super(slots, inputSlots, capacity, maxReceive, maxExtract);
        this.tank = new FluidTank(tankCapacity) {
            @Override
            protected void onContentsChanged() {
                markDirty();
            }
        };
        this.tank.setTileEntity(this);
        this.handler = new IFluidHandler() {
            @Override
            public IFluidTankProperties[] getTankProperties() {
                return tank.getTankProperties();
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                return resource != null && isFluidValid(resource) ? tank.fill(resource, doFill) : 0;
            }

            @Nullable
            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                return allowExternalDrain() ? tank.drain(resource, doDrain) : null;
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                return allowExternalDrain() ? tank.drain(maxDrain, doDrain) : null;
            }
        };
    }

    protected boolean isFluidValid(FluidStack fluid) {
        return true;
    }

    protected boolean allowExternalDrain() {
        return true;
    }

    public FluidTank getTank() {
        return tank;
    }

    /** Call at the end of the server tick; sends the tank to clients when it changed. */
    protected void syncFluid() {
        FluidStack fluid = tank.getFluid();
        int amount = fluid == null ? 0 : fluid.amount;
        String name = fluid == null ? "" : fluid.getFluid().getName();
        boolean changed = amount != syncedAmount || !name.equals(syncedFluid);
        if (changed && world.getTotalWorldTime() - lastSync >= SYNC_INTERVAL) {
            lastSync = world.getTotalWorldTime();
            syncedAmount = amount;
            syncedFluid = name;
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Tank", tank.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        tank.readFromNBT(tag.getCompoundTag("Tank"));
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }
}
