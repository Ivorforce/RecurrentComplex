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

    @Nullable
    public String getDefaultID()
    {
        return defaultID;
    }

    public String getFileSuffix()
    {
        return fileSuffix;
    }

    public void register(@Nonnull String id, boolean custom, @Nonnull T values, @Nonnull Metadata metadata)
    {
        presets.put(id, values, custom);
        this.metadata.put(id, metadata, custom);
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
        T t = presets.getMap().get(id);
        if (t != null)
            return gson.fromJson(gson.toJsonTree(t), getType());
        return null;
    }

    @Nonnull
    public Optional<String> title(String id)
    {
        return metadata(id).map(m -> m.title);
    }

    @Nonnull
    public Optional<List<String>> description(String id)
    {
        return metadata(id).map(d -> Lists.newArrayList(d.description));
    }

    @Nonnull
    public Optional<Metadata> metadata(String id)
    {
        Metadata meta = metadata.getMap().get(id);
        if (meta != null)
            return Optional.ofNullable(gson.fromJson(gson.toJsonTree(meta), Metadata.class));
        return Optional.empty();
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
    public boolean loadFile(Path path, FileLoadContext context)
    {
        Preset<T> preset = null;
        String name = context.customID != null ? context.customID : FilenameUtils.getBaseName(path.getFileName().toString());

        try
        {
            String file = new String(Files.readAllBytes(path));
            preset = read(file);
        }
        catch (IOException e)
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
    }

    public Preset<T> read(String file)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(file).getAsJsonObject();

        T t = gson.fromJson(jsonObject.get("data"), getType());
        Metadata metadata = gson.fromJson(jsonObject.get("metadata"), Metadata.class);

        return new Preset<T>(t, metadata);
    }

    public String write(String id)
    {
        return gson.toJson(preset(id));
    }

    public void save(String id, boolean activeFolder) throws IOException
    {
        FileUtils.write(FileUtils.getFile(RCFileTypeRegistry.getDirectory(activeFolder), String.format("%s.%s", id, getFileSuffix())), write(id));
    }

    public static class Metadata
    {
        public String title;
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
