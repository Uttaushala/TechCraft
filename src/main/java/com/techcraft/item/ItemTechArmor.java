package com.techcraft.item;

import com.techcraft.ModConfig;
import com.techcraft.TechCraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Energy-powered armor. It has no durability; instead each piece holds energy. Helmet: night vision in the dark and
 * water breathing. Chestplate: jetpack. Leggings: sprint speed. Boots: absorb fall damage (see ArmorEvents).
 */
public class ItemTechArmor extends ItemArmor {
    /** Player data flag set by the jetpack network packet while the jump key is held. */
    public static final String JETPACK_FLAG = "techcraft_jetpack";

    private static final ArmorMaterial MATERIAL = EnumHelper.addArmorMaterial("TECHCRAFT_ARMOR", TechCraft.MODID + ":tech",
            0, new int[]{2, 5, 6, 2}, 12, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F);

    public ItemTechArmor(String name, EntityEquipmentSlot slot) {
        super(MATERIAL, 0, slot);
        setRegistryName(TechCraft.MODID, name);
        setTranslationKey(TechCraft.MODID + "." + name);
        setCreativeTab(TechCraft.TAB);
    }

    public static int capacity() {
        return Math.max(1, ModConfig.equipment.armorCapacity);
    }

    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        EnergyItems.setStored(stack, capacity(), capacity());
        return stack;
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote) {
            return;
        }
        switch (armorType) {
            case HEAD:
                tickHelmet(world, player, stack);
                break;
            case CHEST:
                tickJetpack(player, stack);
                break;
            case LEGS:
                if (player.isSprinting() && EnergyItems.use(stack, 3)) {
                    player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 20, 1, true, false));
                }
                break;
            default:
                break;
        }
    }

    private static void tickHelmet(World world, EntityPlayer player, ItemStack stack) {
        if (world.getTotalWorldTime() % 20 != 0) {
            return;
        }
        BlockPos head = player.getPosition().up();
        if (world.getLight(head) < 7 && EnergyItems.use(stack, 20)) {
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 260, 0, true, false));
        }
        if (player.isInsideOfMaterial(net.minecraft.block.material.Material.WATER) && EnergyItems.use(stack, 20)) {
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 40, 0, true, false));
        }
    }

    private static void tickJetpack(EntityPlayer player, ItemStack stack) {
        NBTTagCompound data = player.getEntityData();
        if (!data.getBoolean(JETPACK_FLAG)) {
            return;
        }
        if (!EnergyItems.use(stack, ModConfig.equipment.jetpackEnergyPerTick)) {
            data.setBoolean(JETPACK_FLAG, false);
            return;
        }
        player.fallDistance = 0.0F;
        JetpackSupport.keepAlive(player);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return EnergyItems.durability(stack, capacity());
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 0x2F7BFF;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            EnergyItems.addCreativeVariants(this, capacity(), items);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EnergyItems.tooltip(stack, capacity(), tooltip);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyItems.provider(stack, ItemTechArmor::capacity, () -> ModConfig.equipment.armorTransfer);
    }
}
