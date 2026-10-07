package com.techcraft.tile;

import com.techcraft.block.BlockMachine;
import com.techcraft.energy.SidedEnergyWrapper;
import com.techcraft.energy.TechEnergyStorage;
import com.techcraft.inventory.AutomationItemHandler;
import com.techcraft.item.ItemUpgrade;
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
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class TileMachineBase extends TileEntity implements ITickable {
    public static final int FIELD_ENERGY = 0;
    public static final int FIELD_CAPACITY = 1;
    public static final int FIELD_PROGRESS = 2;
    public static final int FIELD_PROGRESS_MAX = 3;
    public static final int FIELD_RATE = 4;
    /** Three consecutive fields: the packed side modes for energy, items and fluids. */
    public static final int FIELD_SIDES = 5;
    public static final int FIELD_COUNT = 8;

    protected static final int IN = 1;
    protected static final int OUT = 2;

    /** Keeps the "active" look for a moment so the block does not flicker when energy is tight. */
    private static final int ACTIVE_COOLDOWN = 20;
    private static final int EJECT_INTERVAL = 8;

    protected final TechEnergyStorage energy;
    protected final ItemStackHandler inventory;
    protected final ItemStackHandler upgrades;
    protected final SideConfig sides = new SideConfig();
    private int activeTicks;
    private boolean bypassValidation;
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
                return bypassValidation || isItemValidForSlot(slot, stack);
            }
        };
        this.upgrades = new ItemStackHandler(getUpgradeSlotCount()) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof ItemUpgrade;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 4;
            }
        };
    }

    protected abstract void tickServer();

    protected boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }

    /** Slots shown in the GUI, in inventory order. The first slots are the input slots. */
    public abstract List<Slot> createSlots();

    public abstract int getInputSlotCount();

    /** How many upgrade slots this machine has. Must not depend on instance state (called from the constructor). */
    public int getUpgradeSlotCount() {
        return 0;
    }

    public List<Slot> createUpgradeSlots() {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < upgrades.getSlots(); i++) {
            slots.add(new SlotItemHandler(upgrades, i, 152, 26 + i * 18));
        }
        return slots;
    }

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

    // ---- upgrades ----

    protected int upgradeCount(ItemUpgrade.Type type) {
        int count = 0;
        for (int i = 0; i < upgrades.getSlots(); i++) {
            ItemStack stack = upgrades.getStackInSlot(i);
            if (stack.getItem() instanceof ItemUpgrade && ((ItemUpgrade) stack.getItem()).getType() == type) {
                count += stack.getCount();
            }
        }
        return count;
    }

    protected double speedFactor() {
        return 1.0 + 0.5 * upgradeCount(ItemUpgrade.Type.SPEED);
    }

    /** Multiplier on the energy a machine uses per tick. */
    protected double energyFactor() {
        double efficiency = Math.max(0.2, 1.0 - 0.15 * upgradeCount(ItemUpgrade.Type.EFFICIENCY));
        return (1.0 + 0.8 * upgradeCount(ItemUpgrade.Type.SPEED)) * efficiency;
    }

    // ---- side configuration ----

    public SideConfig getSideConfig() {
        return sides;
    }

    /** Order used by the GUI: front, back, left, right, top, bottom. */
    public EnumFacing uiFace(int index) {
        EnumFacing front = getFacing();
        switch (index) {
            case 0:
                return front;
            case 1:
                return front.getOpposite();
            case 2:
                return front.rotateY();
            case 3:
                return front.rotateYCCW();
            case 4:
                return EnumFacing.UP;
            default:
                return EnumFacing.DOWN;
        }
    }

    public void cycleSide(int resource, EnumFacing face) {
        sides.cycle(resource, face);
        markDirty();
        if (world != null) {
            world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
        }
    }

    protected final int access(int resource, EnumFacing side, boolean defaultIn, boolean defaultOut) {
        switch (sides.get(resource, side)) {
            case DISABLED:
                return 0;
            case INPUT:
                return IN;
            case OUTPUT:
                return OUT;
            default:
                return (defaultIn ? IN : 0) | (defaultOut ? OUT : 0);
        }
    }

    protected boolean defaultEnergyInput(EnumFacing side) {
        return true;
    }

    protected boolean defaultEnergyOutput(EnumFacing side) {
        return false;
    }

    protected boolean defaultItemInput(EnumFacing side) {
        return true;
    }

    protected boolean defaultItemOutput(EnumFacing side) {
        return true;
    }

    /** Whether the machine pushes finished items into the neighbour on this face by itself. */
    protected boolean defaultItemEject(EnumFacing side) {
        return false;
    }

    protected final int energyAccess(EnumFacing side) {
        return access(SideConfig.ENERGY, side, defaultEnergyInput(side), defaultEnergyOutput(side));
    }

    @Nullable
    protected IEnergyStorage getEnergyCapability(@Nullable EnumFacing side) {
        if (side == null) {
            return energy;
        }
        int access = energyAccess(side);
        return access == 0 ? null : new SidedEnergyWrapper(energy, (access & IN) != 0, (access & OUT) != 0);
    }

    @Nullable
    protected IItemHandler getItemCapability(@Nullable EnumFacing side) {
        if (side == null) {
            return inventory;
        }
        int access = access(SideConfig.ITEMS, side, defaultItemInput(side), defaultItemOutput(side));
        return access == 0 ? null
                : new AutomationItemHandler(inventory, getInputSlotCount(), (access & IN) != 0, (access & OUT) != 0);
    }

    // ---- ticking ----

    @Override
    public void update() {
        if (!world.isRemote) {
            tickServer();
            autoOutputs();
        }
    }

    protected void autoOutputs() {
        if (world.getTotalWorldTime() % EJECT_INTERVAL == 0) {
            ejectItems();
        }
    }

    protected boolean ejectsItems(EnumFacing side) {
        FaceMode mode = sides.get(SideConfig.ITEMS, side);
        return mode == FaceMode.OUTPUT || (mode == FaceMode.DEFAULT && defaultItemEject(side));
    }

    /** Pushes one stack of output into every neighbour whose face is set to eject. */
    private void ejectItems() {
        if (inventory.getSlots() == 0) {
            return;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!ejectsItems(side) || !world.isBlockLoaded(pos.offset(side))) {
                continue;
            }
            TileEntity neighbour = world.getTileEntity(pos.offset(side));
            IItemHandler target = neighbour == null ? null
                    : neighbour.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
            if (target == null) {
                continue;
            }
            for (int slot = getInputSlotCount(); slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack remainder = ItemHandlerHelper.insertItem(target, stack.copy(), false);
                int moved = stack.getCount() - remainder.getCount();
                if (moved > 0) {
                    inventory.extractItem(slot, moved, false);
                    break;
                }
            }
        }
    }

    /** Inserts into any slot of the machine's own inventory, ignoring what the slots accept from outside. */
    protected ItemStack insertInternal(ItemStack stack) {
        bypassValidation = true;
        try {
            for (int slot = 0; slot < inventory.getSlots() && !stack.isEmpty(); slot++) {
                stack = inventory.insertItem(slot, stack, false);
            }
        } finally {
            bypassValidation = false;
        }
        return stack;
    }

    public IEnergyStorage getEnergy() {
        return energy;
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public ItemStackHandler getUpgradeInventory() {
        return upgrades;
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
                return id >= FIELD_SIDES && id < FIELD_SIDES + SideConfig.RESOURCES ? sides.encode(id - FIELD_SIDES) : 0;
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

    /** Pushes up to {@code maxTransfer} FE into neighbouring energy receivers, through faces that output energy. */
    protected void pushEnergy(int maxTransfer) {
        for (EnumFacing side : EnumFacing.VALUES) {
            int available = Math.min(maxTransfer, energy.getEnergyStored());
            if (available <= 0) {
                return;
            }
            if ((energyAccess(side) & OUT) == 0) {
                continue;
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
            return hasEnergy() && getEnergyCapability(facing) != null;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory.getSlots() > 0) {
            return getItemCapability(facing) != null;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && hasEnergy()) {
            IEnergyStorage storage = getEnergyCapability(facing);
            return storage == null ? null : CapabilityEnergy.ENERGY.cast(storage);
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory.getSlots() > 0) {
            IItemHandler handler = getItemCapability(facing);
            return handler == null ? null : CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", inventory.serializeNBT());
        if (upgrades.getSlots() > 0) {
            tag.setTag("Upgrades", upgrades.serializeNBT());
        }
        energy.writeToNBT(tag);
        tag.setInteger("ActiveTicks", activeTicks);
        tag.setIntArray("Sides", new int[]{
                sides.encode(SideConfig.ENERGY), sides.encode(SideConfig.ITEMS), sides.encode(SideConfig.FLUIDS)});
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Inventory")) {
            inventory.deserializeNBT(tag.getCompoundTag("Inventory"));
        }
        if (tag.hasKey("Upgrades") && upgrades.getSlots() > 0) {
            upgrades.deserializeNBT(tag.getCompoundTag("Upgrades"));
        }
        energy.readFromNBT(tag);
        activeTicks = tag.getInteger("ActiveTicks");
        int[] packed = tag.getIntArray("Sides");
        for (int i = 0; i < Math.min(packed.length, SideConfig.RESOURCES); i++) {
            sides.decode(i, packed[i]);
        }
    }
}
