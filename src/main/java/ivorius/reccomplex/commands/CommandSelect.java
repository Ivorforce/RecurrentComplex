/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSelect extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "select";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectSet.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        SelectionOwner owner = RCCommands.getSelectionOwner(commandSender, null, false);

        String subcommand = parameters.get().first().require();
        switch (subcommand)
        {
            case "clear":
                owner.setSelection(null);
                break;
            case "get":
                commandSender.sendMessage(ServerTranslations.format("commands.selectSet.get", translatePoint(owner.getSelectedPoint1()), translatePoint(owner.getSelectedPoint2())));
                if (owner.hasValidSelection())
                    commandSender.sendMessage(ServerTranslations.format("commands.selectSet.size", translateSize(owner.getSelection().areaSize()), IvVecMathHelper.product(owner.getSelection().areaSize())));
                break;
            case "both":
            case "point1":
            case "point2":
                if (!subcommand.equals("point2"))
                {
                    if (owner.getSelectedPoint1() == null)
                        owner.setSelectedPoint1(commandSender.getPosition());

                    owner.setSelectedPoint1(parameters.mc().move(1).pos(owner.getSelectedPoint1(), false).require());
                }
                if (!subcommand.equals("point1"))
                {
                    if (owner.getSelectedPoint2() == null)
                        owner.setSelectedPoint2(commandSender.getPosition());

                    owner.setSelectedPoint2(parameters.mc().move(1).pos(owner.getSelectedPoint2(), false).require());
                }

                break;
            default:
                throw ServerTranslations.wrongUsageException("commands.selectSet.usage");
        }
    }

    protected Object translatePoint(BlockPos coord)
    {
        return coord != null
                ? String.format("[%d,%d,%d]", coord.getX(), coord.getY(), coord.getZ())
                : ServerTranslations.format("commands.selectSet.point.none");
    }

    protected Object translateSize(int[] size)
    {
        return size != null
                ? String.format("[%d,%d,%d]", size[0], size[1], size[2])
                : ServerTranslations.format("commands.selectSet.point.none");
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.startRC()
                .any("both", "clear", "point1", "point2", "get")
                .pos()
                .get(server, sender, args, pos);
    }
}
