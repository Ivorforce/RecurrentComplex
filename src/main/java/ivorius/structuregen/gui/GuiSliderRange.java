package ivorius.structuregen.gui;

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
        setValue(0, range.getMin());
        setValue(1, range.getMax());
    }

    public FloatRange getRange()
    {
        return new FloatRange(Math.min(getValue(0), getValue(1)), Math.max(getValue(0), getValue(1)));
    }
}
