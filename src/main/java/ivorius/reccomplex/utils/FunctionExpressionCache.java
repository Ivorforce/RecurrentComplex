/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Lists;
import net.minecraft.util.text.TextFormatting;
import ivorius.reccomplex.utils.algebra.Algebra;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

/**
 * Created by lukas on 23.03.15.
 */
public class FunctionExpressionCache<T> extends ExpressionCache<T>
{
    protected final SortedSet<VariableType<T>> types = new TreeSet<>();

    public FunctionExpressionCache(Algebra<T> algebra, String expression)
    {
        super(algebra, expression);
    }

    public FunctionExpressionCache(Algebra<T> algebra, T emptyResult, String emptyResultRepresentation, String expression)
    {
        super(algebra, emptyResult, emptyResultRepresentation, expression);
    }

    public void addType(VariableType<T> type)
    {
        types.add(type);
    }

    public void addTypes(Collection<VariableType<T>> types)
    {
        this.types.addAll(types);
    }

    public void addTypes(VariableType<T> type, Function<VariableType<T>, VariableType<T>> functions)
    {
        addTypes(IvLists.enumerate(type, functions));
    }

    public void removeType(VariableType<T> type)
    {
        types.remove(type);
    }

    public Set<VariableType<T>> types()
    {
        return Collections.unmodifiableSet(types);
    }

    protected VariableType<T> type(final String var)
    {
        return types.stream()
                .filter(input -> var.startsWith(input.prefix) && var.endsWith(input.suffix))
                .findFirst().orElseGet(() -> null);
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
                    : TextFormatting.RED + input;
        }) : TextFormatting.RED + expression;
    }

    public static class AliasType<T, V extends VariableType<T>> extends VariableType<T>
    {
        public V parent;

        public AliasType(String prefix, String suffix, V parent)
        {
            super(prefix, suffix);
            this.parent = parent;
        }

        @Override
        public T evaluate(String var, Object... args)
        {
            return parent.evaluate(var, args);
        }

        @Override
        public boolean isKnown(String var, Object... args)
        {
            return parent.isKnown(var, args);
        }

        @Override
        public String getRepresentation(String var, Object... args)
        {
            return parent.getRepresentation(var, args);
        }
    }

    public static abstract class VariableType<T> implements Comparable<VariableType>
    {
        protected String prefix;
        protected String suffix;

        public VariableType(String prefix, String suffix)
        {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public abstract T evaluate(String var, Object... args);

        public abstract boolean isKnown(String var, Object... args);

        public abstract String getRepresentation(String var, Object... args);

        @Override
        public int compareTo(@Nonnull VariableType o)
        {
            return o.prefix.compareTo(prefix);
        }

        public AliasType<T, VariableType<T>> alias(String prefix, String suffix)
        {
            return new AliasType<T, VariableType<T>>(prefix, suffix, this);
        }
    }
}
