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

package ivorius.ivtoolkit.raytracing;

public class IvRaytracedIntersection
{
    private IvRaytraceableObject hitObject;
    private Object hitInfo;
    private double[] point;

    public IvRaytracedIntersection(IvRaytraceableObject hitObject, Object hitInfo, double[] point)
    {
        this.hitObject = hitObject;
        this.hitInfo = hitInfo;
        this.point = point;
    }

    public Object getUserInfo()
    {
        return this.hitObject.userInfo;
    }

    public IvRaytraceableObject getHitObject()
    {
        return hitObject;
    }

    public Object getHitInfo()
    {
        return hitInfo;
    }

    public double getX()
    {
        return point[0];
    }

    public double getY()
    {
        return point[1];
    }

    public double getZ()
    {
        return point[2];
    }

    public double[] getPoint()
    {
        return point.clone();
    }

    @Override
    public String toString()
    {
        return String.format("%s: [%.3f, %.3f, %.3f]", String.valueOf(this.getUserInfo()), this.getX(), this.getY(), this.getZ());
    }
}
