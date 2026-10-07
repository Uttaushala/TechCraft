package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Collections;
import java.util.List;

public class TileCoalGenerator extends TileMachineBase {
    private int burnTime;
    private int burnTimeTotal;

    public TileCoalGenerator() {
        super(1, 1, ModConfig.generator.capacity, 0, ModConfig.generator.maxOutput);
    }

    @Override
    protected void tickServer() {
        boolean full = energy.getEnergyStored() >= energy.getMaxEnergyStored();

        if (burnTime <= 0 && !full) {
            ItemStack fuel = inventory.getStackInSlot(0);
            int fuelTime = TileEntityFurnace.getItemBurnTime(fuel);
            if (fuelTime > 0) {
                burnTime = burnTimeTotal = Math.max(1, (int) (fuelTime * ModConfig.generator.burnTimeMultiplier));
                ItemStack container = fuel.getItem().getContainerItem(fuel);
                if (fuel.getCount() == 1 && !container.isEmpty()) {
                    inventory.setStackInSlot(0, container);
                } else {
                    inventory.extractItem(0, 1, false);
                }
            }
        }

        boolean burning = false;
        if (burnTime > 0 && !full) {
            burnTime--;
            energy.generate(ModConfig.generator.energyPerTick);
            burning = true;
        }
        rate = burning ? ModConfig.generator.energyPerTick : 0;
        updateActive(burning);

        pushEnergy(ModConfig.generator.maxOutput);
    }

    @Override
    protected boolean isItemValidForSlot(int slot, ItemStack stack) {
        return TileEntityFurnace.isItemFuel(stack);
    }

    @Override
    protected boolean defaultEnergyInput(EnumFacing side) {
        return false;
    }

    @Override
    protected boolean defaultEnergyOutput(EnumFacing side) {
        return true;
    }

    @Override
    public List<Slot> createSlots() {
        return Collections.singletonList(new SlotItemHandler(inventory, 0, 80, 53));
    }

    @Override
    public int getInputSlotCount() {
        return 1;
    }

    @Override
    public int getRateSign() {
        return 1;
    }

    @Override
    protected int getProgress() {
        return burnTime;
    }

    @Override
    protected int getProgressMax() {
        return burnTimeTotal;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("BurnTime", burnTime);
        tag.setInteger("BurnTimeTotal", burnTimeTotal);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        burnTime = tag.getInteger("BurnTime");
        burnTimeTotal = tag.getInteger("BurnTimeTotal");
    }
}
