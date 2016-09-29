/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import ivorius.reccomplex.RecurrentComplex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.16.
 */
public abstract class FileTypeHandlerString<S> implements FileTypeHandler
{
    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        String name = FileTypeHandler.defaultName(path, context.customID);
        String resource = null;

        try
        {
            resource = new String(Files.readAllBytes(path));
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.warn("Resource is damaged: " + name, e);
        }

        if (resource == null)
            return false;

        S s = null;

        try
        {
            s = read(resource);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.warn("Error reading resource: " + name, e);
        }

        if (s == null)
            return false;

        load(name, s, context);

        return true;
    }

    public abstract S read(String file);

    public abstract void load(String id, S s, FileLoadContext context);
}
