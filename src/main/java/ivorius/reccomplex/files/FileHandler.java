/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.loading.FileSuffixFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 02.10.16.
 */
public class FileHandler
{
    // --------------- Finding

    public List<Path> tryFind(Path path, String name, String suffix)
    {
        Path filename = RCFiles.filenamePath(name, suffix);
        return RCFiles.listFilesRecursively(path, entry -> entry.getFileName().equals(filename), true);
    }

    public Set<String> tryFindIDs(Path path, String suffix)
    {
        return RCFiles.listFilesRecursively(path, new FileSuffixFilter(suffix), true).stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .map(FilenameUtils::removeExtension)
                .collect(Collectors.toSet());
    }

    // --------------- Deleting

    public Pair<Set<Path>, Set<Path>> tryDelete(Path path, String name, String suffix)
    {
        Set<Path> success = new HashSet<>();
        Set<Path> failure = new HashSet<>();

        tryFind(path, name, suffix).forEach(p ->
        {
            try
            {
                Files.delete(p);
                success.add(p);
            }
            catch (IOException e)
            {
                RecurrentComplex.logger.error("Error deleting resource: " + p, e);
                failure.add(p);
            }
        });

        return Pair.of(success, failure);
    }
}
