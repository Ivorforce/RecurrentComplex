/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import com.google.gson.Gson;
import ivorius.reccomplex.RecurrentComplex;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 29.09.16.
 */
public class FileTypeHandlerRegistryString<S> extends FileTypeHandlerRegistry<S>
{
    public Reader<? extends S> reader;
    public Writer<? super S> writer;

    public FileTypeHandlerRegistryString(String suffix, LeveledRegistry<? super S> registry, Reader<? extends S> reader, Writer<? super S> writer)
    {
        super(suffix, registry);
        this.reader = reader;
        this.writer = writer;
    }

    public <RW extends Reader<S> & Writer<S>> FileTypeHandlerRegistryString(String suffix, LeveledRegistry<? super S> registry, RW rw)
    {
        this(suffix, registry, rw, rw);
    }

    public FileTypeHandlerRegistryString(String suffix, LeveledRegistry<? super S> registry, Gson gson, Class<? extends S> type)
    {
        this(suffix, registry, gsonReader(gson, type), gsonWriter(gson, type));
    }

    public FileTypeHandlerRegistryString(String suffix, LeveledRegistry<? super S> registry, Class<? extends S> type)
    {
        this(suffix, registry, gsonReader(type), gsonWriter(type));
    }

    public static <S> Reader<S> gsonReader(Gson gson, Class<? extends S> type)
    {
        return s -> gson.fromJson(s, type);
    }

    public static <S> Reader<S> gsonReader(Class<? extends S> type)
    {
        return gsonReader(new Gson(), type);
    }

    public static <S> Writer<S> gsonWriter(Gson gson, Class<? extends S> type)
    {
        return s -> gson.toJson(s, type);
    }

    public static <S> Writer<S> gsonWriter(Class<? extends S> type)
    {
        return gsonWriter(new Gson(), type);
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

    @Override
    @ParametersAreNonnullByDefault
    public void write(Path path, S s) throws Exception
    {
        if (Files.exists(path))
            Files.delete(path);

        Files.write(path, write(s).getBytes());
    }

    public String write(S s) throws Exception
    {
        if (writer == null)
            throw new UnsupportedOperationException("write");

        return writer.write(s);
    }

    public S read(String file) throws Exception
    {
        if (reader == null)
             throw new UnsupportedOperationException("read");

        return reader.read(file);
    }

    public interface Reader<S>
    {
        S read(String json) throws Exception;
    }

    public interface Writer<S>
    {
        String write(S s) throws Exception;
    }
}
