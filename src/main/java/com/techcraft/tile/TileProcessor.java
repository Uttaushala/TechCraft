package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Arrays;
import java.util.List;

/** A machine that turns one input item into an output using energy. */
public abstract class TileProcessor extends TileMachineBase {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private final ModConfig.Processor config;
    private int progress;

    protected TileProcessor(ModConfig.Processor config) {
        super(2, 1, config.capacity, config.capacity, 0);
        this.config = config;
    }

    /** @return the result for one input item, or {@link ItemStack#EMPTY} if it can't be processed */
    protected abstract ItemStack getResult(ItemStack input);

    @Override
    protected void tickServer() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        ItemStack result = input.isEmpty() ? ItemStack.EMPTY : getResult(input);

        boolean working = false;
        if (!result.isEmpty() && canOutput(result)) {
            if (energy.consume(config.energyPerTick)) {
                working = true;
                progress++;
                if (progress >= config.ticksPerOperation) {
                    progress = 0;
                    inventory.extractItem(SLOT_INPUT, 1, false);
                    ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
                    if (output.isEmpty()) {
                        inventory.setStackInSlot(SLOT_OUTPUT, result.copy());
                    } else {
                        output.grow(result.getCount());
                        markDirty();
                    }
                }
            }
        } else if (progress != 0) {
            progress = 0;
            markDirty();
        }
        rate = working ? config.energyPerTick : 0;
        updateActive(working);
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemHandlerHelper.canItemStacksStack(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    @Override
    protected boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == SLOT_INPUT && !getResult(stack).isEmpty();
    }

    @Override
    public List<Slot> createSlots() {
        return Arrays.asList(
                new SlotItemHandler(inventory, SLOT_INPUT, 56, 35),
                new SlotItemHandler(inventory, SLOT_OUTPUT, 116, 35));
    }

    @Override
    public int getInputSlotCount() {
        return 1;
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
    protected int getProgressMax() {
        return config.ticksPerOperation;
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
