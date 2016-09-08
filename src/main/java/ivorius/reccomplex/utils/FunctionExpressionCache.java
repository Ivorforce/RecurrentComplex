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
public class FunctionExpressionCache<T, A, U> extends ExpressionCache<T>
{
    protected final SortedSet<VariableType<T, A, U>> types = new TreeSet<>();

    public FunctionExpressionCache(Algebra<T> algebra, String expression)
    {
        super(algebra, expression);
    }

    public FunctionExpressionCache(Algebra<T> algebra, T emptyResult, String emptyResultRepresentation, String expression)
    {
        super(algebra, emptyResult, emptyResultRepresentation, expression);
    }

    public void addType(VariableType<T, A, U> type)
    {
        types.add(type);
    }

    public void addTypes(Collection<VariableType<T, A, U>> types)
    {
        this.types.addAll(types);
    }

    public void addTypes(VariableType<T, A, U> type, Function<VariableType<T, A, U>, VariableType<T, A, U>>... functions)
    {
        addType(type);
        addTypes(IvLists.enumerate(type, functions));
    }

    public void removeType(VariableType<T, A, U> type)
    {
        types.remove(type);
    }

    public Set<VariableType<T, A, U>> types()
    {
        return Collections.unmodifiableSet(types);
    }

    @Nullable
    public VariableType<T, A, U> type(final String var)
    {
        return types.stream()
                .filter(input -> var.startsWith(input.prefix) && var.endsWith(input.suffix))
                .findFirst().orElseGet(() -> null);
    }

    public Validity variableValidity(String var, U u)
    {
        VariableType<T, A, U> type = type(var);
        return type == null ? Validity.ERROR : type.validity(var.substring(type.prefix.length()), u);
    }

    public T evaluateVariable(String var, A a)
    {
        VariableType<T, A, U> type = type(var);
        return type != null ? type.evaluate(var.substring(type.prefix.length()), a) : null;
    }

    public Validity validity(final U u)
    {
        Validity[] worst = new Validity[]{Validity.KNOWN};
        if (parsedExpression != null)
        {
            parsedExpression.walkVariables(s -> {
                Validity validity = variableValidity(s.identifier, u);
                if (validity.ordinal() > worst[0].ordinal())
                    worst[0] = validity;
                return validity.ordinal() < Validity.values().length - 1;
            });
            return worst[0];
        }
        return Validity.ERROR;
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
                VariableType<T, A, U> type = type(s.identifier);
                if (type == null) // TODO Ask the type for an error too (requires an instance of U)
                    exception[0] = new ParseException(String.format("Type of '%s' unknown", s.identifier), s.index);
                return true;
            });

            if (exception[0] != null)
                throw exception[0];
        }
    }

    public T evaluate(final A a)
    {
        return parsedExpression != null ? parsedExpression.evaluate(var -> evaluateVariable(var, a)) : emptyExpressionResult;
    }

    @Nonnull
    public String getDisplayString(final U u)
    {
        return parsedExpression != null ? parsedExpression.toString(input -> {
            VariableType<T, A, U> type = type(input);
            return type != null
                    ? type.getRepresentation(input.substring(type.prefix.length()), type.prefix, type.suffix, u)
                    : TextFormatting.RED + input;
        }) : TextFormatting.RED + expression;
    }

    public enum Validity
    {
        KNOWN,
        UNKNOWN,
        ERROR
    }

    public static class AliasType<T, A, U, V extends VariableType<T, A, U>> extends VariableType<T, A, U>
    {
        public V parent;

        public AliasType(String prefix, String suffix, V parent)
        {
            super(prefix, suffix);
            this.parent = parent;
        }

        @Override
        public T evaluate(String var, A a)
        {
            return parent.evaluate(var, a);
        }

        @Override
        public Validity validity(String var, U u)
        {
            return parent.validity(var, u);
        }

        @Override
        public String getRepresentation(String var, String prefix, String suffix, U u)
        {
            return parent.getRepresentation(var, prefix, suffix, u);
        }
    }

    public static abstract class VariableType<T, A, U> implements Comparable<VariableType>
    {
        protected String prefix;
        protected String suffix;

        public VariableType(String prefix, String suffix)
        {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public String getSuffix()
        {
            return suffix;
        }

        public abstract T evaluate(String var, A a);

        public abstract Validity validity(String var, U u);

        public TextFormatting getRepresentation(Validity validity)
        {
            return validity == Validity.KNOWN ? TextFormatting.GREEN
                    : validity == Validity.UNKNOWN ? TextFormatting.YELLOW
                    : TextFormatting.RED;
        }

        public String getRepresentation(String var, String prefix, String suffix, U u)
        {
            return TextFormatting.BLUE + prefix
                    + getRepresentation(validity(var, u)) + var
                    + TextFormatting.BLUE + suffix + TextFormatting.RESET;
        }

        @Override
        public int compareTo(@Nonnull VariableType o)
        {
            return o.prefix.compareTo(prefix);
        }

        public AliasType<T, A, U, VariableType<T, A, U>> alias(String prefix, String suffix)
        {
            return new AliasType<>(prefix, suffix, this);
        }
    }
}
