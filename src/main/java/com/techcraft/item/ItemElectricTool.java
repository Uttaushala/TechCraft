package com.techcraft.item;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.IntSupplier;

/** Mines at diamond level and fast while it has energy, and like bare hands without it. */
public class ItemElectricTool extends ItemEnergyBase {
    private final Set<String> toolClasses;
    private final Set<Material> fastMaterials;
    private final IntSupplier energyPerBlock;
    private final IntSupplier speed;

    public ItemElectricTool(String name, IntSupplier capacity, IntSupplier transfer, IntSupplier energyPerBlock,
                            IntSupplier speed, Set<String> toolClasses, Set<Material> fastMaterials) {
        super(name, capacity, transfer);
        this.energyPerBlock = energyPerBlock;
        this.speed = speed;
        this.toolClasses = toolClasses;
        this.fastMaterials = fastMaterials;
    }

    private boolean isPowered(ItemStack stack) {
        return getStored(stack) >= energyPerBlock.getAsInt() && getStored(stack) > 0;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return toolClasses;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player,
                               @Nullable IBlockState blockState) {
        return toolClasses.contains(toolClass) && isPowered(stack) ? 3 : -1;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return isPowered(stack) && fastMaterials.contains(state.getMaterial()) ? speed.getAsInt() : 1.0F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos,
                                    EntityLivingBase entity) {
        if (!world.isRemote && state.getBlockHardness(world, pos) != 0.0F) {
            useEnergy(stack, energyPerBlock.getAsInt());
        }
        return true;
    }
}
