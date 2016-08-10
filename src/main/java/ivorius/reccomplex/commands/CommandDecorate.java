/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.BlockSurfaceArea;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;

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

        BlockSurfaceArea area = new BlockSurfaceArea(RCCommands.parseSurfaceBlockPos(commandSender, args, 0, false), RCCommands.parseSurfaceBlockPos(commandSender, args, 2, false));
        BlockSurfaceArea chunkArea =  new BlockSurfaceArea(getChunkPos(area.getPoint1()), getChunkPos(area.getPoint2()));

        chunkArea.forEach(coord -> WorldGenStructures.generateRandomStructuresInChunk(commandSender.getEntityWorld().rand, new ChunkCoordIntPair(coord.x, coord.z), commandSender.getEntityWorld(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getChunkProvider(), commandSender.getEntityWorld().getBiomeGenForCoords(coord.blockPos(0))));
    }

    @Nonnull
    protected static BlockSurfacePos getChunkPos(BlockSurfacePos point)
    {
        return new BlockSurfacePos(point.getX() >> 4, point.getZ() >> 4);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length > 0 && args.length < 5)
            return getListOfStringsMatchingLastWord(args, "~");

        return null;
    }
}
