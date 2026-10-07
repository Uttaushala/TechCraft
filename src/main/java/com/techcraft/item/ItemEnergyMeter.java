package com.techcraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

/**
 * Right-click any block to see its Forge Energy buffer and what the clicked face accepts or gives. Handy for
 * checking how a cable or machine from another mod connects.
 */
public class ItemEnergyMeter extends ItemBase {
    public ItemEnergyMeter(String name) {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        TileEntity tile = world.getTileEntity(pos);
        IEnergyStorage energy = tile == null ? null : tile.getCapability(CapabilityEnergy.ENERGY, facing);
        IFluidHandler fluids = tile == null ? null : tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);

        if (energy == null && fluids == null) {
            player.sendMessage(new TextComponentTranslation("msg.techcraft.meter.none"));
            return EnumActionResult.SUCCESS;
        }
        if (energy != null) {
            player.sendMessage(new TextComponentTranslation("msg.techcraft.meter.energy",
                    String.format("%,d", energy.getEnergyStored()), String.format("%,d", energy.getMaxEnergyStored())));
            String mode = energy.canReceive() && energy.canExtract() ? "both"
                    : energy.canReceive() ? "in" : energy.canExtract() ? "out" : "none";
            player.sendMessage(new TextComponentTranslation("msg.techcraft.meter.face",
                    facing.getName(), new TextComponentTranslation("msg.techcraft.meter.mode." + mode)));
        }
        if (fluids != null) {
            for (IFluidTankProperties tank : fluids.getTankProperties()) {
                String name = tank.getContents() == null ? "-" : tank.getContents().getLocalizedName();
                int amount = tank.getContents() == null ? 0 : tank.getContents().amount;
                player.sendMessage(new TextComponentTranslation("msg.techcraft.meter.fluid", name,
                        String.format("%,d", amount), String.format("%,d", tank.getCapacity())));
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
