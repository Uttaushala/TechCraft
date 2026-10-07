package com.techcraft.item;

import com.techcraft.TechCraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Stops vanilla from kicking players for "flying" while the jetpack is working. */
public final class JetpackSupport {
    private static boolean failed;

    public static void keepAlive(EntityPlayer player) {
        if (failed || !(player instanceof EntityPlayerMP) || ((EntityPlayerMP) player).connection == null) {
            return;
        }
        try {
            ReflectionHelper.setPrivateValue(NetHandlerPlayServer.class, ((EntityPlayerMP) player).connection, 0,
                    "floatingTickCount", "field_147365_f");
        } catch (Throwable t) {
            failed = true;
            TechCraft.LOGGER.warn("Could not reset the fly-kick counter; the server needs allow-flight=true to use the jetpack", t);
        }
    }

    private JetpackSupport() {
    }
}
