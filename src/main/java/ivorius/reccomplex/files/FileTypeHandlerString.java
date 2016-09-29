/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import com.google.gson.Gson;
import ivorius.reccomplex.RecurrentComplex;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.16.
 */
public abstract class FileTypeHandlerString<S> implements FileTypeHandler
{
    public abstract boolean loadFile(String file, Path path, @Nullable String customID, FileLoadContext context) throws Exception;

    @Override
    public boolean loadFile(Path path, @Nullable String customID, FileLoadContext context)
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
            return loadFile(resource, path, customID, context);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error loading resource: " + path, e);
        }

        return false;
    }
}
