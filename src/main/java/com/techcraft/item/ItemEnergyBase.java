package com.techcraft.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

/** An item that stores Forge Energy in its NBT, so any mod's charger or cable can fill it. */
public class ItemEnergyBase extends ItemBase {
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
        return EnergyItems.getStored(stack);
    }

    /** Takes energy for the item's own use, ignoring the transfer limit. */
    protected boolean useEnergy(ItemStack stack, int amount) {
        return EnergyItems.use(stack, amount);
    }

    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        EnergyItems.setStored(stack, getCapacity(), getCapacity());
        return stack;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return EnergyItems.durability(stack, getCapacity());
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
            EnergyItems.addCreativeVariants(this, getCapacity(), items);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EnergyItems.tooltip(stack, getCapacity(), tooltip);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyItems.provider(stack, this::getCapacity, transfer);
    }
}
