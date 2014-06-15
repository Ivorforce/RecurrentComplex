package ivorius.structuregen.ivtoolkit;

import java.util.Iterator;

/**
 * Created by lukas on 09.06.14.
 */
public class BlockArea implements Iterable<BlockCoord>
{
    private BlockCoord point1;
    private BlockCoord point2;

    public BlockArea(BlockCoord point1, BlockCoord point2)
    {
        this.point1 = point1;
        this.point2 = point2;
    }

    public BlockCoord getPoint1()
    {
        return point1;
    }

    public void setPoint1(BlockCoord point1)
    {
        this.point1 = point1;
    }

    public BlockCoord getPoint2()
    {
        return point2;
    }

    public void setPoint2(BlockCoord point2)
    {
        this.point2 = point2;
    }

    public BlockCoord getLowerCorner()
    {
        return point1.getLowerCorner(point2);
    }

    public BlockCoord getHigherCorner()
    {
        return point1.getHigherCorner(point2);
    }

    public int[] areaSize()
    {
        BlockCoord lower = getLowerCorner();
        BlockCoord higher = getHigherCorner();

        return new int[]{higher.x - lower.x + 1, higher.y - lower.y + 1, higher.z - lower.z + 1};
    }

    public boolean contains(BlockCoord coord)
    {
        BlockCoord lower = getLowerCorner();
        BlockCoord higher = getHigherCorner();

        return coord.x >= lower.x && coord.y >= lower.y && coord.z >= lower.z && coord.x <= higher.x && coord.y <= higher.y && coord.z <= higher.z;
    }

    @Override
    public Iterator<BlockCoord> iterator()
    {
        return new BlockAreaIterator(getLowerCorner(), getHigherCorner());
    }
}
