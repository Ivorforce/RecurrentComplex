/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.ivtoolkit.blocks.BlockSurfaceArea;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.WorldGenStructures;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandDecorate extends CommandBase
{
    @Nonnull
    protected static BlockSurfacePos getChunkPos(BlockSurfacePos point)
    {
        return new BlockSurfacePos(point.getX() >> 4, point.getZ() >> 4);
    }

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
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 4)
            throw new WrongUsageException("commands.decorate.usage");

        BlockSurfaceArea area = new BlockSurfaceArea(RCCommands.parseSurfaceBlockPos(commandSender, args, 0, false), RCCommands.parseSurfaceBlockPos(commandSender, args, 2, false));
        BlockSurfaceArea chunkArea = new BlockSurfaceArea(getChunkPos(area.getPoint1()), getChunkPos(area.getPoint2()));
        Predicate<StructureInfo> structurePredicate = RCCommands.tryParseStructurePredicate(args, 4, () -> structureInfo -> true);

        WorldServer world = (WorldServer) commandSender.getEntityWorld();
        chunkArea.forEach(coord -> WorldGenStructures.decorate(world, world.rand, new ChunkPos(coord.x, coord.z), structurePredicate));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length >= 1 && args.length <= 2)
            return getTabCompletionCoordinateXZ(args, 0, pos);
        if (args.length >= 3 && args.length <= 4)
            return getTabCompletionCoordinateXZ(args, 2, pos);
        else if (args.length >= 5)
            return RCCommands.completeResourceMatcher(args);

        return Collections.emptyList();
    }
}
