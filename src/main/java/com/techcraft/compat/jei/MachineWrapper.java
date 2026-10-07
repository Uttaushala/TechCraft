package com.techcraft.compat.jei;

import com.techcraft.recipe.DisplayRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class MachineWrapper implements IRecipeWrapper {
    final DisplayRecipe recipe;

    public MachineWrapper(DisplayRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, recipe.inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
    }

    public static final class Crusher extends MachineWrapper {
        public Crusher(DisplayRecipe recipe) {
            super(recipe);
        }
    }

    public static final class Compressor extends MachineWrapper {
        public Compressor(DisplayRecipe recipe) {
            super(recipe);
        }
    }

    public static final class Alloy extends MachineWrapper {
        public Alloy(DisplayRecipe recipe) {
            super(recipe);
        }
    }
}
