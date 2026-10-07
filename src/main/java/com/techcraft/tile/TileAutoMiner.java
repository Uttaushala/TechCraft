package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/** Digs out a square column below itself one layer at a time, from just under the machine down to Y 1. */
public class TileAutoMiner extends TileGridMachine {
    private static final int SCAN_LIMIT = 64;

    private int layer = Integer.MIN_VALUE;
    private int cursor;
    private boolean finished;

    public TileAutoMiner() {
        super(ModConfig.miner.capacity, 0);
    }

    private BlockPos current() {
        int radius = ModConfig.miner.radius;
        int size = 2 * radius + 1;
        return new BlockPos(pos.getX() - radius + cursor % size, layer, pos.getZ() - radius + cursor / size);
    }

    private void advance() {
        int size = 2 * ModConfig.miner.radius + 1;
        if (++cursor >= size * size) {
            cursor = 0;
            layer--;
        }
    }

    @Override
    protected void tickServer() {
        ModConfig.Miner config = ModConfig.miner;
        if (layer == Integer.MIN_VALUE) {
            layer = pos.getY() - 1;
        }
        boolean working = false;
        int cost = Math.max(1, (int) Math.round((double) config.energyPerBlock / config.ticksPerBlock * energyFactor()));

        if (!finished && hasFreeSlot()) {
            BlockPos target = null;
            for (int i = 0; i < SCAN_LIMIT; i++) {
                if (layer < 1) {
                    finished = true;
                    break;
                }
                BlockPos p = current();
                if (!world.isBlockLoaded(p)) {
                    break;
                }
                if (isMinable(p, config.mineTileEntities)) {
                    target = p;
                    break;
                }
                advance();
            }
            if (target != null && energy.consume(cost)) {
                working = true;
                progress += (int) Math.round(SCALE * speedFactor());
                if (progress >= config.ticksPerBlock * SCALE) {
                    progress = 0;
                    mine(target);
                    advance();
                }
            }
        }
        rate = working ? cost : 0;
        updateActive(working);
    }

    @Override
    protected int getProgressMax() {
        return ModConfig.miner.ticksPerBlock * SCALE;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Layer", layer);
        tag.setInteger("Cursor", cursor);
        tag.setBoolean("Finished", finished);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        layer = tag.hasKey("Layer") ? tag.getInteger("Layer") : Integer.MIN_VALUE;
        cursor = tag.getInteger("Cursor");
        finished = tag.getBoolean("Finished");
    }
}
