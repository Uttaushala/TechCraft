package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.util.math.BlockPos;

/** Breaks the block in front of it and stores the drops. */
public class TileBlockBreaker extends TileGridMachine {
    public TileBlockBreaker() {
        super(ModConfig.blockBreaker.capacity, 0);
    }

    @Override
    protected void tickServer() {
        ModConfig.Breaker config = ModConfig.blockBreaker;
        BlockPos target = pos.offset(getFacing());
        boolean working = false;
        int cost = Math.max(1, (int) Math.round((double) config.energyPerBlock / config.ticksPerBlock * energyFactor()));

        if (hasFreeSlot() && world.isBlockLoaded(target) && isMinable(target, config.mineTileEntities)) {
            if (energy.consume(cost)) {
                working = true;
                progress += (int) Math.round(SCALE * speedFactor());
                if (progress >= config.ticksPerBlock * SCALE) {
                    progress = 0;
                    mine(target);
                }
            }
        } else if (progress != 0) {
            progress = 0;
        }
        rate = working ? cost : 0;
        updateActive(working);
    }

    @Override
    protected int getProgressMax() {
        return ModConfig.blockBreaker.ticksPerBlock * SCALE;
    }
}
