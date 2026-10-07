package com.techcraft.init;

import com.techcraft.block.BlockMachine;
import com.techcraft.tile.TileAlloyFurnace;
import com.techcraft.tile.TileBatteryBox;
import com.techcraft.tile.TileCharger;
import com.techcraft.tile.TileCoalGenerator;
import com.techcraft.tile.TileCompressor;
import com.techcraft.tile.TileCrusher;
import com.techcraft.tile.TileElectricFurnace;
import com.techcraft.tile.TileFluidTank;
import com.techcraft.tile.TileLavaGenerator;
import com.techcraft.tile.TileSolarPanel;
import com.techcraft.tile.TileWindTurbine;

public final class ModBlocks {
    public static final BlockMachine COAL_GENERATOR = new BlockMachine("coal_generator", TileCoalGenerator::new);
    public static final BlockMachine ELECTRIC_FURNACE = new BlockMachine("electric_furnace", TileElectricFurnace::new);
    public static final BlockMachine CRUSHER = new BlockMachine("crusher", TileCrusher::new);
    public static final BlockMachine BATTERY_BOX = new BlockMachine("battery_box", TileBatteryBox::new);

    public static final BlockMachine COMPRESSOR = new BlockMachine("compressor", TileCompressor::new);
    public static final BlockMachine ALLOY_FURNACE = new BlockMachine("alloy_furnace", TileAlloyFurnace::new);
    public static final BlockMachine CHARGER = new BlockMachine("charger", TileCharger::new);
    public static final BlockMachine LAVA_GENERATOR = new BlockMachine("lava_generator", TileLavaGenerator::new);
    public static final BlockMachine FLUID_TANK = new BlockMachine("fluid_tank", TileFluidTank::new);
    public static final BlockMachine WIND_TURBINE = new BlockMachine("wind_turbine", TileWindTurbine::new).noActiveLight();
    public static final BlockMachine SOLAR_PANEL = new BlockMachine("solar_panel", TileSolarPanel.Basic::new).noActiveLight();
    public static final BlockMachine SOLAR_PANEL_ADVANCED =
            new BlockMachine("solar_panel_advanced", TileSolarPanel.Advanced::new).noActiveLight();
    public static final BlockMachine SOLAR_PANEL_ULTIMATE =
            new BlockMachine("solar_panel_ultimate", TileSolarPanel.Ultimate::new).noActiveLight();
    public static final BlockMachine BATTERY_BOX_ADVANCED =
            new BlockMachine("battery_box_advanced", TileBatteryBox.Advanced::new);
    public static final BlockMachine BATTERY_BOX_ULTIMATE =
            new BlockMachine("battery_box_ultimate", TileBatteryBox.Ultimate::new);

    public static final BlockMachine[] MACHINES = {
            COAL_GENERATOR, ELECTRIC_FURNACE, CRUSHER, BATTERY_BOX,
            COMPRESSOR, ALLOY_FURNACE, CHARGER, LAVA_GENERATOR, FLUID_TANK, WIND_TURBINE,
            SOLAR_PANEL, SOLAR_PANEL_ADVANCED, SOLAR_PANEL_ULTIMATE, BATTERY_BOX_ADVANCED, BATTERY_BOX_ULTIMATE
    };

    private ModBlocks() {
    }
}
