/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.loading;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Arrays;

/**
* Created by lukas on 05.10.14.
*/
public class FileSuffixFilter implements DirectoryStream.Filter<Path>, FilenameFilter
{
    private Iterable<String> acceptedSuffixes;

    public FileSuffixFilter(Iterable<String> acceptedSuffixes)
    {
        this.acceptedSuffixes = acceptedSuffixes;
    }

    public FileSuffixFilter(String... acceptedSuffixes)
    {
        this(Arrays.asList(acceptedSuffixes));
    }

    @Override
    public boolean accept(Path entry) throws IOException
    {
        return accept(entry.getFileName().toString());
    }

    @Override
    public boolean accept(File dir, String name)
    {
        return accept(name);
    }

    protected boolean accept(String name)
    {
        for (String s : acceptedSuffixes)
            if (FilenameUtils.isExtension(name, s))
                return true;
        return false;
    }
}
