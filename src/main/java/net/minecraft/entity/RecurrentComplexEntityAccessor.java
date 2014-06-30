package net.minecraft.entity;

import java.util.UUID;

/**
 * Created by lukas on 05.06.14.
 */
public class RecurrentComplexEntityAccessor
{
    public static void resetEntityUUID(Entity entity)
    {
        entity.entityUniqueID = UUID.randomUUID();
    }
}
