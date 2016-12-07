/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectShift extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "shift";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectShift.usage");
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        if (args.length < 3)
            throw ServerTranslations.wrongUsageException("commands.selectShift.usage");

        int x = parseInt(args[0]), y = parseInt(args[1]), z = parseInt(args[2]);

        selectionOwner.setSelectedPoint1(selectionOwner.getSelectedPoint1().add(x, y, z));
        selectionOwner.setSelectedPoint2(selectionOwner.getSelectedPoint2().add(x, y, z));
    }
}
