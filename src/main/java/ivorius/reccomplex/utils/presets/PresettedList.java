/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.presets;

import com.google.common.collect.Lists;
import ivorius.reccomplex.utils.PresetRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lukas on 27.02.15.
 */
public class PresettedList<T> extends PresettedObject<ArrayList<T>>
{
    public PresettedList(@Nonnull PresetRegistry<ArrayList<T>> presetRegistry, String preset)
    {
        super(presetRegistry, preset);
    }

    @SafeVarargs
    public final void setContents(T... ts)
    {
        super.setContents(Lists.newArrayList(ts));
    }

    public void setContents(Collection<T> ts)
    {
        super.setContents(Lists.newArrayList(ts));
    }
}
