package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

/** Produces energy from flowing water on its four sides, like a waterfall running past it. */
public class TileWaterWheel extends TileSimpleGenerator {
    public TileWaterWheel() {
        super(ModConfig.waterWheel.perWater * 4 * 400, ModConfig.waterWheel.perWater * 4 * 3);
    }

    @Override
    protected int computeOutput() {
        int flowing = 0;
        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            IBlockState state = world.getBlockState(pos.offset(side));
            if (state.getMaterial() == Material.WATER && state.getBlock() instanceof BlockLiquid
                    && state.getValue(BlockLiquid.LEVEL) > 0) {
                flowing++;
            }
        }
        return flowing * ModConfig.waterWheel.perWater;
    }
}
