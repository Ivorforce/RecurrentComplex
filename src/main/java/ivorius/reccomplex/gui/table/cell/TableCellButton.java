/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.GuiTexturedButton;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellButton extends TableCellDefault
{
    protected GuiTexturedButton button = new GuiTexturedButton(-1, 0, 0, 0, 0, "");

    public String actionID;
    public List<String> tooltip;

    private List<Runnable> listeners = new ArrayList<>();

    public TableCellButton(String id, String actionID, String title, List<String> tooltip, boolean enabled)
    {
        super(id);
        this.actionID = actionID;
        setTitle(title);
        setTooltip(tooltip);
        setEnabled(enabled);
    }

    public TableCellButton(String id, String actionID, String title, List<String> tooltip)
    {
        super(id);
        this.actionID = actionID;
        setTitle(title);
        setTooltip(tooltip);
    }

    public TableCellButton(String id, String actionID, String title, boolean enabled)
    {
        super(id);
        this.actionID = actionID;
        setTitle(title);
        setEnabled(enabled);
    }

    public TableCellButton(String id, String actionID, String title)
    {
        super(id);
        this.actionID = actionID;
        setTitle(title);
    }

    public void addAction(Runnable action)
    {
        listeners.add(action);
    }

    public void removeAction(Runnable action)
    {
        listeners.remove(action);
    }

    public List<Runnable> actions()
    {
        return Collections.unmodifiableList(listeners);
    }

    public String getTitle()
    {
        return button.displayString;
    }

    public void setTitle(String title)
    {
        button.displayString = title;
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        button.enabled = enabled;
    }

    public ResourceLocation getTexture()
    {
        return button.getTexture();
    }

    public void setTexture(ResourceLocation texture)
    {
        if (button != null)
            button.setTexture(texture);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        screen.addButton(this, 0, button);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);
        button.visible = !hidden;
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);
        for (Runnable listener : listeners)
            listener.run();
    }

    @Override
    public void setBounds(Bounds bounds)
    {
        super.setBounds(bounds);
        GuiTexturedButton.setBounds(bounds, button);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        if (tooltip != null && button != null)
            screen.drawTooltipRect(tooltip, Bounds.fromButton(button), mouseX, mouseY, getFontRenderer());
    }
}
