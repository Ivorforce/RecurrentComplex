/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellButton extends TableCellDefault
{
    private GuiButton button = null;

    public String actionID;
    public String title;
    public List<String> tooltip;
    public boolean enabled = true;

    private List<TableCellActionListener> listeners = new ArrayList<>();

    public TableCellButton(String id, String actionID, String title, List<String> tooltip, boolean enabled)
    {
        super(id);
        this.actionID = actionID;
        this.title = title;
        this.tooltip = tooltip;
        this.enabled = enabled;
    }

    public TableCellButton(String id, String actionID, String title, List<String> tooltip)
    {
        super(id);
        this.actionID = actionID;
        this.title = title;
        this.tooltip = tooltip;
    }

    public TableCellButton(String id, String actionID, String title, boolean enabled)
    {
        super(id);
        this.actionID = actionID;
        this.title = title;
        this.enabled = enabled;
    }

    public TableCellButton(String id, String actionID, String title)
    {
        super(id);
        this.actionID = actionID;
        this.title = title;
    }

    public void addListener(TableCellActionListener listener)
    {
        listeners.add(listener);
    }

    public TableCellActionListener addAction(Runnable runnable)
    {
        TableCellActionListener listener = (cell, action) -> runnable.run();
        listeners.add(listener);
        return listener;
    }

    public void removeListener(TableCellActionListener listener)
    {
        listeners.remove(listener);
    }

    public List<TableCellActionListener> listeners()
    {
        return Collections.unmodifiableList(listeners);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();

        int buttonWidth = bounds.getWidth();

        button = new GuiButton(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, buttonWidth, 20, title);
        button.visible = !isHidden();
        button.enabled = enabled;
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
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        for (TableCellActionListener listener : listeners)
            listener.actionPerformed(this, actionID);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        if (tooltip != null && button != null)
            screen.drawTooltipRect(tooltip, TableCellPresetAction.getBounds(button), mouseX, mouseY, getFontRenderer());
    }
}
