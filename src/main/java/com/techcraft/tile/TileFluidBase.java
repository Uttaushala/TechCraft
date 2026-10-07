package com.techcraft.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
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
    /** mB moved per tick into a neighbour on faces set to eject. */
    private static final int PUSH_RATE = 500;

    protected final FluidTank tank;
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
    }

    protected boolean isFluidValid(FluidStack fluid) {
        return true;
    }

    protected boolean defaultFluidInput(EnumFacing side) {
        return true;
    }

    protected boolean defaultFluidOutput(EnumFacing side) {
        return true;
    }

    protected boolean defaultFluidEject(EnumFacing side) {
        return false;
    }

    public FluidTank getTank() {
        return tank;
    }

    @Nullable
    private IFluidHandler getFluidCapability(@Nullable EnumFacing side) {
        if (side == null) {
            return new Access(true, true);
        }
        int access = access(SideConfig.FLUIDS, side, defaultFluidInput(side), defaultFluidOutput(side));
        return access == 0 ? null : new Access((access & IN) != 0, (access & OUT) != 0);
    }

    /** The tank as seen through one face: fill and drain can each be switched off. */
    private final class Access implements IFluidHandler {
        private final boolean canFill;
        private final boolean canDrain;

        Access(boolean canFill, boolean canDrain) {
            this.canFill = canFill;
            this.canDrain = canDrain;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return tank.getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return canFill && resource != null && isFluidValid(resource) ? tank.fill(resource, doFill) : 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return canDrain ? tank.drain(resource, doDrain) : null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return canDrain ? tank.drain(maxDrain, doDrain) : null;
        }
    }

    protected boolean ejectsFluid(EnumFacing side) {
        FaceMode mode = sides.get(SideConfig.FLUIDS, side);
        return mode == FaceMode.OUTPUT || (mode == FaceMode.DEFAULT && defaultFluidEject(side));
    }

    @Override
    protected void autoOutputs() {
        super.autoOutputs();
        if (tank.getFluidAmount() <= 0) {
            return;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!ejectsFluid(side) || !world.isBlockLoaded(pos.offset(side))) {
                continue;
            }
            TileEntity neighbour = world.getTileEntity(pos.offset(side));
            IFluidHandler target = neighbour == null ? null
                    : neighbour.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
            if (target != null) {
                FluidUtil.tryFluidTransfer(target, tank, PUSH_RATE, true);
            }
        }
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
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return getFluidCapability(facing) != null;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler handler = getFluidCapability(facing);
            return handler == null ? null : CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler);
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
