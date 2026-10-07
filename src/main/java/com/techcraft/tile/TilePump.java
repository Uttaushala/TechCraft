package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Drains the liquid body below it a bucket at a time and pushes the fluid out of every face except the bottom. */
public class TilePump extends TileFluidBase {
    private static final int SCALE = 4;
    private static final int MAX_SEARCH = 600;
    private static final int SEARCH_RADIUS = 16;

    private int progress;
    @Nullable
    private BlockPos target;

    public TilePump() {
        super(0, 0, ModConfig.pump.capacity, ModConfig.pump.capacity, 0, ModConfig.pump.tankCapacity);
    }

    @Override
    protected void tickServer() {
        ModConfig.Pump config = ModConfig.pump;
        boolean working = false;
        int cost = Math.max(1, (int) Math.round(config.energyPerTick * energyFactor()));
        if (tank.getCapacity() - tank.getFluidAmount() >= Fluid.BUCKET_VOLUME) {
            if (target == null && world.getTotalWorldTime() % 20 == 0) {
                target = findSource();
            }
            if (target != null && energy.consume(cost)) {
                working = true;
                progress += (int) Math.round(SCALE * speedFactor());
                if (progress >= config.ticksPerBucket * SCALE) {
                    progress = 0;
                    pumpBucket();
                }
            }
        }
        rate = working ? cost : 0;
        updateActive(working);
        syncFluid();
    }

    private void pumpBucket() {
        IFluidHandler source = target == null ? null : FluidUtil.getFluidHandler(world, target, EnumFacing.UP);
        FluidStack simulated = source == null ? null : source.drain(Fluid.BUCKET_VOLUME, false);
        if (simulated == null || simulated.amount < Fluid.BUCKET_VOLUME || tank.fill(simulated, false) < Fluid.BUCKET_VOLUME) {
            target = null;
            return;
        }
        if (ModConfig.pump.infiniteWater && simulated.getFluid() == FluidRegistry.WATER) {
            tank.fill(simulated, true);
        } else {
            FluidStack drained = source.drain(Fluid.BUCKET_VOLUME, true);
            if (drained != null) {
                tank.fill(drained, true);
            }
            target = null;
        }
    }

    private static boolean isLiquid(IBlockState state) {
        return state.getMaterial().isLiquid() || state.getBlock() instanceof IFluidBlock;
    }

    /** Walks down to the first liquid, then looks through the connected body for the highest source block. */
    @Nullable
    private BlockPos findSource() {
        BlockPos start = null;
        for (int i = 1; i <= ModConfig.pump.range; i++) {
            BlockPos p = pos.down(i);
            if (!world.isBlockLoaded(p)) {
                return null;
            }
            IBlockState state = world.getBlockState(p);
            if (isLiquid(state)) {
                start = p;
                break;
            }
            if (!state.getBlock().isAir(state, world, p)) {
                break;
            }
        }
        if (start == null) {
            return null;
        }
        Fluid fluid = FluidRegistry.lookupFluidForBlock(world.getBlockState(start).getBlock());
        if (fluid == null) {
            return null;
        }

        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(start);
        seen.add(start);
        BlockPos best = null;
        while (!queue.isEmpty() && seen.size() < MAX_SEARCH) {
            BlockPos p = queue.poll();
            if (FluidRegistry.lookupFluidForBlock(world.getBlockState(p).getBlock()) != fluid) {
                continue;
            }
            IFluidHandler handler = FluidUtil.getFluidHandler(world, p, EnumFacing.UP);
            FluidStack content = handler == null ? null : handler.drain(Fluid.BUCKET_VOLUME, false);
            if (content != null && content.amount >= Fluid.BUCKET_VOLUME && (best == null || p.getY() > best.getY())) {
                best = p;
            }
            for (EnumFacing side : EnumFacing.VALUES) {
                BlockPos next = p.offset(side);
                if (Math.abs(next.getX() - pos.getX()) <= SEARCH_RADIUS && Math.abs(next.getZ() - pos.getZ()) <= SEARCH_RADIUS
                        && world.isBlockLoaded(next) && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return best;
    }

    @Override
    protected boolean defaultFluidInput(EnumFacing side) {
        return false;
    }

    @Override
    protected boolean defaultFluidEject(EnumFacing side) {
        return side != EnumFacing.DOWN;
    }

    @Override
    public List<Slot> createSlots() {
        return Collections.emptyList();
    }

    @Override
    public int getInputSlotCount() {
        return 0;
    }

    @Override
    public int getUpgradeSlotCount() {
        return 2;
    }

    @Override
    public int getRateSign() {
        return -1;
    }

    @Override
    protected int getProgress() {
        return progress;
    }

    @Override
    protected int getProgressMax() {
        return ModConfig.pump.ticksPerBucket * SCALE;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Progress", progress);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        progress = tag.getInteger("Progress");
    }
}
