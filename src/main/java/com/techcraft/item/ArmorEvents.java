package com.techcraft.item;

import com.techcraft.ModConfig;
import com.techcraft.TechCraft;
import com.techcraft.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Tech Boots turn fall damage into energy use. */
@Mod.EventBusSubscriber(modid = TechCraft.MODID)
public final class ArmorEvents {
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        ItemStack boots = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.FEET);
        int perBlock = ModConfig.equipment.bootsEnergyPerBlock;
        if (boots.getItem() != ModItems.TECH_BOOTS || perBlock <= 0) {
            return;
        }
        float distance = event.getDistance();
        float absorbable = (float) EnergyItems.getStored(boots) / perBlock;
        float absorbed = Math.min(distance, absorbable);
        if (absorbed > 0) {
            EnergyItems.use(boots, (int) Math.ceil(absorbed * perBlock));
            event.setDistance(distance - absorbed);
        }
    }

    private ArmorEvents() {
    }
}
