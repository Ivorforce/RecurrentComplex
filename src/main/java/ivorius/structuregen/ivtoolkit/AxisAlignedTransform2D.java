package ivorius.structuregen.ivtoolkit;

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
}
