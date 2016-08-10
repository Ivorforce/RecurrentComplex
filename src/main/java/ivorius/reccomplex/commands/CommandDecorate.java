/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandDecorate extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "decorate";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcdecorate.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        BlockPos coord;

        if (args.length >= 2)
            coord = RCCommands.parseXZBlockPos(commandSender, args, 0, false);
        else
            coord = commandSender.getPosition();

        WorldGenStructures.generateRandomStructureInChunk(commandSender.getEntityWorld().rand, coord.getX() >> 4, coord.getZ() >> 4, commandSender.getEntityWorld(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getBiomeGenForCoords(coord));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1 || args.length == 3)
            return getListOfStringsMatchingLastWord(args, "~");

        return null;
    }
}
