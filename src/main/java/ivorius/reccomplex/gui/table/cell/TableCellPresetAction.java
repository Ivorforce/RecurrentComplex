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

/**
 * Created by lukas on 02.06.14.
 */
public class TableCellPresetAction extends TableCellDefault
{
    protected GuiButton leftButton;
    protected GuiButton rightButton;
    protected GuiButton runActionButton;

    protected String currentActionID;

    protected TableCellButton[] actions;

    protected List<TableCellActionListener> listeners = new ArrayList<>();

    public TableCellPresetAction(String id, TableCellButton... actions)
    {
        super(id);
        this.actions = actions;
        currentActionID = actions[0].actionID;
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

    public TableCellButton[] getActions()
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

        int curIndex = currentActionIndex();
        String curTitle = curIndex >= 0 ? actions[curIndex].title : currentActionID;
        runActionButton = new GuiButton(-1, bounds.getMinX() + directionButtonWidth + 1, buttonY, presetButtonWidth - 2, 20, curTitle);
        runActionButton.visible = !isHidden();
        screen.addButton(this, 2, runActionButton);

        setActionButtonActive();
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

    public void move(int plus)
    {
        TableCellButton newAction = actions[(((currentActionIndex() + plus) % actions.length) + actions.length) % actions.length];
        currentActionID = newAction.actionID;
        runActionButton.displayString = newAction.title;

        setActionButtonActive();
    }

    @Override
    public void drawFloating(GuiTable screen, int mouseX, int mouseY, float partialTicks)
    {
        super.drawFloating(screen, mouseX, mouseY, partialTicks);

        TableCellButton action = currentAction();
        if (action != null && action.tooltip != null)
            screen.drawTooltipRect(action.tooltip, Bounds.fromButton(runActionButton), mouseX, mouseY, getFontRenderer());
    }

    private TableCellButton currentAction()
    {
        int index = currentActionIndex();
        return index >= 0 ? actions[index] : null;
    }

    private int currentActionIndex()
    {
        int currentIndex = -1;

        for (int i = 0; i < actions.length; i++)
        {
            TableCellButton action = actions[i];

            if (action.actionID.equals(currentActionID))
                currentIndex = i;
        }

        return currentIndex;
    }

    private void setActionButtonActive()
    {
        if (runActionButton != null)
        {
            int currentActionIndex = currentActionIndex();
            runActionButton.enabled = currentActionIndex >= 0 && actions[currentActionIndex].enabled;
        }
    }
}
