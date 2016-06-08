/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 27.02.15.
 */
public class PresettedList<T>
{
    protected final List<T> list = new ArrayList<>();
    @Nonnull
    protected ListPresets<T> listPresets;
    @Nullable
    protected String preset;

    public PresettedList(@Nonnull ListPresets<T> listPresets, String preset)
    {
        this.listPresets = listPresets;
        setPreset(preset);
    }

    @Nonnull
    public ListPresets<T> getListPresets()
    {
        return listPresets;
    }

    public void setListPresets(@Nonnull ListPresets<T> listPresets)
    {
        this.listPresets = listPresets;
    }

    @Nullable
    public String getPreset()
    {
        return preset;
    }

    public boolean setPreset(@Nullable String preset)
    {
        this.preset = preset;
        return tryLoadFromPreset();
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
        setPreset(listPresets.defaultType());
    }

    @SafeVarargs
    public final void setContents(T... ts)
    {
        setContents(Arrays.asList(ts));
    }

    public void setContents(List<T> ts)
    {
        setToCustom();
        list.clear();
        list.addAll(ts);
    }

    public List<T> getList()
    {
        tryLoadFromPreset();
        return list;
    }

    protected boolean tryLoadFromPreset()
    {
        if (this.preset != null)
        {
            List<T> preset = listPresets.preset(this.preset);
            if (preset != null)
            {
                list.clear();
                list.addAll(preset);
                return true;
            }
        }

        return false;
    }
}
