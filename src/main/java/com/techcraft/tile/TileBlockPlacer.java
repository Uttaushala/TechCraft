package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

/** Places blocks from its inventory into the space in front of it. */
public class TileBlockPlacer extends TileGridMachine {
    public TileBlockPlacer() {
        super(ModConfig.blockPlacer.capacity, 9);
    }

    @Override
    protected boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

    @Override
    protected void tickServer() {
        ModConfig.Placer config = ModConfig.blockPlacer;
        EnumFacing facing = getFacing();
        BlockPos target = pos.offset(facing);
        boolean working = false;
        int cost = Math.max(1, (int) Math.round((double) config.energyPerBlock / config.ticksPerBlock * energyFactor()));

        int slot = firstBlockSlot();
        if (slot >= 0 && world.isBlockLoaded(target)
                && world.getBlockState(target).getBlock().isReplaceable(world, target)) {
            if (energy.consume(cost)) {
                working = true;
                progress += (int) Math.round(SCALE * speedFactor());
                if (progress >= config.ticksPerBlock * SCALE) {
                    progress = 0;
                    place(slot, target, facing);
                }
            }
        } else if (progress != 0) {
            progress = 0;
        }
        rate = working ? cost : 0;
        updateActive(working);
    }

    private int firstBlockSlot() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1;
    }

    private void place(int slot, BlockPos target, EnumFacing facing) {
        ItemStack stack = inventory.getStackInSlot(slot);
        ItemBlock item = (ItemBlock) stack.getItem();
        Block block = item.getBlock();
        FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer) world);
        if (!world.mayPlace(block, target, false, facing, null)) {
            return;
        }
        IBlockState state = block.getStateForPlacement(world, target, facing, 0.5F, 0.5F, 0.5F,
                item.getMetadata(stack.getMetadata()), player, EnumHand.MAIN_HAND);
        if (item.placeBlockAt(stack, player, world, target, facing, 0.5F, 0.5F, 0.5F, state)) {
            inventory.extractItem(slot, 1, false);
        }
    }

    @Override
    protected int getProgressMax() {
        return ModConfig.blockPlacer.ticksPerBlock * SCALE;
    }
}
