/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 27.02.15.
 */
public class PresettedList<T>
{
    public final List<T> list = new ArrayList<>();
    @Nonnull
    protected ListPresets<T> listPresets;
    @Nullable
    protected String preset;

    public PresettedList(ListPresets<T> listPresets, String preset)
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
        return loadListFromPreset();
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
        setPreset(listPresets.defaultType);
    }

    public void setContents(List<T> ts)
    {
        setToCustom();
        list.clear();
        list.addAll(ts);
    }

    public boolean loadListFromPreset()
    {
        list.clear();

        if (preset != null)
        {
            List<T> presetList = listPresets.preset(preset);
            if (presetList != null)
            {
                list.addAll(presetList);
                return true;
            }

            return false;
        }

        return false;
    }
}
