/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import java.util.stream.Stream;

/**
 * Created by lukas on 19.09.16.
 */
public class LineSelections
{
    public static LineSelection combine(Stream<LineSelection> selections, boolean additive)
    {
        LineSelection selection = new LineSelection(!additive);
        selections.forEach(s -> selection.set(s, additive, additive));
        return selection;
    }
}
