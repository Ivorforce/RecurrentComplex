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

    private boolean generatesCacheValid = false;
    private Map<String, S> generatingMap = new HashMap<>();

    public String description;

    public SimpleCustomizableRegistry(String description)
    {
        this.description = description;
    }

    public Map<String, S> map()
    {
        return Collections.unmodifiableMap(items.getMap());
    }

    public Map<String, S> activeMap()
    {
        return Collections.unmodifiableMap(generatingMap);
    }

    public Collection<S> all()
    {
        return Collections.unmodifiableCollection(items.getMap().values());
    }

    public Collection<S> allActive()
    {
        ensureGeneratesCache();
        return generatingMap.values();
    }

    @Nullable
    public S getActive(String id)
    {
        ensureGeneratesCache();
        return generatingMap.get(id);
    }

    @Nullable
    public S get(String id)
    {
        return items.getMap().get(id);
    }

    @Nonnull
    public Set<String> activeIDs()
    {
        ensureGeneratesCache();
        return generatingMap.keySet();
    }

    @Nonnull
    public Set<String> ids()
    {
        return items.getMap().keySet();
    }

    public boolean has(String id)
    {
        return items.getMap().containsKey(id);
    }

    public boolean hasActive(String id)
    {
        ensureGeneratesCache();
        return generatingMap.containsKey(id);
    }

    public String id(S s)
    {
        return items.getMap().inverse().get(s);
    }

    @Override
    public S register(String id, String domain, S s, boolean active, boolean custom)
    {
        invalidateGeneratesCache();

        datas.put(id, new Data(id, active, domain), custom);
        S old = items.put(id, s, custom);

        RecurrentComplex.logger.info(String.format(old != null ? "Replaced %s '%s'" : "Registered %s '%s'", description, id));

        return old;
    }

    @Override
    public S unregister(String id, boolean custom)
    {
        invalidateGeneratesCache();
        datas.remove(id, custom);
        return items.remove(id, custom);
    }

    @Override
    public void clearCustomFiles()
    {
        invalidateGeneratesCache();
        items.clearCustom();
    }

    private void ensureGeneratesCache()
    {
        if (!generatesCacheValid)
        {
            generatingMap = datas.getMap().values().stream()
                    .filter(d -> d.active)
                    .map(d -> d.id).collect(Collectors.toMap(s -> s, s -> items.getMap().get(s)));
            generatesCacheValid = true;
        }
    }

    private void invalidateGeneratesCache()
    {
        generatesCacheValid = false;
    }

    private class Data
    {
        String id;
        boolean active;
        String domain;

        public Data(String id, boolean active, String domain)
        {
            this.id = id;
            this.active = active;
            this.domain = domain;
        }
    }
}
