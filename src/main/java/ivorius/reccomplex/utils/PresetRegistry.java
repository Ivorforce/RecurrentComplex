/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by lukas on 26.02.15.
 */
public abstract class PresetRegistry<T> implements FileTypeHandler
{
    protected final CustomizableMap<String, T> presets = new CustomizableMap<>();
    protected final CustomizableMap<String, Metadata> metadata = new CustomizableMap<>();
    protected Gson gson;
    @Nullable
    protected String defaultID;
    protected String fileSuffix;

    public PresetRegistry(String fileSuffix)
    {
        this.fileSuffix = fileSuffix;

        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        registerGson(builder);
        gson = builder.create();
    }

    public String getFileSuffix()
    {
        return fileSuffix;
    }

    public void register(@Nonnull String id, boolean custom, @Nonnull T t, @Nullable Metadata metadata)
    {
        presets.put(id, t, custom);
        this.metadata.put(id, metadata != null ? metadata : new Metadata(id, new String[0]), custom);
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

    @Nonnull
    public Optional<T> preset(String id)
    {
        return Optional.ofNullable(presets.getMap().get(id))
                .map(this::copy);
    }

    public T copy(T t)
    {
        return gson.fromJson(gson.toJsonTree(t), getType());
    }

    @Nonnull
    public Optional<Preset<T>> fullPreset(String id)
    {
        return metadata(id).flatMap(m -> preset(id).map(p -> new Preset<>(p, m)));
    }

    @Nonnull
    public Optional<String> title(String id)
    {
        return metadata(id).map(m -> m.title);
    }

    @Nonnull
    public Optional<List<String>> description(String id)
    {
        return metadata(id).map(d -> d.description).map(Lists::newArrayList);
    }

    @Nonnull
    public Optional<Metadata> metadata(String id)
    {
        return Optional.ofNullable(metadata.getMap().get(id))
                .map(meta -> gson.fromJson(gson.toJsonTree(meta), Metadata.class));
    }

    public Collection<String> allIDs()
    {
        return presets.getMap().keySet();
    }

    public boolean has(String id)
    {
        return presets.getMap().containsKey(id);
    }

    protected abstract void registerGson(GsonBuilder builder);

    protected abstract Type getType();

    @Override
    public boolean loadFile(Path path, String customID, FileLoadContext context)
    {
        Preset<T> preset = null;
        String name = FileTypeHandler.defaultName(path, customID);

        try
        {
            String file = new String(Files.readAllBytes(path));
            preset = read(file).get();
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.warn("Error reading preset", e);
        }

        if (preset != null)
        {
            register(name, context.custom, preset.t, preset.metadata);

            return true;
        }

        return false;
    }

    @Override
    public void clearCustomFiles()
    {
        presets.clearCustom();
        metadata.clearCustom();
    }

    public Optional<Preset<T>> read(String file)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(file).getAsJsonObject();

        T t = gson.fromJson(jsonObject.get("data"), getType());
        Metadata metadata = gson.fromJson(jsonObject.get("metadata"), Metadata.class);

        return t != null && metadata != null ? Optional.of(new Preset<>(t, metadata)) : Optional.empty();
    }

    public String write(String id)
    {
        return fullPreset(id).map(p ->
        {
            JsonObject object = new JsonObject();

            object.add("data", gson.toJsonTree(p.t));
            object.add("metadata", gson.toJsonTree(p.metadata));

            return gson.toJson(object);
        }).orElse(null);
    }

    public void save(String id, boolean activeFolder) throws IOException
    {
        File file = FileUtils.getFile(RCFileTypeRegistry.getDirectory(activeFolder), String.format("%s.%s", id, getFileSuffix()));
        if (file.exists())
            file.delete();
        FileUtils.write(file, write(id));
    }

    public static class Metadata
    {
        public String title = "";
        public String[] description;

        public Metadata(String title, String[] description)
        {
            this.title = title;
            this.description = description;
        }
    }

    protected static class Preset<T>
    {
        public T t;
        public Metadata metadata;

        public Preset(T t, Metadata metadata)
        {
            this.t = t;
            this.metadata = metadata;
        }
    }
}
