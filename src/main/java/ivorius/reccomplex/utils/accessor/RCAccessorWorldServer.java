/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.accessor;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraft.world.WorldServer;

import java.lang.reflect.*;

/**
 * Created by lukas on 28.03.15.
 */
public class RCAccessorWorldServer
{
    private static Class<?> worldServerServerBlockEventListClass;
    private static Field blockEventQueueField;
    private static Constructor<?> blockEventListConstructor;

    public static void ensureBlockEventArray(WorldServer worldServer)
    {
        if (worldServerServerBlockEventListClass == null)
        {
            try
            {
                worldServerServerBlockEventListClass = Class.forName("net.minecraft.world.WorldServer$ServerBlockEventList");
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        if (blockEventQueueField == null)
            blockEventQueueField = ReflectionHelper.findField(WorldServer.class, "field_147490_S", "blockEventQueue");
        if (blockEventListConstructor == null)
        {
            blockEventListConstructor = worldServerServerBlockEventListClass.getDeclaredConstructors()[0];
            blockEventListConstructor.setAccessible(true);
        }

        try
        {
            if (blockEventQueueField.get(worldServer) == null)
            {
                Object instance = Array.newInstance(worldServerServerBlockEventListClass, 2);
                Array.set(instance, 0, blockEventListConstructor.newInstance());
                Array.set(instance, 1, blockEventListConstructor.newInstance());
                blockEventQueueField.set(worldServer, instance);
            }
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
}
