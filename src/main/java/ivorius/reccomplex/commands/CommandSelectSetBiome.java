/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.ivtoolkit.blocks.BlockSurfaceArea;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectSetBiome extends CommandBase
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "setbiome";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcsetbiome.usage");
    }

    public static int biomeArrayIndex(BlockSurfacePos p)
    {
        // From Biome
        int i = p.getX() & 15;
        int j = p.getZ() & 15;
        return j << 4 | i;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return RCCommands.completeBiome(args);

        return Collections.emptyList();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length < 1)
            throw ServerTranslations.wrongUsageException("commands.rcremember.usage");

        Biome biome = RCCommands.parseBiome(args[0]);
        byte biomeID = (byte)(Biome.REGISTRY.getIDForObject(biome) & 255);

        World world = commandSender.getEntityWorld();

        // TODO Send to clients somehow
        BlockSurfaceArea.from(RCCommands.getSelectionOwner(commandSender, null, true).getSelection()).forEach(p -> {
            Chunk chunk = world.getChunkFromChunkCoords(p.getX() >> 4, p.getZ() >> 4);
            chunk.getBiomeArray()[biomeArrayIndex(p)] = biomeID;
            chunk.setModified(true);
        });
    }
}
