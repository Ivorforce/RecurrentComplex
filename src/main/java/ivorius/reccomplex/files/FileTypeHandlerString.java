/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import com.google.gson.Gson;
import ivorius.reccomplex.RecurrentComplex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.16.
 */
public class FileTypeHandlerString<S> extends FileTypeHandlerRegistry<S>
{
    public Reader<? extends S> reader;

    public FileTypeHandlerString(String fileSuffix, CustomizableRegistry<? super S> registry, Reader<? extends S> reader)
    {
        super(fileSuffix, registry);
        this.reader = reader;
    }

    public FileTypeHandlerString(String fileSuffix, CustomizableRegistry<? super S> registry, Gson gson, Class<? extends S> type)
    {
        this(fileSuffix, registry, gsonReader(gson, type));
    }

    public FileTypeHandlerString(String fileSuffix, CustomizableRegistry<? super S> registry, Class<? extends S> type)
    {
        this(fileSuffix, registry, gsonReader(type));
    }

    public static <S> Reader<S> gsonReader(Gson gson, Class<? extends S> type)
    {
        return s -> gson.fromJson(s, type);
    }

    public static <S> Reader<S> gsonReader(Class<? extends S> type)
    {
        return gsonReader(new Gson(), type);
    }

    @Override
    public S read(Path path, String name)
    {
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
            return null;

        try
        {
            return read(resource);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.warn("Error reading resource: " + name, e);
        }

        return null;
    }

    public S read(String file) throws Exception
    {
        return reader.read(file);
    }

    public interface Reader<S>
    {
        S read(String json) throws Exception;
    }
}
