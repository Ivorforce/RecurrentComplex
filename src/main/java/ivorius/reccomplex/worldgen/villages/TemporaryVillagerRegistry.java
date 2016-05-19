/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.villages;

import com.google.common.collect.Sets;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lukas on 27.03.15.
 */
public class TemporaryVillagerRegistry
{
    protected static Field handlerField;
    protected static TemporaryVillagerRegistry INSTANCE = new TemporaryVillagerRegistry();

    protected Set<VillagerRegistry.IVillageCreationHandler> registeredHandlers = new HashSet<>();

    public static TemporaryVillagerRegistry instance()
    {
        return INSTANCE;
    }

    protected static Map<Class<?>, VillagerRegistry.IVillageCreationHandler> getMap()
    {
        if (handlerField == null)
            handlerField = ReflectionHelper.findField(VillagerRegistry.class, "villageCreationHandlers");

        try
        {
            return (Map<Class<?>, VillagerRegistry.IVillageCreationHandler>) handlerField.get(VillagerRegistry.instance());
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public void register(VillagerRegistry.IVillageCreationHandler handler)
    {
        addToRegistry(handler);
        registeredHandlers.add(handler);
    }

    private void addToRegistry(VillagerRegistry.IVillageCreationHandler handler)
    {
        VillagerRegistry.instance().registerVillageCreationHandler(handler);
    }

    public void unregister(VillagerRegistry.IVillageCreationHandler handler)
    {
        removeFromRegistry(handler);
        registeredHandlers.remove(handler);
    }

    private void removeFromRegistry(VillagerRegistry.IVillageCreationHandler handler)
    {
        Map<Class<?>, VillagerRegistry.IVillageCreationHandler> map = getMap();
        if (map != null)
            map.remove(handler.getComponentClass());
    }

    public void setHandlers(Set<VillagerRegistry.IVillageCreationHandler> handlers)
    {
        Sets.difference(registeredHandlers, handlers).forEach(this::removeFromRegistry);

        Sets.difference(handlers, registeredHandlers).forEach(this::addToRegistry);

        registeredHandlers.clear();
        registeredHandlers.addAll(handlers);
    }
}
