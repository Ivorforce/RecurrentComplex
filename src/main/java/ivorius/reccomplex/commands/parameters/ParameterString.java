/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandBase;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Created by lukas on 07.06.17.
 */
public class ParameterString<P extends ParameterString<P>> extends Parameter<String, P>
{
    public ParameterString(Parameter<String, ?> other)
    {
        super(other);
    }

    protected ParameterString(int moved, String name, List<String> params)
    {
        super(moved, name, params, null);
    }

    public static BinaryOperator<String> join()
    {
        return (s, s2) -> s + " " + s2;
    }

    @Nonnull
    @Override
    protected Function<List<String>, String> initial()
    {
        return list -> get(list, 0);
    }

    @Override
    public P copy(Parameter<String, ?> p)
    {
        //noinspection unchecked
        return (P) new ParameterString(p);
    }

    public Parameter<String[], ?> varargs()
    {
        return super.varargs(String[]::new);
    }

    // Natives

    public Parameter<Integer, ?> asInt()
    {
        return map(CommandBase::parseInt);
    }

    public Parameter<Boolean, ?> asBoolean()
    {
        return map(CommandBase::parseBoolean);
    }

    public Parameter<Double, ?> asDouble()
    {
        return map(CommandBase::parseDouble);
    }

    public Parameter<Long, ?> asLong()
    {
        return map(CommandBase::parseLong);
    }
}
