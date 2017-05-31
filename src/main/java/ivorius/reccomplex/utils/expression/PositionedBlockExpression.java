/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.utils.RCBlockLogic;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.ivtoolkit.world.WorldCache;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.text.ParseException;
import java.util.function.Function;

/**
 * Created by lukas on 16.09.16.
 */
public class PositionedBlockExpression extends BoolFunctionExpressionCache<PositionedBlockExpression.Argument, Object>
{
    public static final String BLOCK_PREFIX = "block.";
    public static final String IS_PREFIX = "is:";
    public static final String SUSTAIN_PREFIX = "sustains.";
    public static final String BLOCKS_PREFIX = "blocks:";

    public final MCRegistry registry;

    public PositionedBlockExpression(MCRegistry registry)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Block");

        this.registry = registry;

        addTypes(new BlockVariableType(BLOCK_PREFIX, "", registry), v -> v.alias("", ""));
        addTypes(new IsVariableType(IS_PREFIX, ""));
        addType(new SustainVariableType(SUSTAIN_PREFIX, ""));
        addType(new BlocksVariableType(PositionedBlockExpression.BLOCKS_PREFIX, ""));
    }

    public static class Argument
    {
        public MockWorld world;
        public BlockPos pos;
        public IBlockState state;

        public Argument(MockWorld world, BlockPos pos, IBlockState state)
        {
            this.world = world;
            this.pos = pos;
            this.state = state;
        }

        public static Argument at(MockWorld world, BlockPos pos)
        {
            return new Argument(world, pos, world.getBlockState(pos));
        }

        public static Argument at(World world, BlockPos pos)
        {
            return at(new MockWorld.Real(world), pos);
        }

        public static Argument at(WorldCache cache, BlockPos pos)
        {
            return at(new MockWorld.Cache(cache), pos);
        }
    }

    public static class BlockVariableType extends DelegatingVariableType<Boolean, Argument, Object, IBlockState, Object, BlockExpression>
    {
        public final MCRegistry registry;

        public BlockVariableType(String prefix, String suffix, MCRegistry registry)
        {
            super(prefix, suffix);
            this.registry = registry;
        }

        @Override
        public IBlockState convertEvaluateArgument(String var, Argument argument)
        {
            return argument.state;
        }

        @Override
        public BlockExpression createCache()
        {
            return new BlockExpression(registry);
        }
    }

    public static class IsVariableType extends VariableType<Boolean, Argument, Object>
    {
        public IsVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<Argument>, Boolean> parse(String var) throws ParseException
        {
            switch (var)
            {
                case "leaves":
                    return argument -> argument.get().state.getMaterial() == Material.LEAVES
                            || argument.get().state.getBlock().isLeaves(argument.get().state, argument.get().world.asWorld(), argument.get().pos);
                case "air":
                    return argument -> argument.get().state.getMaterial() == Material.AIR
                            || argument.get().state.getBlock().isAir(argument.get().state, argument.get().world.asWorld(), argument.get().pos);
                case "foliage":
                    return argument -> RCBlockLogic.isFoliage(argument.get().state, argument.get().world.asWorld(), argument.get().pos);
                case "replaceable":
                    return argument -> argument.get().state.getBlock().isReplaceable(argument.get().world.asWorld(), argument.get().pos);
                case "liquid":
                    return argument -> argument.get().state.getMaterial() instanceof MaterialLiquid;
                case "water":
                    return argument -> argument.get().state.getMaterial() == Material.WATER;
                case "lava":
                    return argument -> argument.get().state.getMaterial() == Material.LAVA;
                default:
                    throw new ParseException("Unknown Type: " + var, 0); // TODO WHERE??
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
        public Function<SupplierCache<Argument>, Boolean> parse(String var) throws ParseException
        {
            switch (var)
            {
                case "trees":
                    return argument -> argument.get().state.getBlock().canSustainPlant(argument.get().state, argument.get().world.asWorld(), argument.get().pos, EnumFacing.UP, (BlockSapling) Blocks.SAPLING);
                case "mushrooms":
                    return argument -> argument.get().state.getBlock() == Blocks.DIRT || argument.get().state.getBlock() == Blocks.GRASS || argument.get().state.getBlock() == Blocks.MYCELIUM;
                case "cacti":
                    return argument -> Blocks.CACTUS.canBlockStay(argument.get().world.asWorld(), argument.get().pos);
                default:
                    throw new ParseException("Unknown Type: " + var, 0); // TODO WHERE??
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
        public Function<SupplierCache<Argument>, Boolean> parse(String var) throws ParseException
        {
            switch (var)
            {
                case "movement":
                    return argument -> argument.get().state.getMaterial().blocksMovement();
                case "light":
                    return argument -> argument.get().state.getMaterial().blocksLight();
                default:
                    throw new ParseException("Unknown Type: " + var, 0); // TODO WHERE??
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
