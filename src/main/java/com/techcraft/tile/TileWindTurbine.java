package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.util.math.MathHelper;

public class TileWindTurbine extends TileSimpleGenerator {
    public TileWindTurbine() {
        super(ModConfig.windTurbine.maxOutput * 400, ModConfig.windTurbine.maxOutput * 6);
    }

    @Override
    protected int computeOutput() {
        if (!world.provider.hasSkyLight() || !world.canSeeSky(pos.up())) {
            return 0;
        }
        float height = MathHelper.clamp((pos.getY() - 50) / 120.0F, 0.0F, 1.0F);
        float weather = world.isThundering() ? 1.6F : world.isRaining() ? 1.3F : 1.0F;
        return Math.round(ModConfig.windTurbine.maxOutput * height * weather);
    }
}
