/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.ivtoolkit.gui.GuiControlListener;
import ivorius.ivtoolkit.gui.GuiSliderMultivalue;
import ivorius.ivtoolkit.gui.GuiSliderRange;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.utils.scale.Scale;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellFloatRange extends TableCellPropertyDefault<FloatRange> implements GuiControlListener<GuiSliderMultivalue>
{
    protected GuiSliderRange slider;

    protected boolean enabled = true;
    protected float min;
    protected float max;
    protected Scale scale = Scales.none();

    protected String titleFormat = "%.4f";

    public TableCellFloatRange(String id, FloatRange value, float min, float max, String titleFormat)
    {
        super(id, value);

        this.min = min;
        this.max = max;
        this.titleFormat = titleFormat;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;

        if (slider != null)
        {
            slider.enabled = enabled;
        }
    }

    public float getMin()
    {
        return min;
    }

    public void setMin(float min)
    {
        this.min = min;
    }

    public float getMax()
    {
        return max;
    }

    public void setMax(float max)
    {
        this.max = max;
    }

    public Scale getScale()
    {
        return scale;
    }

    public void setScale(Scale scale)
    {
        this.scale = scale;
    }

    public String getTitleFormat()
    {
        return titleFormat;
    }

    public void setTitleFormat(String titleFormat)
    {
        this.titleFormat = titleFormat;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        if (slider == null)
        {
            slider = new GuiSliderRange(-1, 0, 0, 0, 0, getRangeString());
            slider.addListener(this);
        }
        updateSliderBounds(bounds);

        slider.setMinValue(scale.out(min));
        slider.setMaxValue(scale.out(max));
        slider.enabled = enabled;

        slider.setRange(Scales.out(scale, property));
        slider.visible = !isHidden();

        screen.addButton(this, 0, slider);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (slider != null)
            slider.visible = !hidden;
    }

    @Override
    public void valueChanged(GuiSliderMultivalue gui)
    {
        property = Scales.in(scale, ((GuiSliderRange) gui).getRange());
        slider.displayString = getRangeString();

        alertListenersOfChange();
    }

    @Override
    public void setPropertyValue(FloatRange value)
    {
        super.setPropertyValue(value);

        if (slider != null)
        {
            slider.setRange(Scales.out(scale, property));
            slider.displayString = getRangeString();
        }
    }

    protected void updateSliderBounds(Bounds bounds)
    {
        Bounds.set(slider, Bounds.fromSize(bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20));
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);

        if (slider != null)
            updateSliderBounds(bounds);
    }

    private String getRangeString()
    {
        return String.format(titleFormat, property.getMin()) + " - " + String.format(titleFormat, property.getMax());
    }
}
