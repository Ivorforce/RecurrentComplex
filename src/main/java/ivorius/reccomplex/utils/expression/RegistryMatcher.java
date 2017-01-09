/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import net.minecraft.util.text.TextFormatting;

import java.text.ParseException;
import java.util.function.Function;

/**
 * Created by lukas on 19.09.14.
 */
public class RegistryMatcher extends BoolFunctionExpressionCache<LeveledRegistry, LeveledRegistry>
{
    public static final String HAS_PREFIX = "has:";

    public RegistryMatcher()
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any");

        addTypes(new RegistryVariableType(HAS_PREFIX, ""), t -> t.alias("#", ""));
    }

    protected static class RegistryVariableType extends VariableType<Boolean, LeveledRegistry, LeveledRegistry>
    {
        public RegistryVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Function<SupplierCache<LeveledRegistry>, Boolean> parse(String var) throws ParseException
        {
            return registry -> registry.get().has(var);
        }

        @Override
        public Validity validity(String var, LeveledRegistry registry)
        {
            return registry.has(var) ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
