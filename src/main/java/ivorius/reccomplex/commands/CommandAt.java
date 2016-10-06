/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 03.08.14.
 */
public class CommandAt extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "at";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return ServerTranslations.usage("commands.rcat.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw ServerTranslations.wrongUsageException("commands.rcat.usage");

        Entity entity = getEntity(server, commandSender, args[0]);
        String command = buildString(args, 1);

        server.commandManager.executeCommand(new RepositionedSender(commandSender, entity), command);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getAllUsernames()) : Collections.emptyList();
    }

    public static class RepositionedSender extends DelegatingSender
    {
        private final ICommandSender positionRef;

        public RepositionedSender(ICommandSender sender, ICommandSender positionRef)
        {
            super(sender);
            this.positionRef = positionRef;
        }

        @Override
        public BlockPos getPosition()
        {
            return positionRef.getPosition();
        }

        @Override
        public Vec3d getPositionVector()
        {
            return positionRef.getPositionVector();
        }

        @Override
        public World getEntityWorld()
        {
            return positionRef.getEntityWorld();
        }
    }
}
