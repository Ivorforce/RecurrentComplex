/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.schematic;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.CommandSplit;

/**
 * Created by lukas on 01.06.17.
 */
public class CommandSchematic extends CommandSplit
{
    public CommandSchematic()
    {
        add(new CommandImportSchematic());
        add(new CommandExportSchematic());
        add(new CommandConvertSchematic());
    }

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "schematic";
    }
}
