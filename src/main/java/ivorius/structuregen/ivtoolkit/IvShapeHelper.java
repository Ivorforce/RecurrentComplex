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

/**
 * Created by lukas on 09.06.14.
 */
public class IvShapeHelper
{
    public static boolean isPointInSpheroid(double[] point, double[] spheroidOrigin, double[] spheroidRadius)
    {
        double totalDist = 0.0;

        for (int i = 0; i < point.length; i++)
        {
            double dist = (point[i] - spheroidOrigin[i]) / spheroidRadius[i];
            totalDist += dist * dist;
        }

        return totalDist < 1.0;
    }
}
