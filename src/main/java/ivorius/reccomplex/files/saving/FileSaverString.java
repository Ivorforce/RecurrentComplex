/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.files.saving;

import com.google.gson.Gson;
import ivorius.reccomplex.files.loading.LeveledRegistry;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by lukas on 03.10.16.
 */
public class FileSaverString<S> extends FileSaverAdapter<S>
{
    public Writer<? super S> writer;

    public FileSaverString(String id, String suffix, LeveledRegistry<S> registry, Writer<? super S> writer)
    {
        super(id, suffix, registry);
        this.writer = writer;
    }

    public FileSaverString(String id, String suffix, LeveledRegistry<S> registry, Gson gson, Class<? extends S> type)
    {
        this(id, suffix, registry, gsonSaver(gson, type));
    }

    public FileSaverString(String id, String suffix, LeveledRegistry<S> registry, Class<? extends S> type)
    {
        this(id, suffix, registry, gsonSaver(type));
    }

    public static <S> Writer<S> gsonSaver(Gson gson, Class<? extends S> type)
    {
        return s -> gson.toJson(s, type);
    }

    public static <S> Writer<S> gsonSaver(Class<? extends S> type)
    {
        return gsonSaver(new Gson(), type);
    }

    @Override
    public void saveFile(Path path, S s) throws Exception
    {
        Files.write(path, write(s).getBytes());
    }

    public String write(S s) throws Exception
    {
        if (writer == null)
            throw new UnsupportedOperationException("write");

        return writer.write(s);
    }

    public interface Writer<S>
    {
        String write(S s) throws Exception;
    }
}
