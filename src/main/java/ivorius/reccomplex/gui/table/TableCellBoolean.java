/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.client.gui.GuiButton;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellBoolean extends TableCellPropertyDefault<Boolean>
{
    private GuiButton button;

    protected String trueTitle;
    protected String falseTitle;

    public TableCellBoolean(String id, boolean value)
    {
        this(id, value, IvTranslations.get("structures.gui.true"), IvTranslations.get("structures.gui.false"));
    }

    public TableCellBoolean(String id, Boolean value, String trueTitle, String falseTitle)
    {
        super(id, value);
        this.trueTitle = trueTitle;
        this.falseTitle = falseTitle;
    }

    public String getTrueTitle()
    {
        return trueTitle;
    }

    public void setTrueTitle(String trueTitle)
    {
        this.trueTitle = trueTitle;
    }

    public String getFalseTitle()
    {
        return falseTitle;
    }

    public void setFalseTitle(String falseTitle)
    {
        this.falseTitle = falseTitle;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        button = new GuiButton(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, getCurrentButtonTitle());

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

        if (button != null) button.displayString = getCurrentButtonTitle();
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        this.property = !property;
        if (button != null) button.displayString = getCurrentButtonTitle();

        alertListenersOfChange();
    }

    public String getCurrentButtonTitle()
    {
        return property ? trueTitle : falseTitle;
    }
}
