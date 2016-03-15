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
import ivorius.reccomplex.utils.CustomizableBiMap;
import ivorius.reccomplex.utils.CustomizableMap;
import ivorius.reccomplex.utils.CustomizableSet;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry
{
    public static final GenericItemCollectionRegistry INSTANCE = new GenericItemCollectionRegistry();

    private CustomizableMap<String, Component> allComponents = new CustomizableMap<>();
    private Map<String, String> componentDomains = Maps.newHashMap();

    private CustomizableSet<String> persistentlyDisabledComponents = new CustomizableSet<>();
    private Set<String> generatingComponents = new HashSet<>();

    private Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Component.class, new Component.Serializer());
        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public boolean register(Component component, String key, String domain, boolean generates, boolean custom)
    {
        if (RCConfig.shouldInventoryGeneratorLoad(key, domain))
        {
            persistentlyDisabledComponents.setContains(!generates, custom, key);

            String baseString = allComponents.put(key, component, custom) != null ? "Replaced inventory generation component '%s'" : "Registered generation component '%s'";
            RecurrentComplex.logger.info(String.format(baseString, key));

            componentDomains.put(key, domain);

            clearCaches();

            return true;
        }

        return false;
    }

    public Component component(String key)
    {
        return allComponents.getMap().get(key);
    }

    public Set<String> allComponentKeys()
    {
        return Collections.unmodifiableSet(allComponents.getMap().keySet());
    }

    public void unregister(String key, boolean custom)
    {
        persistentlyDisabledComponents.get(custom).remove(key);  // Clean up space
        allComponents.remove(key, custom);
        clearCaches();
    }

    public void clearCustom()
    {
        persistentlyDisabledComponents.custom.clear();
        allComponents.clearCustom();
    }

    public boolean isLoaded(String key)
    {
        return allComponents.getMap().containsKey(key);
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

        for (Map.Entry<String, Component> entry : allComponents.getMap().entrySet())
        {
            Component component = entry.getValue();
            String key = entry.getKey();

            if (!persistentlyDisabledComponents.get(allComponents.hasCustom(key)).contains(key)
                    && RCConfig.shouldInventoryGeneratorGenerate(key, componentDomains.get(key))
                    && component.areDependenciesResolved())
                newGeneratingComponents.add(key);
        }

        for (String key : Sets.difference(newGeneratingComponents, generatingComponents))
        {
            Component component = allComponents.getMap().get(key);

            GenericItemCollection collection = registerGetGenericItemCollection(component.inventoryGeneratorID);
            collection.components.add(component);
        }

        for (String key : Sets.difference(generatingComponents, newGeneratingComponents))
        {
            Component component = allComponents.getMap().get(key);

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
