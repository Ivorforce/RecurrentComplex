/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.gui;

/**
 * Created by lukas on 12.06.14.
 */
public class GuiSliderRange extends GuiSliderMultivalue
{
    public GuiSliderRange(int id, int x, int y, int width, int height, String displayString)
    {
        super(id, x, y, width, height, 2, displayString);
    }

    public void setRange(FloatRange range)
    {
        boolean firstLower = getValue(0) < getValue(1);

        setValue(firstLower ? 0 : 1, range.getMin());
        setValue(firstLower ? 1 : 0, range.getMax());
    }

    public FloatRange getRange()
    {
        return new FloatRange(Math.min(getValue(0), getValue(1)), Math.max(getValue(0), getValue(1)));
    }
}
