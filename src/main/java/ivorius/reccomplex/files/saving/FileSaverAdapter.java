/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.saving;

import ivorius.reccomplex.files.loading.LeveledRegistry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.NoSuchElementException;

/**
 * Created by lukas on 02.10.16.
 */
public abstract class FileSaverAdapter<S>
{
    protected String id;
    protected String suffix;
    protected LeveledRegistry<S> registry;

    public FileSaverAdapter(String id, String suffix, LeveledRegistry<S> registry)
    {
        this.id = id;
        this.suffix = suffix;
        this.registry = registry;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public LeveledRegistry<S> getRegistry()
    {
        return registry;
    }

    public void setRegistry(LeveledRegistry<S> registry)
    {
        this.registry = registry;
    }

    public void saveFile(Path path, String id) throws Exception
    {
        S s = registry.get(id);
        if (s == null)
            throw new NoSuchElementException();
        saveFile(path, s);
    }

    @ParametersAreNonnullByDefault
    public abstract void saveFile(Path path, S s) throws Exception;
}
