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

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.RotationHelper;

import java.util.Arrays;

/**
 * Created by lukas on 08.06.14.
 */
public class AxisAlignedTransform2D
{
    public static final AxisAlignedTransform2D ORIGINAL = new AxisAlignedTransform2D(new int[][]{{1, 0}, {0, 1}});

    /**
     * [from][to]
     */
    private int[][] matrix;

    protected AxisAlignedTransform2D(int[][] matrix)
    {
        this.matrix = matrix;
    }

    public static AxisAlignedTransform2D transform(int rotationClockwise, boolean flipX)
    {
        return flipX ? ORIGINAL.rotateClockwise(rotationClockwise).flipX() : ORIGINAL.rotateClockwise(rotationClockwise);
    }

    public static AxisAlignedTransform2D transform(AxisAlignedTransform2D original, int rotationClockwise, boolean flipX)
    {
        return flipX ? original.rotateClockwise(rotationClockwise).flipX() : original.rotateClockwise(rotationClockwise);
    }

    public int[][] getMatrix()
    {
        return matrix.clone();
    }

    public AxisAlignedTransform2D rotateClockwise(int steps)
    {
        AxisAlignedTransform2D transform2D = this;

        for (int i = 0; i < steps % 4; i++)
        {
            transform2D = transform2D.rotateClockwise();
        }
        for (int i = 0; i < -(steps % 4); i++)
        {
            transform2D = transform2D.rotateCounterClockwise();
        }

        return transform2D;
    }

    public AxisAlignedTransform2D rotateClockwise()
    {
        return new AxisAlignedTransform2D(new int[][]{{this.matrix[0][1], -this.matrix[0][0]}, {this.matrix[1][1], -this.matrix[1][0]}});
    }

    public AxisAlignedTransform2D rotateCounterClockwise(int steps)
    {
        return rotateClockwise(-steps);
    }

    public AxisAlignedTransform2D rotateCounterClockwise()
    {
        return new AxisAlignedTransform2D(new int[][]{{-this.matrix[0][1], this.matrix[0][0]}, {-this.matrix[1][1], this.matrix[1][0]}});
    }

    public AxisAlignedTransform2D flipX()
    {
        return new AxisAlignedTransform2D(new int[][]{{-this.matrix[0][0], this.matrix[0][1]}, {-this.matrix[1][0], this.matrix[1][1]}});
    }

    public AxisAlignedTransform2D flipZ()
    {
        return new AxisAlignedTransform2D(new int[][]{{this.matrix[0][0], -this.matrix[0][1]}, {this.matrix[1][0], -this.matrix[1][1]}});
    }

    public BlockCoord apply(BlockCoord position, int[] size)
    {
        int[] center = new int[]{size[0] / 2, size[1] / 2, size[2] / 2};
        int[] shifted = new int[]{position.x - center[0], position.y - center[1], position.z - center[2]};
        int[] transformed = new int[]{shifted[0] * matrix[0][0] + shifted[2] * matrix[1][0], shifted[1], shifted[0] * matrix[0][1] + shifted[2] * matrix[1][1]};

        return new BlockCoord(transformed[0] + center[0], transformed[1] + center[1], transformed[2] + center[2]);
    }

    public double[] apply(double[] position, int[] size)
    {
        int[] center = new int[]{size[0] / 2, size[1] / 2, size[2] / 2};
        double[] shifted = new double[]{position[0] - center[0], position[1] - center[1], position[2] - center[2]};
        double[] transformed = new double[]{shifted[0] * matrix[0][0] + shifted[2] * matrix[1][0], shifted[1], shifted[0] * matrix[0][1] + shifted[2] * matrix[1][1]};

        return new double[]{transformed[0] + center[0], transformed[1] + center[1], transformed[2] + center[2]};
    }

    public void rotateBlock(World world, BlockCoord coord, Block block)
    {
        int number = matrix[0][0] > 0 ? 0 : (matrix[0][0] < 0 ? 2 : (matrix[0][1] < 0 ? 3 : 1));

        for (int i = 0; i < number; i++)
        {
            block.rotateBlock(world, coord.x, coord.y, coord.z, ForgeDirection.UP);
        }
    }
}
