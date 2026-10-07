package com.techcraft.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

/** Shared code for items that keep Forge Energy in their NBT: batteries, tools, armor. */
public final class EnergyItems {
    private static final String TAG = "Energy";

    public static int getStored(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getInteger(TAG);
    }

    public static void setStored(ItemStack stack, int energy, int capacity) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger(TAG, Math.max(0, Math.min(capacity, energy)));
    }

    /** Takes energy for the item's own use, ignoring the transfer limit. */
    public static boolean use(ItemStack stack, int amount) {
        int stored = getStored(stack);
        if (stored < amount) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            tag.setInteger(TAG, stored - amount);
        }
        return true;
    }

    public static double durability(ItemStack stack, int capacity) {
        return 1.0 - (double) getStored(stack) / Math.max(1, capacity);
    }

    public static void addCreativeVariants(Item item, int capacity, NonNullList<ItemStack> items) {
        items.add(new ItemStack(item));
        ItemStack full = new ItemStack(item);
        setStored(full, capacity, capacity);
        items.add(full);
    }

    @SideOnly(Side.CLIENT)
    public static void tooltip(ItemStack stack, int capacity, List<String> tooltip) {
        tooltip.add(I18n.format("tooltip.techcraft.energy", String.format("%,d", getStored(stack)),
                String.format("%,d", capacity)));
    }

    /** Exposes the stack's energy as a Forge energy capability so any mod's charger or cable can fill it. */
    public static ICapabilityProvider provider(ItemStack stack, IntSupplier capacity, IntSupplier transfer) {
        return new ICapabilityProvider() {
            private final IEnergyStorage storage = new IEnergyStorage() {
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    int stored = getStored(stack);
                    int accepted = Math.min(Math.min(capacity.getAsInt() - stored, transfer.getAsInt()), maxReceive);
                    if (accepted > 0 && !simulate) {
                        setStored(stack, stored + accepted, capacity.getAsInt());
                    }
                    return Math.max(0, accepted);
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    int stored = getStored(stack);
                    int extracted = Math.min(Math.min(stored, transfer.getAsInt()), maxExtract);
                    if (extracted > 0 && !simulate) {
                        setStored(stack, stored - extracted, capacity.getAsInt());
                    }
                    return Math.max(0, extracted);
                }

                @Override
                public int getEnergyStored() {
                    return getStored(stack);
                }

                @Override
                public int getMaxEnergyStored() {
                    return capacity.getAsInt();
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

    private EnergyItems() {
    }
}
