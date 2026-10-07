package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;

/** Produces energy from every lava block touching it on a side or below. */
public class TileGeothermalGenerator extends TileSimpleGenerator {
    public TileGeothermalGenerator() {
        super(ModConfig.geothermal.perLava * 5 * 400, ModConfig.geothermal.perLava * 5 * 3);
    }

    @Override
    protected int computeOutput() {
        int lava = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side != EnumFacing.UP && world.getBlockState(pos.offset(side)).getMaterial() == Material.LAVA) {
                lava++;
            }
        }
        return lava * ModConfig.geothermal.perLava;
    }
}
