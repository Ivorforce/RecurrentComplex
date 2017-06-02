/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.CommandSplit;

/**
 * Created by lukas on 02.06.17.
 */
public class CommandNatural extends CommandSplit
{
    public CommandNatural()
    {
        add(new CommandNaturalAll());
        add(new CommandNaturalSpace());
        add(new CommandNaturalFloor());
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "natural";
    }
}
