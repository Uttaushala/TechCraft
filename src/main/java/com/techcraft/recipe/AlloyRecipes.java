package com.techcraft.recipe;

import com.techcraft.TechCraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Alloy furnace recipes. Inputs and outputs are matched through the ore dictionary, so the alloys come out as
 * whatever bronze/steel/invar/... the modpack already has, and a recipe is skipped when no mod provides its output.
 */
public final class AlloyRecipes {
    private static final List<Recipe> RECIPES = new ArrayList<>();

    public static void init() {
        RECIPES.clear();
        add(ore("ingotCopper"), 3, ore("ingotTin"), 1, "ingotBronze", 4);
        add(ore("ingotIron"), 1, item(new ItemStack(Items.COAL, 1, OreDictionary.WILDCARD_VALUE)), 2, "ingotSteel", 1);
        add(ore("ingotIron"), 2, ore("ingotNickel"), 1, "ingotInvar", 3);
        add(ore("ingotGold"), 1, ore("ingotSilver"), 1, "ingotElectrum", 2);
        add(ore("ingotCopper"), 1, ore("ingotNickel"), 1, "ingotConstantan", 2);
        TechCraft.LOGGER.info("Registered {} alloy furnace recipes", RECIPES.size());
    }

    private static void add(Ingredient a, int countA, Ingredient b, int countB, String outputOre, int outputCount) {
        List<ItemStack> outputs = OreDictionary.getOres(outputOre, false);
        if (outputs.isEmpty()) {
            TechCraft.LOGGER.info("Skipping alloy recipe for {}: no such item in the ore dictionary", outputOre);
            return;
        }
        ItemStack output = outputs.get(0).copy();
        if (output.getMetadata() == OreDictionary.WILDCARD_VALUE) {
            output.setItemDamage(0);
        }
        output.setCount(outputCount);
        RECIPES.add(new Recipe(a, countA, b, countB, output));
    }

    private static Ingredient ore(String name) {
        return new Ingredient(name, ItemStack.EMPTY);
    }

    private static Ingredient item(ItemStack stack) {
        return new Ingredient(null, stack);
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (Recipe recipe : RECIPES) {
            recipes.add(new DisplayRecipe(Arrays.asList(recipe.a.stacks(recipe.countA), recipe.b.stacks(recipe.countB)),
                    recipe.output.copy()));
        }
        return recipes;
    }

    public static boolean isIngredient(ItemStack stack) {
        for (Recipe recipe : RECIPES) {
            if (recipe.a.matches(stack) || recipe.b.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    /** Finds a recipe for the two input stacks, in either order. */
    @Nullable
    public static Match find(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return null;
        }
        for (Recipe recipe : RECIPES) {
            if (recipe.accepts(first, recipe.a, recipe.countA) && recipe.accepts(second, recipe.b, recipe.countB)) {
                return new Match(recipe, 0, 1);
            }
            if (recipe.accepts(second, recipe.a, recipe.countA) && recipe.accepts(first, recipe.b, recipe.countB)) {
                return new Match(recipe, 1, 0);
            }
        }
        return null;
    }

    public static final class Match {
        public final Recipe recipe;
        /** Input slot that holds ingredient A / B of the recipe. */
        public final int slotA;
        public final int slotB;

        Match(Recipe recipe, int slotA, int slotB) {
            this.recipe = recipe;
            this.slotA = slotA;
            this.slotB = slotB;
        }
    }

    public static final class Recipe {
        final Ingredient a;
        public final int countA;
        final Ingredient b;
        public final int countB;
        public final ItemStack output;

        Recipe(Ingredient a, int countA, Ingredient b, int countB, ItemStack output) {
            this.a = a;
            this.countA = countA;
            this.b = b;
            this.countB = countB;
            this.output = output;
        }

        boolean accepts(ItemStack stack, Ingredient ingredient, int count) {
            return stack.getCount() >= count && ingredient.matches(stack);
        }
    }

    private static final class Ingredient {
        @Nullable
        final String ore;
        final ItemStack stack;

        Ingredient(@Nullable String ore, ItemStack stack) {
            this.ore = ore;
            this.stack = stack;
        }

        /** The stacks that satisfy this ingredient, each with the required count. */
        List<ItemStack> stacks(int count) {
            List<ItemStack> result = new ArrayList<>();
            if (ore == null) {
                result.add(stack.copy());
            } else {
                for (ItemStack candidate : OreDictionary.getOres(ore, false)) {
                    result.add(candidate.copy());
                }
            }
            for (ItemStack candidate : result) {
                candidate.setCount(count);
            }
            return result;
        }

        boolean matches(ItemStack candidate) {
            if (ore == null) {
                return OreDictionary.itemMatches(stack, candidate, false);
            }
            int id = OreDictionary.getOreID(ore);
            for (int candidateId : OreDictionary.getOreIDs(candidate)) {
                if (candidateId == id) {
                    return true;
                }
            }
            return false;
        }
    }

    private AlloyRecipes() {
    }
}
