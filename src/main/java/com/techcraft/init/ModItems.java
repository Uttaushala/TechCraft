package com.techcraft.init;

import com.techcraft.item.ItemBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public final class ModItems {
    public static final Item IRON_DUST = new ItemBase("iron_dust");
    public static final Item GOLD_DUST = new ItemBase("gold_dust");
    public static final Item CIRCUIT = new ItemBase("circuit");
    public static final Item MACHINE_FRAME = new ItemBase("machine_frame");

    public static final Item COAL_GENERATOR = itemBlock(ModBlocks.COAL_GENERATOR);
    public static final Item ELECTRIC_FURNACE = itemBlock(ModBlocks.ELECTRIC_FURNACE);
    public static final Item CRUSHER = itemBlock(ModBlocks.CRUSHER);
    public static final Item BATTERY_BOX = itemBlock(ModBlocks.BATTERY_BOX);

    public static final Item[] ALL = {
            IRON_DUST, GOLD_DUST, CIRCUIT, MACHINE_FRAME,
            COAL_GENERATOR, ELECTRIC_FURNACE, CRUSHER, BATTERY_BOX
    };

    private static Item itemBlock(net.minecraft.block.Block block) {
        Item item = new ItemBlock(block);
        item.setRegistryName(block.getRegistryName());
        return item;
    }

    private ModItems() {
    }
}
