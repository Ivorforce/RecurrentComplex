/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

/**
 * Created by lukas on 29.05.17.
 */
public class GenerationSanityChecker
{
    protected static final TIntSet FAILED_DIMENSIONS = new TIntHashSet();

    public static void init()
    {
        FAILED_DIMENSIONS.clear();

        // Sanity check for chunk population
        GameRegistry.registerWorldGenerator((Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) ->
        {
            ChunkPos pos = new ChunkPos(chunkX, chunkZ);
            if (!WorldStructureGenerationData.get(world).checkChunk(pos))
                return;

            if (FAILED_DIMENSIONS.add(world.provider.getDimension()))
                RecurrentComplex.logger.error(String.format("Chunk finished generating without Forge population being triggered (dimension %d). This is a bug with the dimension - please report this to the dimension's author. Recurrent Complex will proceed to generate in compatibility mode.", world.provider.getDimension()));

            WorldGenStructures.decorate((WorldServer) world, random, pos, null);
        }, 1);
    }
}
