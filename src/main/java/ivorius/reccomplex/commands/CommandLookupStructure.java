/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.Metadata;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandLookupStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "lookup";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rclookup.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length >= 1)
        {
            String strucKey = args[0];
            GenericStructureInfo structureInfo = CommandExportStructure.getGenericStructureInfo(strucKey);
            Metadata metadata = structureInfo.metadata;

            commandSender.addChatMessage(ServerTranslations.format(
                    StructureRegistry.isStructureGenerating(strucKey) ? "commands.rclookup.reply.generates" : "commands.rclookup.reply.silent",
                    strucKey,
                    metadata.authors.trim().isEmpty() ? ServerTranslations.format("commands.rclookup.reply.noauthor") : metadata.authors,
                    metadata.weblink.trim().isEmpty() ? ServerTranslations.format("commands.rclookup.reply.nolink") : metadata.weblink
            ));

            if (!metadata.comment.trim().isEmpty())
                commandSender.addChatMessage(ServerTranslations.format("commands.rclookup.reply.comment", metadata.comment));
        }
        else
        {
            throw ServerTranslations.commandException("commands.rclookup.usage");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = StructureRegistry.allStructureIDs();

            return getListOfStringsFromIterableMatchingLastWord(args, allStructureNames);
        }

        return null;
    }
}
