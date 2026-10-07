package com.techcraft.item;

import com.google.common.collect.Multimap;
import com.techcraft.ModConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/** A sword that hits hard only while it has energy; each hit costs some. */
public class ItemEnergySword extends ItemEnergyBase {
    public ItemEnergySword(String name) {
        super(name, () -> ModConfig.equipment.swordCapacity, () -> ModConfig.equipment.toolTransfer);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            double damage = getStored(stack) >= ModConfig.equipment.swordEnergyPerHit
                    ? ModConfig.equipment.swordDamage - 1 : 0.0;
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4, 0));
        }
        return modifiers;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        useEnergy(stack, ModConfig.equipment.swordEnergyPerHit);
        return true;
    }
}
