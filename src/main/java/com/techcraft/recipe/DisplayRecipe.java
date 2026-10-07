package com.techcraft.recipe;

import net.minecraft.item.ItemStack;

import java.util.List;

/** A machine recipe as shown in recipe viewers: for each input slot, the stacks that are accepted there. */
public final class DisplayRecipe {
    public final List<List<ItemStack>> inputs;
    public final ItemStack output;

    public DisplayRecipe(List<List<ItemStack>> inputs, ItemStack output) {
        this.inputs = inputs;
        this.output = output;
    }
}
