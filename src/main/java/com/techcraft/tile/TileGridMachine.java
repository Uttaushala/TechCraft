package com.techcraft.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Base for machines with a 3x3 inventory that work on the world: miner, breaker, placer and grinder. With no input
 * slots the grid is output-only and finished items are pushed into any neighbouring inventory automatically.
 */
public abstract class TileGridMachine extends TileMachineBase {
    protected static final int SCALE = 4;

    private final int inputSlots;
    protected int progress;

    protected TileGridMachine(int capacity, int inputSlots) {
        super(9, inputSlots, capacity, capacity, 0);
        this.inputSlots = inputSlots;
    }

    protected boolean hasFreeSlot() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Can the block at {@code p} be broken by a machine? */
    protected boolean isMinable(BlockPos p, boolean allowTileEntities) {
        IBlockState state = world.getBlockState(p);
        if (state.getBlock().isAir(state, world, p) || state.getMaterial().isLiquid()
                || state.getBlockHardness(world, p) < 0) {
            return false;
        }
        return allowTileEntities || world.getTileEntity(p) == null;
    }

    /** Breaks the block and stores its drops; anything that doesn't fit is dropped in the world. */
    protected void mine(BlockPos p) {
        IBlockState state = world.getBlockState(p);
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, p, state, 0);
        world.destroyBlock(p, false);
        for (ItemStack drop : drops) {
            ItemStack left = insertInternal(drop);
            if (!left.isEmpty()) {
                InventoryHelper.spawnItemStack(world, p.getX(), p.getY(), p.getZ(), left);
            }
        }
    }

    @Override
    public List<Slot> createSlots() {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            slots.add(new SlotItemHandler(inventory, i, 62 + (i % 3) * 18, 17 + (i / 3) * 18));
        }
        return slots;
    }

    @Override
    public int getInputSlotCount() {
        return inputSlots;
    }

    @Override
    protected boolean defaultItemInput(EnumFacing side) {
        return inputSlots > 0;
    }

    @Override
    protected boolean defaultItemOutput(EnumFacing side) {
        return inputSlots == 0;
    }

    @Override
    protected boolean defaultItemEject(EnumFacing side) {
        return inputSlots == 0;
    }

    @Override
    public int getUpgradeSlotCount() {
        return 2;
    }

    @Override
    public int getRateSign() {
        return -1;
    }

    @Override
    protected int getProgress() {
        return progress;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Progress", progress);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        progress = tag.getInteger("Progress");
    }
}
