/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.raytracing;

import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IvRaytracer
{
    public static List<IvRaytracedIntersection> getIntersections(List<IvRaytraceableObject> objects, double x, double y, double z, double xDir, double yDir, double zDir)
    {
        ArrayList<IvRaytracedIntersection> hitPoints = new ArrayList<>();

        for (IvRaytraceableObject object : objects)
        {
            object.addIntersectionsForLineToList(hitPoints, x, y, z, xDir, yDir, zDir);
        }

        return hitPoints;
    }

    public static IvRaytracedIntersection findFirstIntersection(List<IvRaytracedIntersection> intersections, double x, double y, double z, double xDir, double yDir, double zDir)
    {
        IvRaytracedIntersection firstPoint = null;
        double firstDistanceSQ = -1;

        for (IvRaytracedIntersection point : intersections)
        {
            double pointDistSQ = (x - point.getX()) * (x - point.getX()) + (y - point.getY()) * (y - point.getY()) + (z - point.getZ()) * (z - point.getZ());

            if (firstDistanceSQ < 0 || pointDistSQ < firstDistanceSQ)
            {
                firstPoint = point;
                firstDistanceSQ = pointDistSQ;
            }
        }

        return firstPoint;
    }

    public static IvRaytracedIntersection getFirstIntersection(List<IvRaytraceableObject> objects, double x, double y, double z, double xDir, double yDir, double zDir)
    {
        ArrayList<IvRaytracedIntersection> intersections = new ArrayList<>();

        for (IvRaytraceableObject object : objects)
        {
            object.addIntersectionsForLineToList(intersections, x, y, z, xDir, yDir, zDir);
        }

        return findFirstIntersection(intersections, x, y, z, xDir, yDir, zDir);
    }

    public static void drawStandardOutlines(List<IvRaytraceableObject> objects)
    {
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
//		GL11.glColor4f(0, 0, 0, 0.4f);

        Random r = new Random(0);
        for (IvRaytraceableObject object : objects)
        {
            int color = r.nextInt();
            GL11.glColor4f(((color >> 16) & 255) / 255.0f, ((color >> 8) & 255) / 255.0f, ((color >> 0) & 255) / 255.0f, 0.8F);
            object.drawOutlines();
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
