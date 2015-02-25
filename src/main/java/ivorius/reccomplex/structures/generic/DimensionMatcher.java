/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.primitives.Ints;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.Algebra;
import ivorius.reccomplex.utils.BoolAlgebra;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.Visitor;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by lukas on 19.09.14.
 */
public class DimensionMatcher extends ExpressionCache implements Predicate<WorldProvider>
{
    public static final String DIMENSION_TYPE_PREFIX = "$";

    public DimensionMatcher(String expression)
    {
        super(expression);
    }

    public static boolean isKnownVariable(final String var, final Collection<Integer> dimensions)
    {
        Integer dimID;
        if (var.startsWith(DIMENSION_TYPE_PREFIX))
                return DimensionDictionary.allRegisteredTypes().contains(var.substring(DIMENSION_TYPE_PREFIX.length()));
        else if ((dimID = Ints.tryParse(var)) != null)
            return dimensions.contains(dimID);
        else
            return false;
    }

    public boolean containsUnknownVariables()
    {
        if (parsedExpression != null)
        {
            final Collection<Integer> dimensions = Arrays.asList(DimensionManager.getIDs());

            return !parsedExpression.walkVariables(new Visitor<String>()
            {
                @Override
                public boolean visit(final String s)
                {
                    return isKnownVariable(s, dimensions);
                }
            });
        }

        return true;
    }

    @Override
    public boolean apply(final WorldProvider input)
    {
        return parsedExpression != null && parsedExpression.evaluate(new Function<String, Boolean>()
        {
            @Override
            public Boolean apply(String var)
            {
                if (var.startsWith(DIMENSION_TYPE_PREFIX))
                    return DimensionDictionary.dimensionMatchesType(input,
                            var.substring(DIMENSION_TYPE_PREFIX.length()));

                Integer dimID = Ints.tryParse(var);
                return dimID != null && input.dimensionId == dimID;
            }
        });
    }

    @Override
    public String getDisplayString()
    {
        final Collection<Integer> dimensions = Arrays.asList(DimensionManager.getIDs());

        return parsedExpression != null ? parsedExpression.toString(new Function<String, String>()
        {
            @Nullable
            @Override
            public String apply(String input)
            {
                return (isKnownVariable(input, dimensions) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) + input + EnumChatFormatting.RESET;
            }
        }) : EnumChatFormatting.RED + expression;
    }
}
