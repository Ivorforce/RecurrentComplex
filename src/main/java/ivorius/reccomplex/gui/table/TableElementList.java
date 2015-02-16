/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class TableElementList extends TableElementPropertyDefault<String>
{
    private GuiButton button;

    private List<Option> options;

    public TableElementList(String id, String title, String value, List<Option> options)
    {
        super(id, title, value);
        this.options = options;
    }

    public TableElementList(String id, String title, String value, Option... options)
    {
        this(id, title, value, Arrays.asList(options));
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
            button.visible = !hidden;
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
        int optionIndex = prevOptionIndex < 0 ? 0 : (prevOptionIndex + 1) % options.size();

        setPropertyValue(options.get(optionIndex).id);

        alertListenersOfChange();
    }

    @Override
    public void draw(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(screen, mouseX, mouseY, partialTicks);

        Option option = currentOption();
        if (option != null && option.tooltip != null)
            screen.drawTooltipRect(option.tooltip, bounds(), mouseX, mouseY, Minecraft.getMinecraft().fontRenderer);
    }

    private void updateButtonTitle()
    {
        if (button != null)
        {
            int index = currentOptionIndex();
            button.displayString = index >= 0 ? options.get(index).title : getPropertyValue();
        }
    }

    private Option currentOption()
    {
        int index = currentOptionIndex();
        return index >= 0 ? options.get(index) : null;
    }

    private int currentOptionIndex()
    {
        for (int i = 0; i < options.size(); i++)
        {
            if (options.get(i).id.equals(getPropertyValue()))
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
        public List<String> tooltip;

        public Option(String id, String title)
        {
            this.id = id;
            this.title = title;
        }

        public Option(String id, String title, List<String> tooltip)
        {
            this.id = id;
            this.title = title;
            this.tooltip = tooltip;
        }
    }
}
