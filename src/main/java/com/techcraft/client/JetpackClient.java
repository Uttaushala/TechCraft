package com.techcraft.client;

import com.techcraft.ModConfig;
import com.techcraft.TechCraft;
import com.techcraft.init.ModItems;
import com.techcraft.item.EnergyItems;
import com.techcraft.network.PacketJetpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Reads the jump key, pushes the player upwards while the jetpack has energy and tells the server. */
@Mod.EventBusSubscriber(modid = TechCraft.MODID, value = Side.CLIENT)
public final class JetpackClient {
    private static final double MAX_RISE = 0.5;
    private static final double THRUST = 0.12;

    private static boolean sent;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || mc.isGamePaused()) {
            return;
        }
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        boolean wearing = chest.getItem() == ModItems.TECH_CHESTPLATE
                && EnergyItems.getStored(chest) >= Math.max(1, ModConfig.equipment.jetpackEnergyPerTick);
        boolean active = wearing && mc.gameSettings.keyBindJump.isKeyDown() && !player.capabilities.isFlying
                && !player.isInWater() && !player.isRiding();

        if (active) {
            player.motionY = Math.min(player.motionY + THRUST, MAX_RISE);
            player.fallDistance = 0.0F;
            player.world.spawnParticle(EnumParticleTypes.FLAME, player.posX, player.posY + 0.3, player.posZ,
                    (player.world.rand.nextDouble() - 0.5) * 0.05, -0.2, (player.world.rand.nextDouble() - 0.5) * 0.05);
        }
        if (active != sent) {
            sent = active;
            TechCraft.NETWORK.sendToServer(new PacketJetpack(active));
        }
    }

    private JetpackClient() {
    }
}
