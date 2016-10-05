/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.utils.algebra.BoolFunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.RCBoolAlgebra;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by lukas on 19.09.14.
 */
public class RegistryMatcher extends BoolFunctionExpressionCache<LeveledRegistry, LeveledRegistry>
{
    public static final String HAS_PREFIX = "has:";

    public RegistryMatcher(String expression)
    {
        super(RCBoolAlgebra.algebra(), true, TextFormatting.GREEN + "Any", expression);

        addTypes(new RegistryVariableType(HAS_PREFIX, ""), t -> t.alias("#", ""));

        testVariables();
    }

    protected static class RegistryVariableType extends VariableType<Boolean, LeveledRegistry, LeveledRegistry>
    {
        public RegistryVariableType(String prefix, String suffix)
        {
            super(prefix, suffix);
        }

        @Override
        public Boolean evaluate(String var, LeveledRegistry registry)
        {
            return registry.has(var);
        }

        @Override
        public Validity validity(String var, LeveledRegistry registry)
        {
            return registry.has(var) ? Validity.KNOWN : Validity.UNKNOWN;
        }
    }
}
