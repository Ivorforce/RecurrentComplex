/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandSelecting extends CommandExpecting
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "selecting";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .xyz()
                .xyz()
                .command()
                .commandArguments(p -> p.get().move(6)).repeat();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        BlockPos p1 = parameters.mc().pos(commandSender.getPosition(), false).require();
        BlockPos p2 = parameters.mc().move(3).pos(commandSender.getPosition(), false).require();
        String command = parameters.get().move(6).text().optional().orElse("");

        server.commandManager.executeCommand(new SelectingSender(commandSender, p1, p2), command);
    }

    public static class SelectingSender extends DelegatingSender implements SelectionOwner
    {
        private BlockPos point1;
        private BlockPos point2;

        public SelectingSender(ICommandSender sender, BlockPos point1, BlockPos point2)
        {
            super(sender);
            this.point1 = point1;
            this.point2 = point2;
        }

        @Nullable
        @Override
        public BlockPos getSelectedPoint1()
        {
            return point1;
        }

        @Override
        public void setSelectedPoint1(@Nullable BlockPos pos)
        {
            point1 = pos;
        }

        @Nullable
        @Override
        public BlockPos getSelectedPoint2()
        {
            return point2;
        }

        @Override
        public void setSelectedPoint2(@Nullable BlockPos pos)
        {
            point2 = pos;
        }
    }

}
