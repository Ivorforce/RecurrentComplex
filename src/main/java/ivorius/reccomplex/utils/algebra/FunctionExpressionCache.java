/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

import ivorius.ivtoolkit.util.IvLists;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by lukas on 23.03.15.
 */
public class FunctionExpressionCache<T, A, U> extends ExpressionCache<T>
{
    protected final SortedSet<VariableType<T, ? super A, ? super U>> types = new TreeSet<>();

    public FunctionExpressionCache(Algebra<T> algebra)
    {
        super(algebra);
    }

    public FunctionExpressionCache(Algebra<T> algebra, T emptyResult, String emptyResultRepresentation)
    {
        super(algebra, emptyResult, emptyResultRepresentation);
    }

    public void addType(VariableType<T, ? super A, ? super U> type)
    {
        types.add(type);
    }

    public void addTypes(Collection<VariableType<T, ? super A, ? super U>> types)
    {
        this.types.addAll(types);
    }

    @SafeVarargs
    public final void addTypes(VariableType<T, ? super A, ? super U> type, Function<VariableType<T, ? super A, ? super U>, VariableType<T, ? super A, ? super U>>... functions)
    {
        addType(type);
        addTypes(IvLists.enumerate(type, functions));
    }

    public void removeType(VariableType<T, A, U> type)
    {
        types.remove(type);
    }

    public Set<VariableType<T, ? super A, ? super U>> types()
    {
        return Collections.unmodifiableSet(types);
    }

    @Nullable
    public VariableType<T, ? super A, ? super U> type(final String var)
    {
        return types.stream()
                .filter(input -> var.startsWith(input.prefix) && var.endsWith(input.suffix))
                .findFirst().orElseGet(() -> null);
    }

    public Validity variableValidity(String var, U u)
    {
        VariableType<T, ? super A, ? super U> type = type(var);
        return type == null ? Validity.ERROR : type.validity(var.substring(type.prefix.length()), u);
    }

    public Validity validity(final U u)
    {
        Validity[] worst = new Validity[]{Validity.KNOWN};
        if (parsedExpression != null)
        {
            parsedExpression.walkVariables(s ->
            {
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
    protected Algebra.VariableParser<Function<? extends SupplierCache<? super A>, T>> variableParser()
    {
        return var ->
        {
            VariableType<T, ? super A, ? super U> type = type(var);
            if (type != null)
                return type.parse(var.substring(type.prefix.length()));
            else
                throw new ParseException(String.format("Type of '%s' unknown", var), 0); // TODO Where??
        };
    }

    public T evaluate(final SupplierCache<A> a)
    {
        @SuppressWarnings("unchecked") Algebra.Expression<T, Function<SupplierCache<? super A>, T>> expression = (Algebra.Expression<T, Function<SupplierCache<? super A>, T>>) this.parsedExpression;
        return parsedExpression != null ? expression.evaluate(fun -> fun.apply(a)) : emptyExpressionResult;
    }

    public T evaluate(final Supplier<A> a)
    {
        return evaluate(SupplierCache.of(a));
    }

    public T evaluate(final A a)
    {
        return evaluate(SupplierCache.direct(a));
    }

    @Nonnull
    public String getDisplayString(final U u)
    {
        return parsedExpression != null ? parsedExpression.toString(input ->
                variableDisplayString(input, u)) : TextFormatting.RED + expression;
    }

    public String variableDisplayString(String variable, U u)
    {
        VariableType<T, ? super A, ? super U> type = type(variable);
        return type != null
                ? type.getRepresentation(variable.substring(type.prefix.length()), type.prefix, type.suffix, u)
                : TextFormatting.RED + variable;
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
        public Function<SupplierCache<A>, T> parse(String var) throws ParseException
        {
            return parent.parse(var);
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

        public abstract Function<SupplierCache<A>, T> parse(String var) throws ParseException;

        public abstract Validity validity(String var, U u);

        public TextFormatting getRepresentation(Validity validity)
        {
            return validity == Validity.KNOWN ? TextFormatting.GREEN
                    : validity == Validity.UNKNOWN ? TextFormatting.YELLOW
                    : TextFormatting.RED;
        }

        public String getRepresentation(String var, String prefix, String suffix, U u)
        {
            return TextFormatting.BLUE + prefix + TextFormatting.RESET
                    + getVarRepresentation(var, u) + TextFormatting.RESET
                    + TextFormatting.BLUE + suffix + TextFormatting.RESET;
        }

        protected String getVarRepresentation(String var, U u)
        {
            return getRepresentation(validity(var, u)) + var;
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
