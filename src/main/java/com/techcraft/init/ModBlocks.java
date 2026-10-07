package com.techcraft.init;

import com.techcraft.block.BlockMachine;
import com.techcraft.tile.TileBatteryBox;
import com.techcraft.tile.TileCoalGenerator;
import com.techcraft.tile.TileCrusher;
import com.techcraft.tile.TileElectricFurnace;

public final class ModBlocks {
    public static final BlockMachine COAL_GENERATOR = new BlockMachine("coal_generator", TileCoalGenerator::new);
    public static final BlockMachine ELECTRIC_FURNACE = new BlockMachine("electric_furnace", TileElectricFurnace::new);
    public static final BlockMachine CRUSHER = new BlockMachine("crusher", TileCrusher::new);
    public static final BlockMachine BATTERY_BOX = new BlockMachine("battery_box", TileBatteryBox::new);

    public static final BlockMachine[] MACHINES = {COAL_GENERATOR, ELECTRIC_FURNACE, CRUSHER, BATTERY_BOX};

    private ModBlocks() {
    }
}
