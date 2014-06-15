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

import java.util.ArrayList;
import java.util.List;

public class IvBezierPath3D
{
    private List<IvBezierPoint3D> bezierPoints;

    private List<Double> cachedDistances;
    private double cachedFullDistance;
    private List<Double> cachedProgresses;

    private boolean isDirty;

    public IvBezierPath3D()
    {
        cachedProgresses = new ArrayList<Double>();
        cachedDistances = new ArrayList<Double>();

        setBezierPoints(new ArrayList<IvBezierPoint3D>());
    }

    public IvBezierPath3D(List<IvBezierPoint3D> bezierPoints)
    {
        cachedProgresses = new ArrayList<Double>();
        cachedDistances = new ArrayList<Double>();

        setBezierPoints(bezierPoints);
    }

    public void buildDistances()
    {
        isDirty = false;

        cachedFullDistance = 0.0;
        cachedDistances.clear();
        cachedProgresses.clear();

        IvBezierPoint3D previousPoint = null;
        for (IvBezierPoint3D bezierPoint : bezierPoints)
        {
            if (previousPoint != null)
            {
                double distance = 0.0;

                int samples = 50;
                for (int i = 0; i < samples; i++)
                {
                    double[] bezierFrom = previousPoint.getBezierDirectionPointFrom();
                    double[] bezierTo = bezierPoint.getBezierDirectionPointTo();

                    double[] position = IvMathHelper.cubicMix(previousPoint.position, bezierFrom, bezierTo, bezierPoint.position, (double) i / (double) samples);
                    double[] position1 = IvMathHelper.cubicMix(previousPoint.position, bezierFrom, bezierTo, bezierPoint.position, (double) (i + 1) / (double) samples);

                    distance += IvMathHelper.distance(position, position1);
                }

                cachedFullDistance += distance;
                cachedDistances.add(distance);
            }

            previousPoint = bezierPoint;
        }

        for (Double d : cachedDistances)
        {
            cachedProgresses.add(d / cachedFullDistance);
        }
    }

    public double getPathLengthInRange(int startIndex, int endIndex)
    {
        double maxProgress = 0.0;

        int arraySize = cachedProgresses.size();
        for (int i = startIndex; i < endIndex; i++)
        {
            maxProgress += cachedProgresses.get(i % arraySize);
        }

        return maxProgress;
    }

    public double getPathLength()
    {
        return cachedFullDistance;
    }

    private IvBezierPoint3DCachedStep getCachedStep(int leftIndex, int rightIndex, double leftProgress, double rightProgress, double innerProgress)
    {
        return new IvBezierPoint3DCachedStep(bezierPoints.get(leftIndex), leftIndex, bezierPoints.get(rightIndex), rightIndex, leftProgress, rightProgress, innerProgress);
    }

    public IvBezierPoint3DCachedStep getCachedStep(int leftIndex, int rightIndex, double innerProgress)
    {
        double leftProgress = getPathLengthInRange(0, leftIndex);
        double rightProgress = getPathLengthInRange(0, rightIndex);
        return getCachedStep(leftIndex, rightIndex, leftProgress, rightProgress, innerProgress);
    }

    public IvBezierPoint3DCachedStep getCachedStep(double progress)
    {
        progress = ((progress % 1.0) + 1.0) % 1.0;
        double curProgress = 0.0;

        for (int i = 1; i < bezierPoints.size(); i++)
        {
            double distance = cachedProgresses.get(i - 1);

            if ((progress - distance) <= 0.0)
            {
                return getCachedStep(i - 1, i, curProgress, curProgress + distance, progress / distance);
            }

            progress -= distance;
            curProgress += distance;
        }

        int bezierPointsLength = bezierPoints.size();
        return getCachedStep(bezierPointsLength - 2, bezierPointsLength - 1, 1.0f);
    }

    public IvBezierPoint3DCachedStep getCachedStepAfterStep(IvBezierPoint3DCachedStep cachedStep, double stepSize)
    {
        return getCachedStep(cachedStep.getBezierPathProgress() + stepSize);
    }

    public double[] getMotion(IvBezierPoint3DCachedStep cachedStep, IvBezierPoint3DCachedStep cachedStep1)
    {
        return IvMathHelper.difference(cachedStep1.getPosition(), cachedStep.getPosition());
    }

    public double[] getPVector(IvBezierPoint3DCachedStep cachedStep, double stepSize)
    {
        double[] motion1 = getMotion(cachedStep, getCachedStepAfterStep(cachedStep, stepSize * 0.3));
        double[] motion2 = getMotion(cachedStep, getCachedStepAfterStep(cachedStep, stepSize * 0.6));

//        IvBezierPoint3DCachedStep nextStep = getCachedStep(nextPositionProgress);
//        double[] motionNext1 = getMotion(nextStep, getCachedStep(nextPositionProgress + stepSize * 0.3));
//        double[] motionNext2 = getMotion(nextStep, getCachedStep(nextPositionProgress + stepSize * 0.6));
//
//        double[] pVector = IvMathHelper.crossProduct(IvMathHelper.mix(motion1, motionNext1, cachedStep.progress), IvMathHelper.mix(motion2, motionNext2, cachedStep.progress));
        double[] pVector = IvMathHelper.crossProduct(motion1, motion2);

        return pVector;
    }

    public double[] getNaturalRotation(IvBezierPoint3DCachedStep cachedStep, double stepSize)
    {
        double[] motion1 = getMotion(cachedStep, getCachedStepAfterStep(cachedStep, stepSize * 0.3));
        double[] spherical = IvMathHelper.sphericalFromCartesian(motion1);

        return new double[]{-spherical[0] / Math.PI * 180.0, spherical[1] / Math.PI * 180.0 + 90.0};
    }

    public void markDirty()
    {
        isDirty = true;
    }

    public boolean isDirty()
    {
        return isDirty;
    }

    public void setBezierPoints(List<IvBezierPoint3D> points)
    {
        this.bezierPoints = new ArrayList<IvBezierPoint3D>();
        this.bezierPoints.addAll(points);

        markDirty();
    }

    public List<IvBezierPoint3D> getBezierPoints()
    {
        List<IvBezierPoint3D> l = new ArrayList<IvBezierPoint3D>(bezierPoints.size());
        l.addAll(bezierPoints);
        return l;
    }

    public void removeBezierPoint(IvBezierPoint3D point)
    {
        bezierPoints.remove(point);
        markDirty();
    }

    public void addBezierPoint(IvBezierPoint3D point)
    {
        bezierPoints.add(point);
        markDirty();
    }

    public void addBezierPoints(List<IvBezierPoint3D> points)
    {
        bezierPoints.addAll(points);
        markDirty();
    }
}
