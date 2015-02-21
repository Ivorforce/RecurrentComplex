/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import ivorius.reccomplex.operation.Operation;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandVisual extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "visual";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return "commands.rcvisual.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length < 2)
            throw new WrongUsageException("commands.rcvisual.usage");

        boolean enabled = parseBoolean(commandSender, args[1]);

        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(player);

        switch (args[0])
        {
            case "rulers":
                structureEntityInfo.showGrid = enabled;
                structureEntityInfo.sendOptionsToClients(player);
                break;
            default:
                throw new WrongUsageException("commands.rcvisual.usage");
        }

        if (enabled)
            commandSender.addChatMessage(new ChatComponentTranslation("commands.rcvisual.enabled", args[0]));
        else
            commandSender.addChatMessage(new ChatComponentTranslation("commands.rcvisual.disabled", args[0]));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "rulers");
        if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, "true", "false");

        return null;
    }
}
