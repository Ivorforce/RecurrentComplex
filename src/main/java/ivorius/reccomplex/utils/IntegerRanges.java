/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.gui.IntegerRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by lukas on 18.09.16.
 */
public class IntegerRanges
{
    public static IntStream stream(Collection<IntegerRange> ranges)
    {
        return ranges.stream().flatMapToInt(c -> IntStream.range(c.min, c.max + 1)).distinct();
    }

    public static void cut(List<IntegerRange> ranges, Collection<IntegerRange> cut)
    {
        ranges.removeIf(r -> cut(r, cut));
    }

    public static boolean cut(IntegerRange range, Collection<IntegerRange> cut)
    {
        return cut.stream().anyMatch(r -> range.getMin() >= r.getMin() && range.getMax() <= r.getMax());
    }

    public static boolean intersects(IntegerRange range, List<IntegerRange> ranges)
    {
        return ranges.stream().anyMatch(r -> intersects(range, r));
    }

    public static boolean intersects(IntegerRange range1, IntegerRange range2)
    {
        int min = Math.max(range1.min, range2.min);
        int max = Math.min(range1.max, range2.max);
        return min < max;
    }

    @Nullable
    public static IntegerRange crop(IntegerRange range, List<IntegerRange> ranges)
    {
        int[] i = new int[]{range.max, range.min};
        ranges.stream().filter(r -> intersects(range, r)).forEach(r ->
        {
            i[0] = Math.min(i[0], r.min);
            i[1] = Math.max(i[1], r.max);
        });
        return i[0] < i[1] ? new IntegerRange(Math.max(range.min, i[0]), Math.min(range.max, i[1])) : null;
    }

    @Nullable
    public static IntegerRange intersection(IntegerRange range1, IntegerRange range2)
    {
        int min = Math.max(range1.min, range2.min);
        int max = Math.min(range1.max, range2.max);
        return min < max ? new IntegerRange(min, max) : null;
    }

    @Nonnull
    public static IntegerRange from(int p1, int p2)
    {
        return new IntegerRange(Math.min(p1, p2), Math.max(p1, p2));
    }
}
