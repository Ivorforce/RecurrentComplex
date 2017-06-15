/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.*;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.commands.parameters.IvP;
import ivorius.reccomplex.world.gen.feature.WorldGenStructures;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandDecorateOne extends CommandExpecting
{
    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "decorateone";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);
        WorldServer entityWorld = (WorldServer) commandSender.getEntityWorld();

        BlockSurfacePos pos = parameters.get(0).to(IvP.surfacePos(commandSender.getPosition(), false)).require();

        if (!WorldGenStructures.generateRandomStructureInChunk(entityWorld.rand, pos.chunkCoord(), entityWorld, entityWorld.getBiome(pos.blockPos(0))))
            throw RecurrentComplex.translations.commandException("commands.rcdecorateone.none");
    }

    @Override
    public void expect(Expect expect)
    {
        expect.then(MCE::xz);
    }
}
