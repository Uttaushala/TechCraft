package com.techcraft.item;

import com.techcraft.TechCraft;
import net.minecraft.item.Item;

public class ItemBase extends Item {
    public ItemBase(String name) {
        setRegistryName(TechCraft.MODID, name);
        setTranslationKey(TechCraft.MODID + "." + name);
        setCreativeTab(TechCraft.TAB);
    }
}
