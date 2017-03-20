/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDimensionDict extends CommandBase
{
    public static TIntList allDimensionsOfType(String type)
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
    public String getName()
    {
        return RCConfig.commandPrefix + "dimension";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.dimensiondict.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.dimensiondict.usage");

        switch (args[0])
        {
            case "types":
            {
                int dimensionID = parseInt(args[1]);

                commandSender.sendMessage(ServerTranslations.format("commands.dimensiondict.get", dimensionID,
                        ServerTranslations.join(Lists.newArrayList(DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID))).stream()
                                .map(RCTextStyle::dimensionType).toArray())
                ));
                break;
            }
            case "list":
            {
                String type = args[1];

                commandSender.sendMessage(ServerTranslations.format("commands.dimensiondict.list", type,
                        ServerTranslations.join(Arrays.stream(allDimensionsOfType(type).toArray())
                                .mapToObj(RCTextStyle::dimension).toArray())
                ));
                break;
            }
            default:
                throw ServerTranslations.wrongUsageException("commands.dimensiondict.usage");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "types", "list");

        if (args[0].equals("types"))
        {
            Integer[] dimensions = DimensionManager.getIDs();
            String[] dimensionStrings = new String[dimensions.length];

            for (int i = 0; i < dimensions.length; i++)
                dimensionStrings[i] = String.valueOf(dimensions[i]);

            return getListOfStringsMatchingLastWord(args, dimensionStrings);
        }
        else if (args[0].equals("list"))
            return getListOfStringsMatchingLastWord(args, DimensionDictionary.allRegisteredTypes());

        return Collections.emptyList();
    }
}
