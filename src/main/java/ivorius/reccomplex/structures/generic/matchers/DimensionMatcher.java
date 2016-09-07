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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by lukas on 19.09.14.
 */
public class DimensionMatcher extends FunctionExpressionCache<Boolean> implements Predicate<WorldProvider>
{
    public static final String DIMENSION_ID_PREFIX = "id=";
    public static final String DIMENSION_TYPE_PREFIX = "type=";

    public DimensionMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Dimension", expression);
        addTypes(new DimensionVariableType(DIMENSION_ID_PREFIX, ""), t -> t.alias("", ""));
        addTypes(new DimensionDictVariableType( DIMENSION_TYPE_PREFIX, ""), t -> t.alias("$", ""));

        testVariables();
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

    protected static class DimensionVariableType extends VariableType<Boolean>
    {
        public DimensionVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, Object... args)
        {
            Integer dimID = Ints.tryParse(var);
            return dimID != null && ((WorldProvider) args[0]).getDimension() == dimID;
        }

        @Override
        public boolean isKnown(final String var, final Object... args)
        {
            Integer dimID = Ints.tryParse(var);
            return dimID != null && ArrayUtils.contains(DimensionManager.getIDs(), dimID);
        }
    }

    protected static class DimensionDictVariableType extends VariableType<Boolean>
    {
        public DimensionDictVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
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
