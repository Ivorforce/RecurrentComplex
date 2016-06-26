/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.reccomplex.utils.algebra.Algebra;
import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by lukas on 23.03.15.
 */
public class PrefixedTypeExpressionCache<T> extends ExpressionCache<T>
{
    protected final SortedSet<VariableType<T>> types = new TreeSet<>();

    public PrefixedTypeExpressionCache(Algebra<T> algebra, String expression)
    {
        super(algebra, expression);
    }

    public PrefixedTypeExpressionCache(Algebra<T> algebra, T emptyResult, String emptyResultRepresentation, String expression)
    {
        super(algebra, emptyResult, emptyResultRepresentation, expression);
    }

    public void addType(VariableType<T> type)
    {
        types.add(type);
    }

    public void removeType(VariableType<T> type)
    {
        types.remove(type);
    }

    public SortedSet<VariableType<T>> types()
    {
        return Collections.unmodifiableSortedSet(types);
    }

    protected VariableType<T> type(final String var)
    {
        return types.stream().filter(input -> var.startsWith(input.prefix)).findFirst().orElseGet(() -> null);
    }

    protected boolean isKnownVariable(String var, Object... args)
    {
        VariableType<T> type = type(var);
        return type != null && type.isKnown(var.substring(type.prefix.length()), args);
    }

    protected T evaluateVariable(String var, Object... args)
    {
        VariableType<T> type = type(var);
        return type != null ? type.evaluate(var.substring(type.prefix.length()), args) : null;
    }

    @Override
    public boolean containsUnknownVariables()
    {
        return containsUnknownVariables(new Object[0]);
    }

    protected boolean containsUnknownVariables(final Object... args)
    {
        return parsedExpression != null && !parsedExpression.walkVariables(s -> isKnownVariable(s, args));
    }

    protected T evaluate(final Object... args)
    {
        return parsedExpression != null ? parsedExpression.evaluate(var -> evaluateVariable(var, args)) : null;
    }

    @Nonnull
    @Override
    public String getDisplayString()
    {
        return getDisplayString(new Object[0]);
    }

    @Nonnull
    public String getDisplayString(final Object... args)
    {
        return parsedExpression != null ? parsedExpression.toString(input -> {
            VariableType<T> type = type(input);
            return type != null
                    ? type.getRepresentation(input.substring(type.prefix.length()), args)
                    : EnumChatFormatting.RED + input;
        }) : EnumChatFormatting.RED + expression;
    }

    public static abstract class VariableType<T> implements Comparable<VariableType>
    {
        protected String prefix;

        public VariableType(String prefix)
        {
            this.prefix = prefix;
        }

        public abstract T evaluate(String var, Object... args);

        public abstract boolean isKnown(String var, Object... args);

        public abstract String getRepresentation(String var, Object... args);

        @Override
        public int compareTo(@Nonnull VariableType o)
        {
            return o.prefix.compareTo(prefix);
        }
    }
}
