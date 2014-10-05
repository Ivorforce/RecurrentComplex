/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.files;

import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by lukas on 05.10.14.
 */
public class RCFileHelper
{
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

        String s = uri.toString();
        int separator = s.indexOf("!/");
        String entryName = s.substring(separator + 2);
        URI fileURI = URI.create(s.substring(0, separator));

        FileSystem fs = FileSystems.newFileSystem(fileURI, Collections.<String, Object>emptyMap());
        return fs.getPath(entryName);
    }

    public static Path pathFromResourceLocation(ResourceLocation resourceLocation) throws URISyntaxException, IOException
    {
        URL resource = RCFileHelper.class.getResource("/assets/" + resourceLocation.getResourceDomain() + "/" + resourceLocation.getResourcePath());
        return resource != null ? Paths.get(resource.toURI()) : null;
    }

    public static List<Path> listFilesRecursively(Path dir, final DirectoryStream.Filter<Path> filter, final boolean recursive) throws IOException
    {
        final List<Path> files = new ArrayList<>();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                if (filter.accept(file))
                    files.add(file);

                return recursive ?  FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
            }
        });
        return files;
    }

}
