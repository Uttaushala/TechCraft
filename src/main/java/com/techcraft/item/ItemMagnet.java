package com.techcraft.item;

import com.techcraft.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Right-click to switch on; while on, it pulls dropped items towards you and uses a little energy. */
public class ItemMagnet extends ItemEnergyBase {
    private static final String ACTIVE = "Active";

    public ItemMagnet(String name) {
        super(name, () -> ModConfig.equipment.magnetCapacity, () -> ModConfig.equipment.toolTransfer);
    }

    public static boolean isActive(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(ACTIVE);
    }

    public static void setActive(ItemStack stack, boolean active) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean(ACTIVE, active);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        setActive(stack, !isActive(stack));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity holder, int slot, boolean selected) {
        if (world.isRemote || !isActive(stack) || !(holder instanceof EntityPlayer)) {
            return;
        }
        if (!useEnergy(stack, ModConfig.equipment.magnetEnergyPerTick)) {
            setActive(stack, false);
            return;
        }
        double range = ModConfig.equipment.magnetRange;
        AxisAlignedBB area = holder.getEntityBoundingBox().grow(range);
        for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, area)) {
            if (item.isDead || item.cannotPickup()) {
                continue;
            }
            Vec3d pull = new Vec3d(holder.posX - item.posX, holder.posY + 0.5 - item.posY, holder.posZ - item.posZ);
            if (pull.lengthVector() < 0.8 || !useEnergy(stack, 1)) {
                continue;
            }
            Vec3d motion = pull.normalize().scale(0.4);
            item.motionX = motion.x;
            item.motionY = motion.y;
            item.motionZ = motion.z;
            item.velocityChanged = true;
        }
    }
}
