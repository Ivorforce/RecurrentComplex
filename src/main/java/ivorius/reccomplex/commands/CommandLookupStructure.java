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
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;

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

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rclookup.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 1)
        {
            String strucKey = args[0];
            GenericStructureInfo structureInfo = CommandExportStructure.getGenericStructureInfo(strucKey);
            Metadata metadata = structureInfo.metadata;

            boolean hasAuthor = !metadata.authors.trim().isEmpty();
            IChatComponent author = hasAuthor ? new ChatComponentText(StringUtils.abbreviate(metadata.authors, 30)) : ServerTranslations.format("commands.rclookup.reply.noauthor");
            if (hasAuthor)
                author.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(metadata.authors)));

            boolean hasWeblink = !metadata.weblink.trim().isEmpty();
            IChatComponent weblink = hasWeblink ? new ChatComponentText(StringUtils.abbreviate(metadata.weblink, 30)) : ServerTranslations.format("commands.rclookup.reply.nolink");
            if (hasWeblink)
            {
                weblink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, metadata.weblink));
                weblink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(metadata.weblink)));
            }

            commandSender.addChatMessage(ServerTranslations.format(
                    StructureRegistry.INSTANCE.isStructureGenerating(strucKey) ? "commands.rclookup.reply.generates" : "commands.rclookup.reply.silent",
                    strucKey, author, weblink));

            if (!metadata.comment.trim().isEmpty())
                commandSender.addChatMessage(ServerTranslations.format("commands.rclookup.reply.comment", metadata.comment));
        }
        else
        {
            throw ServerTranslations.commandException("commands.rclookup.usage");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            Set<String> allStructureNames = StructureRegistry.INSTANCE.allStructureIDs();

            return getListOfStringsMatchingLastWord(args, allStructureNames);
        }

        return null;
    }
}
