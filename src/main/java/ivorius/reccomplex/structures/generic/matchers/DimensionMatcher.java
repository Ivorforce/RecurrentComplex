/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import joptsimple.internal.Strings;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Predicate;

/**
 * Created by lukas on 19.09.14.
 */
public class DimensionMatcher extends FunctionExpressionCache<Boolean, WorldProvider, Object> implements Predicate<WorldProvider>
{
    public static final String DIMENSION_ID_PREFIX = "id=";
    public static final String DIMENSION_TYPE_PREFIX = "type=";

    public DimensionMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Dimension", expression);
        addTypes(new DimensionVariableType(DIMENSION_ID_PREFIX, ""), t -> t.alias("", ""));
        addTypes(new DimensionDictVariableType(DIMENSION_TYPE_PREFIX, ""), t -> t.alias("$", ""));

        testVariables();
    }

    public static String ofTypes(String... dimensionTypes)
    {
        return DIMENSION_TYPE_PREFIX + Strings.join(dimensionTypes, " & " + DIMENSION_TYPE_PREFIX);
    }

    @Override
    public boolean test(final WorldProvider input)
    {
        return evaluate(input);
    }

    protected class DimensionVariableType extends VariableType<Boolean, WorldProvider, Object>
    {
        public DimensionVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, WorldProvider provider)
        {
            Integer dimID = Ints.tryParse(var);
            return dimID != null && provider.getDimension() == dimID;
        }

        @Override
        public Validity validity(final String var, final Object args)
        {
            Integer dimID = Ints.tryParse(var);
            return dimID != null && ArrayUtils.contains(DimensionManager.getIDs(), dimID)
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }

    protected class DimensionDictVariableType extends VariableType<Boolean, WorldProvider, Object>
    {
        public DimensionDictVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, WorldProvider provider)
        {
            return DimensionDictionary.dimensionMatchesType(provider, var);
        }

        @Override
        public Validity validity(String var, Object args)
        {
            return DimensionDictionary.allRegisteredTypes().contains(var)
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
