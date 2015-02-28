/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.ivtoolkit.gui.GuiControlListener;
import ivorius.ivtoolkit.gui.GuiSlider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementFloatNullable extends TableElementPropertyDefault<Float> implements GuiControlListener<GuiSlider>
{
    protected GuiSlider slider;
    protected GuiButton nullButton;

    protected boolean enabled = true;
    protected float defaultValue;
    protected float min;
    protected float max;

    protected String titleFormat = "%.4f";

    protected String buttonTitleNull;
    protected String buttonTitleCustom;
    protected float nullButtonWidth = 0.08f;

    public TableElementFloatNullable(String id, String title, float value, float defaultValue, float min, float max, String buttonTitleNull, String buttonTitleCustom)
    {
        super(id, title, value);

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
        int sliderWidth = MathHelper.floor_float(bounds.getWidth() * (1.0f - nullButtonWidth)) - 2;
        slider = new GuiSlider(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, sliderWidth, 20, "");
        slider.setMinValue(min);
        slider.setMaxValue(max);
        slider.addListener(this);

        updateSliderValue();
        slider.visible = !isHidden();

        screen.addButton(this, 0, slider);

        int nullButtonWidth = MathHelper.floor_float(bounds.getWidth() * this.nullButtonWidth) - 2;
        nullButton = new GuiButton(-1, bounds.getMinX() + sliderWidth + 2, bounds.getMinY() + (bounds.getHeight() - 20) / 2, nullButtonWidth, 20, property != null ? buttonTitleCustom : buttonTitleNull);

        nullButton.enabled = enabled;
        nullButton.visible = !isHidden();

        screen.addButton(this, 1, nullButton);
    }

    protected void updateSliderValue()
    {
        slider.enabled = enabled && property != null;
        slider.setValue(getActiveValue());
        slider.displayString = String.format(titleFormat, getActiveValue());
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
        }
    }

    @Override
    public void valueChanged(GuiSlider gui)
    {
        property = gui.getValue();
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
}
