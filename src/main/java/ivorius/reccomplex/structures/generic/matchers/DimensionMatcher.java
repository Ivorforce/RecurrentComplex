/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.base.Predicate;
import com.google.common.primitives.Ints;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.*;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import joptsimple.internal.Strings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by lukas on 19.09.14.
 */
public class DimensionMatcher extends PrefixedTypeExpressionCache<Boolean> implements Predicate<WorldProvider>
{
    public static final String DIMENSION_TYPE_PREFIX = "$";

    public DimensionMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, EnumChatFormatting.GREEN + "Any Dimension", expression);
        addType(new DimensionVariableType(""));
        addType(new DimensionDictVariableType(DIMENSION_TYPE_PREFIX));
    }

    public static String ofTypes(String... dimensionTypes)
    {
        return DIMENSION_TYPE_PREFIX + Strings.join(dimensionTypes, " & " + DIMENSION_TYPE_PREFIX);
    }

    @Override
    public boolean apply(final WorldProvider input)
    {
        return evaluate(input);
    }

    protected static class DimensionVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public DimensionVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            Integer dimID = Ints.tryParse(var);
            return dimID != null && ((WorldProvider) args[0]).getDimensionId() == dimID;
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            Integer dimID = Ints.tryParse(var);
            return dimID != null && ArrayUtils.contains(DimensionManager.getIDs(), dimID);
        }
    }

    protected static class DimensionDictVariableType extends ExpressionCaches.SimpleVariableType<Boolean>
    {
        public DimensionDictVariableType(String prefix)
        {
            super(prefix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            return DimensionDictionary.dimensionMatchesType((WorldProvider) args[0], var);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return DimensionDictionary.allRegisteredTypes().contains(var);
        }
    }
}
