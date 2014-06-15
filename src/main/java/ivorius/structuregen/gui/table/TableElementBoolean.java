package ivorius.structuregen.gui.table;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementBoolean extends TableElementPropertyDefault<Boolean>
{
    private GuiButton button;

    public TableElementBoolean(String id, String title, boolean value)
    {
        super(id, title, value);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        button = new GuiButton(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, "" + getPropertyValue());

        updateButtonDisplayString();
        button.visible = !isHidden();

        screen.addButton(this, 0, button);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (button != null)
        {
            button.visible = !hidden;
        }
    }

    @Override
    public void setPropertyValue(Boolean value)
    {
        super.setPropertyValue(value);

        updateButtonDisplayString();
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        this.property = !property;
        updateButtonDisplayString();

        alertListenersOfChange();
    }

    private void updateButtonDisplayString()
    {
        if (button != null)
        {
            button.displayString = I18n.format(property ? "structures.gui.true" : "structures.gui.false");
        }
    }
}
