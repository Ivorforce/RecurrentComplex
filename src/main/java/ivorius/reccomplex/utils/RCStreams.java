/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by lukas on 18.09.16.
 */
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
}
