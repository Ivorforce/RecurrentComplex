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

package ivorius.ivtoolkit.blocks;

import net.minecraft.util.AxisAlignedBB;

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

    public AxisAlignedBB asAxisAlignedBB()
    {
        BlockCoord lower = getLowerCorner();
        BlockCoord higher = getHigherCorner();

        return AxisAlignedBB.getBoundingBox(lower.x, lower.y, lower.z, higher.x, higher.y, higher.z);
    }

    @Override
    public Iterator<BlockCoord> iterator()
    {
        return new BlockAreaIterator(getLowerCorner(), getHigherCorner());
    }
}
