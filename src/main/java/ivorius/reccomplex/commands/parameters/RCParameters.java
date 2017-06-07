/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Created by lukas on 31.05.17.
 */
public class RCParameters extends IvParameters
{
    public RCParameters(Parameters blueprint)
    {
        super(blueprint);
    }

    public static RCParameters of(String[] args, Function<Parameters, Parameters> c)
    {
        return new RCParameters(Parameters.of(args, c));
    }

    @Override
    public RCParameter<?> get(int idx)
    {
        return new RCParameter(super.get(idx));
    }

    @Override
    public RCParameter<?> get(@Nonnull String name)
    {
        return new RCParameter(super.get(name));
    }
}
