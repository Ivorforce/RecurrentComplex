/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.tools.IvStreams;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 10.08.16.
 */
public class Chunks
{
    public static boolean contains(ChunkCoordIntPair chunkPos, BlockSurfacePos pos)
    {
        return (chunkPos.chunkXPos >> 4) == pos.x && (chunkPos.chunkZPos >> 4) == pos.z;
    }

    public static Stream<BlockSurfacePos> repeatIntersections(ChunkCoordIntPair chunkPos, BlockSurfacePos pos, int repeatX, int repeatZ)
    {
        int lowestX = pos.x + (((chunkPos.chunkXPos << 4) - pos.x) / repeatX) * repeatX;
        int lowestZ = pos.z + (((chunkPos.chunkZPos << 4) - pos.z) / repeatZ) * repeatZ;

        int repeatsX = (15 - (lowestX - (chunkPos.chunkXPos << 4))) / repeatX;
        int repeatsZ = (15 - (lowestZ - (chunkPos.chunkZPos << 4))) / repeatZ;

        return IvStreams.flatMapToObj(IntStream.range(0, repeatsX + 1), iX -> IntStream.range(0, repeatsZ + 1).mapToObj(iZ -> new BlockSurfacePos(lowestX + iX * repeatX, lowestZ + iZ * repeatZ)));
    }
}
