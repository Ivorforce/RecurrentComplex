/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.blocks.BlockSurfaceArea;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.commands.parameters.IvP;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.world.gen.feature.WorldGenStructures;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandDecorate extends CommandExpecting
{
    @Nonnull
    protected static BlockSurfacePos getChunkPos(BlockSurfacePos point)
    {
        return new BlockSurfacePos(point.getX() >> 4, point.getZ() >> 4);
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "decorate";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                // TODO Bugged in MCOPts
                .then(MCE::xz).atOnce(2).descriptionU("x1", "z1")
                .then(MCE::xz).atOnce(2).descriptionU("x2", "z2")
                .named("exp").words(RCE::structurePredicate)
                .flag("one")
        ;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        BlockSurfaceArea area = new BlockSurfaceArea(
                parameters.get(0).to(IvP.surfacePos(commandSender.getPosition(), false)).require(),
                parameters.get(2).to(IvP.surfacePos(commandSender.getPosition(), false)).require()
        );
        BlockSurfaceArea chunkArea = new BlockSurfaceArea(getChunkPos(area.getPoint1()), getChunkPos(area.getPoint2()));
        Predicate<Structure> structurePredicate = parameters.get("exp").to(RCP::structurePredicate).optional().orElse(structureInfo -> true);

        WorldServer world = (WorldServer) commandSender.getEntityWorld();

        if (parameters.has("one"))
        {
            BlockSurfacePos lower = area.getLowerCorner();
            int[] size = area.areaSize();

            BlockPos pos = new BlockPos(lower.x + world.rand.nextInt(size[0]), 0, lower.z + world.rand.nextInt(size[1]));

            if (!WorldGenStructures.generateRandomStructureInChunk(world.rand, new ChunkPos(pos), world, world.getBiome(pos)))
                throw RecurrentComplex.translations.commandException("commands.rcdecorateone.none");
        }
        else
            chunkArea.forEach(coord -> WorldGenStructures.decorate(world, world.rand, new ChunkPos(coord.x, coord.z), structurePredicate));
    }
}
