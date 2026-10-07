package com.techcraft.recipe;

import com.techcraft.TechCraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Every "ingotX" becomes the "plateX" of whichever mod provides one (ours, Thermal, Immersive Engineering...). */
public final class CompressorRecipes {
    private static final Map<String, ItemStack> ORE_RECIPES = new HashMap<>();

    public static void init() {
        ORE_RECIPES.clear();
        for (String name : OreDictionary.getOreNames()) {
            if (!name.startsWith("ingot") || name.length() <= 5) {
                continue;
            }
            List<ItemStack> plates = OreDictionary.getOres("plate" + name.substring(5), false);
            if (!plates.isEmpty() && !OreDictionary.getOres(name, false).isEmpty()) {
                ItemStack output = plates.get(0).copy();
                if (output.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    output.setItemDamage(0);
                }
                output.setCount(1);
                ORE_RECIPES.put(name, output);
            }
        }
        TechCraft.LOGGER.info("Registered {} compressor ore dictionary recipes", ORE_RECIPES.size());
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, ItemStack> entry : ORE_RECIPES.entrySet()) {
            List<ItemStack> inputs = OreDictionary.getOres(entry.getKey(), false);
            if (!inputs.isEmpty()) {
                recipes.add(new DisplayRecipe(Collections.singletonList(new ArrayList<>(inputs)), entry.getValue().copy()));
            }
        }
        return recipes;
    }

    public static ItemStack getResult(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int id : OreDictionary.getOreIDs(input)) {
            ItemStack output = ORE_RECIPES.get(OreDictionary.getOreName(id));
            if (output != null) {
                return output.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private CompressorRecipes() {
    }
}
