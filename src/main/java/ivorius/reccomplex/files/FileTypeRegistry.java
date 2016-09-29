/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by lukas on 18.09.15.
 */
public class FileTypeRegistry
{
    private final Map<String, FileTypeHandler> handlers = new HashMap<>();

    public FileTypeHandler get(String suffix)
    {
        return handlers.get(suffix);
    }

    public FileTypeHandler put(String suffix, FileTypeHandler value)
    {
        return handlers.put(suffix, value);
    }

    public Set<String> keySet()
    {
        return handlers.keySet();
    }

    // --------------- Loading

    public void clearFiles(LeveledRegistry.Level level)
    {
        handlers.values().forEach(h -> h.clearFiles(level));
    }

    public void clearFiles(Collection<String> suffices, LeveledRegistry.Level level)
    {
        handlers.entrySet().stream().filter(entry -> suffices.contains(entry.getKey())).forEach(entry -> entry.getValue().clearFiles(level));
    }

    public int tryLoadAll(ResourceLocation resourceLocation, FileLoadContext context)
    {
        return tryLoadAll(resourceLocation, context, keySet());
    }

    public int tryLoadAll(ResourceLocation resourceLocation, FileLoadContext context, Collection<String> suffices)
    {
        try
        {
            Path path = RCFileHelper.pathFromResourceLocation(resourceLocation);
            if (path != null)
                return tryLoadAll(path, context, suffices);
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
        }

        return 0;
    }

    public int tryLoadAll(Path path, FileLoadContext context)
    {
        return tryLoadAll(path, context, keySet());
    }

    public int tryLoadAll(Path path, FileLoadContext context, Collection<String> suffices)
    {
        int added = 0;

        try
        {
            List<Path> paths = RCFileHelper.listFilesRecursively(path, new FileSuffixFilter(suffices), true);

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

    public boolean tryLoad(ResourceLocation resourceLocation, String customID, FileLoadContext context)
    {
        try
        {
            Path path = RCFileHelper.pathFromResourceLocation(resourceLocation);
            if (path != null)
                return load(path, customID, context);
            else
                RecurrentComplex.logger.error("Can't find path for '" + resourceLocation + "'");
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
        }

        return false;
    }

    public boolean tryLoad(Path file, String customID, FileLoadContext context)
    {
        try
        {
            return load(file, customID, context);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error loading resource: " + file, e);
        }

        return false;
    }

    public boolean load(Path file, String customID, FileLoadContext context) throws Exception
    {
        FileTypeHandler handler = get(FilenameUtils.getExtension(file.getFileName().toString()));

        return handler != null && handler.loadFile(file, customID, context);
    }
}
