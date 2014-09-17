/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.ivtoolkit.gui.*;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementFloatRange extends TableElementPropertyDefault<FloatRange> implements GuiControlListener<GuiSliderMultivalue>
{
    private GuiSliderRange slider;

    private int floatDisplayPrecision;

    private boolean enabled = true;
    private float min;
    private float max;

    public TableElementFloatRange(String id, String title, FloatRange value, float min, float max, int floatDisplayPrecision)
    {
        super(id, title, value);

        this.min = min;
        this.max = max;
        this.floatDisplayPrecision = floatDisplayPrecision;
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

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        slider = new GuiSliderRange(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, getRangeString());
        slider.setMinValue(min);
        slider.setMaxValue(max);
        slider.enabled = enabled;
        slider.addListener(this);

        slider.setRange(property);
        slider.visible = !isHidden();

        screen.addButton(this, 0, slider);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (slider != null)
        {
            slider.visible = !hidden;
        }
    }

    @Override
    public void valueChanged(GuiSliderMultivalue gui)
    {
        property = slider.getRange();
        slider.displayString = getRangeString();

        alertListenersOfChange();
    }

    @Override
    public void setPropertyValue(FloatRange value)
    {
        super.setPropertyValue(value);

        if (slider != null)
        {
            slider.setRange(value);
            slider.displayString = getRangeString();
        }
    }

    private String getRangeString()
    {
        return getNumberString(property.getMin(), floatDisplayPrecision) + " - " + getNumberString(property.getMax(), floatDisplayPrecision);
    }

    private static String getNumberString(float number, int precision)
    {
        return String.format("%." + precision + "f", number);
    }
}
