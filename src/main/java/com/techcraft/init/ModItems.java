package com.techcraft.init;

import com.techcraft.ModConfig;
import com.techcraft.block.BlockMachine;
import com.techcraft.item.ItemBase;
import com.techcraft.item.ItemElectricTool;
import com.techcraft.item.ItemEnergyBase;
import com.techcraft.item.ItemEnergyMeter;
import com.techcraft.item.ItemTechWrench;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public final class ModItems {
    /** Metals that get a plate; the ore dictionary name is "plate" + the name here. */
    public static final String[] PLATE_METALS = {"Iron", "Gold", "Copper", "Tin", "Lead", "Silver", "Bronze", "Steel"};

    public static final Item IRON_DUST = new ItemBase("iron_dust");
    public static final Item GOLD_DUST = new ItemBase("gold_dust");
    public static final Item CIRCUIT = new ItemBase("circuit");
    public static final Item MACHINE_FRAME = new ItemBase("machine_frame");

    public static final Item[] PLATES = new Item[PLATE_METALS.length];

    public static final ItemEnergyBase BATTERY_BASIC = new ItemEnergyBase("battery_basic",
            () -> ModConfig.equipment.batteryBasicCapacity, () -> ModConfig.equipment.batteryBasicTransfer);
    public static final ItemEnergyBase BATTERY_ADVANCED = new ItemEnergyBase("battery_advanced",
            () -> ModConfig.equipment.batteryAdvancedCapacity, () -> ModConfig.equipment.batteryAdvancedTransfer);
    public static final ItemEnergyBase BATTERY_ULTIMATE = new ItemEnergyBase("battery_ultimate",
            () -> ModConfig.equipment.batteryUltimateCapacity, () -> ModConfig.equipment.batteryUltimateTransfer);

    public static final Item ELECTRIC_DRILL = new ItemElectricTool("electric_drill",
            () -> ModConfig.equipment.toolCapacity, () -> ModConfig.equipment.toolTransfer,
            () -> ModConfig.equipment.drillEnergyPerBlock, () -> ModConfig.equipment.drillSpeed,
            new HashSet<>(Arrays.asList("pickaxe", "shovel")),
            new HashSet<>(Arrays.asList(Material.ROCK, Material.IRON, Material.ANVIL, Material.GROUND, Material.SAND,
                    Material.GRASS, Material.CLAY, Material.SNOW, Material.CRAFTED_SNOW)));
    public static final Item CHAINSAW = new ItemElectricTool("chainsaw",
            () -> ModConfig.equipment.toolCapacity, () -> ModConfig.equipment.toolTransfer,
            () -> ModConfig.equipment.chainsawEnergyPerBlock, () -> ModConfig.equipment.chainsawSpeed,
            new HashSet<>(Arrays.asList("axe")),
            new HashSet<>(Arrays.asList(Material.WOOD, Material.LEAVES, Material.PLANTS, Material.VINE)));
    public static final Item TECH_WRENCH = new ItemTechWrench("tech_wrench");
    public static final Item ENERGY_METER = new ItemEnergyMeter("energy_meter");

    /** Every item to register: plain items first, then the item form of every machine block. */
    public static final Item[] ALL;

    static {
        List<Item> all = new ArrayList<>(Arrays.asList(IRON_DUST, GOLD_DUST, CIRCUIT, MACHINE_FRAME));
        for (int i = 0; i < PLATE_METALS.length; i++) {
            PLATES[i] = new ItemBase("plate_" + PLATE_METALS[i].toLowerCase());
            all.add(PLATES[i]);
        }
        all.addAll(Arrays.asList(BATTERY_BASIC, BATTERY_ADVANCED, BATTERY_ULTIMATE,
                ELECTRIC_DRILL, CHAINSAW, TECH_WRENCH, ENERGY_METER));
        for (BlockMachine block : ModBlocks.MACHINES) {
            ItemBlock item = new ItemBlock(block);
            item.setRegistryName(block.getRegistryName());
            all.add(item);
        }
        ALL = all.toArray(new Item[0]);
    }

    public static Item itemOf(BlockMachine block) {
        return Item.getItemFromBlock(block);
    }

    private ModItems() {
    }
}
