package com.techcraft.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Inventory view for hoppers/pipes: insert only into input slots, extract only from output slots
 * (and leftovers in input slots, e.g. empty buckets from lava fuel). Either direction can be switched off per face.
 */
public class AutomationItemHandler implements IItemHandler {
    private final IItemHandlerModifiable inventory;
    private final int inputSlots;
    private final boolean allowInsert;
    private final boolean allowExtract;

    public AutomationItemHandler(IItemHandlerModifiable inventory, int inputSlots, boolean allowInsert, boolean allowExtract) {
        this.inventory = inventory;
        this.inputSlots = inputSlots;
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
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
        return allowInsert && slot < inputSlots ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowExtract || (slot < inputSlots && inventory.isItemValid(slot, inventory.getStackInSlot(slot)))) {
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
        return allowInsert && slot < inputSlots && inventory.isItemValid(slot, stack);
    }
}
