/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import ivorius.ivtoolkit.blocks.BlockSurfaceArea;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.*;
import ivorius.reccomplex.commands.rcparameters.RCExpect;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectSetBiome extends CommandExpecting
{
    public static int biomeArrayIndex(BlockSurfacePos p)
    {
        // From Biome
        int i = p.getX() & 15;
        int j = p.getZ() & 15;
        return j << 4 | i;
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "setbiome";
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .biome();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        Biome biome = parameters.get(0).to(MCP::biome).require();
        byte biomeID = (byte) (Biome.REGISTRY.getIDForObject(biome) & 255);

        World world = commandSender.getEntityWorld();

        // TODO Send to clients somehow
        BlockSurfaceArea.from(RCCommands.getSelectionOwner(commandSender, null, true).getSelection()).forEach(p ->
        {
            Chunk chunk = world.getChunkFromChunkCoords(p.getX() >> 4, p.getZ() >> 4);
            chunk.getBiomeArray()[biomeArrayIndex(p)] = biomeID;
            chunk.setModified(true);
        });
    }
}
