/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.utils.BlockStates;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by lukas on 03.03.15.
 */
public class BlockMatcher extends FunctionExpressionCache<Boolean, IBlockState, Object> implements Predicate<IBlockState>
{
    public static final String BLOCK_ID_PREFIX = "id=";
    public static final String METADATA_PREFIX = "metadata=";

    public BlockMatcher(MCRegistry registry, String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Block", expression);

        addTypes(new BlockVariableType(BLOCK_ID_PREFIX, "", registry), t -> t.alias("", ""));
        addTypes(new MetadataVariableType(METADATA_PREFIX, ""), t -> t.alias("#", ""));

        testVariables();
    }

    public static String of(MCRegistry registry, Block block)
    {
        return registry.idFromBlock(block).toString();
    }

    public static String of(MCRegistry registry, Block block, Integer metadata)
    {
        return String.format("%s & %s%d", registry.idFromBlock(block), METADATA_PREFIX, metadata);
    }

    public static String of(MCRegistry registry, Block block, IntegerRange range)
    {
        return String.format("%s & %s%d-%d", registry.idFromBlock(block), METADATA_PREFIX, range.min, range.max);
    }

    @Override
    public boolean test(final IBlockState input)
    {
        return evaluate(input);
    }

    public class BlockVariableType extends VariableType<Boolean, IBlockState, Object>
    {
        public MCRegistry registry;

        public BlockVariableType(String prefix, String suffix, MCRegistry registry)
        {
            super(prefix, suffix);
            this.registry = registry;
        }

        @Override
        public Boolean evaluate(String var, IBlockState state)
        {
            return state.getBlock() == registry.blockFromID(new ResourceLocation(var));
        }

        @Override
        public Validity validity(String var, Object object)
        {
            ResourceLocation location = new ResourceLocation(var); // Since MC defaults to air now
            return registry.blockFromID(location) != Blocks.AIR || location.equals(Block.REGISTRY.getNameForObject(Blocks.AIR))
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    public class MetadataVariableType extends VariableType<Boolean, IBlockState, Object>
    {
        public MetadataVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        public IntegerRange parseMetadataExp(String var)
        {
            if (var.contains("-"))
            {
                List<String> split = Splitter.on('-').splitToList(var);

                if (split.size() != 2)
                    return null;

                Integer left = parseMetadata(split.get(0));
                Integer right = parseMetadata(split.get(1));

                return left != null && right != null ? new IntegerRange(Math.min(left, right), Math.max(left, right)) : null;
            }

            Integer meta = parseMetadata(var);
            return meta != null ? new IntegerRange(meta, meta) : null;
        }

        public Integer parseMetadata(String var)
        {
            Integer integer = Ints.tryParse(var);
            return integer != null && integer >= 0 && integer < 16 ? integer : null;
        }

        @Override
        public Boolean evaluate(String var, IBlockState state)
        {
            IntegerRange range = parseMetadataExp(var);
            int metadata = BlockStates.toMetadata(state);

            return range != null && metadata >= range.min && metadata <= range.max;
        }

        @Override
        public Validity validity(String var, Object object)
        {
            return parseMetadataExp(var) != null ? Validity.KNOWN : Validity.ERROR;
        }
    }
}
