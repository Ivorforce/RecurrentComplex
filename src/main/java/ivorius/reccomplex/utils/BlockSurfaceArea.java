/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 09.06.14.
 */
public class BlockSurfaceArea implements Iterable<BlockSurfacePos>
{
    private BlockSurfacePos point1;
    private BlockSurfacePos point2;

    public BlockSurfaceArea(BlockSurfacePos point1, BlockSurfacePos point2)
    {
        this.point1 = point1;
        this.point2 = point2;
    }

    public static BlockSurfaceArea areaFromSize(BlockSurfacePos coord, int[] size)
    {
        if (size[0] <= 0 || size[1] <= 0 || size[2] <= 0)
            throw new IllegalArgumentException();

        return new BlockSurfaceArea(coord, new BlockSurfacePos(coord.getX() + size[0] - 1, coord.getZ() + size[2] - 1));
    }

    public BlockSurfacePos getPoint1()
    {
        return point1;
    }

    public void setPoint1(BlockSurfacePos point1)
    {
        this.point1 = point1;
    }

    public BlockSurfacePos getPoint2()
    {
        return point2;
    }

    public void setPoint2(BlockSurfacePos point2)
    {
        this.point2 = point2;
    }

    public BlockSurfacePos getLowerCorner()
    {
        return BlockSurfacePositions.getLowerCorner(point1, point2);
    }

    public BlockSurfacePos getHigherCorner()
    {
        return BlockSurfacePositions.getHigherCorner(point1, point2);
    }

    public int[] areaSize()
    {
        BlockSurfacePos lower = getLowerCorner();
        BlockSurfacePos higher = getHigherCorner();

        return new int[]{higher.getX() - lower.getX() + 1, higher.getZ() - lower.getZ() + 1};
    }

    public boolean contains(BlockSurfacePos coord)
    {
        BlockSurfacePos lower = getLowerCorner();
        BlockSurfacePos higher = getHigherCorner();

        return coord.getX() >= lower.getX() && coord.getZ() >= lower.getZ() && coord.getX() <= higher.getX() && coord.getZ() <= higher.getZ();
    }

    @Override
    public Iterator<BlockSurfacePos> iterator()
    {
        return stream().iterator();
    }

    public Stream<BlockSurfacePos> stream()
    {
        BlockSurfacePos lower = getLowerCorner();
        BlockSurfacePos higher = getHigherCorner();
        return IvStreams.flatMapToObj(IntStream.range(lower.x, higher.x + 1), x -> IntStream.range(lower.z, higher.z + 1).mapToObj(z -> new BlockSurfacePos(x, z)));
    }
}