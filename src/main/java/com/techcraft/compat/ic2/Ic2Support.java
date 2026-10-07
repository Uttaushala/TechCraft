package com.techcraft.compat.ic2;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

/** The only place that touches IC2's energy net events; every call must be guarded by {@link #isLoaded()}. */
public final class Ic2Support {
    public static boolean isLoaded() {
        return Loader.isModLoaded("ic2");
    }

    @Optional.Method(modid = "ic2")
    public static void join(Object tile) {
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
    }

    @Optional.Method(modid = "ic2")
    public static void leave(Object tile) {
        MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile));
    }

    /** Max EU per packet for an IC2 tier. */
    public static double packetSize(int tier) {
        return 8.0 * Math.pow(4.0, tier);
    }

    private Ic2Support() {
    }
}
