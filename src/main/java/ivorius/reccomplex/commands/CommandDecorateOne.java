/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.BlockSurfacePos;
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
public class CommandDecorateOne extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "decorateone";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcdecorateone.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        BlockSurfacePos coord;

        if (args.length >= 2)
            coord = RCCommands.parseSurfaceBlockPos(commandSender, args, 0, false);
        else
            coord = BlockSurfacePos.from(commandSender.getPosition());

        if (!WorldGenStructures.generateRandomStructureInChunk(commandSender.getEntityWorld().rand, coord.chunkCoord(), commandSender.getEntityWorld(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getBiomeGenForCoords(coord.blockPos(0))))
        {
            throw ServerTranslations.commandException("commands.rcdecorateone.none");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1 || args.length == 2)
            return getListOfStringsMatchingLastWord(args, "~");

        return null;
    }
}
