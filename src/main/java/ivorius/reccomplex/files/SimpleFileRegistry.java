/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.CustomizableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 29.09.16.
 */
public class SimpleFileRegistry<S> implements FileRegistry<S>
{
    private CustomizableMap<String, S> items = new CustomizableMap<>();
    private CustomizableMap<String, Data> datas = new CustomizableMap<>();

    private boolean generatesCacheValid = false;
    private Set<String> generatesCache = new HashSet<>();

    public String description;

    public SimpleFileRegistry(String description)
    {
        this.description = description;
    }

    public Collection<S> all()
    {
        return items.getMap().values();
    }

    public Collection<S> allActive()
    {
        ensureGeneratesCache();
        return generatesCache.stream().map(items.getMap()::get).collect(Collectors.toList());
    }

    @Nullable
    public S getActive(String id)
    {
        ensureGeneratesCache();
        return generatesCache.contains(id) ? items.getMap().get(id) : null;
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
        return generatesCache;
    }

    @Nonnull
    public Set<String> ids()
    {
        return items.getMap().keySet();
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
        items.clearCustom();
    }

    private void ensureGeneratesCache()
    {
        if (!generatesCacheValid)
        {
            generatesCache = datas.getMap().values().stream()
                    .filter(d -> d.active)
                    .map(d -> d.id).collect(Collectors.toSet());
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
