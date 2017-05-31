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
    public RCParameters(Set<String> flags, ListMultimap<String, String> params)
    {
        super(flags, params);
    }

    public static RCParameters of(String[] args)
    {
        return of(args, RCParameters::new);
    }

    public RCParameter rc()
    {
        return new RCParameter(get());
    }

    public RCParameter rc(@Nonnull String name) throws CommandException
    {
        return new RCParameter(get(name));
    }
}
