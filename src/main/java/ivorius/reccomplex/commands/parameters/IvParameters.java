/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.ListMultimap;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Created by lukas on 31.05.17.
 */
public class IvParameters extends Parameters
{
    public IvParameters(Parameters blueprint)
    {
        super(blueprint);
    }

    public static IvParameters of(String[] args)
    {
        return of(args, IvParameters::new);
    }

    public MCParameter mc()
    {
        return new MCParameter(get());
    }

    public MCParameter mc(@Nonnull String name)
    {
        return new MCParameter(get(name));
    }

    public IvParameter iv()
    {
        return new IvParameter(get());
    }

    public IvParameter iv(@Nonnull String name)
    {
        return new IvParameter(get(name));
    }
}
