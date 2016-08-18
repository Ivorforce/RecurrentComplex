/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import com.google.common.collect.Lists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.blocks.materials.RCMaterials;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockGenericSpace extends BlockTyped
{
    public static final PropertyBool VISIBLE = PropertyBool.create("visible");

    public static final AxisAlignedBB SPACE_AABB;

    static {
        float lowB = 1.0f / 16.0f * 5.0f;
        float highB = 1f - lowB;
        SPACE_AABB = new AxisAlignedBB(lowB, lowB, lowB, highB, highB, highB);
    }

    public BlockGenericSpace()
    {
        super(RCMaterials.materialNegativeSpace);

        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, 0).withProperty(VISIBLE, true));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return SPACE_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state)
    {
        return false;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        return Lists.newArrayList();
    }

    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return NULL_AABB;
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

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE, VISIBLE);
    }
}
