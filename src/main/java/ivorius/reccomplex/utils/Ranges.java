/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

/**
 * Created by lukas on 12.03.15.
 */
public class Ranges
{
    public static int[] range(int start, int end, int stride)
    {
        if (end < start)
        {
            int[] range = new int[(start - end) / stride];
            for (int i = 0; i < range.length; i++)
                range[i] = i * -stride + start;
            return range;
        }
        else
        {
            int[] range = new int[(end - start) / stride];
            for (int i = 0; i < range.length; i++)
                range[i] = i * stride + start;
            return range;
        }
    }

    public static int[] range(int start, int end)
    {
        if (end < start)
        {
            int[] range = new int[start - end];
            while ((++end) <= start)
                range[start - end] = end;
            return range;
        }
        else
        {
            int[] range = new int[end - start];
            while ((--end) >= start)
                range[end - start] = end;
            return range;
        }
    }

    public static int[] to(int end)
    {
        int[] range = new int[end];
        while ((--end) >= 0)
            range[end] = end;
        return range;
    }
}
