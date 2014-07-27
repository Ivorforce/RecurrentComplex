/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.ivtoolkit.gui.GuiControlListener;
import ivorius.ivtoolkit.gui.GuiSlider;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementFloat extends TableElementPropertyDefault<Float> implements GuiControlListener<GuiSlider>
{
    private GuiSlider slider;

    private boolean enabled = true;
    private float min;
    private float max;

    public TableElementFloat(String id, String title, float value, float min, float max)
    {
        super(id, title, value);

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
        slider = new GuiSlider(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, String.valueOf(property));
        slider.setMinValue(min);
        slider.setMaxValue(max);
        slider.enabled = enabled;
        slider.addListener(this);

        slider.setValue(property);
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
    public void valueChanged(GuiSlider gui)
    {
        property = gui.getValue();
        gui.displayString = String.valueOf(property);

        alertListenersOfChange();
    }

    @Override
    public void setPropertyValue(Float value)
    {
        super.setPropertyValue(value);

        if (slider != null)
        {
            slider.setValue(value);
            slider.displayString = String.valueOf(property);
        }
    }
}
