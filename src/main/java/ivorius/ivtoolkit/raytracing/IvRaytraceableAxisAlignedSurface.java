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

import net.minecraft.client.renderer.Tessellator;

import java.util.List;

public class IvRaytraceableAxisAlignedSurface extends IvRaytraceableObject
{
    private double x, y, z;
    private double width, height, depth;

    public IvRaytraceableAxisAlignedSurface(Object userInfo, double x, double y, double z, double width, double height, double depth)
    {
        super(userInfo);

        this.x = x;
        this.y = y;
        this.z = z;

        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public double getWidth()
    {
        return width;
    }

    public void setWidth(double width)
    {
        this.width = width;
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    public double getDepth()
    {
        return depth;
    }

    public void setDepth(double depth)
    {
        this.depth = depth;
    }

    @Override
    public void addRaytracedIntersectionsForLineToList(List<IvRaytracedIntersection> list, double x, double y, double z, double xDir, double yDir, double zDir)
    {
        checkValid();

        double t = 0;
        boolean cantHit = false;
        boolean onPlane = false;

        if (width == 0)
        {
            double xDist = (this.x - x);

            if (xDist == 0 && xDir == 0)
            {
                onPlane = true;
            }
            else if (xDist != 0 && xDir == 0)
            {
                cantHit = true;
            }
            else
            {
                t = xDist / xDir;
            }
        }
        else if (height == 0)
        {
            double yDist = (this.y - y);

            if (yDist == 0 && yDir == 0)
            {
                onPlane = true;
            }
            else if (yDist != 0 && yDir == 0)
            {
                cantHit = true;
            }
            else
            {
                t = yDist / yDir;
            }
        }
        else if (depth == 0)
        {
            double zDist = (this.z - z);

            if (zDist == 0 && zDir == 0)
            {
                onPlane = true;
            }
            else if (zDist != 0 && zDir == 0)
            {
                cantHit = true;
            }
            else
            {
                t = zDist / zDir;
            }
        }

        if (onPlane)
        {
            return; // For our purposes, this is not hit :<
        }

        if (!cantHit)
        {
            double[] hitPoint = new double[]{x + t * xDir, y + t * yDir, z + t * zDir};

            if (withinBounds(hitPoint[0], this.x, this.x + this.width) && withinBounds(hitPoint[1], this.y, this.y + this.height) && withinBounds(hitPoint[2], this.z, this.z + this.depth))
            {
                list.add(new IvRaytracedIntersection(this, hitPoint));
            }
        }
    }

    private boolean withinBounds(double value, double min, double max)
    {
        return value >= min && value <= max;
    }

    private void checkValid()
    {
        int lengths0 = 0;
        if (width == 0)
        {
            lengths0++;
        }
        if (height == 0)
        {
            lengths0++;
        }
        if (depth == 0)
        {
            lengths0++;
        }

        if (lengths0 != 1)
        {
            throw new ArithmeticException("Axis aligned surface must have exactly one length that is zero. (But has: " + lengths0 + ")");
        }
    }

    @Override
    public void drawOutlines()
    {
        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawing(3);
        if (width == 0)
        {
            tessellator.addVertex(x, y, z);
            tessellator.addVertex(x, y + height, z);
            tessellator.addVertex(x, y + height, z + depth);
            tessellator.addVertex(x, y, z + depth);
            tessellator.addVertex(x, y, z);
        }
        if (height == 0)
        {
            tessellator.addVertex(x, y, z);
            tessellator.addVertex(x + width, y, z);
            tessellator.addVertex(x + width, y, z + depth);
            tessellator.addVertex(x, y, z + depth);
            tessellator.addVertex(x, y, z);
        }
        if (depth == 0)
        {
            tessellator.addVertex(x, y, z);
            tessellator.addVertex(x + width, y, z);
            tessellator.addVertex(x + width, y + height, z);
            tessellator.addVertex(x, y + height, z);
            tessellator.addVertex(x, y, z);
        }
        tessellator.draw();
    }
}
