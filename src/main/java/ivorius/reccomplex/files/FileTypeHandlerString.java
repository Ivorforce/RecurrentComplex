/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.16.
 */
public abstract class FileTypeHandlerString<S> extends FileTypeHandler
{
    public FileTypeHandlerString(String suffix)
    {
        super(suffix);
    }

    @ParametersAreNonnullByDefault
    public abstract boolean loadFile(String file, Path path, String id, FileLoadContext context) throws Exception;

    @Override
    @ParametersAreNonnullByDefault
    public boolean loadFile(Path path, String id, FileLoadContext context)
    {
        String resource = null;

        try
        {
            resource = new String(Files.readAllBytes(path));
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.error("Resource is damaged: " + path, e);
        }

        if (resource == null)
            return false;

        try
        {
            return loadFile(resource, path, id, context);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error loading resource: " + path, e);
        }

        return false;
    }
}
