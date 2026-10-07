package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class TileCoalGenerator extends TileFuelGenerator {
    public TileCoalGenerator() {
        super(ModConfig.generator);
    }

    @Override
    protected int getFuelTime(ItemStack stack) {
        return TileEntityFurnace.getItemBurnTime(stack);
    }
}
