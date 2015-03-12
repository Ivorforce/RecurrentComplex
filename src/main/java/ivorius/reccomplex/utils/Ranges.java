/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by lukas on 12.03.15.
 */
public class Ranges
{
    public static Iterable<Integer> rangeIterable(int start, int end, int stride)
    {
        return new RangeIterable(start, end, end < start ? -stride : stride);
    }

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

    public static Iterable<Integer> rangeIterable(int start, int end)
    {
        return rangeIterable(start, end, 1);
    }

    public static int[] range(int start, int end)
    {
        return range(start, end, 1);
    }

    public static Iterable<Integer> toIterable(int end)
    {
        return rangeIterable(0, end, 1);
    }

    public static int[] to(int end)
    {
        return range(0, end, 1);
    }

    protected static class RangeIterable implements Iterable<Integer>
    {
        protected int start;
        protected int end;
        protected int stride;

        public RangeIterable(int start, int end, int stride)
        {
            this.start = start;
            this.end = end;
            this.stride = stride;
        }

        @Override
        public Iterator<Integer> iterator()
        {
            return new RangeIterator(start, end, stride);
        }
    }

    protected static class RangeIterator implements Iterator<Integer>
    {
        protected int idx;
        protected int end;
        protected int stride;

        public RangeIterator(int idx, int end, int stride)
        {
            this.idx = idx;
            this.end = end;
            this.stride = stride;
        }

        @Override
        public boolean hasNext()
        {
            return stride == 0 || ((stride > 0 == idx < end) && idx != end);
        }

        @Override
        public Integer next()
        {
            if (!hasNext())
                throw new NoSuchElementException();

            int ret = idx;
            idx += stride;
            return ret;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
