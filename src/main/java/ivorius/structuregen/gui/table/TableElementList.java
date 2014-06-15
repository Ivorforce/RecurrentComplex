/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.table;

import net.minecraft.client.gui.GuiButton;

import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementList extends TableElementPropertyDefault<String>
{
    private GuiButton button;

    private List<Option> optionIDs;

    public TableElementList(String id, String title, String value, List<Option> optionIDs)
    {
        super(id, title, value);
        this.optionIDs = optionIDs;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();
        button = new GuiButton(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, "");

        button.visible = !isHidden();
        updateButtonTitle();

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
    public void setPropertyValue(String value)
    {
        super.setPropertyValue(value);

        updateButtonTitle();
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        int prevOptionIndex = currentOptionIndex();
        int optionIndex = prevOptionIndex < 0 ? 0 : (prevOptionIndex + 1) % optionIDs.size();

        setPropertyValue(optionIDs.get(optionIndex).id);

        alertListenersOfChange();
    }

    private void updateButtonTitle()
    {
        if (button != null)
        {
            int index = currentOptionIndex();
            button.displayString = index >= 0 ? optionIDs.get(index).title : getPropertyValue();
        }
    }

    private int currentOptionIndex()
    {
        for (int i = 0; i < optionIDs.size(); i++)
        {
            if (optionIDs.get(i).id.equals(getPropertyValue()))
            {
                return i;
            }
        }

        return -1;
    }

    public static class Option
    {
        public String id;
        public String title;

        public Option(String id, String title)
        {
            this.id = id;
            this.title = title;
        }
    }
}
