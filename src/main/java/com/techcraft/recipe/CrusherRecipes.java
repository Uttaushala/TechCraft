package com.techcraft.recipe;

import com.techcraft.TechCraft;
import com.techcraft.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Crusher recipes. Besides a few fixed recipes, every "oreX" that has a matching "dustX" in the
 * ore dictionary is crushed into two dusts, and every "ingotX" into one dust, so ores from other
 * mods in the modpack work automatically.
 */
public final class CrusherRecipes {
    private static final List<Recipe> ITEM_RECIPES = new ArrayList<>();
    private static final Map<String, ItemStack> ORE_RECIPES = new HashMap<>();

    public static void init() {
        ITEM_RECIPES.clear();
        ORE_RECIPES.clear();

        add(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
        add(new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.SAND));
        add(new ItemStack(Items.BONE), new ItemStack(Items.DYE, 6, 15));
        add(new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.BLAZE_POWDER, 4));
        add(new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.STRING, 4));

        // Prefer our own dusts for iron and gold.
        ORE_RECIPES.put("oreIron", new ItemStack(ModItems.IRON_DUST, 2));
        ORE_RECIPES.put("oreGold", new ItemStack(ModItems.GOLD_DUST, 2));
        ORE_RECIPES.put("ingotIron", new ItemStack(ModItems.IRON_DUST, 1));
        ORE_RECIPES.put("ingotGold", new ItemStack(ModItems.GOLD_DUST, 1));
        ORE_RECIPES.put("oreRedstone", new ItemStack(Items.REDSTONE, 6));
        ORE_RECIPES.put("oreLapis", new ItemStack(Items.DYE, 8, 4));
        ORE_RECIPES.put("oreCoal", new ItemStack(Items.COAL, 2));
        ORE_RECIPES.put("oreDiamond", new ItemStack(Items.DIAMOND, 2));
        ORE_RECIPES.put("oreEmerald", new ItemStack(Items.EMERALD, 2));
        ORE_RECIPES.put("oreQuartz", new ItemStack(Items.QUARTZ, 3));

        for (String name : OreDictionary.getOreNames()) {
            if (name.startsWith("ore") && name.length() > 3) {
                addOreDictDust(name, "dust" + name.substring(3), 2);
            } else if (name.startsWith("ingot") && name.length() > 5) {
                addOreDictDust(name, "dust" + name.substring(5), 1);
            }
        }
        TechCraft.LOGGER.info("Registered {} crusher ore dictionary recipes", ORE_RECIPES.size());
    }

    private static void addOreDictDust(String input, String dust, int count) {
        if (ORE_RECIPES.containsKey(input) || OreDictionary.getOres(input, false).isEmpty()) {
            return;
        }
        List<ItemStack> dusts = OreDictionary.getOres(dust, false);
        if (!dusts.isEmpty()) {
            ItemStack output = dusts.get(0).copy();
            if (output.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                output.setItemDamage(0);
            }
            output.setCount(count);
            ORE_RECIPES.put(input, output);
        }
    }

    /** Every recipe with the stacks each ore dictionary entry currently stands for, for recipe viewers. */
    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (Recipe recipe : ITEM_RECIPES) {
            recipes.add(new DisplayRecipe(Collections.singletonList(Collections.singletonList(recipe.input.copy())), recipe.output.copy()));
        }
        for (Map.Entry<String, ItemStack> entry : ORE_RECIPES.entrySet()) {
            List<ItemStack> inputs = OreDictionary.getOres(entry.getKey(), false);
            if (!inputs.isEmpty()) {
                recipes.add(new DisplayRecipe(Collections.singletonList(new ArrayList<>(inputs)), entry.getValue().copy()));
            }
        }
        return recipes;
    }

    public static void add(ItemStack input, ItemStack output) {
        ITEM_RECIPES.add(new Recipe(input, output));
    }

    public static ItemStack getResult(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (Recipe recipe : ITEM_RECIPES) {
            if (OreDictionary.itemMatches(recipe.input, input, false)) {
                return recipe.output.copy();
            }
        }
        for (int id : OreDictionary.getOreIDs(input)) {
            ItemStack output = ORE_RECIPES.get(OreDictionary.getOreName(id));
            if (output != null) {
                return output.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static final class Recipe {
        final ItemStack input;
        final ItemStack output;

        Recipe(ItemStack input, ItemStack output) {
            this.input = input;
            this.output = output;
        }
    }

    private CrusherRecipes() {
    }
}
