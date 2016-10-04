/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.ivtoolkit.gui.GuiControlListener;
import ivorius.ivtoolkit.gui.GuiSlider;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellInteger extends TableCellPropertyDefault<Integer> implements GuiControlListener<GuiSlider>
{
    private GuiSlider slider;

    private boolean enabled = true;
    private int min;
    private int max;

    public TableCellInteger(String id, int value, int min, int max)
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
        if (slider == null)
        {
            slider = new GuiSlider(-1, 0, 0, 0, 0, String.valueOf(property));
            slider.addListener(this);
        }
        updateSliderBounds(bounds);

        slider.setMinValue(min);
        slider.setMaxValue(max);
        slider.enabled = enabled;

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
        property = MathHelper.floor_float(gui.getValue() + 0.5f);

        gui.setValue(property);
        gui.displayString = String.valueOf(property);

        alertListenersOfChange();
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
