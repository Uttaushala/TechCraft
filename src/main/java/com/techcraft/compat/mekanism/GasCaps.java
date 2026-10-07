package com.techcraft.compat.mekanism;

import mekanism.api.gas.IGasHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/** Filled in by Forge once Mekanism registers its gas capability. Never loaded when Mekanism is absent. */
public final class GasCaps {
    @CapabilityInject(IGasHandler.class)
    public static Capability<IGasHandler> GAS_HANDLER = null;

    private GasCaps() {
    }
}
