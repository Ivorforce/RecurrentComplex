/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.expression;

import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.algebra.FunctionExpressionCache;
import ivorius.reccomplex.utils.algebra.SupplierCache;
import net.minecraft.util.text.TextFormatting;

import java.text.ParseException;
import java.util.function.Function;

/**
 * Created by lukas on 07.09.16.
 */
public abstract class DelegatingVariableType<T, A, U, CA, CU, C extends FunctionExpressionCache<T, CA, CU>> extends FunctionExpressionCache.VariableType<T, A, U>
{
    protected C cache;

    public DelegatingVariableType(String prefix, String suffix)
    {
        super(prefix, suffix);
    }

    protected C cache()
    {
        return cache != null ? cache : (cache = createCache());
    }

    public CA convertEvaluateArgument(String var, A a)
    {
        return convertArgument(var, a);
    }

    public CU convertIsKnownArgument(String var, U u)
    {
        //noinspection unchecked
        return (CU) convertArgument(var, (A) u);
    }

    public CA convertArgument(String var, A a)
    {
        return null;
    }

    @Override
    public Function<SupplierCache<A>, T> parse(String var) throws ParseException
    {
        C c = ExpressionCache.of(createCache(), var);
        return a -> c.evaluate(() -> convertEvaluateArgument(var, a.get()));
    }

    @Override
    public FunctionExpressionCache.Validity validity(String var, U u)
    {
        return cache().variableValidity(var, convertIsKnownArgument(var, u));
    }

    @Override
    protected String getVarRepresentation(String var, U u)
    {
        return cache().variableDisplayString(var, convertIsKnownArgument(var, u));
    }

    @Override
    public String getRepresentation(String var, String prefix, String suffix, U u)
    {
        int prefixSymbolStart = prefix.length() > 0 ? prefix.length() - 1 : 0;
        return TextFormatting.BLUE + prefix.substring(0, prefixSymbolStart)
                + TextFormatting.YELLOW + prefix.substring(prefixSymbolStart)
                + TextFormatting.RESET + getVarRepresentation(var, u) + TextFormatting.RESET
                + TextFormatting.BLUE + suffix;
    }

    public abstract C createCache();
}
