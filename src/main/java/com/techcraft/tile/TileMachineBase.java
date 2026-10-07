package com.techcraft.tile;

import com.techcraft.block.BlockMachine;
import com.techcraft.energy.SidedEnergyWrapper;
import com.techcraft.energy.TechEnergyStorage;
import com.techcraft.inventory.AutomationItemHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public abstract class TileMachineBase extends TileEntity implements ITickable {
    public static final int FIELD_ENERGY = 0;
    public static final int FIELD_CAPACITY = 1;
    public static final int FIELD_PROGRESS = 2;
    public static final int FIELD_PROGRESS_MAX = 3;
    public static final int FIELD_RATE = 4;
    public static final int FIELD_COUNT = 5;

    /** Keeps the "active" look for a moment so the block does not flicker when energy is tight. */
    private static final int ACTIVE_COOLDOWN = 20;

    protected final TechEnergyStorage energy;
    protected final ItemStackHandler inventory;
    private final IItemHandler automationInventory;
    private int activeTicks;
    /** FE per tick produced or used right now, shown in the GUI. */
    protected int rate;

    protected TileMachineBase(int slots, int inputSlots, int capacity, int maxReceive, int maxExtract) {
        this.energy = new TechEnergyStorage(capacity, maxReceive, maxExtract, this::markDirty);
        this.inventory = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isItemValidForSlot(slot, stack);
            }
        };
        this.automationInventory = new AutomationItemHandler(inventory, inputSlots);
    }

    protected abstract void tickServer();

    protected boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }

    /** Slots shown in the GUI, in inventory order. The first slots are the input slots. */
    public abstract List<Slot> createSlots();

    public abstract int getInputSlotCount();

    /** +1 for generators, -1 for consumers, 0 when the GUI should not show a rate. */
    public int getRateSign() {
        return 0;
    }

    /** Machines without an energy buffer (e.g. the fluid tank) don't advertise the energy capability. */
    protected boolean hasEnergy() {
        return true;
    }

    protected int getProgress() {
        return 0;
    }

    protected int getProgressMax() {
        return 0;
    }

    /** Energy access from a given side. Machines accept energy on every side by default. */
    protected IEnergyStorage getEnergyCapability(@Nullable EnumFacing side) {
        return side == null ? energy : new SidedEnergyWrapper(energy, true, false);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            tickServer();
        }
    }

    public IEnergyStorage getEnergy() {
        return energy;
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public int getField(int id) {
        switch (id) {
            case FIELD_ENERGY:
                return energy.getEnergyStored();
            case FIELD_CAPACITY:
                return energy.getMaxEnergyStored();
            case FIELD_PROGRESS:
                return getProgress();
            case FIELD_PROGRESS_MAX:
                return getProgressMax();
            case FIELD_RATE:
                return rate;
            default:
                return 0;
        }
    }

    public EnumFacing getFacing() {
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockMachine) {
                return state.getValue(BlockMachine.FACING);
            }
        }
        return EnumFacing.NORTH;
    }

    protected void updateActive(boolean working) {
        if (working) {
            activeTicks = ACTIVE_COOLDOWN;
        } else if (activeTicks > 0) {
            activeTicks--;
        }
        IBlockState state = world.getBlockState(pos);
        boolean active = activeTicks > 0;
        if (state.getBlock() instanceof BlockMachine && state.getValue(BlockMachine.ACTIVE) != active) {
            world.setBlockState(pos, state.withProperty(BlockMachine.ACTIVE, active), 3);
        }
    }

    /** Pushes up to {@code maxTransfer} FE into neighbouring energy receivers. */
    protected void pushEnergy(int maxTransfer, EnumFacing... sides) {
        for (EnumFacing side : sides) {
            int available = Math.min(maxTransfer, energy.getEnergyStored());
            if (available <= 0) {
                return;
            }
            BlockPos target = pos.offset(side);
            if (!world.isBlockLoaded(target)) {
                continue;
            }
            TileEntity tile = world.getTileEntity(target);
            if (tile == null || !tile.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite())) {
                continue;
            }
            IEnergyStorage receiver = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());
            if (receiver != null && receiver.canReceive()) {
                int accepted = receiver.receiveEnergy(available, false);
                if (accepted > 0) {
                    energy.consume(accepted);
                    maxTransfer -= accepted;
                }
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        // Toggling the ACTIVE property must not recreate the tile entity.
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return hasEnergy();
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory.getSlots() > 0) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && hasEnergy()) {
            return CapabilityEnergy.ENERGY.cast(getEnergyCapability(facing));
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory.getSlots() > 0) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(facing == null ? inventory : automationInventory);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", inventory.serializeNBT());
        energy.writeToNBT(tag);
        tag.setInteger("ActiveTicks", activeTicks);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Inventory")) {
            inventory.deserializeNBT(tag.getCompoundTag("Inventory"));
        }
        energy.readFromNBT(tag);
        activeTicks = tag.getInteger("ActiveTicks");
    }
}
