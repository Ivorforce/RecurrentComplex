/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import java.io.ByteArrayOutputStream;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 21.03.16.
 */
public class IvStreams
{
    public static <T> Stream<T> flatMapToObj(IntStream stream, IntFunction<Stream<? extends T>> function)
    {
        return stream.mapToObj(function).flatMap(Function.identity());
    }

    public static <T> Stream<T> flatMapToObj(DoubleStream stream, DoubleFunction<Stream<? extends T>> function)
    {
        return stream.mapToObj(function).flatMap(Function.identity());
    }

    public static <T> Stream<T> flatMapToObj(LongStream stream, LongFunction<Stream<? extends T>> function)
    {
        return stream.mapToObj(function).flatMap(Function.identity());
    }

    public static byte[] toByteArray(IntStream stream) {
        return stream.collect(ByteArrayOutputStream::new, (baos, i) -> baos.write((byte) i),
                (baos1, baos2) -> baos1.write(baos2.toByteArray(), 0, baos2.size()))
                .toByteArray();
    }
}
