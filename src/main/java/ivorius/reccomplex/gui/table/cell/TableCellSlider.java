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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public abstract class TableCellSlider<T> extends TableCellPropertyDefault<T> implements GuiControlListener<GuiSlider>
{
    protected GuiSlider slider;

    protected T min;
    protected T max;
    protected Scale scale;

    public TableCellSlider(String id, T value, T min, T max)
    {
        super(id, value);

        slider = new GuiSlider(-1, 0, 0, 0, 0, "");
        slider.addListener(this);

        scale = Scales.none();
        setMin(min);
        setMax(max);
        setPropertyValue(value);
    }

    public T getMin()
    {
        return min;
    }

    public void setMin(T min)
    {
        this.min = min;
        slider.setMinValue(scale.out(serialize(min)));
    }

    public T getMax()
    {
        return max;
    }

    public void setMax(T max)
    {
        this.max = max;
        slider.setMaxValue(scale.out(serialize(max)));
    }

    public Scale getScale()
    {
        return scale;
    }

    public void setScale(Scale scale)
    {
        this.scale = scale;
        setMin(min);
        setMax(max);
        slider.setValue(scale.out(serialize(property)));
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        slider.enabled = enabled;
    }

    protected abstract float serialize(T t);

    @Nullable
    protected abstract T deserialize(float val);

    protected abstract String displayString();

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

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
    public void valueChanged(GuiSlider gui)
    {
        property = deserialize(scale.in(gui.getValue()));
        gui.displayString = displayString();

        alertListenersOfChange();
    }

    @Override
    public void setPropertyValue(T value)
    {
        super.setPropertyValue(value);

        if (slider != null)
        {
            slider.setValue(scale.out(serialize(property)));
            slider.displayString = displayString();
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
}
