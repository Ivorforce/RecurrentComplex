/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.text.TextFormatting;
import ivorius.reccomplex.utils.algebra.Algebra;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
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

    @Nullable
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
        return parsedExpression != null && !parsedExpression.walkVariables(s -> isKnownVariable(s.identifier, args));
    }

    @Override
    protected void testVariables(@Nonnull Algebra.Expression<T> expression) throws ParseException
    {
        super.testVariables(expression);

        // Pre-setup
        if (types != null)
        {
            ParseException[] exception = new ParseException[1];
            expression.walkVariables(s -> {
                if (type(s.identifier) == null)
                    exception[0] = new ParseException(String.format("Type of '%s' unknown", s.identifier), s.index);
                return true;
            });

            if (exception[0] != null)
                throw exception[0];
        }
    }

    protected T evaluate(final Object... args)
    {
        return parsedExpression != null ? parsedExpression.evaluate(var -> evaluateVariable(var, args)) : emptyExpressionResult;
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

        public String getRepresentation(String var, Object... args)
        {
            TextFormatting variableColor = isKnown(var, args) ? TextFormatting.GREEN : TextFormatting.YELLOW;
            return TextFormatting.BLUE + prefix + variableColor + var + TextFormatting.BLUE + suffix + TextFormatting.RESET;
        }

        @Override
        public int compareTo(@Nonnull VariableType o)
        {
            return o.prefix.compareTo(prefix);
        }

        public AliasType<T, VariableType<T>> alias(String prefix, String suffix)
        {
            return new AliasType<>(prefix, suffix, this);
        }
    }
}
