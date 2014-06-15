/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.commands;

import ivorius.structuregen.StructureGen;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

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
                StructureGen.chEditStructure.sendBeginEdit(entityPlayerMP, structureInfo, args[0]);
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
            Set<String> allStructureNames = StructureHandler.getAllStructureNames();

            return getListOfStringsMatchingLastWord(args, allStructureNames.toArray(new String[allStructureNames.size()]));
        }

        return null;
    }
}
