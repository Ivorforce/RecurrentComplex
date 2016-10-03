/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.common.collect.Sets;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry extends SimpleLeveledRegistry<Component>
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
    public Component register(String id, String domain, Component component, boolean active, ILevel level)
    {
        if (component.inventoryGeneratorID == null || component.inventoryGeneratorID.length() == 0) // Legacy support
            component.inventoryGeneratorID = id;

        if (active && !RCConfig.shouldInventoryGeneratorGenerate(id, domain))
            active = false;

        Set<String> generating = activeIDs().stream().collect(Collectors.toSet());

        Component register = super.register(id, domain, component, active, level);

        clearCaches(generating);

        return register;
    }

    @Override
    public Component unregister(String id, ILevel level)
    {
        Set<String> generating = activeIDs().stream().collect(Collectors.toSet());

        Component rt = super.unregister(id, level);

        clearCaches(generating);

        return rt;
    }

    private void clearCaches(Set<String> generatingComponents)
    {
        Set<String> newGeneratingComponents = activeIDs();

        for (String key : Sets.difference(newGeneratingComponents, generatingComponents))
        {
            Component component = get(key);

            GenericItemCollection collection = registerGetGenericItemCollection(component.inventoryGeneratorID, status(key).getDomain());
            collection.components.add(component);
        }

        for (String key : Sets.difference(generatingComponents, newGeneratingComponents))
        {
            Component component = get(key);

            WeightedItemCollection collection = WeightedItemCollectionRegistry.INSTANCE.get(component.inventoryGeneratorID);

            if (collection instanceof GenericItemCollection)
            {
                ((GenericItemCollection) collection).components.remove(component);

                if (((GenericItemCollection) collection).components.size() == 0)
                    WeightedItemCollectionRegistry.INSTANCE.unregister(component.inventoryGeneratorID, LeveledRegistry.Level.CUSTOM);
            }
        }
    }

    private GenericItemCollection registerGetGenericItemCollection(String key, String domain)
    {
        WeightedItemCollection collection = WeightedItemCollectionRegistry.INSTANCE.get(key);
        if (collection == null || !(collection instanceof GenericItemCollection))
        {
            collection = new GenericItemCollection();
            WeightedItemCollectionRegistry.INSTANCE.register(key, domain, collection, true, LeveledRegistry.Level.CUSTOM);
        }
        return (GenericItemCollection) collection;
    }
}
