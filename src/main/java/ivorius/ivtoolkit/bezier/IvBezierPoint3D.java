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

package ivorius.ivtoolkit.bezier;

public class IvBezierPoint3D
{
    public double[] position;
    public double[] bezierDirection;
    public double rotation;

    public int color;

    public double fontSize;

    public IvBezierPoint3D(double[] position, double[] bezierDirection, int color, double rotation)
    {
        this.position = position;

        this.bezierDirection = bezierDirection;

        this.color = color;

        this.rotation = rotation;

        this.fontSize = 1.0;
    }

    public IvBezierPoint3D(double[] position, double[] bezierDirection, int color, double rotation, double fontSize)
    {
        this(position, bezierDirection, color, rotation);

        this.fontSize = fontSize;
    }

    public double getAlpha()
    {
        return ((color >> 24) & 255) / 255.0;
    }

    public double getRed()
    {
        return ((color >> 16) & 255) / 255.0;
    }

    public double getGreen()
    {
        return ((color >> 8) & 255) / 255.0;
    }

    public double getBlue()
    {
        return (color & 255) / 255.0;
    }

    public double[] getBezierDirectionPointTo()
    {
        double[] result = new double[bezierDirection.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = position[i] - bezierDirection[i];
        }

        return result;
    }

    public double[] getBezierDirectionPointFrom()
    {
        double[] result = new double[bezierDirection.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = position[i] + bezierDirection[i];
        }

        return result;
    }

    public double getFontSize()
    {
        return fontSize;
    }
}
