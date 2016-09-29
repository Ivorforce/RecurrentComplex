/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.LeveledBiMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 29.09.16.
 */
public class SimpleLeveledRegistry<S> implements LeveledRegistry<S>
{
    private LeveledBiMap<String, S> items = new LeveledBiMap<>(LeveledRegistry.Level.values().length);
    private LeveledBiMap<String, Data> datas = new LeveledBiMap<>(items.levels());

    private boolean activeCacheValid = false;
    private Map<String, S> activeMap = new HashMap<>();

    public String description;

    public SimpleLeveledRegistry(String description)
    {
        this.description = description;
    }

    public LeveledBiMap<String, S> contents()
    {
        return items;
    }

    public LeveledBiMap<String, Data> datas()
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
    public S register(String id, String domain, S s, boolean active, ILevel level)
    {
        invalidateActiveCache();

        datas.put(id, new Data(id, active, domain), level.getLevel());
        S old = items.put(id, s, level.getLevel());

        RecurrentComplex.logger.trace(String.format(old != null ? "Replaced %s '%s' at level %s" : "Registered %s '%s' at level %s", description, id, level));

        return old;
    }

    @Override
    public S unregister(String id, ILevel level)
    {
        invalidateActiveCache();
        datas.remove(id, level.getLevel());
        return items.remove(id, level.getLevel());
    }

    @Override
    public void clear(ILevel level)
    {
        RecurrentComplex.logger.trace(String.format("Cleared all %s at level %s", description, level));
        invalidateActiveCache();
        items.clear(level.getLevel());
        datas.clear(level.getLevel());
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
