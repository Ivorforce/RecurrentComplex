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
        boolean has = preset != null && presetRegistry.has(preset);

        if (!has)
        {
            setToDefault();
            RecurrentComplex.logger.warn(String.format("Failed to find %s preset: %s", presetRegistry.getRegistry().description, preset));
        }
        else
        {
            this.preset = preset;
            t = null;
        }

        return has;
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
        String defaultPreset = presetRegistry.defaultID();

        if (!presetRegistry.has(defaultPreset))
            throw new IllegalStateException(String.format("Default preset named '%s' not found!", defaultPreset));

        setPreset(defaultPreset);
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
        if (this.preset != null && (t = presetContents()) != null)
            return true;

        setToDefault();
        t = presetContents();

        return false;
    }

    private T presetContents()
    {
        return presetRegistry.preset(this.preset).orElse(null);
    }
}
