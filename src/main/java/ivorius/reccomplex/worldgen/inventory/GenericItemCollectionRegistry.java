/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry
{
    public static final GenericItemCollectionRegistry INSTANCE = new GenericItemCollectionRegistry();

    private Map<String, Component> allComponents = new HashMap<>();
    private Map<String, String> componentDomains = Maps.newHashMap();

    private Set<String> persistentlyDisabledComponents = new HashSet<>();
    private Set<String> generatingComponents = new HashSet<>();

    private Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Component.class, new Component.Serializer());
        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public boolean register(Component component, String key, String domain, boolean generates)
    {
        if (RCConfig.shouldInventoryGeneratorLoad(key, domain))
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

    public Component component(String key)
    {
        return allComponents.get(key);
    }

    public Set<String> allComponentKeys()
    {
        return Collections.unmodifiableSet(allComponents.keySet());
    }

    public void removeGenerator(String key)
    {
        allComponents.remove(key);
        clearCaches();
    }

    public boolean register(ResourceLocation resourceLocation, String key, boolean generates)
    {
        Component component = ItemCollectionSaveHandler.readInventoryGenerator(resourceLocation);
        return component != null && register(component, key, resourceLocation.getResourceDomain(), generates);
    }

    public boolean isLoaded(String key)
    {
        return allComponents.containsKey(key);
    }

    public boolean isActive(String key)
    {
        return generatingComponents.contains(key);
    }

    public String createJSONFromComponent(Component inventoryGenerator)
    {
        return gson.toJson(inventoryGenerator, Component.class);
    }

    public Component createComponentFromJSON(String json) throws InventoryLoadException
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

    private void clearCaches()
    {
        Set<String> newGeneratingComponents = new HashSet<>();

        for (Map.Entry<String, Component> entry : allComponents.entrySet())
        {
            Component component = entry.getValue();
            String key = entry.getKey();

            if (!persistentlyDisabledComponents.contains(key)
                    && RCConfig.shouldInventoryGeneratorGenerate(key, componentDomains.get(key))
                    && component.areDependenciesResolved())
                newGeneratingComponents.add(key);
        }

        for (String key : Sets.difference(newGeneratingComponents, generatingComponents))
        {
            Component component = allComponents.get(key);

            GenericItemCollection collection = registerGetGenericItemCollection(component.inventoryGeneratorID);
            collection.components.add(component);
            generatingComponents.add(key);
        }

        for (String key : Sets.difference(generatingComponents, newGeneratingComponents))
        {
            Component component = allComponents.get(key);

            WeightedItemCollection collection = WeightedItemCollectionRegistry.itemCollection(component.inventoryGeneratorID);

            if (collection instanceof GenericItemCollection)
            {
                ((GenericItemCollection) collection).components.remove(component);

                if (((GenericItemCollection) collection).components.size() == 0)
                    WeightedItemCollectionRegistry.unregister(component.inventoryGeneratorID);
            }
        }

        generatingComponents = newGeneratingComponents;
    }

    private GenericItemCollection registerGetGenericItemCollection(String key)
    {
        WeightedItemCollection collection = WeightedItemCollectionRegistry.itemCollection(key);
        if (collection == null || !(collection instanceof GenericItemCollection))
        {
            collection = new GenericItemCollection();
            WeightedItemCollectionRegistry.register(collection, key);
        }
        return (GenericItemCollection) collection;
    }
}
