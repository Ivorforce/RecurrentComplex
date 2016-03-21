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

    public void clearCustomFiles()
    {
        handlers.values().forEach(FileTypeHandler::clearCustomFiles);
    }

    public void clearCustomFiles(Collection<String> suffices)
    {
        handlers.entrySet().stream().filter(entry -> suffices.contains(entry.getKey())).forEach(entry -> entry.getValue().clearCustomFiles());
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
                if (tryLoad(file, context))
                    added ++;
            }

            return added;
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.error("Error loading resources from directory", e);
        }

        return added;
    }

    public boolean tryLoad(ResourceLocation resourceLocation, FileLoadContext context)
    {
        try
        {
            Path path = RCFileHelper.pathFromResourceLocation(resourceLocation);
            if (path != null)
                return tryLoad(path, context);
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
        }

        return false;
    }

    public boolean tryLoad(Path file, FileLoadContext context)
    {
        try
        {
            FileTypeHandler handler = get(FilenameUtils.getExtension(file.getFileName().toString()));
            return handler.loadFile(file, context);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error loading resource", e);
        }

        return false;
    }
}
