package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.recipe.CrusherRecipes;
import net.minecraft.item.ItemStack;

public class TileCrusher extends TileProcessor {
    public TileCrusher() {
        super(ModConfig.crusher);
    }

    @Override
    protected ItemStack getResult(ItemStack input) {
        return CrusherRecipes.getResult(input);
    }
}
