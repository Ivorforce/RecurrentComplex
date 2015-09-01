/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils.scale;

import ivorius.ivtoolkit.gui.FloatRange;

/**
 * Created by lukas on 01.09.15.
 */
public class Scales
{
    private static Scale none = new Scale()
    {
        @Override
        public float in(float val)
        {
            return val;
        }

        @Override
        public float out(float val)
        {
            return val;
        }
    };

    public static Scale none()
    {
        return none;
    }

    public static Scale reverse(final Scale a)
    {
        return new Scale()
        {
            @Override
            public float in(float val)
            {
                return a.out(val);
            }

            @Override
            public float out(float val)
            {
                return a.in(val);
            }
        };
    }

    public static Scale combine(final Scale a, final Scale b)
    {
        return new Scale()
        {
            @Override
            public float in(float val)
            {
                return b.in(a.in(val));
            }

            @Override
            public float out(float val)
            {
                return a.out(b.out(val));
            }
        };
    }

    public static Scale combine(final Scale... scales)
    {
        return new Scale()
        {
            @Override
            public float in(float val)
            {
                for (Scale scale : scales)
                    val = scale.in(val);
                return val;
            }

            @Override
            public float out(float val)
            {
                for (int i = scales.length - 1; i >= 0; i--)
                    val = scales[i].out(val);
                return val;
            }
        };
    }

    public static Scale shift(final float shift)
    {
        return new Scale()
        {
            @Override
            public float in(float val)
            {
                return val + shift;
            }

            @Override
            public float out(float val)
            {
                return val - shift;
            }
        };
    }

    public static Scale linear(final float covariant)
    {
        return new Scale()
        {
            @Override
            public float in(float val)
            {
                return val * covariant;
            }

            @Override
            public float out(float val)
            {
                return val / covariant;
            }
        };
    }

    public static Scale pow(final float pow)
    {
        return new Scale()
        {
            @Override
            public float in(float val)
            {
                return (float) Math.pow(val, pow);
            }

            @Override
            public float out(float val)
            {
                return (float) Math.pow(val, 1 / pow);
            }
        };
    }

    public static Scale exponential(final float base)
    {
        return new Scale()
        {
            @Override
            public float in(float val)
            {
                return (float) Math.pow(base, val);
            }

            @Override
            public float out(float val)
            {
                return (float) (Math.log(val) / Math.log(base));
            }
        };
    }

    public static FloatRange in(Scale scale, FloatRange range)
    {
        return new FloatRange(scale.in(range.getMin()), scale.in(range.getMax()));
    }

    public static FloatRange out(Scale scale, FloatRange range)
    {
        return new FloatRange(scale.out(range.getMin()), scale.out(range.getMax()));
    }
}
