package com.techcraft.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Inventory view for hoppers/pipes: insert only into input slots, extract only from output slots
 * (and leftovers in input slots, e.g. empty buckets from lava fuel).
 */
public class AutomationItemHandler implements IItemHandler {
    private final IItemHandlerModifiable inventory;
    private final int inputSlots;

    public AutomationItemHandler(IItemHandlerModifiable inventory, int inputSlots) {
        this.inventory = inventory;
        this.inputSlots = inputSlots;
    }

    @Override
    public int getSlots() {
        return inventory.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return slot < inputSlots ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < inputSlots && inventory.isItemValid(slot, inventory.getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return inventory.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot < inputSlots && inventory.isItemValid(slot, stack);
    }
}
