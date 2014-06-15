/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

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
