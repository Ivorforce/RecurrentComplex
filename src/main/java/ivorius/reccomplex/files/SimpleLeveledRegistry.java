/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.loading.LeveledRegistry;
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
    private LeveledBiMap<String, Status> stati = new LeveledBiMap<>(items.levels());

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

    public LeveledBiMap<String, Status> stati()
    {
        return stati;
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

    @Override
    @Nullable
    public S get(String id)
    {
        return items.getMap().get(id);
    }

    @Override
    public Status status(String id)
    {
        return stati.getMap().get(id);
    }

    @Nonnull
    public Set<String> activeIDs()
    {
        ensureActiveCache();
        return Collections.unmodifiableSet(activeMap.keySet());
    }

    @Nonnull
    @Override
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

        stati.put(id, new Status(id, active, domain, level), level.getLevel());
        S old = items.put(id, s, level.getLevel());

        RecurrentComplex.logger.trace(String.format(old != null ? "Replaced %s '%s' at level %s" : "Registered %s '%s' at level %s", description, id, level));

        return old;
    }

    @Override
    public S unregister(String id, ILevel level)
    {
        invalidateActiveCache();
        stati.remove(id, level.getLevel());
        return items.remove(id, level.getLevel());
    }

    @Override
    public void clear(ILevel level)
    {
        RecurrentComplex.logger.trace(String.format("Cleared all %s at level %s", description, level));
        invalidateActiveCache();
        items.clear(level.getLevel());
        stati.clear(level.getLevel());
    }

    private void ensureActiveCache()
    {
        if (!activeCacheValid)
        {
            activeMap = stati.getMap().values().stream()
                    .filter(LeveledRegistry.Status::isActive)
                    .map(LeveledRegistry.Status::getId).collect(Collectors.toMap(s -> s, s -> items.getMap().get(s)));
            activeCacheValid = true;
        }
    }

    private void invalidateActiveCache()
    {
        activeCacheValid = false;
    }

    public class Status implements LeveledRegistry.Status
    {
        protected String id;
        protected boolean active;
        protected String domain;
        protected ILevel level;

        public Status(String id, boolean active, String domain, ILevel level)
        {
            this.id = id;
            this.active = active;
            this.domain = domain;
            this.level = level;
        }

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public boolean isActive()
        {
            return active;
        }

        @Override
        public String getDomain()
        {
            return domain;
        }

        @Override
        public ILevel getLevel()
        {
            return level;
        }

        @Override
        public void setActive(boolean active)
        {
            this.active = active;
            invalidateActiveCache();
        }
    }
}
