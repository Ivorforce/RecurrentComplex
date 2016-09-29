/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.common.collect.Sets;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.SimpleCustomizableRegistry;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry extends SimpleCustomizableRegistry<Component>
{
    public static final GenericItemCollectionRegistry INSTANCE = new GenericItemCollectionRegistry();

    private static class ComponentData
    {
        public boolean disabled;
        public String domain;

        public ComponentData(boolean disabled, String domain)
        {
            this.disabled = disabled;
            this.domain = domain;
        }
    }

    public GenericItemCollectionRegistry() {super("generic item collection component");}

    @Override
    public Component register(String id, String domain, Component component, boolean active, boolean custom)
    {
        if (component.inventoryGeneratorID == null || component.inventoryGeneratorID.length() == 0) // Legacy support
            component.inventoryGeneratorID = id;

        if (active && !RCConfig.shouldInventoryGeneratorGenerate(id, domain))
            active = false;

        Set<String> generating = activeIDs().stream().collect(Collectors.toSet());

        Component register = super.register(id, domain, component, active, custom);

        clearCaches(generating);

        return register;
    }

    @Override
    public Component unregister(String id, boolean custom)
    {
        Set<String> generating = activeIDs().stream().collect(Collectors.toSet());

        Component rt = super.unregister(id, custom);

        clearCaches(generating);

        return rt;
    }

    private void clearCaches(Set<String> generatingComponents)
    {
        Set<String> newGeneratingComponents = activeIDs();

        for (String key : Sets.difference(newGeneratingComponents, generatingComponents))
        {
            Component component = get(key);

            GenericItemCollection collection = registerGetGenericItemCollection(component.inventoryGeneratorID);
            collection.components.add(component);
        }

        for (String key : Sets.difference(generatingComponents, newGeneratingComponents))
        {
            Component component = get(key);

            WeightedItemCollection collection = WeightedItemCollectionRegistry.itemCollection(component.inventoryGeneratorID);

            if (collection instanceof GenericItemCollection)
            {
                ((GenericItemCollection) collection).components.remove(component);

                if (((GenericItemCollection) collection).components.size() == 0)
                    WeightedItemCollectionRegistry.unregister(component.inventoryGeneratorID);
            }
        }
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
