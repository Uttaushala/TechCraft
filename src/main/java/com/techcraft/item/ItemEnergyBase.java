package com.techcraft.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

/** An item that stores Forge Energy in its NBT, so any mod's charger or cable can fill it. */
public class ItemEnergyBase extends ItemBase {
    private static final String TAG = "Energy";

    private final IntSupplier capacity;
    private final IntSupplier transfer;

    public ItemEnergyBase(String name, IntSupplier capacity, IntSupplier transfer) {
        super(name);
        this.capacity = capacity;
        this.transfer = transfer;
        setMaxStackSize(1);
    }

    public int getCapacity() {
        return Math.max(1, capacity.getAsInt());
    }

    public static int getStored(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getInteger(TAG);
    }

    private void setStored(ItemStack stack, int energy) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger(TAG, Math.max(0, Math.min(getCapacity(), energy)));
    }

    /** Takes energy for the item's own use, ignoring the transfer limit. */
    protected boolean useEnergy(ItemStack stack, int amount) {
        int stored = getStored(stack);
        if (stored < amount) {
            return false;
        }
        setStored(stack, stored - amount);
        return true;
    }

    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        setStored(stack, getCapacity());
        return stack;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0 - (double) getStored(stack) / getCapacity();
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 0x2F7BFF;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            items.add(createFull());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.techcraft.energy",
                String.format("%,d", getStored(stack)), String.format("%,d", getCapacity())));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ICapabilityProvider() {
            private final IEnergyStorage storage = new IEnergyStorage() {
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    int stored = getStored(stack);
                    int accepted = Math.min(Math.min(getCapacity() - stored, transfer.getAsInt()), maxReceive);
                    if (accepted > 0 && !simulate) {
                        setStored(stack, stored + accepted);
                    }
                    return Math.max(0, accepted);
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    int stored = getStored(stack);
                    int extracted = Math.min(Math.min(stored, transfer.getAsInt()), maxExtract);
                    if (extracted > 0 && !simulate) {
                        setStored(stack, stored - extracted);
                    }
                    return Math.max(0, extracted);
                }

                @Override
                public int getEnergyStored() {
                    return getStored(stack);
                }

                @Override
                public int getMaxEnergyStored() {
                    return getCapacity();
                }

                @Override
                public boolean canExtract() {
                    return true;
                }

                @Override
                public boolean canReceive() {
                    return true;
                }
            };

            @Override
            public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == CapabilityEnergy.ENERGY;
            }

            @Nullable
            @Override
            public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
                return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(storage) : null;
            }
        };
    }
}
