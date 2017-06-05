/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 30.05.17.
 */
public class Parameter
{
    /**
     * -1 for 'no argument provided'
     */
    protected final int moved;
    protected final String name;
    protected final List<String> params;

    public Parameter(Parameter other)
    {
        moved = other.moved;
        name = other.name;
        params = other.params;
    }

    public Parameter(String name, List<String> params)
    {
        this.moved = 0;
        this.name = name;
        this.params = params;
    }

    protected Parameter(int moved, String name, List<String> params)
    {
        this.moved = moved;
        this.name = name;
        this.params = params;
    }

    protected static String parameterName(Parameter parameter, int index)
    {
        return String.format("%s (%d)", parameter.name != null ? " " + Parameters.LONG_FLAG_PREFIX + parameter.name : "", Math.max(parameter.moved, 0) + index);
    }

    public Parameter move(int idx)
    {
        return new Parameter(moved >= 0 ? moved + idx : moved, name, params.subList(Math.min(idx, params.size()), params.size()));
    }

    public Result<String> first()
    {
        return at(0);
    }

    public Result<Integer> intAt(int idx)
    {
        return at(idx).map(CommandBase::parseInt);
    }

    public Result<Boolean> booleanAt(int idx)
    {
        return at(idx).map(CommandBase::parseBoolean);
    }

    public Result<Double> doubleAt(int idx)
    {
        return at(idx).map(CommandBase::parseDouble);
    }

    public Result<Long> longAt(int idx)
    {
        return at(idx).map(CommandBase::parseLong);
    }

    protected void require(int size) throws CommandException
    {
        if (moved < 0)
            throw new ArgumentMissingException(this, size);
        if (!has(size))
            throw new ParameterNotFoundException(this, size);
    }

    public int count()
    {
        return params.size();
    }

    public boolean has(int size)
    {
        return size <= count();
    }

    public Result<String> at(int index)
    {
        return new Result<>(() ->
        {
            require(index + 1);
            return params.get(index);
        });
    }

    public String[] varargs()
    {
        return params.stream().toArray(String[]::new);
    }

    public List<String> varargsList()
    {
        return params;
    }

    public Result<String> text()
    {
        return at(0).map(s -> Strings.join(params, " "));
    }

    public Stream<Parameter> stream()
    {
        return IntStream.range(0, params.size()).mapToObj(this::move);
    }

    public interface Supplier<T>
    {
        T get() throws CommandException;
    }

    public interface Function<T, O>
    {
        O apply(T t) throws CommandException;
    }

    public static class Result<T>
    {
        private Supplier<T> t;

        public Result(Supplier<T> t)
        {
            this.t = t;
        }

        public static <T> Result<T> empty()
        {
            return new Result<T>(() -> null);
        }

        public Result<T> filter(Predicate<T> fun)
        {
            return filter(fun, null);
        }

        public Result<T> filter(Predicate<T> fun, @Nullable Function<T, CommandException> esc)
        {
            return new Result<T>(() ->
            {
                T t = this.t.get();
                if (!fun.test(t) && esc != null) throw esc.apply(t);
                return t;
            });
        }

        public <O> Result<O> map(Function<T, O> fun)
        {
            return map(fun, null);
        }

        public <O> Result<O> map(Function<T, O> fun, @Nullable Function<T, CommandException> exc)
        {
            return new Result<>(() ->
            {
                T t = this.t.get();

                if (t == null) return null;

                O o = fun.apply(t);
                if (o == null && exc != null) throw exc.apply(t);

                return o;
            });
        }

        public <O> Result<O> flatMap(Function<T, Result<O>> fun)
        {
            return new Result<>(() ->
            {
                T t = this.t.get();

                if (t == null) return null;

                return fun.apply(t).t.get();
            });
        }

        public Result<T> orElse(T t)
        {
            return new Result<T>(() ->
            {
                T p = this.t.get();
                return p != null ? p : t;
            });
        }

        public Result<T> orElseGet(Supplier<T> supplier)
        {
            return new Result<T>(() ->
            {
                T t = this.t.get();
                return t != null ? t : supplier.get();
            });
        }

        public Result<T> failable()
        {
            return new Result<T>(() ->
            {
                try
                {
                    return t.get();
                }
                catch (CommandException e)
                {
                    return null;
                }
            });
        }

        public Result<T> missable()
        {
            return new Result<T>(() ->
            {
                try
                {
                    return t.get();
                }
                catch (ParameterNotFoundException e)
                {
                    return null;
                }
            });
        }

        @Nonnull
        public T require() throws CommandException
        {
            T t = this.t.get();
            if (t == null) throw new CommandException("Parameter missing!");
            return t;
        }

        public Optional<T> optional() throws CommandException
        {
            T t = null;

            try
            {
                t = this.t.get();
            }
            catch (ParameterNotFoundException ignored)
            {
            }

            return Optional.ofNullable(t);
        }

        public Optional<T> tryGet()
        {
            T t = null;

            try
            {
                t = this.t.get();
            }
            catch (CommandException ignored)
            {
            }

            return Optional.ofNullable(t);
        }

        @Override
        public String toString()
        {
            return tryGet().map(Object::toString).orElse("null");
        }
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
