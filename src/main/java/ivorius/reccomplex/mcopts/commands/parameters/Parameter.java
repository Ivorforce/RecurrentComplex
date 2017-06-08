/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.mcopts.commands.parameters;

import ivorius.reccomplex.mcopts.MCOpts;
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
public class Parameter<T>
{
    /**
     * -1 for 'no argument provided'
     */
    protected final int moved;
    protected final String name;
    protected final List<String> params;

    @Nonnull
    protected final Function<List<String>, T> fun;

    public Parameter(Parameter<T> other)
    {
        moved = other.moved;
        name = other.name;
        params = other.params;
        fun = other.fun;
    }

    public Parameter(Parameter<?> other, @Nonnull Function<List<String>, T> fun)
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
        //noinspection unchecked
        this.fun = fun != null ? fun : (Function<List<String>, T>) initial();
    }

    public String name(int index)
    {
        if (name != null && index == 0)
            return Parameters.LONG_FLAG_PREFIX + name;
        return String.format("%s(%d)", name != null ? Parameters.LONG_FLAG_PREFIX + name + " " : "", Math.max(moved, 0) + index);
    }

    @Nonnull
    protected Function<List<String>, String> initial()
    {
        return list -> get(list, 0);
    }

    // Size

    public boolean isSet()
    {
        return moved >= 0;
    }

    protected <L> L get(List<L> list, int idx) throws CommandException
    {
        if (!isSet())
            throw ArgumentMissingException.create(this, idx);
        if (list.isEmpty())
            throw ParameterNotFoundException.create(this, idx);
        return list.get(idx);
    }

    public int count()
    {
        return params.size();
    }

    public boolean has(int size)
    {
        return size <= count();
    }

    // Result

    @Nonnull
    public Function<List<String>, T> function()
    {
        return fun;
    }

    public Parameter<T> filter(Predicate<T> fun)
    {
        return filter(fun, null);
    }

    public Parameter<T> filter(Predicate<T> fun, @Nullable Function<T, CommandException> esc)
    {
        return new Parameter<T>(this, s ->
        {
            T t = function().apply(s);
            if (!fun.test(t) && esc != null) throw esc.apply(t);
            return t;
        });
    }

    public <O> Parameter<O> to(java.util.function.Function<Parameter<String>, Parameter<O>> fun)
    {
        //noinspection unchecked
        return fun.apply((Parameter<String>) this);
    }

    public <O> Parameter<O> map(Function<T, O> fun)
    {
        return map(fun, null);
    }

    public <O> Parameter<O> map(Function<T, O> fun, @Nullable Function<T, CommandException> exc)
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

    public <O> Parameter<O> flatMap(Function<T, Parameter<O>> fun)
    {
        return new Parameter<>(this, s ->
        {
            T t = function().apply(s);

            if (t == null) return null;

            Parameter<O> po = fun.apply(t);
            return po.function().apply(po.params);
        });
    }

    public Parameter<T> orElse(T t)
    {
        return orElseGet(() -> t);
    }

    public Parameter<T> orElseGet(Supplier<T> supplier)
    {
        return new Parameter<T>(this, s ->
        {
            try
            {
                return function().apply(s);
            }
            catch (ParameterNotFoundException e)
            {
                return supplier.get();
            }
        });
    }

    @Nonnull
    public T require() throws CommandException
    {
        T t = function().apply(params);
        if (t == null) throw MCOpts.translations.commandException("commands.parameters.invalid.generic", name(0));
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

    public Parameter<T> rest(BinaryOperator<T> operator)
    {
        return new Parameter<>(this, p ->
        {
            T t = function().apply(Collections.singletonList(get(p, 0)));
            for (int i = 1; i < p.size(); i++)
                t = operator.apply(t, function().apply(Collections.singletonList(p.get(i))));
            return t;
        });
    }

    public Parameter<T> move(int idx)
    {
        //noinspection unchecked
        return idx == 0 ? (Parameter) this
                : new Parameter<>(isSet() ? moved + idx : moved, name, params.subList(Math.min(idx, params.size()), params.size()), fun);
    }

    public Parameter<T[]> varargs(IntFunction<T[]> init)
    {
        return stream().map(s -> s.toArray(init));
    }

    public Parameter<List<T>> varargsList()
    {
        return new Parameter<>(this, p ->
        {
            List<T> list = new ArrayList<>(p.size());
            for (String param : p) list.add(function().apply(Collections.singletonList(param)));
            return list;
        });
    }

    public Parameter<Stream<T>> stream()
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
        private ParameterNotFoundException(String message, Object... objects)
        {
            super(message, objects);
        }

        public static ParameterNotFoundException create(Parameter parameter, int index)
        {
            return MCOpts.translations.object(ParameterNotFoundException::new, "commands.parameters.missing", parameter.name(index));
        }
    }

    public static class ArgumentMissingException extends CommandException
    {
        private ArgumentMissingException(String message, Object... objects)
        {
            super(message, objects);
        }

        public static ArgumentMissingException create(Parameter parameter, int index)
        {
            return MCOpts.translations.object(ArgumentMissingException::new, "commands.parameters.missing.argument", parameter.name(index));
        }
    }
}
