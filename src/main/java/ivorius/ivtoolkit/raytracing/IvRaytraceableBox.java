/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.raytracing;

import java.util.List;

public class IvRaytraceableBox extends IvRaytraceableObject
{
    private double x, y, z;

    private double width, height, depth;

    private IvRaytraceableAxisAlignedSurface[] surfaces;

    public IvRaytraceableBox(Object userInfo, double x, double y, double z, double width, double height, double depth)
    {
        super(userInfo);

        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.surfaces = new IvRaytraceableAxisAlignedSurface[6];
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    public double getDepth()
    {
        return depth;
    }

    @Override
    public void addRaytracedIntersectionsForLineToList(List<IvRaytracedIntersection> list, double x, double y, double z, double xDir, double yDir, double zDir)
    {
        for (IvRaytraceableAxisAlignedSurface surface : this.surfaces)
        {
            surface.addRaytracedIntersectionsForLineToList(list, x, y, z, xDir, yDir, zDir);
        }
    }
}
