/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

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
        RCParameters parameters = RCParameters.of(args);

        switch (parameters.get().first().require())
        {
            case "types":
            {
                WorldProvider provider = parameters.mc().move(1).dimension(commandSender).require().provider;

                commandSender.sendMessage(ServerTranslations.format("commands.dimensiondict.get", provider.getDimension(),
                        ServerTranslations.join(Lists.newArrayList(DimensionDictionary.getDimensionTypes(provider)).stream()
                                .map(RCTextStyle::dimensionType).toArray())
                ));
                break;
            }
            case "list":
            {
                String type = parameters.get().at(1).require();

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
        RCExpect<?> expect = RCExpect.startRC()
                .any("types", "list");

        switch (args[0])
        {
            case "types":
                expect.dimension();
                break;
            case "list":
                expect.next(DimensionDictionary.allRegisteredTypes());
                break;
            default:
                expect.skip(1);
                break;
        }

        return expect.get(server, sender, args, pos);
    }
}
