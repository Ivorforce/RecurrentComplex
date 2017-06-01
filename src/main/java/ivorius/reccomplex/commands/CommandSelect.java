/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandSelect extends CommandSplit
{
    public CommandSelect()
    {
        add(new Command("clear")
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                owner.setSelection(null);
            }
        });

        add(new Command("get")
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                sender.sendMessage(ServerTranslations.format("commands.selectSet.get", translatePoint(owner.getSelectedPoint1()), translatePoint(owner.getSelectedPoint2())));
                if (owner.hasValidSelection())
                    sender.sendMessage(ServerTranslations.format("commands.selectSet.size", translateSize(owner.getSelection().areaSize()), IvVecMathHelper.product(owner.getSelection().areaSize())));
            }
        });

        add(new Command("set", "[x] [y] [z] --first --second", () -> RCExpect.startRC().xyz().flag("first").flag("second"))
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException
            {
                boolean first = !parameters.has("second");
                boolean second = !parameters.has("first");

                if (first)
                {
                    if (owner.getSelectedPoint1() == null)
                        owner.setSelectedPoint1(sender.getPosition());

                    owner.setSelectedPoint1(parameters.mc().pos(owner.getSelectedPoint1(), false).require());
                }
                if (second)
                {
                    if (owner.getSelectedPoint2() == null)
                        owner.setSelectedPoint2(sender.getPosition());

                    owner.setSelectedPoint2(parameters.mc().pos(owner.getSelectedPoint2(), false).require());
                }
            }
        });
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "select";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
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

    public static abstract class Command extends SimpleCommand
    {
        public Command(String name)
        {
            super(name);
        }

        public Command(String name, String usage, Supplier<Expect<?>> expector)
        {
            super(name, usage, expector);
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            SelectionOwner owner = RCCommands.getSelectionOwner(sender, null, false);
            RCParameters parameters = RCParameters.of(args);
            execute(server, sender, parameters, owner);
        }

        public abstract void execute(MinecraftServer server, ICommandSender sender, RCParameters parameters, SelectionOwner owner) throws CommandException;
    }
}
