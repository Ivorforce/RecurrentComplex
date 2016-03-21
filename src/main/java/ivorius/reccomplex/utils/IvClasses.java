/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.reccomplex.RecurrentComplex;

/**
 * Created by lukas on 21.03.16.
 */
public class IvClasses
{
    public static <T> T instantiate(Class<T> clazz)
    {
        T t = null;

        try
        {
            t = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(e);
        }

        return t;
    }
}
