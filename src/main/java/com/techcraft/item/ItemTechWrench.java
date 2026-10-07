package com.techcraft.item;

import com.techcraft.block.BlockMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Click to rotate, sneak-click to pick a TechCraft machine up. Also rotates other mods' blocks that allow it. */
public class ItemTechWrench extends ItemBase {
    public ItemTechWrench(String name) {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockMachine) {
            if (!world.isRemote) {
                if (player.isSneaking()) {
                    world.destroyBlock(pos, true);
                } else {
                    world.setBlockState(pos, state.withProperty(BlockMachine.FACING, state.getValue(BlockMachine.FACING).rotateY()));
                }
            }
            return EnumActionResult.SUCCESS;
        }
        if (!world.isRemote && state.getBlock().rotateBlock(world, pos, facing)) {
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
}
