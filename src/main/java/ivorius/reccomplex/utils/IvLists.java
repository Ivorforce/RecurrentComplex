/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lukas on 07.09.16.
 */
public class IvLists
{
    @SafeVarargs
    public static <S, D> List<D> enumerate(S obj, Function<S, D>... functions)
    {
        return Arrays.stream(functions).map(func -> func.apply(obj)).collect(Collectors.toList());
    }
}
