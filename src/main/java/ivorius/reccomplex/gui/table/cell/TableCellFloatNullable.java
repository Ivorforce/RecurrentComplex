/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.utils.scale.Scale;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 02.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableCellFloatNullable extends TableCellPropertyDefault<Float>
{
    protected TableCellFloatSlider floatCell;
    protected TableCellButton nullButton;

    protected float defaultValue;

    protected String buttonTitleNull;
    protected String buttonTitleCustom;
    protected float nullButtonWidth = 0.08f;

    public TableCellFloatNullable(String id, Float value, float defaultValue, float min, float max, String buttonTitleNull, String buttonTitleCustom)
    {
        super(id, value);

        this.defaultValue = defaultValue;
        this.buttonTitleNull = buttonTitleNull;
        this.buttonTitleCustom = buttonTitleCustom;

        floatCell = new TableCellFloatSlider(null, 0, 0, 0);
        floatCell.addListener(value1 -> {
            setPropertyValue(value1);
            alertListenersOfChange();
        });
        nullButton = new TableCellButton(null, null, "");
        nullButton.addAction(() -> {
            setPropertyValue(getPropertyValue() == null ? defaultValue : null);
            alertListenersOfChange();
        });

        setMin(min);
        setMax(max);
        setPropertyValue(value);
    }

    public Float getMin()
    {
        return floatCell.getMin();
    }

    public void setMin(Float min)
    {
        floatCell.setMin(min);
    }

    public Float getMax()
    {
        return floatCell.getMax();
    }

    public void setMax(Float max)
    {
        floatCell.setMax(max);
    }

    public Scale getScale()
    {
        return floatCell.getScale();
    }

    public void setScale(Scale scale)
    {
        floatCell.setScale(scale);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;

        floatCell.setEnabled(enabled && property != null);
        nullButton.setEnabled(enabled);
    }

    public float getNullButtonWidth()
    {
        return nullButtonWidth;
    }

    public void setNullButtonWidth(float nullButtonWidth)
    {
        this.nullButtonWidth = nullButtonWidth;
        setBounds(bounds());
    }

    protected float getActiveValue()
    {
        return property != null ? property : defaultValue;
    }

    @Override
    public void setPropertyValue(Float value)
    {
        super.setPropertyValue(value);

        if (floatCell != null && nullButton != null)
        {
            floatCell.setPropertyValue(getActiveValue());
            nullButton.setTitle(value != null ? buttonTitleCustom : buttonTitleNull);
            setEnabled(enabled);
        }
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        floatCell.setHidden(hidden);
        nullButton.setHidden(hidden);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        floatCell.initGui(screen);
        nullButton.initGui(screen);
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);

        int nullButtonIntWidth = (int) (bounds.getWidth() * nullButtonWidth);
        floatCell.setBounds(Bounds.fromSize(bounds.getMinX(), bounds.getMinY(), bounds.getWidth() - nullButtonIntWidth - 4, bounds.getHeight()));
        nullButton.setBounds(Bounds.fromSize(bounds.getMaxX() - nullButtonIntWidth, bounds.getMinY(), nullButtonIntWidth, bounds.getHeight()));
    }
}
