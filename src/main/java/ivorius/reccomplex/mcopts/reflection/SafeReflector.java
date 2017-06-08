/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.mcopts.reflection;

import ivorius.reccomplex.mcopts.MCOpts;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created by lukas on 08.06.17.
 */
public class SafeReflector
{
    public static <T> T get(Class<?> clazz, String name, Object object)
    {
        try
        {
            //noinspection unchecked
            return (T) ReflectionHelper.findField(clazz, name).get(object);
        }
        catch (Exception e)
        {
            MCOpts.logger.error(e);
        }

        throw new RuntimeException("Unable to resolve: " + name);
    }

    public static <T> T get(Class<?> clazz, String name, Object object, T fallback)
    {
        try
        {
            //noinspection unchecked
            return (T) ReflectionHelper.findField(clazz, name).get(object);
        }
        catch (Exception e)
        {
            MCOpts.logger.error(e);
        }

        return fallback;
    }

    public static boolean of(Class<?> clazz, String name, FieldTask task)
    {
        try
        {
            task.execute(ReflectionHelper.findField(clazz, name));
            return true;
        }
        catch (Exception e)
        {
            MCOpts.logger.error(e);
        }

        return false;
    }

    public interface FieldTask
    {
        void execute(Field field) throws Exception;
    }
}
