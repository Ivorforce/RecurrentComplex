/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;

import javax.annotation.Nonnull;
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
        return ServerTranslations.usage("commands.decorate.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 4)
            throw new WrongUsageException("commands.decorate.usage");

        BlockArea area = new BlockArea(RCCommands.parseXZBlockPos(commandSender, args, 0, false), RCCommands.parseXZBlockPos(commandSender, args, 2, false));
        BlockArea chunkArea =  new BlockArea(getChunkPos(area.getPoint1()), getChunkPos(area.getPoint2()));

        chunkArea.forEach(coord -> WorldGenStructures.generateRandomStructuresInChunk(commandSender.getEntityWorld().rand, coord.getX(), coord.getZ(), commandSender.getEntityWorld(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getBiomeGenForCoords(coord)));
    }

    @Nonnull
    protected static BlockPos getChunkPos(BlockPos point)
    {
        return new BlockPos(point.getX() >> 4, point.getY() >> 4, point.getZ() >> 4);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length > 0 && args.length < 5)
            return getListOfStringsMatchingLastWord(args, "~");

        return null;
    }
}
