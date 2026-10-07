package com.techcraft;

import com.techcraft.dev.SelfTest;
import com.techcraft.init.ModBlocks;
import com.techcraft.init.ModItems;
import com.techcraft.inventory.GuiHandler;
import com.techcraft.recipe.AlloyRecipes;
import com.techcraft.recipe.CompressorRecipes;
import com.techcraft.recipe.CrusherRecipes;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = TechCraft.MODID, name = TechCraft.NAME, version = TechCraft.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class TechCraft {
    public static final String MODID = "techcraft";
    public static final String NAME = "TechCraft";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static TechCraft instance;

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.COAL_GENERATOR);
        }
    };

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        SelfTest.registerTestOres();

        OreDictionary.registerOre("dustIron", ModItems.IRON_DUST);
        OreDictionary.registerOre("dustGold", ModItems.GOLD_DUST);
        for (int i = 0; i < ModItems.PLATES.length; i++) {
            OreDictionary.registerOre("plate" + ModItems.PLATE_METALS[i], ModItems.PLATES[i]);
        }

        GameRegistry.addSmelting(ModItems.IRON_DUST, new ItemStack(net.minecraft.init.Items.IRON_INGOT), 0.1F);
        GameRegistry.addSmelting(ModItems.GOLD_DUST, new ItemStack(net.minecraft.init.Items.GOLD_INGOT), 0.1F);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Done in postInit so ores and dusts from every other mod in the pack are already registered.
        CrusherRecipes.init();
        CompressorRecipes.init();
        AlloyRecipes.init();
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        if (SelfTest.enabled()) {
            SelfTest.start();
        }
    }
}
