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

package ivorius.structuregen.ivtoolkit.math;

import ivorius.structuregen.ivtoolkit.blocks.BlockCoord;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 08.06.14.
 */
public class AxisAlignedTransform2D
{
    public static final AxisAlignedTransform2D ORIGINAL = new AxisAlignedTransform2D(0, false);

    private final int rotation;
    private final boolean mirrorX;

    public AxisAlignedTransform2D(int rotation, boolean mirrorX)
    {
        this.rotation = ((rotation % 4) + 4) % 4;
        this.mirrorX = mirrorX;
    }

    public static AxisAlignedTransform2D transform(int rotationClockwise, boolean flipX)
    {
        return flipX ? ORIGINAL.rotateClockwise(rotationClockwise).flipX() : ORIGINAL.rotateClockwise(rotationClockwise);
    }

    public static AxisAlignedTransform2D transform(AxisAlignedTransform2D original, int rotationClockwise, boolean flipX)
    {
        return flipX ? original.rotateClockwise(rotationClockwise).flipX() : original.rotateClockwise(rotationClockwise);
    }

    public AxisAlignedTransform2D rotateClockwise(int steps)
    {
        return new AxisAlignedTransform2D(rotation + steps, mirrorX);
    }

    public AxisAlignedTransform2D rotateClockwise()
    {
        return new AxisAlignedTransform2D(rotation + 1, mirrorX);
    }

    public AxisAlignedTransform2D rotateCounterClockwise(int steps)
    {
        return new AxisAlignedTransform2D(rotation - steps, mirrorX);
    }

    public AxisAlignedTransform2D rotateCounterClockwise()
    {
        return new AxisAlignedTransform2D(rotation - 1, mirrorX);
    }

    public AxisAlignedTransform2D flipX()
    {
        return new AxisAlignedTransform2D(rotation, !mirrorX);
    }

    public AxisAlignedTransform2D flipZ()
    {
        return new AxisAlignedTransform2D(rotation + 2, !mirrorX);
    }

    public BlockCoord apply(BlockCoord position, int[] size)
    {
        BlockCoord coord;
        int positionX = mirrorX ? -position.x : position.x;

        switch (rotation)
        {
            case 0:
                coord = position;
                break;
            case 1:
                coord = new BlockCoord(position.z, position.y, size[0] - 1 - positionX);
                break;
            case 2:
                coord = new BlockCoord(size[0] - 1 - positionX, position.y, size[2] - 1 - position.z);
                break;
            case 3:
                coord = new BlockCoord(size[2] - 1 - position.z, position.y, positionX);
                break;
            default:
                throw new InternalError();
        }

        return coord;
    }

    public double[] apply(double[] position, int[] size)
    {
        double[] coord;
        double positionX = mirrorX ? -position[0] : position[0];

        switch (rotation)
        {
            case 0:
                coord = position;
                break;
            case 1:
                coord = new double[]{position[2], position[1], size[0] - 1 - positionX};
                break;
            case 2:
                coord = new double[]{size[0] - 1 - positionX, position[1], size[2] - 1 - position[2]};
                break;
            case 3:
                coord = new double[]{size[2] - 1 - position[2], position[1], positionX};
                break;
            default:
                throw new InternalError();
        }

        return coord;
    }

    public void rotateBlock(World world, BlockCoord coord, Block block)
    {
        for (int i = 0; i < rotation; i++)
        {
            block.rotateBlock(world, coord.x, coord.y, coord.z, ForgeDirection.UP);
        }
    }
}
