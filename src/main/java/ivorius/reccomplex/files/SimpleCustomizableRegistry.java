/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.CustomizableBiMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 29.09.16.
 */
public class SimpleCustomizableRegistry<S> implements CustomizableRegistry<S>
{
    private CustomizableBiMap<String, S> items = new CustomizableBiMap<>();
    private CustomizableBiMap<String, Data> datas = new CustomizableBiMap<>();

    private boolean activeCacheValid = false;
    private Map<String, S> activeMap = new HashMap<>();

    public String description;

    public SimpleCustomizableRegistry(String description)
    {
        this.description = description;
    }

    public CustomizableBiMap<String, S> contents()
    {
        return items;
    }

    public CustomizableBiMap<String, Data> datas()
    {
        return datas;
    }

    public Map<String, S> map()
    {
        return Collections.unmodifiableMap(items.getMap());
    }

    public Map<String, S> activeMap()
    {
        return Collections.unmodifiableMap(activeMap);
    }

    public Collection<S> all()
    {
        return Collections.unmodifiableCollection(items.getMap().values());
    }

    public Collection<S> allActive()
    {
        ensureActiveCache();
        return Collections.unmodifiableCollection(activeMap.values());
    }

    @Nullable
    public S getActive(String id)
    {
        ensureActiveCache();
        return activeMap.get(id);
    }

    @Nullable
    public S get(String id)
    {
        return items.getMap().get(id);
    }

    public Data getData(String id)
    {
        return datas.getMap().get(id);
    }

    @Nonnull
    public Set<String> activeIDs()
    {
        ensureActiveCache();
        return Collections.unmodifiableSet(activeMap.keySet());
    }

    @Nonnull
    public Set<String> ids()
    {
        return Collections.unmodifiableSet(items.getMap().keySet());
    }

    public boolean has(String id)
    {
        return items.getMap().containsKey(id);
    }

    public boolean hasActive(String id)
    {
        ensureActiveCache();
        return activeMap.containsKey(id);
    }

    public String id(S s)
    {
        return items.getMap().inverse().get(s);
    }

    @Override
    public S register(String id, String domain, S s, boolean active, boolean custom)
    {
        invalidateActiveCache();

        datas.put(id, new Data(id, active, domain), custom);
        S old = items.put(id, s, custom);

        RecurrentComplex.logger.trace(String.format(old != null ? "Replaced %s '%s'" : "Registered %s '%s'", description, id));

        return old;
    }

    @Override
    public S unregister(String id, boolean custom)
    {
        invalidateActiveCache();
        datas.remove(id, custom);
        return items.remove(id, custom);
    }

    @Override
    public void clearCustom()
    {
        RecurrentComplex.logger.trace(String.format("Cleared all custom: %s", description));
        invalidateActiveCache();
        items.clearCustom();
    }

    private void ensureActiveCache()
    {
        if (!activeCacheValid)
        {
            activeMap = datas.getMap().values().stream()
                    .filter(d -> d.active)
                    .map(d -> d.id).collect(Collectors.toMap(s -> s, s -> items.getMap().get(s)));
            activeCacheValid = true;
        }
    }

    private void invalidateActiveCache()
    {
        activeCacheValid = false;
    }

    public static class Data
    {
        public final String id;
        public final boolean active;
        public final String domain;

        public Data(String id, boolean active, String domain)
        {
            this.id = id;
            this.active = active;
            this.domain = domain;
        }
    }
}
