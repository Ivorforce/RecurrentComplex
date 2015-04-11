/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.ivtoolkit.gui.*;
import ivorius.reccomplex.utils.RangeHelper;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellIntegerRange extends TableCellPropertyDefault<IntegerRange> implements GuiControlListener<GuiSliderMultivalue>
{
    private GuiSliderRange slider;

    private boolean enabled = true;
    private int min;
    private int max;

    public TableCellIntegerRange(String id, IntegerRange value, int min, int max)
    {
        super(id, value);

        this.min = min;
        this.max = max;
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
        slider = new GuiSliderRange(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, property.getMin() + " - " + property.getMax());
        slider.setMinValue(min);
        slider.setMaxValue(max);
        slider.enabled = enabled;
        slider.addListener(this);

        slider.setRange(new FloatRange(property));
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
        property = RangeHelper.roundedIntRange(slider.getRange());

        slider.setRange(new FloatRange(property));
        slider.displayString = property.getMin() + " - " + property.getMax();

        alertListenersOfChange();
    }

    @Override
    public void setPropertyValue(IntegerRange value)
    {
        super.setPropertyValue(value);

        if (slider != null)
        {
            slider.setRange(new FloatRange(value));
            slider.displayString = property.getMin() + " - " + property.getMax();
        }
    }
}
