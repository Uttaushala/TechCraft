package com.techcraft.compat.jei;

import com.techcraft.ModConfig;
import com.techcraft.TechCraft;
import com.techcraft.init.ModBlocks;
import com.techcraft.recipe.AlloyRecipes;
import com.techcraft.recipe.CompressorRecipes;
import com.techcraft.recipe.CrusherRecipes;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Shows the crusher, compressor and alloy furnace recipes in JEI. Only loaded when JEI is installed. */
@JEIPlugin
public class TechCraftJEI implements IModPlugin {
    static final String CRUSHER = TechCraft.MODID + ".crusher";
    static final String COMPRESSOR = TechCraft.MODID + ".compressor";
    static final String ALLOY = TechCraft.MODID + ".alloy_furnace";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper gui = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(
                new MachineCategory(gui, CRUSHER, "tile.techcraft.crusher.name", 1,
                        () -> ModConfig.crusher.energyPerTick * ModConfig.crusher.ticksPerOperation),
                new MachineCategory(gui, COMPRESSOR, "tile.techcraft.compressor.name", 1,
                        () -> ModConfig.compressor.energyPerTick * ModConfig.compressor.ticksPerOperation),
                new MachineCategory(gui, ALLOY, "tile.techcraft.alloy_furnace.name", 2,
                        () -> ModConfig.alloyFurnace.energyPerTick * ModConfig.alloyFurnace.ticksPerOperation));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.handleRecipes(MachineWrapper.Crusher.class, recipe -> recipe, CRUSHER);
        registry.handleRecipes(MachineWrapper.Compressor.class, recipe -> recipe, COMPRESSOR);
        registry.handleRecipes(MachineWrapper.Alloy.class, recipe -> recipe, ALLOY);

        List<MachineWrapper> crusher = new ArrayList<>();
        CrusherRecipes.displayRecipes().forEach(r -> crusher.add(new MachineWrapper.Crusher(r)));
        List<MachineWrapper> compressor = new ArrayList<>();
        CompressorRecipes.displayRecipes().forEach(r -> compressor.add(new MachineWrapper.Compressor(r)));
        List<MachineWrapper> alloy = new ArrayList<>();
        AlloyRecipes.displayRecipes().forEach(r -> alloy.add(new MachineWrapper.Alloy(r)));
        registry.addRecipes(crusher, CRUSHER);
        registry.addRecipes(compressor, COMPRESSOR);
        registry.addRecipes(alloy, ALLOY);

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.CRUSHER), CRUSHER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.COMPRESSOR), COMPRESSOR);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.ALLOY_FURNACE), ALLOY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.ELECTRIC_FURNACE), VanillaRecipeCategoryUid.SMELTING);
    }
}
