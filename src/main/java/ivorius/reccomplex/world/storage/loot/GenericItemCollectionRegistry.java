/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.SimpleLeveledRegistry;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry extends SimpleLeveledRegistry<GenericItemCollection.Component>
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
    public GenericItemCollection.Component register(String id, String domain, GenericItemCollection.Component component, boolean active, ILevel level)
    {
        if (component.inventoryGeneratorID == null || component.inventoryGeneratorID.length() == 0) // Legacy support
            component.inventoryGeneratorID = id;

        if (active && !RCConfig.shouldInventoryGeneratorGenerate(id, domain))
            active = false;

        GenericItemCollection.Component prev = super.register(id, domain, component, active, level);

        invalidateCache(component.inventoryGeneratorID);

        return prev;
    }

    @Override
    public GenericItemCollection.Component unregister(String id, ILevel level)
    {
        GenericItemCollection.Component rt = super.unregister(id, level);

        invalidateCache(rt.inventoryGeneratorID);

        return rt;
    }

    @Override
    public void clear(ILevel level)
    {
        Collection<GenericItemCollection.Component> removed = Lists.newArrayList(map(level).values());

        super.clear(level);

        for (GenericItemCollection.Component component : removed)
            invalidateCache(component.inventoryGeneratorID);
    }

    private void invalidateCache(String generatorID)
    {
        WeightedItemCollectionRegistry.INSTANCE.unregister(generatorID, LeveledRegistry.Level.CUSTOM);

        for (String key : activeIDs())
        {
            GenericItemCollection.Component component = get(key);

            if (component.inventoryGeneratorID.equals(generatorID))
            {
                GenericItemCollection collection = registerGetGenericItemCollection(component.inventoryGeneratorID, status(key).getDomain());
                collection.components.add(component);
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
