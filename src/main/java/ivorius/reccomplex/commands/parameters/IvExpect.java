/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.reccomplex.dimensions.DimensionDictionary;

/**
 * Created by lukas on 31.05.17.
 */
public class IvExpect<T extends IvExpect<T>> extends MCExpect<T>
{
    IvExpect()
    {

    }

    public static <T extends IvExpect<T>> T expectIv()
    {
        //noinspection unchecked
        return (T) new IvExpect();
    }

    public T xz()
    {
        return x().z();
    }

    public T surfacePos(String x, String z)
    {
        return named(x).x()
                .named(z).z();
    }

    public T dimensionType()
    {
        return next(DimensionDictionary.allRegisteredTypes()).optionalU("dimension type");
    }
}
