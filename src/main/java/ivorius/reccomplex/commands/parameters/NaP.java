/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import net.minecraft.command.CommandBase;

import java.util.function.BinaryOperator;

/**
 * Created by lukas on 07.06.17.
 */
public class NaP
{
    public static BinaryOperator<String> join()
    {
        return (s, s2) -> s + " " + s2;
    }

    public static Parameter<Integer> asInt(Parameter<String> p)
    {
        return p.map(CommandBase::parseInt);
    }

    public static Parameter<Boolean> asBoolean(Parameter<String> p)
    {
        return p.map(CommandBase::parseBoolean);
    }

    // Natives

    public static Parameter<Double> asDouble(Parameter<String> p)
    {
        return p.map(CommandBase::parseDouble);
    }

    public static Parameter<Long> asLong(Parameter<String> p)
    {
        return p.map(CommandBase::parseLong);
    }

    public static Parameter<String[]> varargs(Parameter<String> p)
    {
        return p.varargs(String[]::new);
    }
}
