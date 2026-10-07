package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Burns plant matter: crops, seeds, saplings, leaves, and food that isn't meat. */
public class TileBiomassGenerator extends TileFuelGenerator {
    public TileBiomassGenerator() {
        super(ModConfig.biomass);
    }

    @Override
    protected int getFuelTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if (name.startsWith("crop")) {
                return 300;
            }
            if (name.startsWith("seed") || name.equals("treeSapling")) {
                return 150;
            }
            if (name.equals("sugarcane")) {
                return 200;
            }
            if (name.equals("treeLeaves")) {
                return 50;
            }
        }
        if (stack.getItem() instanceof ItemFood) {
            ItemFood food = (ItemFood) stack.getItem();
            return food.isWolfsFavoriteMeat() ? 0 : food.getHealAmount(stack) * 80;
        }
        return stack.getItem() instanceof ItemSeeds ? 150 : 0;
    }
}
