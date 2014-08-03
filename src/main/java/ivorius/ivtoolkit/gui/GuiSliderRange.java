/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
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
