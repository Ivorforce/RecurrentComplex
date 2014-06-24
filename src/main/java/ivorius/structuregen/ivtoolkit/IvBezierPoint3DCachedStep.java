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

public class IvBezierPoint3DCachedStep
{
    private IvBezierPoint3D leftPoint;
    private int leftPointIndex;
    private double leftPointProgress;

    private IvBezierPoint3D rightPoint;
    private int rightPointIndex;
    private double rightPointProgress;

    private double bezierPathProgress;
    private double innerProgress;

    private double[] cachedPosition;

    public IvBezierPoint3DCachedStep(IvBezierPoint3D leftPoint, int leftPointIndex, IvBezierPoint3D rightPoint, int rightPointIndex, double leftPointProgress, double rightPointProgress, double innerProgress)
    {
        this.leftPoint = leftPoint;
        this.leftPointIndex = leftPointIndex;
        this.rightPoint = rightPoint;
        this.rightPointIndex = rightPointIndex;
        this.leftPointProgress = leftPointProgress;
        this.rightPointProgress = rightPointProgress;
        this.innerProgress = innerProgress;

        this.bezierPathProgress = leftPointProgress + (rightPointProgress - leftPointProgress) * innerProgress;
    }

    public IvBezierPoint3D getLeftPoint()
    {
        return leftPoint;
    }

    public int getLeftPointIndex()
    {
        return leftPointIndex;
    }

    public IvBezierPoint3D getRightPoint()
    {
        return rightPoint;
    }

    public int getRightPointIndex()
    {
        return rightPointIndex;
    }

    public double getLeftPointProgress()
    {
        return leftPointProgress;
    }

    public double getRightPointProgress()
    {
        return rightPointProgress;
    }

    public double getBezierPathProgress()
    {
        return bezierPathProgress;
    }

    public double getInnerProgress()
    {
        return innerProgress;
    }

    public double[] getPosition()
    {
        if (cachedPosition == null)
        {
            double[] bezierFrom = leftPoint.getBezierDirectionPointFrom();
            double[] bezierTo = rightPoint.getBezierDirectionPointTo();

            cachedPosition = IvVecMathHelper.cubicMix(leftPoint.position, bezierFrom, bezierTo, rightPoint.position, getInnerProgress());
        }

        return cachedPosition;
    }
}
