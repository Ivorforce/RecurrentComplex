/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.accessor;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created by lukas on 13.12.16.
 */
public class SafeReflector
{
    public static <T, O> T get(Class<O> clazz, O object, String... names)
    {
        try
        {
            //noinspection unchecked
            return (T) ReflectionHelper.findField(clazz, names).get(object);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error(e);
        }

        throw new RuntimeException("Unable to resolve: " + names);
    }

    public static <T, O> T get(Class<O> clazz, O object, T fallback, String... names)
    {
        try
        {
            //noinspection unchecked
            return (T) ReflectionHelper.findField(clazz, names).get(object);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error(e);
        }

        return fallback;
    }

    public static boolean of(Class<?> clazz, FieldTask task, String... names)
    {
        try
        {
            task.execute(ReflectionHelper.findField(clazz, names));
            return true;
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error(e);
        }

        return false;
    }

    public interface FieldTask
    {
        void execute(Field field) throws Exception;
    }
}
