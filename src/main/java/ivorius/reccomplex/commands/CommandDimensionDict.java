/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.ServerTranslations;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.DimensionManager;

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

                Set<String> types = DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID));
                List<String> typeList = new ArrayList<>();
                typeList.addAll(types);
                commandSender.addChatMessage(ServerTranslations.format("commands.dimensiondict.get", dimensionID, Strings.join(typeList, ", ")));
                break;
            }
            case "list":
            {
                TIntList typeDimensions = allDimensionsOfType(args[1]);
                String[] types = new String[typeDimensions.size()];
                for (int i = 0; i < typeDimensions.size(); i++)
                    types[i] = String.valueOf(typeDimensions.get(i));

                commandSender.addChatMessage(ServerTranslations.format("commands.dimensiondict.list", args[1], Strings.join(types, ", ")));
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
