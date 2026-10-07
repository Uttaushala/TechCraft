package com.techcraft.tile;

import com.techcraft.ModConfig;

public abstract class TileSolarPanel extends TileSimpleGenerator {
    private final int output;

    protected TileSolarPanel(int output) {
        super(output * 400, output * 4);
        this.output = output;
    }

    @Override
    protected int computeOutput() {
        if (!world.provider.hasSkyLight() || !world.canSeeSky(pos.up()) || !world.isDaytime()) {
            return 0;
        }
        return world.isRaining() ? Math.max(1, output / 2) : output;
    }

    public static class Basic extends TileSolarPanel {
        public Basic() {
            super(ModConfig.solar.basic);
        }
    }

    public static class Advanced extends TileSolarPanel {
        public Advanced() {
            super(ModConfig.solar.advanced);
        }
    }

    public static class Ultimate extends TileSolarPanel {
        public Ultimate() {
            super(ModConfig.solar.ultimate);
        }
    }
}
