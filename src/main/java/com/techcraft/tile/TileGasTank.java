package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.compat.mekanism.GasCaps;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Stores one kind of Mekanism gas and connects to pressurized tubes through Mekanism's gas capability. The face modes
 * of the "fluid" side configuration apply to gas as well. Only used when Mekanism is installed.
 */
public class TileGasTank extends TileMachineBase implements IGasDisplay {
    private static final int SYNC_INTERVAL = 10;
    /** mB moved per tick into a neighbour on faces set to output. */
    private static final int PUSH_RATE = 500;

    protected final GasTank gas;
    private int syncedAmount = -1;
    private String syncedGas = "";
    private long lastSync;

    private final IGasHandler handler = new IGasHandler() {
        @Override
        public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
            if (stack == null || !allowsGas(side, true) || !isGasValid(stack.getGas())) {
                return 0;
            }
            int received = gas.receive(stack, doTransfer);
            if (received > 0 && doTransfer) {
                markDirty();
            }
            return received;
        }

        @Override
        public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
            if (!allowsGas(side, false)) {
                return null;
            }
            GasStack drawn = gas.draw(amount, doTransfer);
            if (drawn != null && doTransfer) {
                markDirty();
            }
            return drawn;
        }

        @Override
        public boolean canReceiveGas(EnumFacing side, Gas type) {
            return allowsGas(side, true) && (type == null || isGasValid(type)) && gas.canReceive(type);
        }

        @Override
        public boolean canDrawGas(EnumFacing side, Gas type) {
            return allowsGas(side, false) && gas.canDraw(type);
        }

        @Nonnull
        @Override
        public GasTankInfo[] getTankInfo() {
            return new GasTankInfo[]{gas};
        }
    };

    public TileGasTank() {
        this(ModConfig.gas.tankCapacity, 0, 0);
    }

    protected TileGasTank(int gasCapacity, int energyCapacity, int maxEnergyOutput) {
        super(0, 0, energyCapacity, 0, maxEnergyOutput);
        this.gas = new GasTank(gasCapacity);
    }

    protected boolean isGasValid(Gas type) {
        return true;
    }

    protected boolean defaultGasInput(EnumFacing side) {
        return true;
    }

    protected boolean defaultGasOutput(EnumFacing side) {
        return true;
    }

    private boolean allowsGas(@Nullable EnumFacing side, boolean receive) {
        if (side == null) {
            return true;
        }
        int access = access(SideConfig.FLUIDS, side, defaultGasInput(side), defaultGasOutput(side));
        return (access & (receive ? IN : OUT)) != 0;
    }

    @Override
    protected boolean hasEnergy() {
        return false;
    }

    @Override
    protected void tickServer() {
        pushGas();
        syncGas();
    }

    /** Faces explicitly set to output hand gas to the neighbouring tank, tube or machine. */
    private void pushGas() {
        if (gas.getStored() <= 0) {
            return;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            if (sides.get(SideConfig.FLUIDS, side) != FaceMode.OUTPUT || !world.isBlockLoaded(pos.offset(side))) {
                continue;
            }
            TileEntity neighbour = world.getTileEntity(pos.offset(side));
            IGasHandler target = neighbour == null || GasCaps.GAS_HANDLER == null ? null
                    : neighbour.getCapability(GasCaps.GAS_HANDLER, side.getOpposite());
            GasStack offer = gas.getGas();
            if (target == null || offer == null) {
                continue;
            }
            GasStack portion = new GasStack(offer.getGas(), Math.min(PUSH_RATE, gas.getStored()));
            if (target.canReceiveGas(side.getOpposite(), portion.getGas())) {
                int accepted = target.receiveGas(side.getOpposite(), portion, true);
                if (accepted > 0) {
                    gas.draw(accepted, true);
                    markDirty();
                }
            }
        }
    }

    protected void syncGas() {
        GasStack stack = gas.getGas();
        int amount = stack == null ? 0 : stack.amount;
        String name = stack == null ? "" : stack.getGas().getName();
        boolean changed = amount != syncedAmount || !name.equals(syncedGas);
        if (changed && world.getTotalWorldTime() - lastSync >= SYNC_INTERVAL) {
            lastSync = world.getTotalWorldTime();
            syncedAmount = amount;
            syncedGas = name;
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (GasCaps.GAS_HANDLER != null && capability == GasCaps.GAS_HANDLER) {
            return facing == null || (access(SideConfig.FLUIDS, facing, defaultGasInput(facing), defaultGasOutput(facing)) != 0);
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (GasCaps.GAS_HANDLER != null && capability == GasCaps.GAS_HANDLER) {
            return hasCapability(capability, facing) ? GasCaps.GAS_HANDLER.cast(handler) : null;
        }
        return super.getCapability(capability, facing);
    }

    // ---- GUI ----

    @Override
    public int getGasAmount() {
        return gas.getStored();
    }

    @Override
    public int getGasCapacity() {
        return gas.getMaxGas();
    }

    @Nullable
    @Override
    public String getGasName() {
        GasStack stack = gas.getGas();
        return stack == null ? null : stack.getGas().getLocalizedName();
    }

    @Nullable
    @Override
    public ResourceLocation getGasIcon() {
        GasStack stack = gas.getGas();
        return stack == null ? null : stack.getGas().getIcon();
    }

    @Override
    public int getGasTint() {
        GasStack stack = gas.getGas();
        return stack == null ? 0xFFFFFF : stack.getGas().getTint();
    }

    @Override
    public boolean isBigDisplay() {
        return true;
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
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("GasTank", gas.write(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        gas.read(tag.getCompoundTag("GasTank"));
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
