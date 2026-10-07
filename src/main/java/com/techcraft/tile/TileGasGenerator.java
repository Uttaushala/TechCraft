package com.techcraft.tile;

import com.techcraft.ModConfig;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.util.EnumFacing;

import java.util.HashMap;
import java.util.Map;

/** Burns Mekanism gases (hydrogen and ethene by default) for energy. Only used when Mekanism is installed. */
public class TileGasGenerator extends TileGasTank {
    private final Map<String, Integer> fuels = new HashMap<>();

    public TileGasGenerator() {
        super(ModConfig.gas.generatorTankCapacity, ModConfig.gas.generatorCapacity, ModConfig.gas.generatorMaxOutput);
        for (String entry : ModConfig.gas.fuels) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    fuels.put(parts[0].trim().toLowerCase(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {
                    // A malformed config line just means that gas isn't a fuel.
                }
            }
        }
    }

    @Override
    protected boolean hasEnergy() {
        return true;
    }

    private int energyPerMb(Gas type) {
        Integer value = type == null ? null : fuels.get(type.getName().toLowerCase());
        return value == null ? 0 : value;
    }

    @Override
    protected boolean isGasValid(Gas type) {
        return energyPerMb(type) > 0;
    }

    @Override
    protected boolean defaultGasOutput(EnumFacing side) {
        return false;
    }

    @Override
    protected boolean defaultEnergyInput(EnumFacing side) {
        return false;
    }

    @Override
    protected boolean defaultEnergyOutput(EnumFacing side) {
        return true;
    }

    @Override
    public boolean isBigDisplay() {
        return false;
    }

    @Override
    public int getRateSign() {
        return 1;
    }

    @Override
    protected void tickServer() {
        ModConfig.GasSettings config = ModConfig.gas;
        int produced = 0;
        GasStack stored = gas.getGas();
        if (stored != null && energy.getEnergyStored() < energy.getMaxEnergyStored()) {
            int perMb = energyPerMb(stored.getGas());
            GasStack burned = perMb > 0 ? gas.draw(config.mbPerTick, true) : null;
            if (burned != null) {
                produced = burned.amount * perMb;
                energy.generate(produced);
                markDirty();
            }
        }
        rate = produced;
        updateActive(produced > 0);
        pushEnergy(config.generatorMaxOutput);
        super.tickServer();
    }
}
