/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        return Iterables.find(types, new Predicate<VariableType<T>>()
        {
            @Override
            public boolean apply(@Nullable VariableType input)
            {
                return var.startsWith(input.prefix);
            }
        }, null);
    }

    protected boolean isKnownVariable(String var, Object... args)
    {
        VariableType<T> type = type(var);
        return type != null && type.isKnown(var, args);
    }

    protected T evaluateVariable(String var, Object... args)
    {
        VariableType<T> type = type(var);
        return type != null ? type.evaluate(var, args) : null;
    }

    protected boolean containsUnknownVariables(final Object... args)
    {
        if (parsedExpression != null)
        {
            return !parsedExpression.walkVariables(new Visitor<String>()
            {
                @Override
                public boolean visit(final String s)
                {
                    return isKnownVariable(s, args);
                }
            });
        }

        return true;
    }

    protected T evaluate(final Object... args)
    {
        return parsedExpression != null ? parsedExpression.evaluate(new Function<String, T>()
        {
            @Override
            public T apply(String var)
            {
                return evaluateVariable(var, args);
            }
        }) : null;
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
        return parsedExpression != null ? parsedExpression.toString(new Function<String, String>()
        {
            @Nullable
            @Override
            public String apply(String input)
            {
                VariableType<T> type = type(input);
                return type != null
                        ? type.getRepresentation(input.substring(type.prefix.length()), args)
                        : EnumChatFormatting.RED + input;
            }
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
