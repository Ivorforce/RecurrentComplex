/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

/**
 * Created by lukas on 12.03.15.
 */
public class RCBoolAlgebra
{
    private static Algebra<Boolean> algebra;

    public static Algebra<Boolean> algebra()
    {
        return algebra != null ? algebra : (algebra = new Algebra<>(
                BoolAlgebra.closure(),
                BoolAlgebra.or(), BoolAlgebra.and(),
                BoolAlgebra.not()
        ));
    }
}
