/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 26.02.15.
 */
public abstract class ListPresets<T> implements FileTypeHandler
{
    protected final CustomizableMap<String, List<T>> presets = new CustomizableMap<>();
    protected Gson gson = createGson();
    @Nullable
    protected String defaultType;
    protected String fileSuffix;

    public ListPresets(String fileSuffix)
    {
        this.fileSuffix = fileSuffix;
    }

    @Nullable
    public String getDefaultType()
    {
        return defaultType;
    }

    public String getFileSuffix()
    {
        return fileSuffix;
    }

    public void register(@Nonnull String type, boolean custom, @Nonnull List<T> values)
    {
        presets.put(type, Lists.newArrayList(values), custom);
    }

    @SafeVarargs
    public final void register(@Nonnull String type, boolean custom, @Nonnull T... values)
    {
        presets.put(type, Arrays.asList(values), custom);
    }

    public void setDefault(@Nonnull String type)
    {
        defaultType = type;
    }

    @Nullable
    public String defaultType()
    {
        return defaultType;
    }

    @Nullable
    public List<T> preset(String type)
    {
        List<T> list = presets.getMap().get(type);
        if (list != null)
            return Arrays.asList(gson.fromJson(gson.toJsonTree(list), getType()));
        return null;
    }

    public Collection<String> allTypes()
    {
        return presets.getMap().keySet();
    }

    public boolean has(String type)
    {
        return presets.getMap().containsKey(type);
    }

    protected abstract Gson createGson();

    protected abstract Class<T[]> getType();

    @Override
    public boolean loadFile(Path path, FileLoadContext context)
    {
        T[] t = null;
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

    public T[] read(String file)
    {
        return gson.fromJson(file, getType());
    }

    public String write(String key)
    {
        return gson.toJson(preset(key));
    }
}
