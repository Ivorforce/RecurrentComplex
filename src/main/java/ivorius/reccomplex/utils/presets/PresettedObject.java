/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.presets;

import ivorius.reccomplex.utils.PresetRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Created by lukas on 19.09.16.
 */
public class PresettedObject<T>
{
    @Nonnull
    protected PresetRegistry<T> presetRegistry;
    @Nullable
    protected String preset;

    protected T t;

    public PresettedObject(@Nonnull PresetRegistry<T> presetRegistry, String preset)
    {
        this.presetRegistry = presetRegistry;
        setPreset(preset);
    }

    @Nonnull
    public PresetRegistry<T> getPresetRegistry()
    {
        return presetRegistry;
    }

    public void setPresetRegistry(@Nonnull PresetRegistry<T> presetRegistry)
    {
        this.presetRegistry = presetRegistry;
    }

    @Nullable
    public String getPreset()
    {
        return preset;
    }

    @Nonnull
    public Optional<String> presetTitle()
    {
        return Optional.ofNullable(getPreset()).flatMap(id -> presetRegistry.title(id));
    }

    @Nonnull
    public Optional<List<String>> presetDescription()
    {
        return Optional.ofNullable(getPreset()).flatMap(id -> presetRegistry.description(id));
    }

    public boolean setPreset(@Nullable String preset)
    {
        this.preset = preset;
        t = null;
        return presetRegistry.has(preset);
    }

    public void setToCustom()
    {
        preset = null;
    }

    public boolean isCustom()
    {
        return preset == null;
    }

    public void setToDefault()
    {
        setPreset(presetRegistry.defaultID());
    }

    public T getContents()
    {
        tryLoadFromPreset();
        return t;
    }

    public void setContents(T ts)
    {
        setToCustom();
        t = ts;
    }

    protected boolean tryLoadFromPreset()
    {
        return this.preset != null && (t = presetRegistry.preset(this.preset).orElse(null)) != null;
    }
}
