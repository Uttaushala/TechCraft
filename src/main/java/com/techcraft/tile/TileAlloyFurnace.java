package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.recipe.AlloyRecipes;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Arrays;
import java.util.List;

/** Two inputs (in either order) become one alloy. */
public class TileAlloyFurnace extends TileMachineBase {
    public static final int SLOT_A = 0;
    public static final int SLOT_B = 1;
    public static final int SLOT_OUTPUT = 2;

    private int progress;

    public TileAlloyFurnace() {
        super(3, 2, ModConfig.alloyFurnace.capacity, ModConfig.alloyFurnace.capacity, 0);
    }

    @Override
    protected void tickServer() {
        ModConfig.Processor config = ModConfig.alloyFurnace;
        AlloyRecipes.Match match = AlloyRecipes.find(inventory.getStackInSlot(SLOT_A), inventory.getStackInSlot(SLOT_B));

        boolean working = false;
        if (match != null && canOutput(match.recipe.output)) {
            if (energy.consume(config.energyPerTick)) {
                working = true;
                progress++;
                if (progress >= config.ticksPerOperation) {
                    progress = 0;
                    inventory.extractItem(match.slotA, match.recipe.countA, false);
                    inventory.extractItem(match.slotB, match.recipe.countB, false);
                    ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
                    if (output.isEmpty()) {
                        inventory.setStackInSlot(SLOT_OUTPUT, match.recipe.output.copy());
                    } else {
                        output.grow(match.recipe.output.getCount());
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
        return slot < SLOT_OUTPUT && AlloyRecipes.isIngredient(stack);
    }

    @Override
    public List<Slot> createSlots() {
        return Arrays.asList(
                new SlotItemHandler(inventory, SLOT_A, 44, 25),
                new SlotItemHandler(inventory, SLOT_B, 44, 45),
                new SlotItemHandler(inventory, SLOT_OUTPUT, 116, 35));
    }

    @Override
    public int getInputSlotCount() {
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
    protected int getProgressMax() {
        return ModConfig.alloyFurnace.ticksPerOperation;
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
