/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry
{
    private static Map<String, Component> allComponents = new HashMap<>();
    private static Map<String, String> componentDomains = Maps.newHashMap();

    private static Set<String> persistentlyDisabledComponents = new HashSet<>();
    private static Set<String> generatingComponents = new HashSet<>();

    private static Set<String> registeredGroups = new HashSet<>();

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Component.class, new Component.Serializer());
        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public static boolean register(Component component, String key, String domain, boolean generates)
    {
        if (component.areDependenciesResolved())
        {
            if (!generates)
                persistentlyDisabledComponents.add(key);
            else
                persistentlyDisabledComponents.remove(key);

            String baseString = allComponents.containsKey(key) ? "Replaced inventory generation component '%s'" : "Registered generation component '%s'";
            RecurrentComplex.logger.info(String.format(baseString, key));

            allComponents.put(key, component);
            componentDomains.put(key, domain);

            clearCaches();

            return true;
        }

        return false;
    }

    public static Component component(String key)
    {
        return allComponents.get(key);
    }

    public static Set<String> allComponentKeys()
    {
        return Collections.unmodifiableSet(allComponents.keySet());
    }

    public static void removeGenerator(String key)
    {
        Component component = allComponents.remove(key);

//        if (component != null)
//        {
//            WeightedItemCollection collection = WeightedItemCollectionRegistry.itemCollection(component.inventoryGeneratorID);
//
//            if (collection instanceof GenericItemCollection)
//                ((GenericItemCollection) collection).components.remove(component);
//        }

        clearCaches();
    }

    public static boolean register(ResourceLocation resourceLocation, String key, boolean generates)
    {
        Component component = CustomGenericItemCollectionHandler.readInventoryGenerator(resourceLocation);
        return component != null && register(component, key, resourceLocation.getResourceDomain(), generates);
    }

    public static String createJSONFromComponent(Component inventoryGenerator)
    {
        return gson.toJson(inventoryGenerator, Component.class);
    }

    public static Component createComponentFromJSON(String json) throws InventoryLoadException
    {
        try
        {
            return gson.fromJson(json, Component.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new InventoryLoadException(e);
        }
    }

    private static void clearCaches()
    {
        generatingComponents.clear();

        for (Map.Entry<String, Component> entry : allComponents.entrySet())
        {
            Component component = entry.getValue();
            String key = entry.getKey();

            if (!persistentlyDisabledComponents.contains(key)
//                    && RCConfig.shouldStructureGenerate(key, componentDomains.get(key))
                    && component.areDependenciesResolved())
                generatingComponents.add(key);
        }


        for (String s : registeredGroups)
            WeightedItemCollectionRegistry.unregister(s);

        for (String key : generatingComponents)
        {
            Component component = allComponents.get(key);

            WeightedItemCollection collection = WeightedItemCollectionRegistry.itemCollection(component.inventoryGeneratorID);
            if (collection == null)
            {
                GenericItemCollection itemCollection = new GenericItemCollection();
                itemCollection.components.add(component);
                WeightedItemCollectionRegistry.register(itemCollection, component.inventoryGeneratorID);

                registeredGroups.add(component.inventoryGeneratorID);
            }
            else if (collection instanceof GenericItemCollection)
                ((GenericItemCollection) collection).components.add(component);
            else
                RecurrentComplex.logger.info("Failed creating inventory generator with ID '" + component.inventoryGeneratorID + "'");
        }
    }
}
