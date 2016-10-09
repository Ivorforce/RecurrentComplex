/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvStreams;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 15.09.16.
 */
public class Transforms
{
    public static Stream<AxisAlignedTransform2D> transformStream(IntPredicate rotate, IntPredicate mirror)
    {
        return IvStreams.flatMapToObj(rotationStream(rotate), r -> mirrorStream(mirror).mapToObj(m -> AxisAlignedTransform2D.from(r, m != 0)));
    }

    public static Stream<AxisAlignedTransform2D> transformStream(boolean rotate, boolean mirror)
    {
        return IvStreams.flatMapToObj(rotationStream(rotate), r -> mirrorStream(mirror).mapToObj(m -> AxisAlignedTransform2D.from(r, m != 0)));
    }

    @Nonnull
    protected static IntStream mirrorStream(boolean mirror)
    {
        return IntStream.of(mirror ? new int[]{0, 1} : new int[1]);
    }

    @Nonnull
    protected static IntStream rotationStream(boolean rotate)
    {
        return IntStream.of(rotations(rotate));
    }

    @Nonnull
    protected static IntStream mirrorStream(IntPredicate predicate)
    {
        return IntStream.of(new int[]{0, 1}).filter(predicate);
    }

    @Nonnull
    protected static IntStream rotationStream(IntPredicate predicate)
    {
        return IntStream.of(rotations()).filter(predicate);
    }

    protected static int[] rotations(boolean rotate)
    {
        return rotate ? rotations() : new int[1];
    }

    public static int[] rotations()
    {
        return new int[]{0, 1, 2, 3};
    }

    public static boolean[] mirrors(boolean mirror)
    {
        return mirror ? mirrors() : new boolean[1];
    }

    public static boolean[] mirrors()
    {
        return new boolean[]{true, false};
    }
}
