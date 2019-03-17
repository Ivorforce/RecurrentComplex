/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.mcopts.commands.CommandSplit;
import ivorius.reccomplex.commands.structure.CommandListStructures;
import ivorius.reccomplex.commands.structure.CommandLookupStructure;
import net.minecraft.command.ICommand;

public class CommandStructures extends CommandSplit
{
    protected CommandLookupStructure lookup;
    protected CommandListStructures list;

    public CommandStructures(String name, CommandLookupStructure lookup, CommandListStructures list, ICommand... commands)
    {
        super(name, commands);

        add(this.lookup = lookup);
        add(this.list = list);
    }

    public String lookup(String id)
    {
        return String.format("%s %s", getName(), lookup.getName());
    }

    public String list() {
        return String.format("%s %s", getName(), list.getName());
    }

    public String list(int page)
    {
        return String.format("%s %s %d", getName(), list.getName(), page);
    }
}
