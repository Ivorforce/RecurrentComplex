/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by lukas on 05.10.14.
 */
public class RCFiles
{
    public static String encodePath(String path)
    {
        return path.replaceAll(" ", "%20");
//        try
//        {
//            return URLEncoder.encode(path, "UTF-8");
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            RecurrentComplex.logger.warn("Could not encode path", e);
//        }
//
//        return path;
    }

    static Path resourceToPath(URL resource) throws IOException, URISyntaxException
    {
        Objects.requireNonNull(resource, "Resource URL cannot be null");
        URI uri = resource.toURI();

        String scheme = uri.getScheme();
        if (scheme.equals("file"))
        {
            return Paths.get(uri);
        }

        if (!scheme.equals("jar"))
        {
            throw new IllegalArgumentException("Cannot convert to Path: " + uri);
        }

        String s = encodePath(uri.toString());
        int separator = s.indexOf("!/");
        String entryName = s.substring(separator + 2);
        URI fileURI = URI.create(s.substring(0, separator));

        try
        {
            FileSystem fs = FileSystems.getFileSystem(fileURI);
            if (fs.isOpen())
                return fs.getPath(entryName);
        }
        catch (FileSystemNotFoundException ignored) {}

        FileSystem fs = FileSystems.newFileSystem(fileURI, Collections.emptyMap());
        return fs.getPath(entryName);
    }

    public static Path pathFromResourceLocation(ResourceLocation resourceLocation) throws ResourceLocationLoadException
    {
        try
        {
            URL resource = RCFiles.class.getResource(String.format("/assets/%s%s", resourceLocation.getResourceDomain(),
                    resourceLocation.getResourcePath().isEmpty() ? "" : ("/" + resourceLocation.getResourcePath())));
            return resource != null ? resourceToPath(resource.toURI().toURL()) : null;
        }
        catch (Exception e)
        {
            throw new ResourceLocationLoadException(e, resourceLocation);
        }
    }

    public static Path tryPathFromResourceLocation(ResourceLocation resourceLocation)
    {
        try
        {
            return pathFromResourceLocation(resourceLocation);
        }
        catch (Throwable e)
        {
            RecurrentComplex.logger.error("Error reading from resource location '" + resourceLocation + "'", e);
        }

        return null;
    }

    public static void walkFilesRecursively(Path dir, final DirectoryStream.Filter<Path> filter, final boolean recursive, Consumer<Path> consumer)
    {
        if (!Files.exists(dir))
            return;

        try
        {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    if (filter.accept(file))
                        consumer.accept(file);

                    return recursive ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                }
            });
        }
        catch (IOException e)
        {
            // Should not happen since our visitor doesn't throw IO Exceptions
            RecurrentComplex.logger.error(e);
        }
    }

    public static List<Path> listFilesRecursively(Path dir, final DirectoryStream.Filter<Path> filter, final boolean recursive)
    {
        List<Path> list = new ArrayList<>();
        walkFilesRecursively(dir, filter, recursive, list::add);
        return list;
    }

    public static boolean ensure(File file)
    {
        if (!file.exists() && !file.mkdir())
        {
            System.out.println("Could not create " + file.getName() + " folder");
            return false;
        }

        return true;
    }

    public static File getValidatedFolder(File file, boolean create)
    {
        return (create ? ensure(file) : file.exists()) ? file : null;
    }

    @Nullable
    public static String extension(Path path)
    {
        return FilenameUtils.getExtension(path.getFileName().toString());
    }

    public static Path filenamePath(String name, String extension)
    {
        return Paths.get(name + "." + extension);
    }

    public static class ResourceLocationLoadException extends Exception
    {
        public final ResourceLocation location;

        public ResourceLocationLoadException(ResourceLocation location)
        {
            this.location = location;
        }

        public ResourceLocationLoadException(String message, ResourceLocation location)
        {
            super(message);
            this.location = location;
        }

        public ResourceLocationLoadException(String message, Throwable cause, ResourceLocation location)
        {
            super(message, cause);
            this.location = location;
        }

        public ResourceLocationLoadException(Throwable cause, ResourceLocation location)
        {
            super(cause);
            this.location = location;
        }

        public ResourceLocationLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ResourceLocation location)
        {
            super(message, cause, enableSuppression, writableStackTrace);
            this.location = location;
        }

        public ResourceLocation getLocation()
        {
            return location;
        }
    }
}
