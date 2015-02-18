/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.Entity;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Created by lukas on 31.01.15.
 */
public class RCAccessorEntity
{
    private static Field uniqueID;

    private static void initializeUniqueID()
    {
        if (uniqueID == null)
            uniqueID = ReflectionHelper.findField(Entity.class, "field_96093_i", "entityUniqueID");
    }

    public static void setEntityUniqueID(Entity entity, UUID uuid)
    {
        initializeUniqueID();

        try
        {
            uniqueID.set(entity, uuid);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public static UUID getEntityUniqueID(Entity entity)
    {
        initializeUniqueID();

        try
        {
            return (UUID) uniqueID.get(entity);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
