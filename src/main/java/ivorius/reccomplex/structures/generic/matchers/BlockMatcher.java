/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.utils.ExpressionCaches;
import ivorius.reccomplex.utils.PrefixedTypeExpressionCache;
import ivorius.reccomplex.utils.RCBoolAlgebra;
import net.minecraft.block.Block;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

/**
 * Created by lukas on 03.03.15.
 */
public class BlockMatcher extends PrefixedTypeExpressionCache<Boolean> implements Predicate<BlockMatcher.BlockFragment>
{
    public static final String METADATA_PREFIX = "#";

    public BlockMatcher(MCRegistry registry, String expression)
    {
        super(RCBoolAlgebra.algebra(), true, EnumChatFormatting.GREEN + "Any Block", expression);

        addType(new BlockVariableType("", registry));
        addType(new MetadataVariableType(METADATA_PREFIX));
    }

    public static String of(MCRegistry registry, Block block)
    {
        return registry.idFromBlock(block);
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
    public boolean apply(final BlockFragment input)
    {
        return evaluate(input);
    }

    public static class BlockFragment
    {
        public Block block;
        public int metadata;

        public BlockFragment(Block block, int metadata)
        {
            this.block = block;
            this.metadata = metadata;
        }
    }

    public static class BlockVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public MCRegistry registry;

        public BlockVariableType(String prefix, MCRegistry registry)
        {
            super(prefix);
            this.registry = registry;
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return ((BlockFragment) args[0]).block == registry.blockFromID(var);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return registry.blockFromID(var) != null;
        }
    }

    public static class MetadataVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public MetadataVariableType(String prefix)
        {
            super(prefix);
        }

        public static IntegerRange parseMetadataExp(String var)
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

        public static Integer parseMetadata(String var)
        {
            Integer integer = Ints.tryParse(var);
            return integer != null && integer >= 0 && integer < 16 ? integer : null;
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            IntegerRange range = parseMetadataExp(var);
            int metadata = ((BlockFragment) args[0]).metadata;

            return range != null && metadata >= range.min && metadata <= range.max;
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return parseMetadataExp(var) != null;
        }
    }
}
