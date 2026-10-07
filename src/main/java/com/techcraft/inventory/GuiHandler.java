package com.techcraft.inventory;

import com.techcraft.client.GuiMachine;
import com.techcraft.tile.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    public static final int MACHINE_GUI = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == MACHINE_GUI && tile instanceof TileMachineBase) {
            return new ContainerMachine(player.inventory, (TileMachineBase) tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == MACHINE_GUI && tile instanceof TileMachineBase) {
            return new GuiMachine(new ContainerMachine(player.inventory, (TileMachineBase) tile));
        }
        return null;
    }
}
