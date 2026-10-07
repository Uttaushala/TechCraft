package com.techcraft.tile;

import com.techcraft.ModConfig;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;

/**
 * Hurts mobs in a 5x3x5 area three blocks in front of it, as if a player did it (so loot rules apply), and picks up
 * the drops. Players, villagers, tamed pets, bosses and named mobs are left alone.
 */
public class TileMobGrinder extends TileGridMachine {
    public TileMobGrinder() {
        super(ModConfig.mobGrinder.capacity, 0);
    }

    private AxisAlignedBB area() {
        EnumFacing facing = getFacing();
        return new AxisAlignedBB(pos.offset(facing, 3)).grow(2.0, 0.5, 2.0).offset(0, 0.5, 0);
    }

    private static boolean isTarget(EntityLiving entity) {
        return entity.isEntityAlive() && entity.isNonBoss() && !entity.hasCustomName()
                && !(entity instanceof EntityVillager)
                && !(entity instanceof EntityTameable && ((EntityTameable) entity).isTamed());
    }

    @Override
    protected void tickServer() {
        ModConfig.Grinder config = ModConfig.mobGrinder;
        AxisAlignedBB area = area();
        boolean working = false;
        int cost = Math.max(1, (int) Math.round((double) config.energyPerHit / config.ticksPerHit * energyFactor()));

        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, area, TileMobGrinder::isTarget);
        if (!mobs.isEmpty() && energy.consume(cost)) {
            working = true;
            progress += (int) Math.round(SCALE * speedFactor());
            if (progress >= config.ticksPerHit * SCALE) {
                progress = 0;
                FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer) world);
                for (EntityLiving mob : mobs) {
                    mob.attackEntityFrom(DamageSource.causePlayerDamage(player), config.damage);
                }
            }
        }

        for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class, area.grow(1.0))) {
            if (drop.isDead || drop.cannotPickup()) {
                continue;
            }
            ItemStack left = insertInternal(drop.getItem().copy());
            if (left.isEmpty()) {
                drop.setDead();
            } else {
                drop.setItem(left);
            }
        }
        rate = working ? cost : 0;
        updateActive(working);
    }

    @Override
    protected int getProgressMax() {
        return ModConfig.mobGrinder.ticksPerHit * SCALE;
    }
}
