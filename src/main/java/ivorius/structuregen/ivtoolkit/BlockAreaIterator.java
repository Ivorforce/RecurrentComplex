package ivorius.structuregen.ivtoolkit;

import java.util.Iterator;

/**
 * Created by lukas on 09.06.14.
 */
public class BlockAreaIterator implements Iterator<BlockCoord>
{
    private BlockCoord lower;
    private BlockCoord higher;

    private int curX;
    private int curY;
    private int curZ;

    public BlockAreaIterator(BlockCoord lower, BlockCoord higher)
    {
        this.lower = lower;
        this.higher = higher;

        curX = lower.x;
        curY = lower.y;
        curZ = lower.z;
    }

    @Override
    public boolean hasNext()
    {
        return curX <= higher.x && curY <= higher.y && curZ <= higher.z;
    }

    @Override
    public BlockCoord next()
    {
        BlockCoord retVal = hasNext() ? new BlockCoord(curX, curY, curZ) : null;

        curX ++;

        if (curX > higher.x)
        {
            curX = lower.x;
            curY ++;

            if (curY > higher.y)
            {
                curY = lower.y;
                curZ ++;
            }
        }

        return retVal;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
