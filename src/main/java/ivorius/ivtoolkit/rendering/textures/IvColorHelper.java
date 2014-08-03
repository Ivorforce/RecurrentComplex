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

package ivorius.ivtoolkit.rendering.textures;

import net.minecraft.util.MathHelper;

import java.awt.image.BufferedImage;

/**
 * Created by lukas on 26.07.14.
 */
public class IvColorHelper
{
    public static int[] getARBGInts(int argb)
    {
        return new int[]{argb >>> 24, (argb >>> 16) & 255, (argb >>> 8) & 255, argb & 255};
    }

    public static int[] getARBGInts(float[] argb)
    {
        return new int[]{MathHelper.floor_float(argb[0] * 255.0f + 0.5f), MathHelper.floor_float(argb[1] * 255.0f + 0.5f), MathHelper.floor_float(argb[2] * 255.0f + 0.5f), MathHelper.floor_float(argb[3] * 255.0f + 0.5f)};
    }

    public static float[] getARBGFloats(int[] argb)
    {
        return new float[]{(float) argb[0] / 255.0f, (float) argb[1] / 255.0f, (float) argb[2] / 255.0f, (float) argb[3] / 255.0f};
    }

    public static float[] getARBGFloats(int argb)
    {
        int alpha = argb >>> 24;
        int red = (argb >>> 16) & 255;
        int green = (argb >>> 8) & 255;
        int blue = argb & 255;

        return new float[]{(float) alpha / 255.0f, (float) red / 255.0f, (float) green / 255.0f, (float) blue / 255.0f};
    }

    public static int getARBGInt(float[] argb)
    {
        int alpha = MathHelper.clamp_int(MathHelper.floor_float(argb[0] * 255.0f + 0.5f), 0, 255);
        int red = MathHelper.clamp_int(MathHelper.floor_float(argb[1] * 255.0f + 0.5f), 0, 255);
        int green = MathHelper.clamp_int(MathHelper.floor_float(argb[2] * 255.0f + 0.5f), 0, 255);
        int blue = MathHelper.clamp_int(MathHelper.floor_float(argb[3] * 255.0f + 0.5f), 0, 255);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
