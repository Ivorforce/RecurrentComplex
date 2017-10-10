/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.ivtoolkit.gui.GuiControlListener;
import ivorius.ivtoolkit.gui.GuiSlider;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.utils.scale.Scale;
import ivorius.reccomplex.utils.scale.Scales;

import javax.annotation.Nullable;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellFloat extends TableCellSlider<Float>
{
    protected String titleFormat;

    public TableCellFloat(String id, float value, float min, float max)
    {
        super(id, value, min, max);
        setTitleFormat("%.4f");
    }

    public String getTitleFormat()
    {
        return titleFormat;
    }

    public void setTitleFormat(String titleFormat)
    {
        this.titleFormat = titleFormat;
        slider.displayString = displayString();
    }

    @Override
    protected float serialize(Float aFloat)
    {
        return aFloat;
    }

    @Nullable
    @Override
    protected Float deserialize(float val)
    {
        return val;
    }

    @Override
    protected String displayString()
    {
        return String.format(titleFormat != null ? titleFormat : "%f", getPropertyValue());
    }
}
