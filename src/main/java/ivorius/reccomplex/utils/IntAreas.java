/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.tools.Ranges;
import ivorius.ivtoolkit.tools.Visitor;

/**
 * Created by lukas on 16.04.15.
 */
public class IntAreas
{
    public static boolean visitAllCoords(int[] lower, int[] higher, Visitor<int[]> visitor)
    {
        return visitCoords(lower, higher, lower.clone(), new TIntArrayList(Ranges.to(lower.length)), visitor);
    }

    public static boolean visitCoords(int[] lower, int[] higher, TIntList dimensions, Visitor<int[]> visitor)
    {
        return visitCoords(lower, higher, lower.clone(), dimensions, visitor);
    }

    public static boolean visitCoordsExcept(int[] lower, int[] higher, TIntList except, Visitor<int[]> visitor)
    {
        TIntList dimensions = new TIntArrayList(Ranges.to(lower.length));
        dimensions.removeAll(except);
        return visitCoords(lower, higher, lower.clone(), dimensions, visitor);
    }

    public static boolean visitCoords(int[] lower, int[] higher, int[] coord, TIntList dimensions, Visitor<int[]> visitor)
    {
        int dim = dimensions.get(0);

        for (coord[dim] = lower[dim]; coord[dim] <= higher[dim]; coord[dim]++)
        {
            if (dimensions.size() == 1)
            {
                if (!visitor.visit(coord))
                    return false;
            }
            else if (!visitCoords(lower, higher, coord, dimensions.subList(1, dimensions.size()), visitor))
                return false;
        }

        return true;
    }
}
