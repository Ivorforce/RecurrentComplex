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
import ivorius.reccomplex.mcopts.commands.CommandSplit;
import ivorius.reccomplex.mcopts.commands.SimpleCommand;
import ivorius.reccomplex.mcopts.commands.parameters.*;
import ivorius.reccomplex.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.commands.parameters.expect.IvE;
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

        add(new SimpleCommand("types", () -> Parameters.expect().then(MCE::dimension).required())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, null);

                WorldProvider provider = parameters.get(0).to(MCP.dimension(server, sender)).require().provider;

                sender.sendMessage(ServerTranslations.format("commands.dimensiondict.get", RCTextStyle.dimension(provider.getDimension()),
                        ServerTranslations.join(Lists.newArrayList(DimensionDictionary.getDimensionTypes(provider)).stream()
                                .map(RCTextStyle::dimensionType).toArray())
                ));
            }
        });

        add(new SimpleCommand("list", () -> Parameters.expect().then(IvE::dimensionType).required())
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                Parameters parameters = Parameters.of(args, null);

                String type = parameters.get(0).require();

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
