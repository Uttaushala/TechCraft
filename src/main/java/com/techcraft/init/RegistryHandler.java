package com.techcraft.init;

import com.techcraft.TechCraft;
import com.techcraft.tile.TileBatteryBox;
import com.techcraft.tile.TileCoalGenerator;
import com.techcraft.tile.TileCrusher;
import com.techcraft.tile.TileElectricFurnace;
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
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ModItems.ALL);
    }

    private RegistryHandler() {
    }
}
