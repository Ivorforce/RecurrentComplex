/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure.sight;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.mcopts.commands.CommandSplit;
import net.minecraft.command.ICommand;

/**
 * Created by lukas on 02.06.17.
 */
public class CommandSight extends CommandSplit
{
    public ICommand delete;
    public ICommand info;

    public CommandSight()
    {
        super(RCConfig.commandPrefix + "sight");

        add(delete = new CommandSightDelete());
        add(new CommandSightAdd());
        add(new CommandSightCheck("check"));
        add(info = new CommandSightInfo());
    }
}
