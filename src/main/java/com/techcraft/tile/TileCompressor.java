package com.techcraft.tile;

import com.techcraft.ModConfig;
import com.techcraft.recipe.CompressorRecipes;
import net.minecraft.item.ItemStack;

public class TileCompressor extends TileProcessor {
    public TileCompressor() {
        super(ModConfig.compressor);
    }

    @Override
    protected ItemStack getResult(ItemStack input) {
        return CompressorRecipes.getResult(input);
    }
}
