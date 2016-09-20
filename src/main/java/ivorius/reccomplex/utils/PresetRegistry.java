/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.gson.Gson;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by lukas on 26.02.15.
 */
public abstract class PresetRegistry<T> implements FileTypeHandler
{
    protected final CustomizableMap<String, T> presets = new CustomizableMap<>();
    protected Gson gson = createGson();
    @Nullable
    protected String defaultID;
    protected String fileSuffix;

    public PresetRegistry(String fileSuffix)
    {
        this.fileSuffix = fileSuffix;
    }

    @Nullable
    public String getDefaultID()
    {
        return defaultID;
    }

    public String getFileSuffix()
    {
        return fileSuffix;
    }

    public void register(@Nonnull String id, boolean custom, @Nonnull T values)
    {
        presets.put(id, values, custom);
    }

    public void setDefault(@Nonnull String type)
    {
        defaultID = type;
    }

    @Nullable
    public String defaultID()
    {
        return defaultID;
    }

    @Nullable
    public T preset(String id)
    {
        T list = presets.getMap().get(id);
        if (list != null)
            return gson.fromJson(gson.toJsonTree(list), getType());
        return null;
    }

    public Collection<String> allIDs()
    {
        return presets.getMap().keySet();
    }

    public boolean has(String id)
    {
        return presets.getMap().containsKey(id);
    }

    protected abstract Gson createGson();

    protected abstract Type getType();

    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        T t = null;
        String name = context.customID != null ? context.customID : FilenameUtils.getBaseName(path.getFileName().toString());

        try
        {
            t = read(new String(Files.readAllBytes(path)));
        }
        catch (IOException e)
        {
            RecurrentComplex.logger.warn("Error reading preset", e);
        }

        if (t != null)
        {
            register(name, context.custom, t);

            return true;
        }

        return false;
    }

    @Override
    public void clearCustomFiles()
    {
        presets.clearCustom();
    }

    public T read(String file)
    {
        return gson.fromJson(file, getType());
    }

    public String write(String key)
    {
        return gson.toJson(preset(key));
    }
}
