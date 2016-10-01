/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    @Nullable
    protected static String extension(Path path)
    {
        return FilenameUtils.getExtension(path.getFileName().toString());
    }

    protected static Path filenamePath(String name, String extension)
    {
        return Paths.get(name + "." + extension);
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

    // --------------- Loading

    public void unregister(FileTypeHandler handler)
    {
        handlers.remove(handler.getSuffix());
    }

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
                RecurrentComplex.logger.error(String.format("Reading unsupported: ?.%s", extension(path)), e);
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
            RecurrentComplex.logger.error(String.format("Reading unsupported: ?.%s", extension(file)), e);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error loading resource: " + file, e);
        }

        return false;
    }

    // --------------- Saving

    public boolean load(Path file, @Nullable String customID, FileLoadContext context) throws Exception
    {
        FileTypeHandler handler = get(extension(file));
        String id = defaultName(file, customID);

        return handler != null && handler.loadFile(file, id, context);
    }

    public boolean tryWrite(Path path, String name)
    {
        try
        {
            write(path, name);
            return true;
        }
        catch (UnsupportedOperationException e)
        {
            RecurrentComplex.logger.error(String.format("Writing unsupported: %s", path.getFileName()), e);
        }
        catch (NoSuchElementException e)
        {
            RecurrentComplex.logger.error(String.format("No entry found: %s in %s", name, extension(path)), e);
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

    public void write(Path path, String name) throws Exception
    {
        FileTypeHandler handler = get(extension(path));

        if (handler == null)
            throw new IllegalArgumentException();

        Files.deleteIfExists(path);
        handler.writeFile(path, name);
    }

    // --------------- Finding

    public List<Path> tryFind(Path path, String name, String suffix)
    {
        Path filename = filenamePath(name, suffix);

        try
        {
            return RCFileHelper.listFilesRecursively(path, entry -> entry.getFileName().equals(filename), true);
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.error("Error finding resources: " + path, e);
        }

        return Collections.emptyList();
    }

    public Set<String> tryFindIDs(Path path, String suffix)
    {
        try
        {
            return RCFileHelper.listFilesRecursively(path, new FileSuffixFilter(suffix), true).stream()
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(FilenameUtils::removeExtension)
                    .collect(Collectors.toSet());
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.error("Error finding resources: " + path, e);
        }

        return Collections.emptySet();
    }

    // --------------- Deleting

    public List<Path> tryDelete(Path path, String name, String suffix)
    {
        return tryFind(path, name, suffix).stream().filter(p ->
        {
            try
            {
                Files.delete(p);
                return false;
            }
            catch (IOException e)
            {
                RecurrentComplex.logger.error("Error deleting resource: " + p, e);
            }

            return true;
        }).collect(Collectors.toList());
    }
}
