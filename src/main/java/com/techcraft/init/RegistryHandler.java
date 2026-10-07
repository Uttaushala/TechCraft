package com.techcraft.init;

import com.techcraft.TechCraft;
import com.techcraft.tile.TileAlloyFurnace;
import com.techcraft.tile.TileAutoMiner;
import com.techcraft.tile.TileBiomassGenerator;
import com.techcraft.tile.TileBlockBreaker;
import com.techcraft.tile.TileBlockPlacer;
import com.techcraft.tile.TileBatteryBox;
import com.techcraft.tile.TileCharger;
import com.techcraft.tile.TileCoalGenerator;
import com.techcraft.tile.TileCompressor;
import com.techcraft.tile.TileCrusher;
import com.techcraft.tile.TileElectricFurnace;
import com.techcraft.tile.TileEuToFe;
import com.techcraft.tile.TileFeToEu;
import com.techcraft.tile.TileFluidTank;
import com.techcraft.tile.TileGeothermalGenerator;
import com.techcraft.tile.TileLavaGenerator;
import com.techcraft.tile.TileMobGrinder;
import com.techcraft.tile.TilePump;
import com.techcraft.tile.TileSolarPanel;
import com.techcraft.tile.TileWaterWheel;
import com.techcraft.tile.TileWindTurbine;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = TechCraft.MODID)
public final class RegistryHandler {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(ModBlocks.MACHINES);

        GameRegistry.registerTileEntity(TileCoalGenerator.class, new ResourceLocation(TechCraft.MODID, "coal_generator"));
        GameRegistry.registerTileEntity(TileElectricFurnace.class, new ResourceLocation(TechCraft.MODID, "electric_furnace"));
        GameRegistry.registerTileEntity(TileCrusher.class, new ResourceLocation(TechCraft.MODID, "crusher"));
        GameRegistry.registerTileEntity(TileBatteryBox.class, new ResourceLocation(TechCraft.MODID, "battery_box"));
        GameRegistry.registerTileEntity(TileBatteryBox.Advanced.class, new ResourceLocation(TechCraft.MODID, "battery_box_advanced"));
        GameRegistry.registerTileEntity(TileBatteryBox.Ultimate.class, new ResourceLocation(TechCraft.MODID, "battery_box_ultimate"));
        GameRegistry.registerTileEntity(TileCompressor.class, new ResourceLocation(TechCraft.MODID, "compressor"));
        GameRegistry.registerTileEntity(TileAlloyFurnace.class, new ResourceLocation(TechCraft.MODID, "alloy_furnace"));
        GameRegistry.registerTileEntity(TileCharger.class, new ResourceLocation(TechCraft.MODID, "charger"));
        GameRegistry.registerTileEntity(TileLavaGenerator.class, new ResourceLocation(TechCraft.MODID, "lava_generator"));
        GameRegistry.registerTileEntity(TileFluidTank.class, new ResourceLocation(TechCraft.MODID, "fluid_tank"));
        GameRegistry.registerTileEntity(TileWindTurbine.class, new ResourceLocation(TechCraft.MODID, "wind_turbine"));
        GameRegistry.registerTileEntity(TileSolarPanel.Basic.class, new ResourceLocation(TechCraft.MODID, "solar_panel"));
        GameRegistry.registerTileEntity(TileSolarPanel.Advanced.class, new ResourceLocation(TechCraft.MODID, "solar_panel_advanced"));
        GameRegistry.registerTileEntity(TileSolarPanel.Ultimate.class, new ResourceLocation(TechCraft.MODID, "solar_panel_ultimate"));
        GameRegistry.registerTileEntity(TileEuToFe.class, new ResourceLocation(TechCraft.MODID, "eu_to_fe_converter"));
        GameRegistry.registerTileEntity(TileFeToEu.class, new ResourceLocation(TechCraft.MODID, "fe_to_eu_converter"));
        GameRegistry.registerTileEntity(TilePump.class, new ResourceLocation(TechCraft.MODID, "pump"));
        GameRegistry.registerTileEntity(TileGeothermalGenerator.class, new ResourceLocation(TechCraft.MODID, "geothermal_generator"));
        GameRegistry.registerTileEntity(TileWaterWheel.class, new ResourceLocation(TechCraft.MODID, "water_wheel"));
        GameRegistry.registerTileEntity(TileBiomassGenerator.class, new ResourceLocation(TechCraft.MODID, "biomass_generator"));
        GameRegistry.registerTileEntity(TileAutoMiner.class, new ResourceLocation(TechCraft.MODID, "auto_miner"));
        GameRegistry.registerTileEntity(TileBlockBreaker.class, new ResourceLocation(TechCraft.MODID, "block_breaker"));
        GameRegistry.registerTileEntity(TileBlockPlacer.class, new ResourceLocation(TechCraft.MODID, "block_placer"));
        GameRegistry.registerTileEntity(TileMobGrinder.class, new ResourceLocation(TechCraft.MODID, "mob_grinder"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ModItems.ALL);
    }

    private RegistryHandler() {
    }
}
