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
public class TableElementPresetAction extends TableElementDefault
{
    private GuiButton changePresetButton;
    private GuiButton runActionButton;

    private String currentActionID;

    private String actionTitle;
    private TableElementButton.Action[] actions;

    private List<Listener> listeners = new ArrayList<>();

    public TableElementPresetAction(String id, String title, String actionTitle, TableElementButton.Action... actions)
    {
        super(id, title);
        this.actionTitle = actionTitle;
        this.actions = actions;
        currentActionID = actions[0].id;
    }

    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(Listener listener)
    {
        listeners.remove(listener);
    }

    public List<Listener> listeners()
    {
        return Collections.unmodifiableList(listeners);
    }

    public TableElementButton.Action[] getActions()
    {
        return actions;
    }

    @Override
    public void initGui(GuiTable screen)
    {
        super.initGui(screen);

        Bounds bounds = bounds();

        int curIndex = currentActionIndex();
        String title = curIndex >= 0 ? actions[curIndex].title : currentActionID;
        changePresetButton = new GuiButton(-1, bounds.getMinX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth() / 2 - 2, 20, title);
        changePresetButton.visible = !isHidden();
        screen.addButton(this, 0, changePresetButton);

        runActionButton = new GuiButton(-1, bounds.getCenterX(), bounds.getMinY() + (bounds.getHeight() - 20) / 2, bounds.getWidth() / 2, 20, actionTitle);
        runActionButton.visible = !isHidden();
        screen.addButton(this, 1, runActionButton);

        setActionButtonActive();
    }

    @Override
    public void setHidden(boolean hidden)
    {
        super.setHidden(hidden);

        if (changePresetButton != null)
        {
            changePresetButton.visible = !hidden;
        }
        if (runActionButton != null)
        {
            runActionButton.visible = !hidden;
        }
    }

    @Override
    public void buttonClicked(int buttonID)
    {
        super.buttonClicked(buttonID);

        if (buttonID == 0)
        {
            TableElementButton.Action newAction = actions[(currentActionIndex() + 1) % actions.length];
            currentActionID = newAction.id;
            changePresetButton.displayString = newAction.title;

            setActionButtonActive();
        }
        else if (buttonID == 1)
        {
            for (Listener listener : listeners)
            {
                listener.actionPerformed(this, currentActionID);
            }
        }
    }

    private int currentActionIndex()
    {
        int currentIndex = -1;

        for (int i = 0; i < actions.length; i++)
        {
            TableElementButton.Action action = actions[i];

            if (action.id.equals(currentActionID))
            {
                currentIndex = i;
            }
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

    public static interface Listener
    {
        void actionPerformed(TableElementPresetAction tableElementButton, String actionID);
    }
}
