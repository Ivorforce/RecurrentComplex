/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import com.google.common.collect.Lists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.blocks.materials.RCMaterials;
import net.minecraft.block.BlockColored;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockGenericSpace extends BlockTyped
{
    public static final PropertyBool VISIBLE = PropertyBool.create("visible");

    public BlockGenericSpace()
    {
        super(RCMaterials.materialNegativeSpace);

        float lowB = 1.0f / 16.0f * 5.0f;
        float highB = 1f - lowB;
        setBlockBounds(lowB, lowB, lowB, highB, highB, highB);

        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, 0).withProperty(VISIBLE, true));
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isNormalCube()
    {
        return false;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        return Lists.newArrayList();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return RCConfig.hideRedundantNegativeSpace && Stream.of(EnumFacing.VALUES).allMatch(f -> hideableAtSide(state, worldIn, pos, f)) ? state.withProperty(VISIBLE, false) : state.withProperty(VISIBLE, true);
    }

    protected boolean hideableAtSide(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing f)
    {
        return worldIn.getBlockState(pos.offset(f)).getProperties().get(TYPE) == state.getValue(TYPE);
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[]{TYPE, VISIBLE});
    }
}
