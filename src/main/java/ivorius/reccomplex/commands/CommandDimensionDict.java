/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.ServerTranslations;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDimensionDict extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "dimensiondict";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.dimensiondict.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.dimensiondict.usage");

        switch (args[0])
        {
            case "get":
            {
                int dimensionID = parseInt(commandSender, args[1]);

                List<String> types = Lists.newArrayList(DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID)));
                IChatComponent[] components = new IChatComponent[types.size()];

                for (int i = 0; i < components.length; i++)
                {
                    String type = types.get(i);
                    components[i] = new ChatComponentText(type);
                    components[i].getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            String.format("/%s list %s", getCommandName(), type)));
                    components[i].getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            ServerTranslations.format("commands.dimensiondict.get.number", allDimensionsOfType(type).size())));
                }

                commandSender.addChatMessage(ServerTranslations.format("commands.dimensiondict.get", dimensionID,
                        new ChatComponentTranslation(StringUtils.repeat("%s", ", ", components.length), components)));
                break;
            }
            case "list":
            {
                String type = args[1];

                TIntList typeDimensions = allDimensionsOfType(type);
                IChatComponent[] components = new IChatComponent[typeDimensions.size()];

                for (int i = 0; i < components.length; i++)
                {
                    int dimensionID = typeDimensions.get(i);
                    components[i] = new ChatComponentText(String.valueOf(dimensionID));
                    components[i].getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            String.format("/%s get %s", getCommandName(), dimensionID)));
                    components[i].getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            ServerTranslations.format("commands.dimensiondict.list.number", DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID)).size())));
                }

                commandSender.addChatMessage(ServerTranslations.format("commands.dimensiondict.list", type,
                        new ChatComponentTranslation(StringUtils.repeat("%s", ", ", components.length), components)));
                break;
            }
            default:
                throw ServerTranslations.wrongUsageException("commands.dimensiondict.usage");
        }
    }

    private TIntList allDimensionsOfType(String type)
    {
        TIntList intList = new TIntArrayList();
        for (int d : DimensionManager.getIDs())
        {
            if (DimensionDictionary.dimensionMatchesType(DimensionManager.getProvider(d), type))
                intList.add(d);
        }
        return intList;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "get", "list");

        if (args[0].equals("get"))
        {
            Integer[] dimensions = DimensionManager.getIDs();
            String[] dimensionStrings = new String[dimensions.length];

            for (int i = 0; i < dimensions.length; i++)
                dimensionStrings[i] = String.valueOf(dimensions[i]);

            return getListOfStringsMatchingLastWord(args, dimensionStrings);
        }
        else if (args[0].equals("list"))
            return getListOfStringsFromIterableMatchingLastWord(args, DimensionDictionary.allRegisteredTypes());

        return null;
    }
}
