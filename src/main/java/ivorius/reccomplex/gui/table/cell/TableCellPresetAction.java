/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.cell;

import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.gui.table.GuiTable;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellPresetAction extends TableCellDefault
{
    protected GuiButton leftButton;
    protected GuiButton rightButton;
    protected GuiButton runActionButton;

    protected String currentActionID;

    protected List<TableCellButton> actions = new ArrayList<>();

    protected List<TableCellActionListener> listeners = new ArrayList<>();

    public TableCellPresetAction(String id, List<TableCellButton> actions)
    {
        super(id);
        this.actions.addAll(actions);
        if (actions.size() > 0)
            currentActionID = actions.get(0).actionID;
    }

    public static Stream<TableCellButton> sorted(Stream<TableCellButton> actions)
    {
        return actions.sorted((o1, o2) -> o1.title.compareTo(o2.title));
    }

    public void addListener(TableCellActionListener listener)
    {
        listeners.add(listener);
    }

    public TableCellActionListener addAction(Consumer<String> consumer)
    {
        TableCellActionListener listener = (cell, action) -> consumer.accept(action);
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

    public List<TableCellButton> getActions()
    {
        return actions;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();

        int buttonY = bounds.getMinY() + (bounds.getHeight() - 20) / 2;
        int directionButtonWidth = 20;
        int presetButtonWidth = bounds.getWidth() - directionButtonWidth * 2;

        leftButton = new GuiButton(-1, bounds.getMinX(), buttonY, directionButtonWidth - 1, 20, "<");
        leftButton.visible = !isHidden();
        screen.addButton(this, 0, leftButton);

        rightButton = new GuiButton(-1, bounds.getMinX() + directionButtonWidth + presetButtonWidth + 1, buttonY, directionButtonWidth - 1, 20, ">");
        leftButton.visible = !isHidden();
        screen.addButton(this, 1, rightButton);

        runActionButton = new GuiButton(-1, bounds.getMinX() + directionButtonWidth + 1, buttonY, presetButtonWidth - 2, 20, "");
        setCurrentAction(currentActionID);
        runActionButton.visible = !isHidden();
        screen.addButton(this, 2, runActionButton);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (leftButton != null)
            leftButton.visible = !hidden;
        if (rightButton != null)
            rightButton.visible = !hidden;
        if (runActionButton != null)
            runActionButton.visible = !hidden;
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        if (buttonID == 0 || buttonID == 1)
            move(buttonID == 0 ? -1 : 1);
        else if (buttonID == 2)
        {
            for (TableCellActionListener listener : listeners)
                listener.actionPerformed(this, currentActionID);
        }
    }

    public void setCurrentAction(String action)
    {
        currentActionID = action;
        TableCellButton actionButton = findAction(action);
        runActionButton.displayString = actionButton != null ? actionButton.title : "";
        runActionButton.enabled = actionButton != null && actionButton.enabled;

        setActionButtonActive();
    }

    public void move(int plus)
    {
        setCurrentAction(actions.get((((findIndex(currentActionID) + plus) % actions.size()) + actions.size()) % actions.size()).actionID);
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        TableCellButton action = findAction(currentActionID);
        if (action != null && action.tooltip != null)
            screen.drawTooltipRect(action.tooltip, Bounds.fromButton(runActionButton), mouseX, mouseY, getFontRenderer());
    }

    protected TableCellButton findAction(String actionID)
    {
        int index = findIndex(actionID);
        return index >= 0 ? actions.get(index) : null;
    }

    protected int findIndex(String actionID)
    {
        int currentIndex = -1;

        for (int i = 0; i < actions.size(); i++)
        {
            TableCellButton action = actions.get(i);

            if (action.actionID.equals(actionID))
                currentIndex = i;
        }

        return currentIndex;
    }

    protected void setActionButtonActive()
    {
        if (runActionButton != null)
        {
            int currentActionIndex = findIndex(currentActionID);
            runActionButton.enabled = currentActionIndex >= 0 && actions.get(currentActionIndex).enabled;
        }
    }
}
