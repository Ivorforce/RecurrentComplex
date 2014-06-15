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

import java.util.List;

public class IvRaytraceableAxisAlignedBox extends IvRaytraceableObject
{
    private double x, y, z;

    private double width, height, depth;

    private IvRaytraceableAxisAlignedSurface[] surfaces;

    public IvRaytraceableAxisAlignedBox(Object userInfo, double x, double y, double z, double width, double height, double depth)
    {
        super(userInfo);

        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.surfaces = new IvRaytraceableAxisAlignedSurface[6];
        this.surfaces[0] = new IvRaytraceableAxisAlignedSurface(userInfo, x, y, z, 0, height, depth);
        this.surfaces[1] = new IvRaytraceableAxisAlignedSurface(userInfo, x + width, y, z, 0, height, depth);
        this.surfaces[2] = new IvRaytraceableAxisAlignedSurface(userInfo, x, y, z, width, 0, depth);
        this.surfaces[3] = new IvRaytraceableAxisAlignedSurface(userInfo, x, y + height, z, width, 0, depth);
        this.surfaces[4] = new IvRaytraceableAxisAlignedSurface(userInfo, x, y, z, width, height, 0);
        this.surfaces[5] = new IvRaytraceableAxisAlignedSurface(userInfo, x, y, z + depth, width, height, 0);
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

    @Override
    public void drawOutlines()
    {
        super.drawOutlines();

        for (IvRaytraceableObject object : surfaces)
        {
            object.drawOutlines();
        }
    }
}
