/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.structures.generic.WorldCache;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.RCBlockLogic;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.function.Predicate;

/**
 * Created by lukas on 16.09.16.
 */
public class PositionedBlockMatcher extends FunctionExpressionCache<Boolean, PositionedBlockMatcher.Argument, Object> implements Predicate<PositionedBlockMatcher.Argument>
{
    public static final String BLOCK_PREFIX = "block.";
    public static final String IS_PREFIX = "is:";
    public static final String SUSTAIN_PREFIX = "sustains.";
    public static final String BLOCKS_PREFIX = "blocks:";

    public final MCRegistry registry;

    public PositionedBlockMatcher(MCRegistry registry, String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Block", expression);

        this.registry = registry;

        addTypes(new BlockVariableType(BLOCK_PREFIX, "", registry), v -> v.alias("", ""));
        addTypes(new IsVariableType(IS_PREFIX, ""));
        addType(new SustainVariableType(SUSTAIN_PREFIX, ""));
        addType(new BlocksVariableType(PositionedBlockMatcher.BLOCKS_PREFIX, ""));

        testVariables();
    }

    @Override
    public boolean test(Argument argument)
    {
        return evaluate(argument);
    }

    public static class Argument
    {
        public World world;
        public BlockPos pos;
        public IBlockState state;

        public Argument(World world, BlockPos pos, IBlockState state)
        {
            this.world = world;
            this.pos = pos;
            this.state = state;
        }

        public static Argument at(World world, BlockPos pos)
        {
            return new Argument(world, pos, world.getBlockState(pos));
        }

        public static Argument at(WorldCache cache, BlockPos pos)
        {
            return new Argument(cache.world, pos, cache.getBlockState(pos));
        }
    }

    public static class BlockVariableType extends DelegatingVariableType<Boolean, Argument, Object, IBlockState, Object, BlockMatcher>
    {
        public final MCRegistry registry;

        public BlockVariableType(String prefix, String suffix, MCRegistry registry)
        {
            super(prefix, suffix);
            this.registry = registry;
        }

        @Override
        public IBlockState convertEvaluateArgument(Argument argument)
        {
            return argument.state;
        }

        @Override
        public BlockMatcher createCache(String var)
        {
            return new BlockMatcher(registry, var);
        }
    }

    public static class IsVariableType extends VariableType<Boolean, Argument, Object>
    {
        public IsVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Argument argument)
        {
            IBlockState state = argument.state;

            switch (var)
            {
                case "leaves":
                    return state.getMaterial() == Material.LEAVES
                            || state.getBlock().isLeaves(state, argument.world, argument.pos);
                case "air":
                    return state.getMaterial() == Material.AIR
                            || state.getBlock().isAir(state, argument.world, argument.pos);
                case "foliage":
                    return RCBlockLogic.isFoliage(state, argument.world, argument.pos);
                case "replaceable":
                    return state.getBlock().isReplaceable(argument.world, argument.pos);
                case "liquid":
                    return state.getMaterial() instanceof MaterialLiquid;
                case "water":
                    return state.getMaterial() == Material.WATER;
                case "lava":
                    return state.getMaterial() == Material.LAVA;
                default:
                    return true;
            }
        }

        @Override
        public Validity validity(String var, Object object)
        {
            return var.equals("leaves") || var.equals("air") || var.equals("foliage") || var.equals("replaceable")
                    || var.equals("liquid") || var.equals("water") || var.equals("lava")
                    ? Validity.KNOWN : Validity.ERROR;
        }
    }

    public static class SustainVariableType extends VariableType<Boolean, Argument, Object>
    {
        public SustainVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Argument argument)
        {
            IBlockState state = argument.state;

            switch (var)
            {
                case "trees":
                    return state.getBlock().canSustainPlant(state, argument.world, argument.pos, EnumFacing.UP, (BlockSapling) Blocks.SAPLING);
                case "mushrooms":
                    return state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.MYCELIUM;
                case "cacti":
                    return Blocks.CACTUS.canBlockStay(argument.world, argument.pos);
                default:
                    return true;
            }
        }

        @Override
        public Validity validity(String var, Object object)
        {
            return var.equals("trees") || var.equals("mushrooms") || var.equals("cacti")
                    ? Validity.KNOWN : Validity.ERROR;
        }
    }

    public static class BlocksVariableType extends VariableType<Boolean, Argument, Object>
    {
        public BlocksVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Argument argument)
        {
            IBlockState state = argument.state;

            switch (var)
            {
                case "movement":
                    return state.getMaterial().blocksMovement();
                case "light":
                    return state.getMaterial().blocksLight();
                default:
                    return true;
            }
        }

        @Override
        public Validity validity(String var, Object object)
        {
            return var.equals("movement") || var.equals("light")
                    ? Validity.KNOWN : Validity.ERROR;
        }
    }
}
