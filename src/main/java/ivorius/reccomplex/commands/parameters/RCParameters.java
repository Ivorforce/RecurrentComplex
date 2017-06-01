/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import com.google.common.collect.ListMultimap;
import net.minecraft.command.CommandException;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Created by lukas on 31.05.17.
 */
public class RCParameters extends IvParameters
{
    public RCParameters(Parameters blueprint)
    {
        super(blueprint);
    }

    public static RCParameters of(String[] args, String... flags)
    {
        return of(args, flags, RCParameters::new);
    }

    public RCParameter rc()
    {
        return new RCParameter(get());
    }

    public RCParameter rc(@Nonnull String name)
    {
        return new RCParameter(get(name));
    }
}
