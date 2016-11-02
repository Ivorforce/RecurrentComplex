/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.presets;

import ivorius.reccomplex.RecurrentComplex;
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

    public PresettedObject(@Nonnull PresetRegistry<T> presetRegistry, @Nullable String preset)
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
        // Do not check if preset exists yet - it may just not have been loaded yet!
        // Check when the data is actually needed.
        this.preset = preset;
        t = null;

        return true;
    }

    public void setToCustom()
    {
        loadFromPreset();
        preset = null;
    }

    public boolean isCustom()
    {
        return preset == null;
    }

    public void setToDefault()
    {
        setPreset(defaultPreset());
    }

    public T getContents()
    {
        if (!isCustom())
            loadFromPreset();
        return t;
    }

    public void setContents(T ts)
    {
        preset = null;
        t = ts;
    }

    protected boolean loadFromPreset()
    {
        if (preset == null)
            return true;

        if ((t = presetContents()) != null)
            return true;

        RecurrentComplex.logger.warn(String.format("Failed to find preset (%s): %s", presetRegistry.getRegistry().description, preset));
        t = presetRegistry.preset(defaultPreset()).orElse(null);

        return false;
    }

    protected String defaultPreset()
    {
        String defaultPreset = presetRegistry.defaultID();
        if (!presetRegistry.has(defaultPreset))
            throw new IllegalStateException(String.format("Default preset named '%s' not found!", defaultPreset));
        return defaultPreset;
    }

    private T presetContents()
    {
        return presetRegistry.preset(this.preset).orElse(null);
    }
}
