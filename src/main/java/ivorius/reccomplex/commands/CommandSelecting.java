/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.CapabilitySelection;
import ivorius.reccomplex.commands.parameters.*;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandSelecting extends CommandExpecting
{
    @Override
    public String getName()
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
                .xyz().required()
                .xyz().required()
                .command().required()
                .commandArguments(p -> p.get(6)).repeat();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        BlockPos p1 = parameters.get().pos(commandSender.getPosition(), false).require();
        BlockPos p2 = parameters.get(3).pos(commandSender.getPosition(), false).require();
        String command = parameters.get(6).rest().first().optional().orElse("");

        server.commandManager.executeCommand(new SelectingSender(commandSender, p1, p2), command);
    }

    public static class SelectingSender extends DelegatingSender
    {
        public CapabilitySelection capabilitySelection;

        public SelectingSender(ICommandSender sender, BlockPos point1, BlockPos point2)
        {
            super(sender);
            capabilitySelection = new CapabilitySelection(point1, point2);
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
        {
            if (capability == CapabilitySelection.CAPABILITY)
                return true;
            return super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
        {
            if (capability == CapabilitySelection.CAPABILITY)
                return (T) capabilitySelection;
            return super.getCapability(capability, facing);
        }
    }

}
