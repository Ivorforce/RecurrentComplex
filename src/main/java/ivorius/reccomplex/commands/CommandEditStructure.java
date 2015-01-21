/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.network.PacketEditStructureHandler;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandEditStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "strucEdit";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "commands.strucEdit.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        EntityPlayerMP entityPlayerMP = getCommandSenderAsPlayer(commandSender);

        if (entityPlayerMP != null)
        {
            if (args.length >= 1)
            {
                GenericStructureInfo structureInfo = CommandExportStructure.getGenericStructureInfo(args[0]);
                PacketEditStructureHandler.sendEditStructure(structureInfo, args[0], entityPlayerMP);
            }
            else
            {
                throw new WrongUsageException("commands.strucEdit.usage");
            }
        }
        else
        {
            throw new WrongUsageException("commands.strucEdit.noPlayer");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = StructureRegistry.getAllStructureNames();

            return getListOfStringsMatchingLastWord(args, allStructureNames.toArray(new String[allStructureNames.size()]));
        }

        return null;
    }
}
