/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.optional;

import java.util.Optional;

/**
 * Created by lukas on 31.05.17.
 */
public class IvOptional
{
    public static void ifAbsent(Optional<?> optional, Runnable run)
    {
        if (!optional.isPresent())
            run.run();
    }

    public static <T> Optional<T> replace(Optional<T> optional, Optional<T> replace)
    {
        return replace.isPresent() ? replace : optional;
    }
}
