/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.info;

import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.RCTextStyle;
import ivorius.reccomplex.commands.parameters.CommandSplit;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.commands.parameters.SimpleCommand;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import java.util.Arrays;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDimensionDict extends CommandSplit
{
    public CommandDimensionDict()
    {
        super(RCConfig.commandPrefix + "dimension");

        add(new SimpleCommand("types", "<dimension>", () -> RCExpect.startRC().dimension())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                RCParameters parameters = RCParameters.of(args);

                WorldProvider provider = parameters.mc().dimension(server, sender).require().provider;

                sender.sendMessage(ServerTranslations.format("commands.dimensiondict.get", RCTextStyle.dimension(provider.getDimension()),
                        ServerTranslations.join(Lists.newArrayList(DimensionDictionary.getDimensionTypes(provider)).stream()
                                .map(RCTextStyle::dimensionType).toArray())
                ));
            }
        });

        add(new SimpleCommand("list", () -> RCExpect.startRC().dimensionType().required("dimension type"))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                RCParameters parameters = RCParameters.of(args);

                String type = parameters.get().first().require();

                sender.sendMessage(ServerTranslations.format("commands.dimensiondict.list", RCTextStyle.dimensionType(type),
                        ServerTranslations.join(Arrays.stream(allDimensionsOfType(type).toArray())
                                .mapToObj(RCTextStyle::dimension).toArray())
                ));
            }
        });

        permitFor(2);
    }

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
}
