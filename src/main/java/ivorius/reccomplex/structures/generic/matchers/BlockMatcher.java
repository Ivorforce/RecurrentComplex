/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.RCBoolAlgebra;
import ivorius.reccomplex.utils.Visitor;
import net.minecraft.block.Block;
import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 03.03.15.
 */
public class BlockMatcher extends ExpressionCache<Boolean> implements Predicate<BlockMatcher.BlockFragment>
{
    public static final String METADATA_PREFIX = "#";

    public BlockMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), expression);
    }

    public static String of(Block block)
    {
        return Block.blockRegistry.getNameForObject(block);
    }

    public static String of(Block block, Integer metadata)
    {
        return String.format("%s & %s%d", Block.blockRegistry.getNameForObject(block), METADATA_PREFIX, metadata);
    }

    public static String of(Block block, IntegerRange range)
    {
        return String.format("%s & %s%d-%d", Block.blockRegistry.getNameForObject(block), METADATA_PREFIX, range.min, range.max);
    }

    public static boolean isKnownVariable(final String var)
    {
        return var.startsWith(METADATA_PREFIX)
                ? parseMetadataExp(var.substring(METADATA_PREFIX.length())) != null
                : Block.getBlockFromName(var) != null;
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

    public boolean containsUnknownVariables()
    {
        if (parsedExpression != null)
        {
            return !parsedExpression.walkVariables(new Visitor<String>()
            {
                @Override
                public boolean visit(final String s)
                {
                    return isKnownVariable(s);
                }
            });
        }

        return true;
    }

    @Override
    public boolean apply(final BlockFragment input)
    {
        return parsedExpression != null && parsedExpression.evaluate(new Function<String, Boolean>()
        {
            @Override
            public Boolean apply(String var)
            {
                if (var.startsWith(METADATA_PREFIX))
                {
                    IntegerRange range = parseMetadataExp(var.substring(METADATA_PREFIX.length()));
                    return range != null && input.metadata >= range.min && input.metadata <= range.max;
                }

                return input.block == Block.getBlockFromName(var);
            }
        });
    }

    @Nonnull
    @Override
    public String getDisplayString()
    {
        return parsedExpression != null ? parsedExpression.toString(new Function<String, String>()
        {
            @Nullable
            @Override
            public String apply(String input)
            {
                EnumChatFormatting variableColor = isKnownVariable(input) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW;

                if (input.startsWith(METADATA_PREFIX))
                    return EnumChatFormatting.BLUE + METADATA_PREFIX + variableColor + input.substring(METADATA_PREFIX.length()) + EnumChatFormatting.RESET;
                return variableColor + input + EnumChatFormatting.RESET;
            }
        }) : EnumChatFormatting.RED + expression;
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
}
