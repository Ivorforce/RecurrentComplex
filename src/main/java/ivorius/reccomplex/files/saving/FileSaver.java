/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.saving;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileHandler;
import ivorius.reccomplex.files.RCFiles;
import ivorius.reccomplex.files.loading.FileLoaderAdapter;
import ivorius.reccomplex.files.loading.LeveledRegistry;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by lukas on 02.10.16.
 */
public class FileSaver extends FileHandler
{
    private final Map<String, FileSaverAdapter> adapters = new HashMap<>();

    @Nullable
    public FileSaverAdapter get(String id)
    {
        return adapters.get(id);
    }

    @Nonnull
    public FileSaverAdapter forceGet(String id)
    {
        FileSaverAdapter adapter = get(id);
        if (adapter == null) throw new NoSuchElementException();
        return adapter;
    }

    public FileSaverAdapter register(FileSaverAdapter adapter)
    {
        if (adapters.containsKey(adapter.getId()))
            throw new IllegalArgumentException();

        return adapters.put(adapter.getId(), adapter);
    }

    public void unregister(FileLoaderAdapter handler)
    {
        adapters.remove(handler.getSuffix());
    }

    public Set<String> keySet()
    {
        return adapters.keySet();
    }

    public boolean has(String id)
    {
        return adapters.containsKey(id);
    }

    @Nonnull
    public String suffix(String adapter)
    {
        return forceGet(adapter).getSuffix();
    }

    @Nonnull
    public LeveledRegistry registry(String adapter)
    {
        return forceGet(adapter).getRegistry();
    }

    // --------------- Saving

    public boolean trySave(Path path, String adapter, String name)
    {
        try
        {
            save(path, adapter, name);
            return true;
        }
        catch (NoSuchElementException e)
        {
            RecurrentComplex.logger.error(String.format("No entry found: %s in %s", name, RCFiles.extension(path)), e);
        }
        catch (IllegalArgumentException e)
        {
            RecurrentComplex.logger.error(String.format("No handler found: %s", path.getFileName()), e);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error(String.format("Error writing file: %s", path), e);
        }

        return false;
    }

    public void save(Path directory, String adapter, String name) throws Exception
    {
        FileSaverAdapter adapterObj = forceGet(adapter);

        Path path = directory.resolve(name + "." + adapterObj.getSuffix());

        Files.deleteIfExists(path);
        adapterObj.saveFile(path, name);
    }

    public Pair<Set<Path>, Set<Path>> tryDeleteWithID(Path path, String adapter, String name)
    {
        return tryDelete(path, name, suffix(adapter));
    }
}
