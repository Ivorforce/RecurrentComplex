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

/**
 * Created by lukas on 30.05.17.
 */
public class Parameter
{
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

    public Parameter move(int idx)
    {
        return new Parameter(moved + idx, name, params.subList(idx, params.size()));
    }

    public Result<String> here()
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

    public boolean has(int size)
    {
        return size <= params.size();
    }

    public Result<String> at(int index)
    {
        return new Result<>(() ->
        {
            if (!has(index + 1))
                throw new CommandException(String.format("Missing required parameter: -%s (%d)", name, index + moved));

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

    public String text()
    {
        return Strings.join(params, " ");
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

        public Result<T> orElse(Supplier<T> supplier)
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

        @Nonnull
        public T require() throws CommandException
        {
            T t = this.t.get();
            if (t == null) throw new CommandException("Parameter missing!");
            return t;
        }

        public Optional<T> optional()
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
            return optional().map(Object::toString).orElse("null");
        }
    }
}
