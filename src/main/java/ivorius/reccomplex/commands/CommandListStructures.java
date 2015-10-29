/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandListStructures extends CommandBase
{
 public static final int MAX_RESULTS = 20;

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "list";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rclist.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        int page = args.length >= 1 ? parseInt(commandSender, args[0]) : 0;

        List<String> structureNames = new ArrayList<>();
        structureNames.addAll(StructureRegistry.INSTANCE.allStructureIDs());
        Collections.sort(structureNames, String.CASE_INSENSITIVE_ORDER);

        int startIndex = page * MAX_RESULTS;
        int endIndex = Math.min((page + 1) * MAX_RESULTS, structureNames.size());

        ChatComponentText[] components = new ChatComponentText[endIndex - startIndex + 2];

        for (int i = 0; i < endIndex - startIndex; i++)
            components[i + 1] = CommandSearchStructure.createStructureChatComponent(structureNames.get(startIndex + i));

        components[0] = new ChatComponentText("[<--]");
        if (page > 0)
            linkToPage(components[0], page - 1, ServerTranslations.format("commands.rclist.previous"));

        components[components.length - 1] = new ChatComponentText("[-->]");
        if (page < (structureNames.size() - 1) / MAX_RESULTS)
            linkToPage(components[components.length - 1], page + 1, ServerTranslations.format("commands.rclist.next"));

        commandSender.addChatMessage(new ChatComponentTranslation(StringUtils.repeat("%s", ", ", components.length), (Object[]) components));
    }

    public static void linkToPage(ChatComponentText component, int page, IChatComponent hoverTitle)
    {
        component.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s %d", RCCommands.list.getCommandName(), page)));
        component.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTitle));
        component.getChatStyle().setColor(EnumChatFormatting.AQUA);
    }
}
