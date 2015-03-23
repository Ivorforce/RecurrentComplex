/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.math.IvMathHelper;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.List;

/**
 * Created by lukas on 23.03.15.
 */
public class Icons
{
    public static final IIcon WHOLE_TEXTURE = new SimpleIcon()
    {
        @Override
        public float getMinU()
        {
            return 0;
        }

        @Override
        public float getMaxU()
        {
            return 1;
        }

        @Override
        public float getInterpolatedU(double lerp)
        {
            return (float) lerp;
        }

        @Override
        public float getMinV()
        {
            return 0;
        }

        @Override
        public float getMaxV()
        {
            return 1;
        }

        @Override
        public float getInterpolatedV(double lerp)
        {
            return (float) lerp;
        }
    };

    public static IIcon from(final float minU, final float minV, final float maxU, final float maxV)
    {
        return new Icon(minU, minV, maxU, maxV);
    }

    public static <T> T frame(T[] icons, float ticks)
    {
        return icons[(MathHelper.floor_float(ticks) % icons.length + icons.length) % icons.length];
    }

    public static <T> T frame(List<T> icons, float ticks)
    {
        return icons.get((MathHelper.floor_float(ticks) % icons.size() + icons.size()) % icons.size());
    }

    public static class Icon extends SimpleIcon
    {
        public float minU, minV, maxU, maxV;

        public Icon(float minU, float minV, float maxU, float maxV)
        {
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
        }

        @Override
        public float getMinU()
        {
            return minU;
        }

        public void setMinU(float minU)
        {
            this.minU = minU;
        }

        @Override
        public float getMinV()
        {
            return minV;
        }

        public void setMinV(float minV)
        {
            this.minV = minV;
        }

        @Override
        public float getMaxU()
        {
            return maxU;
        }

        public void setMaxU(float maxU)
        {
            this.maxU = maxU;
        }

        @Override
        public float getMaxV()
        {
            return maxV;
        }

        public void setMaxV(float maxV)
        {
            this.maxV = maxV;
        }
    }

    public static abstract class SimpleIcon implements IIcon
    {
        @Override
        public int getIconWidth()
        {
            return 0;
        }

        @Override
        public int getIconHeight()
        {
            return 0;
        }

        @Override
        public float getInterpolatedU(double lerp)
        {
            return (float) (getMinU() * (1.0 - lerp) + (getMaxU() * lerp));
        }

        @Override
        public float getInterpolatedV(double lerp)
        {
            return (float) (getMinV() * (1.0 - lerp) + (getMaxV() * lerp));
        }

        @Override
        public String getIconName()
        {
            return null;
        }
    }
}
