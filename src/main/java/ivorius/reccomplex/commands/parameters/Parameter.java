/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by lukas on 30.05.17.
 */
public class Parameter<T, P extends Parameter<T, P>>
{
    /**
     * -1 for 'no argument provided'
     */
    protected final int moved;
    protected final String name;
    protected final List<String> params;

    @Nonnull
    protected final Function<List<String>, T> fun;

    public Parameter(Parameter<T, ?> other)
    {
        moved = other.moved;
        name = other.name;
        params = other.params;
        fun = other.fun;
    }

    public Parameter(Parameter<?, ?> other, @Nonnull Function<List<String>, T> fun)
    {
        moved = other.moved;
        name = other.name;
        params = other.params;
        this.fun = fun;
    }

    protected Parameter(int moved, String name, List<String> params, @Nullable Function<List<String>, T> fun)
    {
        this.moved = moved;
        this.name = name;
        this.params = params;
        this.fun = fun != null ? fun : initial();
    }

    protected static String parameterName(Parameter parameter, int index)
    {
        return String.format("%s (%d)", parameter.name != null ? " " + Parameters.LONG_FLAG_PREFIX + parameter.name : "", Math.max(parameter.moved, 0) + index);
    }

    @Nonnull
    protected Function<List<String>, T> initial()
    {
        throw new IllegalStateException();
    }

    // Size

    public boolean isSet()
    {
        return moved >= 0;
    }

    protected String first(List<String> list) throws CommandException
    {
        if (!isSet())
            throw new ArgumentMissingException(this, 0);
        if (list.isEmpty())
            throw new ParameterNotFoundException(this, 0);
        return list.get(0);
    }

    public int count()
    {
        return params.size();
    }

    public boolean has(int size)
    {
        return size <= count();
    }

    // Subclass

    public P copy(Parameter<T, ?> p)
    {
        //noinspection unchecked
        return (P) new Parameter(p);
    }

    // Result

    @Nonnull
    public Function<List<String>, T> function()
    {
        return fun;
    }

    public Parameter<T, P> filter(Predicate<T> fun)
    {
        return filter(fun, null);
    }

    public Parameter<T, P> filter(Predicate<T> fun, @Nullable Function<T, CommandException> esc)
    {
        return new Parameter<T, P>(this, s ->
        {
            T t = function().apply(s);
            if (!fun.test(t) && esc != null) throw esc.apply(t);
            return t;
        });
    }

    public <O> Parameter<O, ?> map(Function<T, O> fun)
    {
        return map(fun, null);
    }

    public <O> Parameter<O, ?> map(Function<T, O> fun, @Nullable Function<T, CommandException> exc)
    {
        return new Parameter<>(this, s ->
        {
            T t = function().apply(s);
            if (t == null) return null;

            O o = fun.apply(t);
            if (o == null && exc != null) throw exc.apply(t);

            return o;
        });
    }

    public <O> Parameter<O, ?> flatMap(Function<T, Parameter<O, ?>> fun)
    {
        return new Parameter<>(this, s ->
        {
            T t = function().apply(s);

            if (t == null) return null;

            Parameter<O, ?> po = fun.apply(t);
            return po.function().apply(po.params);
        });
    }

    public P orElse(T t)
    {
        return orElseGet(() -> t);
    }

    public P orElseGet(Supplier<T> supplier)
    {
        return copy(new Parameter<T, P>(this, s ->
        {
            try
            {
                return function().apply(s);
            }
            catch (ParameterNotFoundException e)
            {
                return supplier.get();
            }
        }));
    }

    @Nonnull
    public T require() throws CommandException
    {
        T t = function().apply(params);
        if (t == null) throw new CommandException("Parameter missing!");
        return t;
    }

    public Optional<T> optional() throws CommandException
    {
        T t = null;

        try
        {
            t = function().apply(params);
        }
        catch (ParameterNotFoundException ignored)
        {
        }

        return Optional.ofNullable(t);
    }

    public T get()
    {
        //noinspection OptionalGetWithoutIsPresent
        return tryGet().get();
    }

    public Optional<T> tryGet()
    {
        T t = null;

        try
        {
            t = function().apply(params);
        }
        catch (CommandException ignored)
        {
        }

        return Optional.ofNullable(t);
    }

    // Rest as arguments

    public P rest(BinaryOperator<T> operator)
    {
        return copy(new Parameter<>(this, p ->
        {
            T t = function().apply(Collections.singletonList(p.get(0)));
            for (int i = 1; i < p.size(); i++) t = operator.apply(t, function().apply(Collections.singletonList(p.get(i))));
            return t;
        }));
    }

    public P move(int idx)
    {
        //noinspection unchecked
        return idx == 0 ? (P) this
                : copy(new Parameter<>(isSet() ? moved + idx : moved, name, params.subList(Math.min(idx, params.size()), params.size()), fun));
    }

    public Parameter<T[], ?> varargs(IntFunction<T[]> init)
    {
        return stream().map(s -> s.toArray(init));
    }

    public Parameter<List<T>, ?> varargsList()
    {
        return new Parameter<>(this, p ->
        {
            List<T> list = new ArrayList<>(p.size());
            for (String param : p) list.add(function().apply(Collections.singletonList(param)));
            return list;
        });
    }

    public Parameter<Stream<T>, ?> stream()
    {
        return varargsList().map(Collection::stream);
    }

    public interface Supplier<T>
    {
        T get() throws CommandException;
    }

    public interface Function<T, O>
    {
        O apply(T t) throws CommandException;
    }

    public static class ParameterNotFoundException extends CommandException
    {
        public ParameterNotFoundException(Parameter parameter, int index)
        {
            super("Missing required parameter:" + parameterName(parameter, index));
        }
    }

    public static class ArgumentMissingException extends CommandException
    {
        public ArgumentMissingException(Parameter parameter, int index)
        {
            super("Parameter mssing an argument:" + parameterName(parameter, index));
        }
    }
}
