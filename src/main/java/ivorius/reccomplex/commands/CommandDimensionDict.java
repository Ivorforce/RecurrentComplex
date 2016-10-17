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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandDimensionDict extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "dimension";
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
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.dimensiondict.usage");

        switch (args[0])
        {
            case "types":
            {
                int dimensionID = parseInt(args[1]);

                List<String> types = Lists.newArrayList(DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID)));
                ITextComponent[] components = new ITextComponent[types.size()];

                for (int i = 0; i < components.length; i++)
                    components[i] = typeTextComponent(types.get(i));

                commandSender.addChatMessage(ServerTranslations.format("commands.dimensiondict.get", dimensionID,
                        ServerTranslations.join(components)));
                break;
            }
            case "list":
            {
                String type = args[1];

                TIntList typeDimensions = allDimensionsOfType(type);
                ITextComponent[] components = new ITextComponent[typeDimensions.size()];

                for (int i = 0; i < components.length; i++)
                    components[i] = dimensionTextComponent(typeDimensions.get(i));

                commandSender.addChatMessage(ServerTranslations.format("commands.dimensiondict.list", type,
                        ServerTranslations.join(components)));
                break;
            }
            default:
                throw ServerTranslations.wrongUsageException("commands.dimensiondict.usage");
        }
    }

    @Nonnull
    public ITextComponent typeTextComponent(String type)
    {
        ITextComponent component = new TextComponentString(type);
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s list %s", getCommandName(), type)));
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.dimensiondict.get.number", allDimensionsOfType(type).size())));
        return component;
    }

    @Nonnull
    public ITextComponent dimensionTextComponent(int dimensionID)
    {
        ITextComponent component = new TextComponentString(String.valueOf(dimensionID));
        component.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/%s types %s", getCommandName(), dimensionID)));
        component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ServerTranslations.format("commands.dimensiondict.list.number", DimensionDictionary.getDimensionTypes(DimensionManager.getProvider(dimensionID)).size())));
        return component;
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
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
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
