package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class TileElectricFurnace extends TileProcessor {
    public TileElectricFurnace() {
        super(ModConfig.electricFurnace);
    }

    @Override
    protected ItemStack getResult(ItemStack input) {
        return FurnaceRecipes.instance().getSmeltingResult(input).copy();
    }
}
