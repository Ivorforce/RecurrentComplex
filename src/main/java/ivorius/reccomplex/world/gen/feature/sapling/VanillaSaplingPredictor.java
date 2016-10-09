/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.sapling;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 09.10.16.
 */
public class VanillaSaplingPredictor implements RCSaplingGenerator.Predictor
{
    // From BlockSapling
    public static Type vanillaType(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!(state.getBlock() == Blocks.SAPLING))
            return null;

        int i = 0;
        int j = 0;
        boolean flag = false;

        switch (state.getValue(BlockSapling.TYPE))
        {
            case SPRUCE:
                label114:

                for (i = 0; i >= -1; --i)
                {
                    for (j = 0; j >= -1; --j)
                    {
                        if (isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.SPRUCE))
                        {
                            flag = true;
                            break label114;
                        }
                    }
                }

                if (!flag)
                {
                    i = 0;
                    j = 0;
                }

                break;
            case BIRCH:
                break;
            case JUNGLE:
                label269:

                for (i = 0; i >= -1; --i)
                {
                    for (j = 0; j >= -1; --j)
                    {
                        if (isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.JUNGLE))
                        {
                            flag = true;
                            break label269;
                        }
                    }
                }

                if (!flag)
                {
                    i = 0;
                    j = 0;
                }

                break;
            case ACACIA:
                break;
            case DARK_OAK:
                label390:

                for (i = 0; i >= -1; --i)
                {
                    for (j = 0; j >= -1; --j)
                    {
                        if (isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.DARK_OAK))
                        {
                            flag = true;
                            break label390;
                        }
                    }
                }

                if (!flag)
                {
                    return Type.NONE;
                }

            case OAK:
        }

        return flag ? Type.BIG : Type.NORMAL;
    }

    private static boolean isTwoByTwoOfType(World worldIn, BlockPos pos, int p_181624_3_, int p_181624_4_, BlockPlanks.EnumType type)
    {
        BlockSapling sapling = (BlockSapling) Blocks.SAPLING;
        return sapling.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_), type) && sapling.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_), type) && sapling.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_ + 1), type) && sapling.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_ + 1), type);
    }

    @Override
    public int complexity(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        Type type = vanillaType(worldIn, pos, state);
        return type == VanillaSaplingPredictor.Type.NORMAL ? 1 : type == VanillaSaplingPredictor.Type.BIG ? 4 : -1;
    }

    enum Type
    {
        NORMAL, BIG, NONE
    }
}
