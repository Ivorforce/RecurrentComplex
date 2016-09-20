/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.files.FileLoadContext;
import ivorius.reccomplex.files.FileTypeHandler;
import ivorius.reccomplex.json.JsonUtils;
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
    public Optional<String> description(String id)
    {
        return metadata(id).map(m -> m.description);
    }

    @Nonnull
    public Optional<List<String>> multilineDescription(String id)
    {
        return description(id).map(IvTranslations::splitLines);
    }

    @Nonnull
    protected Optional<Metadata> metadata(String id)
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

    protected abstract Gson createGson();

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

    protected Preset<T> read(String file)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(file).getAsJsonObject();

        T t = gson.fromJson(jsonObject.get("data"), getType());
        Metadata metadata = gson.fromJson(jsonObject.get("metadata"), Metadata.class);

        return new Preset<T>(t, metadata);
    }

    public String write(String key)
    {
        return gson.toJson(preset(key));
    }

    protected static class Metadata
    {
        public String title;
        public String description;
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
