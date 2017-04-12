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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellFloatNullable extends TableCellPropertyDefault<Float> implements GuiControlListener<GuiSlider>
{
    protected GuiSlider slider;
    protected GuiButton nullButton;

    protected boolean enabled = true;
    protected float defaultValue;
    protected float min;
    protected float max;
    protected Scale scale = Scales.none();

    protected String titleFormat = "%.4f";

    protected String buttonTitleNull;
    protected String buttonTitleCustom;
    protected float nullButtonWidth = 0.08f;

    public TableCellFloatNullable(String id, Float value, float defaultValue, float min, float max, String buttonTitleNull, String buttonTitleCustom)
    {
        super(id, value);

        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.buttonTitleNull = buttonTitleNull;
        this.buttonTitleCustom = buttonTitleCustom;
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

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;

        if (slider != null)
            slider.enabled = enabled;
    }

    public float getNullButtonWidth()
    {
        return nullButtonWidth;
    }

    public void setNullButtonWidth(float nullButtonWidth)
    {
        this.nullButtonWidth = nullButtonWidth;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        if (slider == null)
        {
            slider = new GuiSlider(-1, 0, 0, 0, 0, "");
            slider.addListener(this);
        }
        updateSliderBounds(bounds);
        slider.setMinValue(scale.out(min));
        slider.setMaxValue(scale.out(max));

        updateSliderValue();
        slider.visible = !isHidden();

        screen.addButton(this, 0, slider);

        int nullButtonWidth = MathHelper.floor_float(bounds.getWidth() * this.nullButtonWidth);
        nullButton = new GuiButton(-1, bounds.getMinX() + slider.width + 2, bounds.getMinY() + (bounds.getHeight() - 20) / 2, nullButtonWidth, 20, property != null ? buttonTitleCustom : buttonTitleNull);

        nullButton.enabled = enabled;
        nullButton.visible = !isHidden();

        screen.addButton(this, 1, nullButton);
    }

    protected void updateSliderValue()
    {
        slider.enabled = enabled && property != null;
        float activeValue = getActiveValue();
        slider.setValue(scale.out(activeValue));
        slider.displayString = String.format(titleFormat, activeValue);
    }

    protected float getActiveValue()
    {
        return property != null ? property : defaultValue;
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (slider != null)
            slider.visible = !hidden;
        if (nullButton != null)
            nullButton.visible = !hidden;
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        if (buttonID == 1)
        {
            property = property != null ? null : defaultValue;
            nullButton.displayString = property != null ? buttonTitleCustom : buttonTitleNull;
            updateSliderValue();

            alertListenersOfChange();
        }
    }

    @Override
    public void valueChanged(GuiSlider gui)
    {
        property = scale.in(gui.getValue());
        gui.displayString = String.format(titleFormat, getActiveValue());

        alertListenersOfChange();
    }

    @Override
    public void setPropertyValue(Float value)
    {
        super.setPropertyValue(value);

        if (slider != null)
            updateSliderValue();
    }

    protected void updateSliderBounds(Bounds bounds)
    {
        int sliderWidth = MathHelper.floor_float(bounds.getWidth() * (1.0f - nullButtonWidth)) - 2;
        Bounds.set(slider, Bounds.fromSize(bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, sliderWidth, 20));
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);

        if (slider != null)
            updateSliderBounds(bounds);
    }
}
