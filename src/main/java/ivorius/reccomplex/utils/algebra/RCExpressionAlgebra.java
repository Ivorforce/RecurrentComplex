/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.algebra;

/**
 * Created by lukas on 26.06.16.
 */
public class RCExpressionAlgebra
{
    private static Algebra<Object> algebra;

    public static Algebra<Object> algebra()
    {
        return algebra != null ? algebra : (algebra = new Algebra<>(
        ));
    }
}
