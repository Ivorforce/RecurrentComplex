/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * Created by lukas on 10.08.16.
 */
public class BlockSurfacePos
{
    /**
     * The X position of this Chunk Coordinate Pair
     */
    public final int x;
    /**
     * The Z position of this Chunk Coordinate Pair
     */
    public final int z;

    public BlockSurfacePos(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    public static BlockSurfacePos from(ChunkPos chunkCoords, int x, int z)
    {
        return new BlockSurfacePos(chunkCoords.chunkXPos << 4 + x, chunkCoords.chunkZPos << 4 + z);
    }

    public static BlockSurfacePos from(BlockPos pos)
    {
        return new BlockSurfacePos(pos.getX(), pos.getZ());
    }

    public ChunkPos chunkCoord()
    {
        return new ChunkPos(x >> 4, z >> 4);
    }

    public BlockPos blockPos(int y)
    {
        return new BlockPos(x, y, z );
    }

    public int getX()
    {
        return x;
    }

    public int getZ()
    {
        return z;
    }

    /**
     * converts a chunk coordinate pair to an integer (suitable for hashing)
     */
    public static long chunkXZ2Int(int x, int z)
    {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }

    public int hashCode()
    {
        int i = 1664525 * this.x + 1013904223;
        int j = 1664525 * (this.z ^ -559038737) + 1013904223;
        return i ^ j;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof ChunkPos))
        {
            return false;
        }
        else
        {
            ChunkPos ChunkPos = (ChunkPos) p_equals_1_;
            return this.x == ChunkPos.chunkXPos && this.z == ChunkPos.chunkZPos;
        }
    }

    public String toString()
    {
        return "[" + this.x + ", " + this.z + "]";
    }
}
