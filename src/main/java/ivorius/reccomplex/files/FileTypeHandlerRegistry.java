/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files;

import com.google.gson.Gson;

import java.util.function.Function;

/**
 * Created by lukas on 29.09.16.
 */
public class FileTypeHandlerRegistry<S> extends FileTypeHandlerString<S>
{
    public Function<String, S> reader;
    public FileRegistry<S> registry;

    public FileTypeHandlerRegistry(Function<String, S> reader, FileRegistry<S> registry)
    {
        this.reader = reader;
        this.registry = registry;
    }

    public FileTypeHandlerRegistry(Gson gson, Class<? extends S> type, FileRegistry<S> registry)
    {
        this(gsonReader(gson, type), registry);
    }

    public FileTypeHandlerRegistry(Class<? extends S> type, FileRegistry<S> registry)
    {
        this(gsonReader(type), registry);
    }

    public static <S> Function<String, S> gsonReader(Gson gson, Class<? extends S> type)
    {
        return s -> gson.fromJson(s, type);
    }

    public static <S> Function<String, S> gsonReader(Class<? extends S> type)
    {
        return gsonReader(new Gson(), type);
    }

    @Override
    public S read(String file)
    {
        return reader.apply(file);
    }

    @Override
    public void clearCustomFiles()
    {
        registry.clearCustomFiles();
    }

    @Override
    public void load(String id, S o, FileLoadContext context)
    {
        registry.register(id, context.domain, o, context.active, context.custom);
    }

    public interface Loader<S>
    {
        void load(String id, S o, FileLoadContext context);
    }
}
