/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by lukas on 18.09.16.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class RCStreams
{
    public static <T> boolean visit(Stream<T> source, Predicate<T> action)
    {
        for (T item : (Iterable<T>) source::iterator)
        {
            if (!action.test(item))
                return false;
        }

        return true;
    }

    public static <T> Stream<T> streamopt(Optional<T> opt) {
        if (opt.isPresent())
            return Stream.of(opt.get());
        else
            return Stream.empty();
    }
}
