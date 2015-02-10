/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandCancel extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "cancel";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return "commands.rccancel.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        EntityPlayer player = getCommandSenderAsPlayer(commandSender);
        StructureEntityInfo structureEntityInfo = RCCommands.getStructureEntityInfo(player);

        if (!structureEntityInfo.cancelOperation(commandSender.getEntityWorld(), player))
            throw new CommandException("commands.rc.noOperation");
    }
}
