/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileHandler;
import ivorius.reccomplex.files.RCFiles;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by lukas on 18.09.15.
 */
public class FileLoader extends FileHandler
{
    private final Map<String, FileLoaderAdapter> adapters = new HashMap<>();

    protected static String defaultName(Path path, String customID)
    {
        return customID != null ? customID : FilenameUtils.getBaseName(path.getFileName().toString());
    }

    public FileLoaderAdapter get(String suffix)
    {
        return adapters.get(suffix);
    }

    public FileLoaderAdapter register(FileLoaderAdapter handler)
    {
        if (adapters.containsKey(handler.getSuffix()))
            throw new IllegalArgumentException();

        return adapters.put(handler.getSuffix(), handler);
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

    // --------------- Clearing

    public void clearFiles(LeveledRegistry.Level level)
    {
        adapters.values().forEach(h -> h.clearFiles(level));
    }

    public void clearFiles(Collection<String> suffices, LeveledRegistry.Level level)
    {
        adapters.entrySet().stream().filter(entry -> suffices.contains(entry.getKey())).forEach(entry -> entry.getValue().clearFiles(level));
    }

    // --------------- Loading

    @ParametersAreNonnullByDefault
    public int tryLoadAll(ResourceLocation resourceLocation, FileLoadContext context)
    {
        return tryLoadAll(resourceLocation, context, keySet());
    }

    @ParametersAreNonnullByDefault
    public int tryLoadAll(ResourceLocation resourceLocation, FileLoadContext context, Collection<String> suffices)
    {
        Path path = RCFiles.tryPathFromResourceLocation(resourceLocation);
        return path != null ? tryLoadAll(path, context, suffices) : 0;
    }

    @ParametersAreNonnullByDefault
    public int tryLoadAll(Path path, FileLoadContext context)
    {
        return tryLoadAll(path, context, keySet());
    }

    @ParametersAreNonnullByDefault
    public int tryLoadAll(Path path, FileLoadContext context, Collection<String> suffices)
    {
        int added = 0;

        try
        {
            List<Path> paths = RCFiles.listFilesRecursively(path, new FileSuffixFilter(suffices), true);

            for (Path file : paths)
            {
                if (tryLoad(file, null, context))
                    added++;
            }

            return added;
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.error("Error loading resources from directory", e);
        }

        return added;
    }

    @ParametersAreNonnullByDefault
    public boolean tryLoad(ResourceLocation resourceLocation, @Nullable String customID, FileLoadContext context)
    {
        Path path = null;

        try
        {
            path = RCFiles.pathFromResourceLocation(resourceLocation);
        }
        catch (URISyntaxException | IOException e)
        {
            RecurrentComplex.logger.error("Error finding path from resource location '" + resourceLocation + "'", e);
        }

        if (path != null)
        {
            try
            {
                return load(path, customID, context);
            }
            catch (UnsupportedOperationException e)
            {
                RecurrentComplex.logger.error(String.format("Reading unsupported: ?.%s", RCFiles.extension(path)), e);
            }
            catch (Throwable e)
            {
                RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
            }
        }

        return false;
    }

    @ParametersAreNonnullByDefault
    public boolean tryLoad(Path path, @Nullable String customID, FileLoadContext context)
    {
        try
        {
            return load(path, customID, context);
        }
        catch (UnsupportedOperationException e)
        {
            RecurrentComplex.logger.error(String.format("Reading unsupported: ?.%s", RCFiles.extension(path)), e);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error loading resource: " + path, e);
        }

        return false;
    }

    @ParametersAreNonnullByDefault
    public boolean load(Path path, @Nullable String customID, FileLoadContext context) throws Exception
    {
        FileLoaderAdapter handler = get(RCFiles.extension(path));
        String id = defaultName(path, customID);

        return handler != null && handler.loadFile(path, id, context);
    }
}
