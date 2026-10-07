package com.techcraft.network;

import com.techcraft.init.ModItems;
import com.techcraft.item.ItemTechArmor;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Client to server: the jump key state while wearing the Tech Chestplate. */
public class PacketJetpack implements IMessage {
    private boolean active;

    public PacketJetpack() {
    }

    public PacketJetpack(boolean active) {
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        active = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(active);
    }

    public static class Handler implements IMessageHandler<PacketJetpack, IMessage> {
        @Override
        public IMessage onMessage(PacketJetpack message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                // Ignore the request unless the player really wears the chestplate.
                player.getEntityData().setBoolean(ItemTechArmor.JETPACK_FLAG,
                        message.active && chest.getItem() == ModItems.TECH_CHESTPLATE);
            });
            return null;
        }
    }
}
