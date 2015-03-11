/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.primitives.Ints;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.utils.RCBoolAlgebra;
import ivorius.reccomplex.utils.Visitor;
import joptsimple.internal.Strings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by lukas on 19.09.14.
 */
public class DimensionMatcher extends ExpressionCache<Boolean> implements Predicate<WorldProvider>
{
    public static final String DIMENSION_TYPE_PREFIX = "$";

    public DimensionMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), expression);
    }

    public static String ofTypes(String... dimensionTypes)
    {
        return DIMENSION_TYPE_PREFIX + Strings.join(dimensionTypes, " & " + DIMENSION_TYPE_PREFIX);
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

    @Nonnull
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
                EnumChatFormatting variableColor = isKnownVariable(input, dimensions) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW;

                if (input.startsWith(DIMENSION_TYPE_PREFIX))
                    return EnumChatFormatting.BLUE + DIMENSION_TYPE_PREFIX + variableColor + input.substring(DIMENSION_TYPE_PREFIX.length()) + EnumChatFormatting.RESET;
                return variableColor + input + EnumChatFormatting.RESET;
            }
        }) : EnumChatFormatting.RED + expression;
    }
}
