/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.json.ItemStackSerializer;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.worldgen.MCRegistrySpecial;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandomChestContent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry
{
    private static Map<String, Component> componentMap = new HashMap<>();

    private static Gson gson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Component.class, new Component.Serializer());
        builder.registerTypeAdapter(WeightedRandomChestContent.class, new WeightedRandomChestContentSerializer());
        builder.registerTypeAdapter(ItemStack.class, new ItemStackSerializer(MCRegistrySpecial.INSTANCE));

        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public static void register(Component component, String key)
    {
        if (component.areDependenciesResolved())
        {
            RecurrentComplex.logger.info(componentMap.containsKey(key) ? "Overwrote inventory generator with id '" + key + "'" : "Registered inventory generator with id '" + key + "'");
            componentMap.put(key, component);

            WeightedItemCollection collection = WeightedItemCollectionRegistry.itemCollection(component.inventoryGeneratorID);
            if (collection == null)
            {
                GenericItemCollection itemCollection = new GenericItemCollection();
                itemCollection.components.add(component);
                WeightedItemCollectionRegistry.registerInventoryGenerator(itemCollection, component.inventoryGeneratorID);
            }
            else if (collection instanceof GenericItemCollection)
                ((GenericItemCollection) collection).components.add(component);
            else
                RecurrentComplex.logger.info("Failed creating inventory generator with ID '" + component.inventoryGeneratorID + "'");
        }
    }

    public static Component component(String key)
    {
        return componentMap.get(key);
    }

    public static Set<String> allComponentKeys()
    {
        return componentMap.keySet();
    }

    public static void removeGenerator(String key)
    {
        Component component = componentMap.remove(key);

        if (component != null)
        {
            WeightedItemCollection collection = WeightedItemCollectionRegistry.itemCollection(component.inventoryGeneratorID);

            if (collection instanceof GenericItemCollection)
                ((GenericItemCollection) collection).components.remove(component);
        }
    }

    public static boolean register(ResourceLocation resourceLocation, String key)
    {
        Component component = CustomGenericItemCollectionHandler.readInventoryGenerator(resourceLocation);

        if (component != null)
        {
            register(component, key);
            return true;
        }
        else
            return false;
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
}
