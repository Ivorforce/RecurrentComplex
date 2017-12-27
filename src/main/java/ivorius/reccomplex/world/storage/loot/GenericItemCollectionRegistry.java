/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import com.google.common.collect.Lists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import ivorius.reccomplex.files.SimpleLeveledRegistry;

import java.util.Collection;

/**
 * Created by lukas on 05.01.15.
 */
public class GenericItemCollectionRegistry extends SimpleLeveledRegistry<GenericLootTable.Component>
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
    public GenericLootTable.Component register(String id, String domain, GenericLootTable.Component component, boolean active, ILevel level)
    {
        if (component.tableID == null || component.tableID.length() == 0) // Legacy support
            component.tableID = id;

        if (active && !RCConfig.shouldLootGenerate(id, domain))
            active = false;

        GenericLootTable.Component prev = super.register(id, domain, component, active, level);

        invalidateCache(component.tableID);

        return prev;
    }

    @Override
    public GenericLootTable.Component unregister(String id, ILevel level)
    {
        GenericLootTable.Component rt = super.unregister(id, level);

        invalidateCache(rt.tableID);

        return rt;
    }

    @Override
    public void clear(ILevel level)
    {
        Collection<GenericLootTable.Component> removed = Lists.newArrayList(map(level).values());

        super.clear(level);

        for (GenericLootTable.Component component : removed)
            invalidateCache(component.tableID);
    }

    private void invalidateCache(String generatorID)
    {
        WeightedItemCollectionRegistry.INSTANCE.unregister(generatorID, LeveledRegistry.Level.CUSTOM);

        for (String key : activeIDs())
        {
            GenericLootTable.Component component = get(key);

            if (component.tableID.equals(generatorID))
            {
                GenericLootTable collection = registerGetGenericItemCollection(component.tableID, status(key).getDomain());
                collection.components.add(component);
            }
        }
    }

    private GenericLootTable registerGetGenericItemCollection(String key, String domain)
    {
        LootTable collection = WeightedItemCollectionRegistry.INSTANCE.get(key);
        if (collection == null || !(collection instanceof GenericLootTable))
        {
            collection = new GenericLootTable();
            WeightedItemCollectionRegistry.INSTANCE.register(key, domain, collection, true, LeveledRegistry.Level.CUSTOM);
        }
        return (GenericLootTable) collection;
    }
}
