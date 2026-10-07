package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

/** Charges anything with the Forge energy capability: our batteries and tools and other mods' items too. */
public class TileCharger extends TileMachineBase {
    private static final int SLOTS = 4;

    public TileCharger() {
        super(SLOTS, SLOTS, ModConfig.charger.capacity, ModConfig.charger.capacity, 0);
    }

    @Override
    protected void tickServer() {
        int total = 0;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (storage == null || !storage.canReceive()) {
                continue;
            }
            int available = Math.min(ModConfig.charger.transferRate, energy.getEnergyStored());
            if (available <= 0) {
                break;
            }
            int accepted = storage.receiveEnergy(available, false);
            if (accepted > 0) {
                energy.consume(accepted);
                total += accepted;
            }
        }
        if (total > 0) {
            markDirty();
        }
        rate = total;
        updateActive(total > 0);
    }

    /** Only items that can still take energy go in; full ones can then be pulled out by hoppers and pipes. */
    @Override
    protected boolean isItemValidForSlot(int slot, ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        return storage != null && storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored();
    }

    @Override
    public List<Slot> createSlots() {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            slots.add(new SlotItemHandler(inventory, i, 62 + (i % 2) * 18, 26 + (i / 2) * 18));
        }
        return slots;
    }

    @Override
    public int getInputSlotCount() {
        return SLOTS;
    }

    @Override
    public int getRateSign() {
        return -1;
    }
}
