/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils.expression;

import com.google.common.primitives.Ints;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import joptsimple.internal.Strings;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Function;

/**
 * Created by lukas on 19.09.14.
 */
public class DimensionMatcher extends BoolFunctionExpressionCache<WorldProvider, Object>
{
    public static final String DIMENSION_ID_PREFIX = "id=";
    public static final String DIMENSION_TYPE_PREFIX = "type=";

    public DimensionMatcher()
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any Dimension");

        addTypes(new DimensionVariableType(DIMENSION_ID_PREFIX, ""), t -> t.alias("", ""));
        addTypes(new DimensionDictVariableType(DIMENSION_TYPE_PREFIX, ""), t -> t.alias("$", ""));
    }

    public static String ofTypes(String... dimensionTypes)
    {
        return DIMENSION_TYPE_PREFIX + Strings.join(dimensionTypes, " & " + DIMENSION_TYPE_PREFIX);
    }

    protected class DimensionVariableType extends VariableType<Boolean, WorldProvider, Object>
    {
        public DimensionVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<WorldProvider>, Boolean> parse(String var)
        {
            Integer dimID = Ints.tryParse(var);
            return provider -> dimID != null && provider.get().getDimension() == dimID;
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
        public Function<SupplierCache<WorldProvider>, Boolean> parse(String var)
        {
            return provider -> DimensionDictionary.dimensionMatchesType(provider.get(), var);
        }

        @Override
        public Validity validity(String var, Object args)
        {
            return DimensionDictionary.allRegisteredTypes().contains(var)
                    ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
