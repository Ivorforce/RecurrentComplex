/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.matchers;

import ivorius.reccomplex.utils.FunctionExpressionCache;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Created by lukas on 07.09.16.
 */
public abstract class DelegatingVariableType<T, A, U, CA, CU, C extends FunctionExpressionCache<T, CA, CU>> extends FunctionExpressionCache.VariableType<T, A, U>
{
    public DelegatingVariableType(String prefix, String suffix)
    {
        super(prefix, suffix);
    }

    public CA convertEvaluateArgument(A a)
    {
        return null;
    }

    public CU convertIsKnownArgument(U u)
    {
        return null;
    }

    @Override
    public T evaluate(String var, A a)
    {
        return createEvaluateCache(var, a).evaluate(convertEvaluateArgument(a));
    }

    @Override
    public FunctionExpressionCache.Validity validity(String var, U u)
    {
        return createUnknownCache(var, u).validity(convertIsKnownArgument(u));
    }

    @Override
    public String getRepresentation(String var, U u)
    {
        return TextFormatting.GREEN + prefix.substring(0, prefix.length() - 1)
                + TextFormatting.YELLOW + prefix.substring(prefix.length() - 1)
                + TextFormatting.RESET + createUnknownCache(var, u).getDisplayString(convertIsKnownArgument(u)) + TextFormatting.RESET;
    }

    public C createEvaluateCache(String var, A a)
    {
        return createCache(var);
    }

    public C createUnknownCache(String var, U u)
    {
        return createCache(var);
    }

    protected C createCache(String var)
    {
        throw new NotImplementedException("createCache not implemented!");
    }
}
