/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.capability.SelectionOwner;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import ivorius.reccomplex.utils.ServerTranslations;

/**
 * Created by lukas on 25.05.14.
 */
public abstract class CommandSelectModify extends CommandBase
{
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        SelectionOwner owner = RCCommands.getSelectionOwner(commandSender, null);

        if (owner.hasValidSelection())
            executeSelection(commandSender, owner, args);
        else
            throw ServerTranslations.commandException("commands.selectModify.noSelection");
    }

    public static int[] getMetadatas(String arg) throws CommandException
    {
        try
        {
            String[] strings = arg.split(",");
            int[] ints = new int[strings.length];

            for (int i = 0; i < strings.length; i++)
            {
                ints[i] = Integer.valueOf(strings[i]);
            }

            return ints;
        }
        catch (Exception ex)
        {
            throw ServerTranslations.wrongUsageException("commands.selectModify.invalidMetadata", arg);
        }
    }

    public abstract void executeSelection(ICommandSender sender, SelectionOwner selectionOwner, String[] args) throws CommandException;
}
