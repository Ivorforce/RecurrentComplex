/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.table;

import ivorius.structuregen.gui.GuiControlListener;
import ivorius.structuregen.gui.GuiSlider;
import net.minecraft.util.MathHelper;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementInteger extends TableElementPropertyDefault<Integer> implements GuiControlListener<GuiSlider>
{
    private GuiSlider slider;

    private boolean enabled = true;
    private int min;
    private int max;

    public TableElementInteger(String id, String title, int value, int min, int max)
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
        property = MathHelper.floor_float(gui.getValue());

        gui.setValue(property);
        gui.displayString = String.valueOf(property);

        alertListenersOfChange();
    }

    @Override
    public void setPropertyValue(Integer value)
    {
        super.setPropertyValue(value);

        if (slider != null)
        {
            slider.setValue(value);
            slider.displayString = String.valueOf(property);
        }
    }
}
