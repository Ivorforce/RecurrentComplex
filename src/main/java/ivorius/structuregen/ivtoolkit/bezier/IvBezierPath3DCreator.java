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

package ivorius.structuregen.ivtoolkit.bezier;

import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Collections;

public class IvBezierPath3DCreator
{
    public static IvBezierPath3D createSpiraledBezierPath(double distCenter, double heightDistInf, double spirals, double maxHeight, double fontSize, double heightFontInf, boolean staged)
    {
        ArrayList<IvBezierPoint3D> bezierPoints = new ArrayList<>();

        for (double height = 0; height <= maxHeight; height += maxHeight / spirals)
        {
            double height0 = height;
            double height1 = height + (maxHeight / spirals) * 0.25;
            double height2 = height + (maxHeight / spirals) * 0.5;
            double height3 = height + (maxHeight / spirals) * 0.75;

            double distance0 = distCenter * (1.0 + (staged ? height : height0) * heightDistInf);
            double distance1 = distCenter * (1.0 + (staged ? height : height1) * heightDistInf);
            double distance2 = distCenter * (1.0 + (staged ? height : height2) * heightDistInf);
            double distance3 = distCenter * (1.0 + (staged ? height : height3) * heightDistInf);

            bezierPoints.add(new IvBezierPoint3D(new double[]{distance0, height0, -distance0}, new double[]{-0.5 * distance0, 0.0, -0.5 * distance0}, 0xffffffff, 0.0, fontSize + height0 / maxHeight * heightFontInf));
            if (height1 <= maxHeight)
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{-distance1, height1, -distance1}, new double[]{-0.5 * distance1, 0.0, 0.5 * distance1}, 0xffffffff, 0.0, fontSize + height1 / maxHeight * heightFontInf));
            }
            if (height2 <= maxHeight)
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{-distance2, height2, distance2}, new double[]{0.5 * distance2, 0.0, 0.5 * distance2}, 0xffffffff, 0.0, fontSize + height2 / maxHeight * heightFontInf));
            }
            if (height3 <= maxHeight)
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{distance3, height3, distance3}, new double[]{0.5 * distance3, 0.0, -0.5 * distance3}, 0xffffffff, 0.0, fontSize + height3 / maxHeight * heightFontInf));
            }
        }

        return new IvBezierPath3D(bezierPoints);
    }

    public static IvBezierPath3D createSpiraledBezierPath(double distCenter, double heightDistInf, double spirals, double[] endPos, double fontSize, double heightFontInf, boolean staged)
    {
        ArrayList<IvBezierPoint3D> bezierPoints = new ArrayList<>();

        for (double height = 0; height <= endPos[1]; height += endPos[1] / spirals)
        {
            double height0 = height;
            double height1 = height + (endPos[1] / spirals) * 0.25;
            double height2 = height + (endPos[1] / spirals) * 0.5;
            double height3 = height + (endPos[1] / spirals) * 0.75;

            double x0 = height0 / endPos[1] * endPos[0];
            double x1 = height1 / endPos[1] * endPos[0];
            double x2 = height2 / endPos[1] * endPos[0];
            double x3 = height3 / endPos[1] * endPos[0];

            double z0 = height0 / endPos[1] * endPos[2];
            double z1 = height1 / endPos[1] * endPos[2];
            double z2 = height2 / endPos[1] * endPos[2];
            double z3 = height3 / endPos[1] * endPos[2];

            double distance0 = distCenter * (1.0 + (staged ? height : height0) * heightDistInf);
            double distance1 = distCenter * (1.0 + (staged ? height : height1) * heightDistInf);
            double distance2 = distCenter * (1.0 + (staged ? height : height2) * heightDistInf);
            double distance3 = distCenter * (1.0 + (staged ? height : height3) * heightDistInf);

            bezierPoints.add(new IvBezierPoint3D(new double[]{distance0 + x0, height0, -distance0 + z0}, new double[]{-0.5 * distance0, 0.0, -0.5 * distance0}, 0xffffffff, 0.0, fontSize + height0 / endPos[1] * heightFontInf));
            if (height1 <= endPos[1])
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{-distance1 + x1, height1, -distance1 + z1}, new double[]{-0.5 * distance1, 0.0, 0.5 * distance1}, 0xffffffff, 0.0, fontSize + height1 / endPos[1] * heightFontInf));
            }
            if (height2 <= endPos[1])
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{-distance2 + x2, height2, distance2 + z2}, new double[]{0.5 * distance2, 0.0, 0.5 * distance2}, 0xffffffff, 0.0, fontSize + height2 / endPos[1] * heightFontInf));
            }
            if (height3 <= endPos[1])
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{distance3 + x3, height3, distance3 + z3}, new double[]{0.5 * distance3, 0.0, -0.5 * distance3}, 0xffffffff, 0.0, fontSize + height3 / endPos[1] * heightFontInf));
            }
        }

        return new IvBezierPath3D(bezierPoints);
    }

    public static IvBezierPath3D createSpiraledSphere(double radius, double spirals, double fontSize)
    {
        ArrayList<IvBezierPoint3D> bezierPoints = new ArrayList<>();

        double centerYShift = -radius * 0.5;
        for (double height = 0; height <= radius; height += radius / spirals)
        {
            double height0 = height;
            double height1 = height + (radius / spirals) * 0.25;
            double height2 = height + (radius / spirals) * 0.5;
            double height3 = height + (radius / spirals) * 0.75;

            double distance0 = MathHelper.cos((float) (height0 / radius - 0.5) * 3.1415926f);
            double distance1 = MathHelper.cos((float) (height1 / radius - 0.5) * 3.1415926f);
            double distance2 = MathHelper.cos((float) (height2 / radius - 0.5) * 3.1415926f);
            double distance3 = MathHelper.cos((float) (height3 / radius - 0.5) * 3.1415926f);

            bezierPoints.add(new IvBezierPoint3D(new double[]{distance0, height0 + centerYShift, -distance0}, new double[]{-0.5 * distance0, 0.0, -0.5 * distance0}, 0xffffffff, 0.0, fontSize));
            if (height1 <= radius)
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{-distance1, height1 + centerYShift, -distance1}, new double[]{-0.5 * distance1, 0.0, 0.5 * distance1}, 0xffffffff, 0.0, fontSize));
            }
            if (height2 <= radius)
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{-distance2, height2 + centerYShift, distance2}, new double[]{0.5 * distance2, 0.0, 0.5 * distance2}, 0xffffffff, 0.0, fontSize));
            }
            if (height3 <= radius)
            {
                bezierPoints.add(new IvBezierPoint3D(new double[]{distance3, height3 + centerYShift, distance3}, new double[]{0.5 * distance3, 0.0, -0.5 * distance3}, 0xffffffff, 0.0, fontSize));
            }
        }

        return new IvBezierPath3D(bezierPoints);
    }

    public static IvBezierPath3D createQuickBezierPath(IvBezierPoint3D[] points)
    {
        ArrayList<IvBezierPoint3D> bezierPoints = new ArrayList<>();

        Collections.addAll(bezierPoints, points);

        return new IvBezierPath3D(bezierPoints);
    }

}
