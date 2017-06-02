/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.CommandSplit;
import net.minecraft.command.ICommand;

/**
 * Created by lukas on 02.06.17.
 */
public class CommandSight extends CommandSplit
{
    public ICommand delete;

    public CommandSight()
    {
        add(delete = new CommandSightDelete());
        add(new CommandSightAdd());
        add(new CommandSightCheck("check"));
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "sight";
    }
}
