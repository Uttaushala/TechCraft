package com.techcraft.block;

import com.techcraft.TechCraft;
import com.techcraft.inventory.GuiHandler;
import com.techcraft.tile.TileMachineBase;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

public class BlockMachine extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    private final Supplier<TileMachineBase> tileFactory;
    private boolean lightWhenActive = true;

    public BlockMachine(String name, Supplier<TileMachineBase> tileFactory) {
        super(Material.IRON);
        this.tileFactory = tileFactory;
        setRegistryName(TechCraft.MODID, name);
        setTranslationKey(TechCraft.MODID + "." + name);
        setCreativeTab(TechCraft.TAB);
        setHardness(3.5F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTIVE, false));
    }

    /** For machines whose "active" state shouldn't make them glow (solar panels, wind turbines). */
    public BlockMachine noActiveLight() {
        this.lightWhenActive = false;
        return this;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return tileFactory.get();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, world, pos, facing)) {
            return true;
        }
        if (!world.isRemote && world.getTileEntity(pos) instanceof TileMachineBase) {
            player.openGui(TechCraft.instance, GuiHandler.MACHINE_GUI, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMachineBase) {
            TileMachineBase machine = (TileMachineBase) tile;
            for (IItemHandler inventory : new IItemHandler[]{machine.getInventory(), machine.getUpgradeInventory()}) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                    }
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return lightWhenActive && state.getValue(ACTIVE) ? 13 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTIVE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(ACTIVE, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(ACTIVE) ? 4 : 0);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }
}
