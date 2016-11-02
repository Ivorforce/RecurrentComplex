/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.GuiTexturedButton;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellButton extends TableCellDefault
{
    private GuiTexturedButton button = null;

    public String actionID;
    public String title;
    public List<String> tooltip;
    public boolean enabled = true;

    public ResourceLocation texture;

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

    public ResourceLocation getTexture()
    {
        return texture;
    }

    public void setTexture(ResourceLocation texture)
    {
        this.texture = texture;
        if (button != null)
            button.setTexture(texture);
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();

        button = new GuiTexturedButton(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth(), 20, title);
        button.setTexture(texture);
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
            screen.drawTooltipRect(tooltip, Bounds.fromButton(button), mouseX, mouseY, getFontRenderer());
    }
}
