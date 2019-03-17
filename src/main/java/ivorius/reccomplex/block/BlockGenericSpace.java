/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

import com.google.common.collect.Lists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.block.materials.RCMaterials;
import ivorius.reccomplex.utils.UnstableBlock;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockGenericSpace extends BlockTyped implements UnstableBlock
{
    public static final PropertyBool VISIBLE = PropertyBool.create("visible");

    public static final AxisAlignedBB SPACE_AABB;

    static
    {
        float lowB = 1.0f / 16.0f * 5.0f;
        float highB = 1f - lowB;
        SPACE_AABB = new AxisAlignedBB(lowB, lowB, lowB, highB, highB, highB);
    }

    public BlockGenericSpace()
    {
        super(RCMaterials.materialNegativeSpace);

        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, 0).withProperty(VISIBLE, true));
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return SPACE_AABB;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isNormalCube(IBlockState state)
    {
        return false;
    }

    @Nonnull
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune)
    {
        return Lists.newArrayList();
    }

    @Nullable
    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return RCConfig.hideRedundantNegativeSpace && Stream.of(EnumFacing.VALUES).allMatch(f -> hideableAtSide(state, worldIn, pos, f)) ? state.withProperty(VISIBLE, false) : state.withProperty(VISIBLE, true);
    }

    protected boolean hideableAtSide(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing f)
    {
        IBlockState sideState = worldIn.getBlockState(pos.offset(f));
        return sideState.getBlock() == this && Objects.equals(sideState.getValue(TYPE), sideState.getValue(TYPE));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE, VISIBLE);
    }
}
