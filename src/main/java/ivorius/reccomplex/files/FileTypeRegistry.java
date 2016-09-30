/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by lukas on 18.09.15.
 */
public class FileTypeRegistry
{
    private final Map<String, FileTypeHandler> handlers = new HashMap<>();

    protected static String defaultName(Path path, String customID)
    {
        return customID != null ? customID : FilenameUtils.getBaseName(path.getFileName().toString());
    }

    public FileTypeHandler get(String suffix)
    {
        return handlers.get(suffix);
    }

    public FileTypeHandler register(FileTypeHandler handler)
    {
        if (handlers.containsKey(handler.getSuffix()))
            throw new IllegalArgumentException();

        return handlers.put(handler.getSuffix(), handler);
    }

    public void unregister(FileTypeHandler handler)
    {
        handlers.remove(handler.getSuffix());
    }

    // --------------- Loading

    public Set<String> keySet()
    {
        return handlers.keySet();
    }

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

    public boolean tryLoad(ResourceLocation resourceLocation, @Nullable String customID, FileLoadContext context)
    {
        Path path = null;

        try
        {
            path = RCFileHelper.pathFromResourceLocation(resourceLocation);
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
                RecurrentComplex.logger.error(String.format("Reading unsupported: ?.%s", FilenameUtils.getExtension(path.getFileName().toString())), e);
            }
            catch (Throwable e)
            {
                RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
            }
        }

        return false;
    }

    public boolean tryLoad(Path file, @Nullable String customID, FileLoadContext context)
    {
        try
        {
            return load(file, customID, context);
        }
        catch (UnsupportedOperationException e)
        {
            RecurrentComplex.logger.error(String.format("Reading unsupported: ?.%s", FilenameUtils.getExtension(file.getFileName().toString())), e);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error loading resource: " + file, e);
        }

        return false;
    }

    public boolean load(Path file, @Nullable String customID, FileLoadContext context) throws Exception
    {
        FileTypeHandler handler = get(FilenameUtils.getExtension(file.getFileName().toString()));
        String id = defaultName(file, customID);

        return handler != null && handler.loadFile(file, id, context);
    }

    // --------------- Saving

    public boolean tryWrite(boolean activeFolder, String suffix, String name)
    {
        try
        {
            write(activeFolder, suffix, name);
            return true;
        }
        catch (UnsupportedOperationException e)
        {
            RecurrentComplex.logger.error(String.format("Writing unsupported: %s.%s", name, suffix), e);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error(String.format("Error writing file: %s.%s", name, suffix), e);
        }

        return false;
    }

    public void write(boolean activeFolder, String suffix, String name) throws Exception
    {
        FileTypeHandler handler = get(suffix);

        if (handler == null)
            throw new IllegalArgumentException();

        Path path = FileUtils.getFile(RCFileTypeRegistry.getDirectory(activeFolder), String.format("%s.%s", name, suffix)).toPath();

        Files.deleteIfExists(path);
        handler.writeFile(path, name);
    }
}
